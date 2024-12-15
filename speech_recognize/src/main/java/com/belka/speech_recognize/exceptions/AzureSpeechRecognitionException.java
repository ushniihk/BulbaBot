package com.belka.speech_recognize.exceptions;

/**
 * Exception thrown when an error occurs during speech recognition.
 */
public class AzureSpeechRecognitionException extends RuntimeException {
    public AzureSpeechRecognitionException(String message) {
        super(message);
    }

    public AzureSpeechRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
