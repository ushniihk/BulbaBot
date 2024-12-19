package com.belka.audio.services;

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
    @Value("${bot.audio.speech_recognition.uri}")
    private String speechRecognitionServiceUrl;

    @Override
    public String analyzeVoice(String fileName) {
        String url = speechRecognitionServiceUrl + fileName + ".WAV";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Voice analysis failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error analyzing voice", e);
            throw new RuntimeException("Error analyzing voice: " + e.getMessage(), e);
        }
    }
}
