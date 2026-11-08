package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die MoveValidator-Klasse.
 * Prueft die Bewegungsregeln aller 6 Figuren-Typen.
 * Verwendet leere Bretter mit einzelnen Figuren fuer klare Tests.
 */
class MoveValidatorTest {

    private final MoveValidator validator = new MoveValidator();

    /**
     * Erstellt ein leeres Brett und setzt eine Figur.
     */
    private ChessBoard emptyBoardWith(PieceType type, Color color, int row, int col) {
        ChessBoard board = new ChessBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.setPiece(r, c, null);
            }
        }
        board.setPiece(row, col, new Piece(type, color));
        return board;
    }

    @Test
    void pawn_1Forward_white() {
        ChessBoard board = emptyBoardWith(PieceType.PAWN, Color.WHITE, 6, 4);
        assertTrue(validator.isValidMove(board, 6, 4, 5, 4));
    }

    @Test
    void pawn_2FromStart_white() {
        ChessBoard board = emptyBoardWith(PieceType.PAWN, Color.WHITE, 6, 4);
        assertTrue(validator.isValidMove(board, 6, 4, 4, 4));
    }

    @Test
    void pawn_diagonalCapture_white() {
        ChessBoard board = emptyBoardWith(PieceType.PAWN, Color.WHITE, 6, 4);
        board.setPiece(5, 5, new Piece(PieceType.PAWN, Color.BLACK));
        assertTrue(validator.isValidMove(board, 6, 4, 5, 5));
    }

    @Test
    void pawn_noBackward() {
        ChessBoard board = emptyBoardWith(PieceType.PAWN, Color.WHITE, 6, 4);
        assertFalse(validator.isValidMove(board, 6, 4, 7, 4));
    }

    @Test
    void rook_horizontal() {
        ChessBoard board = emptyBoardWith(PieceType.ROOK, Color.WHITE, 4, 0);
        assertTrue(validator.isValidMove(board, 4, 0, 4, 7));
    }

    @Test
    void rook_vertical() {
        ChessBoard board = emptyBoardWith(PieceType.ROOK, Color.WHITE, 4, 4);
        assertTrue(validator.isValidMove(board, 4, 4, 0, 4));
    }

    @Test
    void rook_notDiagonal() {
        ChessBoard board = emptyBoardWith(PieceType.ROOK, Color.WHITE, 4, 4);
        assertFalse(validator.isValidMove(board, 4, 4, 2, 2));
    }

    @Test
    void rook_blockedByPiece() {
        ChessBoard board = emptyBoardWith(PieceType.ROOK, Color.WHITE, 4, 0);
        board.setPiece(4, 3, new Piece(PieceType.PAWN, Color.BLACK));
        assertTrue(validator.isValidMove(board, 4, 0, 4, 3));  // Capture
        assertFalse(validator.isValidMove(board, 4, 0, 4, 5)); // behind the piece
    }

    @Test
    void knight_LShape() {
        ChessBoard board = emptyBoardWith(PieceType.KNIGHT, Color.WHITE, 4, 4);
        assertTrue(validator.isValidMove(board, 4, 4, 2, 3));
        assertTrue(validator.isValidMove(board, 4, 4, 2, 5));
        assertTrue(validator.isValidMove(board, 4, 4, 3, 2));
        assertTrue(validator.isValidMove(board, 4, 4, 3, 6));
        assertTrue(validator.isValidMove(board, 4, 4, 5, 2));
        assertTrue(validator.isValidMove(board, 4, 4, 5, 6));
        assertTrue(validator.isValidMove(board, 4, 4, 6, 3));
        assertTrue(validator.isValidMove(board, 4, 4, 6, 5));
    }

    @Test
    void knight_jumpsOverPieces() {
        ChessBoard board = emptyBoardWith(PieceType.KNIGHT, Color.WHITE, 4, 4);
        board.setPiece(3, 4, new Piece(PieceType.PAWN, Color.BLACK));
        assertTrue(validator.isValidMove(board, 4, 4, 2, 3));
    }

    @Test
    void bishop_diagonal() {
        ChessBoard board = emptyBoardWith(PieceType.BISHOP, Color.WHITE, 4, 4);
        assertTrue(validator.isValidMove(board, 4, 4, 2, 2));
        assertTrue(validator.isValidMove(board, 4, 4, 6, 6));
        assertTrue(validator.isValidMove(board, 4, 4, 1, 7));
        assertTrue(validator.isValidMove(board, 4, 4, 7, 1));
    }

    @Test
    void bishop_notHorizontal() {
        ChessBoard board = emptyBoardWith(PieceType.BISHOP, Color.WHITE, 4, 4);
        assertFalse(validator.isValidMove(board, 4, 4, 4, 7));
    }

    @Test
    void queen_combinesRookAndBishop() {
        ChessBoard board = emptyBoardWith(PieceType.QUEEN, Color.WHITE, 4, 4);
        assertTrue(validator.isValidMove(board, 4, 4, 4, 7)); // horizontal
        assertTrue(validator.isValidMove(board, 4, 4, 0, 4)); // vertical
        assertTrue(validator.isValidMove(board, 4, 4, 1, 1)); // diagonal
    }

    @Test
    void king_1Square() {
        ChessBoard board = emptyBoardWith(PieceType.KING, Color.WHITE, 4, 4);
        assertTrue(validator.isValidMove(board, 4, 4, 4, 5));
        assertTrue(validator.isValidMove(board, 4, 4, 3, 4));
        assertTrue(validator.isValidMove(board, 4, 4, 3, 5));
    }

    @Test
    void king_not2Squares() {
        ChessBoard board = emptyBoardWith(PieceType.KING, Color.WHITE, 4, 4);
        assertFalse(validator.isValidMove(board, 4, 4, 4, 6));
    }

    @Test
    void ownPiece_notCapturable() {
        ChessBoard board = emptyBoardWith(PieceType.PAWN, Color.WHITE, 4, 4);
        board.setPiece(3, 4, new Piece(PieceType.PAWN, Color.WHITE));
        assertFalse(validator.isValidMove(board, 4, 4, 3, 4));
    }
}
