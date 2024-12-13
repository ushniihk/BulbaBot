package com.belka.speech_recognize.services;

import java.io.File;

/**
 * Voice recognition service interface
 */
public interface VoiceRecognitionService {
    /**
     * process voice message
     *
     * @param audioFile path to audio file
     * @return text from recognized audio
     */
    String processVoiceMessage(File audioFile);

}
