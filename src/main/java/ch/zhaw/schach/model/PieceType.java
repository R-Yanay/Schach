package ch.zhaw.schach.model;

/**
 * Typen der Schachfiguren mit ihren Unicode-Symbolen.
 * Unterstützt verschiedene visuelle Stile: OldSchool, Chess.com, Classic.
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
     * @param color die Farbe der Figur
     * @param style der visuelle Stil (default: OLDSCHOOL)
     */
    public char getSymbol(Color color, PieceStyle style) {
        if (style == PieceStyle.CHESSCOM) {
            return getChessComSymbol(color);
        } else if (style == PieceStyle.CLASSIC) {
            return getClassicSymbol(color);
        } else {
            return getOldSchoolSymbol(color);
        }
    }

    /**
     * Gibt das Unicode-Symbol für die Figur zurück (OldSchool-Standard).
     */
    public char getSymbol(Color color) {
        return getSymbol(color, PieceStyle.OLDSCHOOL);
    }

    /**
     * OldSchool – Standard Unicode-Symbole (hollow/filled).
     */
    private char getOldSchoolSymbol(Color color) {
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

    /**
     * Chess.com – ähnliche Symbole wie auf Chess.com (dicker, ausgefüllt).
     */
    private char getChessComSymbol(Color color) {
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

    /**
     * Classic – traditionelle Symbole mit Buchstaben-ähnlicher Darstellung.
     */
    private char getClassicSymbol(Color color) {
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
