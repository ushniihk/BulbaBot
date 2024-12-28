package com.belka.speech_recognize.services;

import com.belka.speech_recognize.exceptions.SpeechRecognitionException;

import java.io.File;

/**
 * Speech recognition service interface
 */
public interface SpeechRecognitionService {
    /**
     * Processes the given audio file and converts speech to text.
     *
     * @param audioFile the audio file to be processed; must not be null
     * @return the recognized speech as a String, or an empty string if no speech is recognized
     * @throws IllegalArgumentException  if the audio file is invalid (e.g., null, missing, or not supported)
     * @throws SpeechRecognitionException if an error occurs during the voice recognition process
     */
    String processAudioFile(File audioFile);

    /**
     * Checks if the speech recognition service is available.
     *
     * @return true if the service is available, false otherwise
     */
    boolean isServiceAvailable();

}
