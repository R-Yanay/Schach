package ch.zhaw.schach.model;

/**
 * Typen der Schachfiguren mit ihren SVG-Bildern.
 * Unterstützt verschiedene visuelle Stile: Alpha, Leipzig, Horsey.
 */
public enum PieceType {
    PAWN("Pawn"),
    ROOK("Rook"),
    KNIGHT("Knight"),
    BISHOP("Bishop"),
    QUEEN("Queen"),
    KING("King");

    private final String name;

    PieceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Gibt den Dateinamen der SVG-Figur zurück (z.B. "wk.svg" für weißen König).
     * @param color die Farbe der Figur
     */
    public String getSvgFileName(Color color) {
        String colorPrefix = color == Color.WHITE ? "w" : "b";
        String pieceLetter;
        switch (this) {
            case KING: pieceLetter = "K"; break;
            case QUEEN: pieceLetter = "Q"; break;
            case ROOK: pieceLetter = "R"; break;
            case BISHOP: pieceLetter = "B"; break;
            case KNIGHT: pieceLetter = "N"; break;
            case PAWN: pieceLetter = "P"; break;
            default: pieceLetter = "?"; break;
        }
        return colorPrefix + pieceLetter + ".svg";
    }

    /**
     * Gibt den Pfad zur SVG-Datei zurück (relativ zu resources/pieces/).
     * @param color die Farbe der Figur
     * @param style der visuelle Stil
     */
    public String getSvgPath(Color color, PieceStyle style) {
        return "pieces/" + style.getFolderName() + "/" + getSvgFileName(color);
    }
}
