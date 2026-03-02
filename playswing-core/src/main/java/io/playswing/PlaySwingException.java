package io.playswing;

/**
 * Base exception for PlaySwing errors.
 */
public class PlaySwingException extends RuntimeException {

    public PlaySwingException(String message) {
        super(message);
    }

    public PlaySwingException(String message, Throwable cause) {
        super(message, cause);
    }
}
