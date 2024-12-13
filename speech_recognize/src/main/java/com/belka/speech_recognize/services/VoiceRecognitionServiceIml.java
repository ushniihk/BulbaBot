package com.belka.speech_recognize.services;

import com.belka.speech_recognize.utils.AzureSpeechToTextClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@AllArgsConstructor
public class VoiceRecognitionServiceIml implements VoiceRecognitionService {
    private final AzureSpeechToTextClient speechToTextClient;

    public String processVoiceMessage(File audioFile) {
        try {
            return speechToTextClient.recognizeSpeech(audioFile);
        } catch (Exception e) {
            throw new RuntimeException("Voice recognition error", e);
        }
    }
}
