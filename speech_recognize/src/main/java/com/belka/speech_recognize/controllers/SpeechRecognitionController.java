package com.belka.speech_recognize.controllers;


import com.belka.speech_recognize.services.VoiceRecognitionServiceIml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@Slf4j
@RestController
@RequestMapping("/voice")
public class SpeechRecognitionController {

    @Value("${bot.audio.path}")
    private String folderPath;
    private VoiceRecognitionServiceIml voiceRecognitionServiceIml;

    @Autowired
    public void setVoiceRecognitionService(VoiceRecognitionServiceIml voiceRecognitionServiceIml) {
        this.voiceRecognitionServiceIml = voiceRecognitionServiceIml;
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeVoice(@RequestParam("fileName") String fileName) {
        log.info("Starting voice analysis for file: {}", fileName);
        try {
            // File folder path
            Path filePath = Paths.get(folderPath, fileName);
            File file = filePath.toFile();
            // Check if the file exists
            if (!file.exists()) {
                log.warn("File not found: {}", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + fileName);
            }

            // Analyzing the voice
            String result = voiceRecognitionServiceIml.processVoiceMessage(file);
            log.info("Voice analysis completed for file: {}", fileName);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Voice analysis error for file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Voice analysis error: " + e.getMessage());
        }
    }
}