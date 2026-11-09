package ch.zhaw.schach.model;

/**
 * Wird geworfen, wenn ein Spielstand nicht geladen werden kann.
 * Zum Beispiel: Datei fehlt, Datei ist beschädigt oder leer.
 */
public class LoadException extends RuntimeException {

    public LoadException(String message) {
        super(message);
    }

    public LoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
