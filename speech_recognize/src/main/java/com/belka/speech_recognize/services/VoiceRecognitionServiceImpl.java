package com.belka.speech_recognize.services;

import com.belka.speech_recognize.exceptions.VoiceRecognitionException;
import com.belka.speech_recognize.utils.AzureSpeechToTextClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceRecognitionServiceImpl implements VoiceRecognitionService {

    private final AzureSpeechToTextClient speechToTextClient;

    @Override
    @Retryable(value = VoiceRecognitionException.class, maxAttempts = 3)
    public String processVoiceMessage(File audioFile) {
        validateAudioFile(audioFile);

        log.info("Starting voice recognition for file: {} (size: {} bytes, path: {})",
                audioFile.getName(), audioFile.length(), audioFile.getAbsolutePath());

        try {
            String result = speechToTextClient.recognizeSpeech(audioFile);
            log.info("Voice recognition successful for file: {}", audioFile.getName());
            return result;
        } catch (Exception e) {
            log.error("Error during voice recognition for file: {}", audioFile.getName(), e);
            throw new VoiceRecognitionException("Voice recognition failed for file: " + audioFile.getName(), e);
        }
    }

    /**
     * Validates the input audio file.
     *
     * @param audioFile the file to be validated
     * @throws IllegalArgumentException if the file is null, does not exist, or is not a valid audio type
     */
    private void validateAudioFile(File audioFile) {
        if (audioFile == null) {
            log.error("Provided audio file is null");
            throw new IllegalArgumentException("Audio file cannot be null");
        }

        if (!audioFile.exists() || !audioFile.isFile()) {
            log.error("Audio file does not exist or is not a file: {}", audioFile);
            throw new IllegalArgumentException("Audio file does not exist: " + audioFile.getName());
        }

        String fileName = audioFile.getName().toLowerCase();
        if (!fileName.endsWith(".wav")) {
            log.error("Invalid audio file format: {}", fileName);
            throw new IllegalArgumentException("Unsupported audio file format: " + fileName);
        }
    }
}