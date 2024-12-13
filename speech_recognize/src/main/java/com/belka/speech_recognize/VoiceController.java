package com.belka.speech_recognize;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@AllArgsConstructor
@RequestMapping("/voice")
public class VoiceController {
    private final VoiceRecognitionService voiceRecognitionService;

    @Value("${bot.audio.path}")
    private String folderPath;

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeVoice(@RequestParam("fileName") String fileName) {
        try {
            // File folder path
            Path filePath = Paths.get(folderPath, fileName);
            File file = filePath.toFile();
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