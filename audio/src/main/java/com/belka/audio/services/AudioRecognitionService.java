package com.belka.audio.services;

/**
 * service for conversion audio to text
 */
public interface AudioRecognitionService {
    String analyzeAudio(String fileName);
}
