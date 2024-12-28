package com.belka.speech_recognize.exceptions;

/**
 * Exception thrown when an error occurs during voice recognition.
 */
public class SpeechRecognitionException extends RuntimeException {
    public SpeechRecognitionException() {
        super();
    }

    public SpeechRecognitionException(String message) {
        super(message);
    }

    public SpeechRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpeechRecognitionException(Throwable cause) {
        super(cause);
    }
}
