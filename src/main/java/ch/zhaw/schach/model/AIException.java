package ch.zhaw.schach.model;

/**
 * Wird geworfen, wenn die KI-Berechnung zu lange dauert.
 * Die Berechnung wird abgebrochen und eine Meldung angezeigt.
 */
public class AIException extends RuntimeException {

    public AIException(String message) {
        super(message);
    }

    public AIException(String message, Throwable cause) {
        super(message, cause);
    }
}
