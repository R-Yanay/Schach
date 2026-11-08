package ch.zhaw.schach.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Prüft, ob ein Zug nach den Schachregeln gültig ist.
 * Deckt alle 6 Figuren-Typen ab.
 */
public class MoveValidator {

    /**
     * Prüft, ob ein Zug von (fromRow, fromCol) nach (toRow, toCol) gültig ist.
     */
    public boolean isValidMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) return false;

        // Ziel darf nicht eigene Figur sein
        Piece target = board.getPiece(toRow, toCol);
        if (target != null && target.getColor() == piece.getColor()) return false;

        switch (piece.getType()) {
            case PAWN: return isValidPawnMove(board, fromRow, fromCol, toRow, toCol, piece.getColor());
            case ROOK: return isValidRookMove(board, fromRow, fromCol, toRow, toCol);
            case KNIGHT: return isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case BISHOP: return isValidBishopMove(board, fromRow, fromCol, toRow, toCol);
            case QUEEN: return isValidQueenMove(board, fromRow, fromCol, toRow, toCol);
            case KING: return isValidKingMove(fromRow, fromCol, toRow, toCol);
            default: return false;
        }
    }

    /**
     * Gibt alle gültigen Züge für eine Figur zurück.
     */
    public List<Move> getAllValidMoves(ChessBoard board, int row, int col) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(row, col);
        if (piece == null) return moves;

        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                if (isValidMove(board, row, col, toRow, toCol)) {
                    // Check for pawn promotion
                    if (piece.getType() == PieceType.PAWN) {
                        if ((piece.getColor() == Color.WHITE && toRow == 0) ||
                            (piece.getColor() == Color.BLACK && toRow == 7)) {
                            // Promotion – Queen as default
                            moves.add(new Move(row, col, toRow, toCol, PieceType.QUEEN));
                        } else {
                            moves.add(new Move(row, col, toRow, toCol));
                        }
                    } else {
                        moves.add(new Move(row, col, toRow, toCol));
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Bauer: 1 Feld vorwärts, 2 Felder vom Start, diagonal bei Capture.
     */
    private boolean isValidPawnMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol, Color color) {
        int direction = (color == Color.WHITE) ? -1 : 1;
        int startRow = (color == Color.WHITE) ? 6 : 1;

        // 1 Feld vorwärts
        if (fromCol == toCol && toRow == fromRow + direction) {
            if (board.getPiece(toRow, toCol) == null) {
                return true;
            }
        }

        // 2 Felder vom Start
        if (fromCol == toCol && fromRow == startRow && toRow == fromRow + 2 * direction) {
            if (board.getPiece(toRow, toCol) == null &&
                board.getPiece(fromRow + direction, fromCol) == null) {
                return true;
            }
        }

        // Diagonal Capture
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            Piece target = board.getPiece(toRow, toCol);
            if (target != null && target.getColor() != color) {
                return true;
            }
        }

        return false;
    }

    /**
     * Turm: Horizontal/vertikal, nicht durch Figuren.
     */
    private boolean isValidRookMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == toRow) {
            // Horizontal
            int step = Integer.compare(toCol, fromCol);
            int col = fromCol + step;
            while (col != toCol) {
                if (board.getPiece(fromRow, col) != null) return false;
                col += step;
            }
            return true;
        } else if (fromCol == toCol) {
            // Vertikal
            int step = Integer.compare(toRow, fromRow);
            int row = fromRow + step;
            while (row != toRow) {
                if (board.getPiece(row, fromCol) != null) return false;
                row += step;
            }
            return true;
        }
        return false;
    }

    /**
     * Springer: L-Form, springt über Figuren.
     */
    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    /**
     * Läufer: Diagonal, nicht durch Figuren.
     */
    private boolean isValidBishopMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false;
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int row = fromRow + rowStep;
        int col = fromCol + colStep;
        while (row != toRow) {
            if (board.getPiece(row, col) != null) return false;
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    /**
     * Dame: Turm + Läufer kombiniert.
     */
    private boolean isValidQueenMove(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        return isValidRookMove(board, fromRow, fromCol, toRow, toCol) ||
               isValidBishopMove(board, fromRow, fromCol, toRow, toCol);
    }

    /**
     * König: 1 Feld in jede Richtung.
     */
    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }
}
