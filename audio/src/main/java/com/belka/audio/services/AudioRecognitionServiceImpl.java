package com.belka.audio.services;

import com.belka.audio.exceptions.AudioRecognitionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AudioRecognitionServiceImpl implements AudioRecognitionService {
    private final RestTemplate restTemplate;
    @Value("${bot.audio.speech.recognition.base-url}${bot.audio.speech.recognition.analyze-endpoint}")
    private String analyzeUrl;
    @Value("${bot.audio.speech.recognition.base-url}${bot.audio.speech.recognition.health-endpoint}")
    private String healthUrl;


    @Override
    public String analyzeAudio(String fileName) {
        String url = analyzeUrl + fileName + ".WAV";
        // Check if the service is available, if not return an empty string
        if (!isServiceAvailable()) {
            log.error("Speech recognition service is not available");
            return "";
        }

        return executePostRequest(url, fileName);
    }

    /**
     * Check if the audio recognition service is available.
     *
     * @return true if the service is available; false otherwise.
     */
    public boolean isServiceAvailable() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            boolean isAvailable = response.getStatusCode() == HttpStatus.OK;
            log.info("Speech recognition service availability: {}", isAvailable);
            return isAvailable;
        } catch (Exception e) {
            log.error("Error checking service availability", e);
            return false;
        }
    }

    /**
     * Executes a POST request and handles the response.
     *
     * @param url      The URL to send the POST request to.
     * @param fileName The name of the file being analyzed (for logging purposes).
     * @return The response body if the request is successful.
     */
    private String executePostRequest(String url, String fileName) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Audio analysis successful for file: {}", fileName);
                return response.getBody();
            } else {
                log.error("Audio analysis failed with status: {}", response.getStatusCode());
                throw new AudioRecognitionException("Audio analysis failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error analyzing audio for file: {}", fileName, e);
            throw new AudioRecognitionException("Error analyzing audio: " + e.getMessage(), e);
        }
    }
}
