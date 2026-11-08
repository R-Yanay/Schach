package ch.zhaw.schach.ai;

import ch.zhaw.schach.model.*;

/**
 * Bewertet eine Schachstellung.
 * Positive Werte = Vorteil für Weiß, negative Werte = Vorteil für Schwarz.
 */
public class Evaluator {

    // Material-Werte
    private static final int PAWN_VALUE = 10;
    private static final int KNIGHT_VALUE = 30;
    private static final int BISHOP_VALUE = 30;
    private static final int ROOK_VALUE = 50;
    private static final int QUEEN_VALUE = 90;
    private static final int KING_VALUE = 900;

    // Positionsbewertung – zentrale Felder sind besser
    private static final int[][] centerBonus = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 2, 2, 2, 2, 1, 0},
            {0, 1, 2, 2, 2, 2, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0}
    };

    /**
     * Bewertet eine Stellung.
     * Positiv = Vorteil für Weiß, negativ = Vorteil für Schwarz.
     */
    public int evaluate(ChessBoard board) {
        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    int value = getPieceValue(piece, row, col);
                    if (piece.getColor() == Color.WHITE) {
                        score += value;
                    } else {
                        score -= value;
                    }
                }
            }
        }

        return score;
    }

    /**
     * Gibt den Wert einer Figur an einer Position zurück.
     */
    private int getPieceValue(Piece piece, int row, int col) {
        int value = getMaterialValue(piece.getType());

        // Positionsbonus für Springer und Bauern
        if (piece.getType() == PieceType.KNIGHT || piece.getType() == PieceType.PAWN) {
            value += centerBonus[row][col];
        }

        return value;
    }

    /**
     * Gibt den Materialwert einer Figur zurück.
     */
    private int getMaterialValue(PieceType type) {
        switch (type) {
            case PAWN: return PAWN_VALUE;
            case KNIGHT: return KNIGHT_VALUE;
            case BISHOP: return BISHOP_VALUE;
            case ROOK: return ROOK_VALUE;
            case QUEEN: return QUEEN_VALUE;
            case KING: return KING_VALUE;
            default: return 0;
        }
    }
}
