package com.belka.speech_recognize.controllers;


import com.belka.speech_recognize.services.SpeechRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/speech")
@RequiredArgsConstructor
public class SpeechRecognitionController {
    private final SpeechRecognitionService speechRecognitionService;
    @Value("${bot.audio.path}")
    private String folderPath;

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeSpeech(@RequestParam("fileName") String fileName) {
        if (!isValidFileName(fileName)) {
            log.warn("Invalid file name provided: {}", fileName);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File name cannot be empty or null.");
        }
        File file = getFile(fileName);
        if (file == null) {
            log.warn("File not found: {}", fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + fileName);
        }
        log.info("Starting speech analysis for file: {}", fileName);
        try {
            String result = speechRecognitionService.processAudioFile(file);
            log.info("Speech analysis completed for file: {}", fileName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Speech analysis error for file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing speech: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> checkHealth() {
        try {
            boolean isServiceAvailable = speechRecognitionService.isServiceAvailable();
            if (isServiceAvailable) {
                return ResponseEntity.ok("Speech recognition service is available.");
            } else {
                log.warn("Speech recognition service is unavailable.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("");
            }
        } catch (Exception e) {
            log.error("Error during health check", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
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
