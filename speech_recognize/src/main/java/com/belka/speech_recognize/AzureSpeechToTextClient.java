package com.belka.speech_recognize;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class AzureSpeechToTextClient {
    @Value("${microsoft.azure.api_key}")
    private String apiKey;
    @Value("${microsoft.azure.service_url}")
    private String serviceUrl;

    public String recognizeSpeech(File audioFile) {
        // Preparing an HTTP-request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(serviceUrl);
            postRequest.addHeader("Ocp-Apim-Subscription-Key", apiKey);
            postRequest.addHeader("Content-Type", "audio/wav");
            postRequest.addHeader("Accept-Language", "ru-RU");
            // Creating an entity with a file
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", audioFile, ContentType.create("audio/wav"), audioFile.getName())
                    .build();
            postRequest.setEntity(entity);


            // executing the request
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                // Parsing the response
                String responseString = EntityUtils.toString(response.getEntity());
                System.out.println("Response: " + responseString);
                return parseResponse(responseString);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error when sending a request: " + e.getMessage(), e);
        }
    }

    private String parseResponse(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        return json.optString("DisplayText", "Error: text not recognized");
    }
}

// todo: add the choose language option, because now it works only with russian language. To change language needs to change a header and serviceUrl
// todo: add logging