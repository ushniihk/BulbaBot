package com.belka.speech_recognize.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hibernate.service.spi.ServiceException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class AzureSpeechToTextClient {
    @Value("${microsoft.azure.api_key}")
    private String apiKey;
    @Value("${microsoft.azure.service_url}")
    private String serviceUrl;
    @Value("${microsoft.azure.accepted_languages}")
    private String acceptedLanguage;

    private static final String AUDIO_CONTENT_TYPE = "audio/wav";
    private static final String DISPLAY_TEXT_KEY = "DisplayText";

    public String recognizeSpeech(File audioFile) {
        log.info("Starting speech recognition for file: {}, size: {} bytes",
                audioFile.getName(), audioFile.length());
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = prepareRequest(audioFile);
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                String responseString = handleResponse(response);
                log.info("Response received: {}", responseString);
                return parseResponse(responseString);
            }
        } catch (IOException e) {
            log.error("I/O error during request: {}", e.getMessage(), e);
            throw new ServiceException("I/O error during speech recognition request.", e);
        }
    }

    private HttpPost prepareRequest(File audioFile) {
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

    private String handleResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 401) {
            log.error("Unauthorized: Invalid API key or insufficient permissions.");
            throw new RuntimeException("Unauthorized: Invalid API key or insufficient permissions.");
        } else if (statusCode >= 400) {
            log.error("Error response from Azure service: HTTP {}", statusCode);
            throw new RuntimeException("Azure service error: HTTP " + statusCode);
        }
        return EntityUtils.toString(response.getEntity());
    }

    private String parseResponse(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        return json.optString(DISPLAY_TEXT_KEY, "Error: text not recognized");
    }
}