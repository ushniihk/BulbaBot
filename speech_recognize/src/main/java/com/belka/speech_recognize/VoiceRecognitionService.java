package com.belka.speech_recognize;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class VoiceRecognitionService {
    private final AzureSpeechToTextClient speechToTextClient;

    public VoiceRecognitionService(AzureSpeechToTextClient speechToTextClient) {
        this.speechToTextClient = speechToTextClient;
    }

    public String processVoiceMessage(File audioFile) {
        try {
            return speechToTextClient.recognizeSpeech(audioFile);
        } catch (Exception e) {
            throw new RuntimeException("Voice recognition error", e);
        }
    }
}
