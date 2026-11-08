package ch.zhaw.schach.model;

/**
 * Ein Zug im Schachspiel.
 * Enthält Start- und Zielposition sowie optionale Sonderaktionen.
 */
public class Move {
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final PieceType promotionPiece; // null wenn keine Umwandlung

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, null);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, PieceType promotionPiece) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.promotionPiece = promotionPiece;
    }

    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }
    public PieceType getPromotionPiece() { return promotionPiece; }

    public boolean isPromotion() {
        return promotionPiece != null;
    }

    @Override
    public String toString() {
        String from = colToLetter(fromCol) + String.valueOf(8 - fromRow);
        String to = colToLetter(toCol) + String.valueOf(8 - toRow);
        return from + " -> " + to;
    }

    private char colToLetter(int col) {
        return (char) ('a' + col);
    }
}
