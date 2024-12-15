package com.belka.speech_recognize.controllers;


import com.belka.speech_recognize.services.VoiceRecognitionService;
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
    private final VoiceRecognitionService voiceRecognitionService;
    @Value("${bot.audio.path}")
    private String folderPath;

    @Autowired
    public SpeechRecognitionController(VoiceRecognitionService voiceRecognitionService) {
        this.voiceRecognitionService = voiceRecognitionService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeVoice(@RequestParam("fileName") String fileName) {
        if (!isValidFileName(fileName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File name cannot be empty or null.");
        }
        File file = getFile(fileName);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + fileName);
        }
        log.info("Starting voice analysis for file: {}", fileName);
        try {
            String result = voiceRecognitionService.processVoiceMessage(file);
            log.info("Voice analysis completed for file: {}", fileName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Voice analysis error for file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing voice message: " + e.getMessage());
        }
    }

    private boolean isValidFileName(String fileName) {
        return fileName != null && !fileName.trim().isEmpty();
    }

    private File getFile(String fileName) {
        Path filePath = Paths.get(folderPath, fileName);
        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            log.info("File not found or not a valid file: {}", filePath.toAbsolutePath());
            return null;
        }
        return file;
    }
}
//todo: make it work asynchronously
//todo: add javadoc and guidebook
