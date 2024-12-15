package com.belka.speech_recognize.exceptions;

/**
 * Exception thrown when an error occurs during voice recognition.
 */
public class VoiceRecognitionException extends RuntimeException {
    public VoiceRecognitionException() {
        super();
    }

    public VoiceRecognitionException(String message) {
        super(message);
    }

    public VoiceRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public VoiceRecognitionException(Throwable cause) {
        super(cause);
    }
}
