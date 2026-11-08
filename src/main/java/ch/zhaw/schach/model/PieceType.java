package ch.zhaw.schach.model;

/**
 * Typen der Schachfiguren mit ihren Unicode-Symbolen.
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
     * Gibt das Unicode-Symbol für die Figur zurück.
     */
    public char getSymbol(Color color) {
        if (color == Color.WHITE) {
            switch (this) {
                case KING: return '♔';
                case QUEEN: return '♕';
                case ROOK: return '♖';
                case BISHOP: return '♗';
                case KNIGHT: return '♘';
                case PAWN: return '♙';
                default: return '?';
            }
        } else {
            switch (this) {
                case KING: return '♚';
                case QUEEN: return '♛';
                case ROOK: return '♜';
                case BISHOP: return '♝';
                case KNIGHT: return '♞';
                case PAWN: return '♟';
                default: return '?';
            }
        }
    }
}
