package com.belka.speech_recognize.utils;

import com.belka.speech_recognize.exceptions.AzureSpeechRecognitionException;
import com.belka.speech_recognize.exceptions.BadRequestException;
import com.belka.speech_recognize.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Client for Microsoft Azure Speech-to-Text API.
 * Handles speech recognition and service availability checks.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AzureSpeechToTextClient {
    @Value("${microsoft.azure.api_key}")
    private String apiKey;
    @Value("${microsoft.azure.service_url}")
    private String serviceUrl;
    @Value("${microsoft.azure.accepted_languages}")
    private String acceptedLanguage;

    private static final String AUDIO_CONTENT_TYPE = "audio/wav";
    private static final String DISPLAY_TEXT_KEY = "DisplayText";
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_SUCCESS_START = 200;
    private static final int HTTP_SUCCESS_END = 299;

    /**
     * Recognizes speech from an audio file using Azure Speech-to-Text API.
     *
     * @param audioFile the audio file to process
     * @return recognized text or an error message
     */
    public String recognizeSpeech(File audioFile) {
        log.info("Starting speech recognition for file: {}, size: {} bytes",
                audioFile.getName(), audioFile.length());
        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpPost postRequest = preparePostRequest(audioFile);
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                String responseString = handleResponse(response);
                log.info("Speech recognition successful for file: {}", audioFile.getName());
                return parseResponse(responseString);
            }
        } catch (IOException e) {
            log.error("I/O error during request: {}", e.getMessage(), e);
            throw new AzureSpeechRecognitionException("I/O error during speech recognition request.", e);
        }
    }

    /**
     * Checks if the Azure Speech service is available.
     *
     * @return true if the service is reachable; false otherwise
     */
    public boolean isServiceAvailable() {
        log.info("Checking availability of Azure Speech Service...");
        try (CloseableHttpClient httpClient = createHttpClient()) {
            // Prepare a minimal valid request
            HttpGet request = new HttpGet(serviceUrl + acceptedLanguage);
            request.addHeader("Ocp-Apim-Subscription-Key", apiKey);

            // Send the request
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                log.info("Health check response code: {}", statusCode);

                // HTTP 400 indicates a malformed audio input but not a service unavailability
                if (statusCode == HTTP_BAD_REQUEST) {
                    log.warn("Received 400 Bad Request: This is expected for a minimal health check.");
                    return true;
                }

                // HTTP 2xx indicates the service is available
                return statusCode >= HTTP_SUCCESS_START && statusCode <= HTTP_SUCCESS_END;
            }
        } catch (IOException e) {
            log.error("Error checking service availability: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Creates an HTTP client with custom configurations.
     *
     * @return a configured CloseableHttpClient instance
     */
    private CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create()
                .setMaxConnTotal(100)  // Maximum of 100 connections simultaneously for all hosts.
                .setMaxConnPerRoute(20)  // Maximum of 20 connections at a time for one specific host.
                .build();
    }

    /**
     * Prepares an HTTP POST request for speech recognition.
     *
     * @param audioFile the audio file to include in the request
     * @return a prepared HttpPost instance
     */
    private HttpPost preparePostRequest(File audioFile) {
        HttpPost postRequest = new HttpPost(serviceUrl + acceptedLanguage);
        postRequest.addHeader("Ocp-Apim-Subscription-Key", apiKey);
        postRequest.addHeader("Content-Type", AUDIO_CONTENT_TYPE);
        postRequest.addHeader("Accept-Language", acceptedLanguage);

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", audioFile, ContentType.create(AUDIO_CONTENT_TYPE), audioFile.getName())
                .build();
        postRequest.setEntity(entity);
        return postRequest;
    }

    /**
     * Handles the response from Azure Speech-to-Text API.
     *
     * @param response the HTTP response
     * @return the response content as a string
     * @throws IOException if an I/O error occurs
     */
    private String handleResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HTTP_UNAUTHORIZED) {
            log.error("Unauthorized: Invalid API key or insufficient permissions.");
            throw new UnauthorizedException("Unauthorized: Invalid API key or insufficient permissions.");
        } else if (statusCode == HTTP_BAD_REQUEST) {
            log.error("Bad Request: HTTP {}", statusCode);
            throw new BadRequestException("Bad Request: HTTP " + statusCode);
        } else if (statusCode >= HTTP_BAD_REQUEST) {
            log.error("Error response from Azure service: HTTP {}", statusCode);
            throw new AzureSpeechRecognitionException("Azure service error: HTTP " + statusCode);
        }
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Parses the API response to extract recognized text.
     *
     * @param jsonResponse the raw JSON response from the API
     * @return recognized text or an error message
     */
    private String parseResponse(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        return json.optString(DISPLAY_TEXT_KEY, "Error: text not recognized");
    }
}