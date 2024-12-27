package com.belka.audio.exceptions;

public class AudioRecognitionException extends RuntimeException {
    public AudioRecognitionException(String message) {
        super(message);
    }

    public AudioRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }
}