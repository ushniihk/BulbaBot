package com.belka.speech_recognize.services;

import java.io.File;
import com.belka.speech_recognize.exceptions.VoiceRecognitionException;

/**
 * Voice recognition service interface
 */
public interface VoiceRecognitionService {
    /**
     * Processes the given audio file and converts speech to text.
     *
     * @param audioFile the audio file to be processed; must not be null
     * @return the recognized speech as a String, or an empty string if no speech is recognized
     * @throws IllegalArgumentException if the audio file is invalid (e.g., null, missing, or not supported)
     * @throws VoiceRecognitionException if an error occurs during the voice recognition process
     */
    String processVoiceMessage(File audioFile);

}
