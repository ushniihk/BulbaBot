package com.belka.speech_recognize;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Objects;

@RestController
@RequestMapping("/voice")
public class VoiceController {
    private final VoiceRecognitionService voiceRecognitionService;

    public VoiceController(VoiceRecognitionService voiceRecognitionService) {
        this.voiceRecognitionService = voiceRecognitionService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeVoice(@RequestParam("fileName") String fileName) {
        try {
            // File folder path
            File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(fileName)).toURI());
            // Check if the file exists
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + fileName);
            }

            // Analyzing the voice
            String result = voiceRecognitionService.processVoiceMessage(file);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Voice analysis error: " + e.getMessage());
        }
    }
}