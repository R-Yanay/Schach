package ch.zhaw.schach.model;

/**
 * Verschiedene visuelle Stile für Schachfiguren.
 * OldSchool = Unicode-Symbole (Standard)
 * ChessCom = Chess.com-ähnliche Symbole
 * Classic = Klassische Symbole
 */
public enum PieceStyle {
    OLDSCHOOL("OldSchool"),
    CHESSCOM("Chess.com"),
    CLASSIC("Classic");

    private final String displayName;

    PieceStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
