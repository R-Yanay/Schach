package ch.zhaw.schach.model;

/**
 * Eine Schachfigur mit Typ und Farbe.
 */
public class Piece {
    private final PieceType type;
    private final Color color;

    public Piece(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    public PieceType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.valueOf(type.getSymbol(color));
    }
}
