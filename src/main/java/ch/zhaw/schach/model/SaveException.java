package ch.zhaw.schach.model;

/**
 * Wird geworfen, wenn ein Spielstand nicht gespeichert werden kann.
 * Zum Beispiel: Festplatte voll, keine Schreibberechtigung.
 */
public class SaveException extends RuntimeException {

    public SaveException(String message) {
        super(message);
    }

    public SaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
