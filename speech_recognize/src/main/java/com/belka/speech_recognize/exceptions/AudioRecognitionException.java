package com.belka.speech_recognize.exceptions;

/**
 * Exception thrown when an error occurs during voice recognition.
 */
public class AudioRecognitionException extends RuntimeException {
    public AudioRecognitionException() {
        super();
    }

    public AudioRecognitionException(String message) {
        super(message);
    }

    public AudioRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AudioRecognitionException(Throwable cause) {
        super(cause);
    }
}
