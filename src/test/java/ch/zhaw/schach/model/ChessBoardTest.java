package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die ChessBoard-Klasse.
 * Prueft, dass Figuren korrekt gesetzt und gelesen werden und die Startposition richtig ist.
 */
class ChessBoardTest {

    @Test
    void setAndGetPiece_roundtrip() {
        ChessBoard board = new ChessBoard();

        // Setze eine Figur auf ein leeres Feld
        Piece bishop = new Piece(PieceType.BISHOP, Color.WHITE);
        board.setPiece(3, 3, bishop);

        // Lesen muss dieselbe Figur zurueckgeben
        Piece retrieved = board.getPiece(3, 3);
        assertNotNull(retrieved);
        assertEquals(PieceType.BISHOP, retrieved.getType());
        assertEquals(Color.WHITE, retrieved.getColor());

        // Entferne die Figur mit null
        board.setPiece(3, 3, null);
        assertNull(board.getPiece(3, 3));
    }

    @Test
    void startPosition_hasAllPieces() {
        ChessBoard board = new ChessBoard();

        // Weiße Figuren Reihe (row 7)
        assertEquals(PieceType.ROOK, board.getPiece(7, 0).getType());
        assertEquals(PieceType.KNIGHT, board.getPiece(7, 1).getType());
        assertEquals(PieceType.BISHOP, board.getPiece(7, 2).getType());
        assertEquals(PieceType.QUEEN, board.getPiece(7, 3).getType());
        assertEquals(PieceType.KING, board.getPiece(7, 4).getType());
        assertEquals(PieceType.BISHOP, board.getPiece(7, 5).getType());
        assertEquals(PieceType.KNIGHT, board.getPiece(7, 6).getType());
        assertEquals(PieceType.ROOK, board.getPiece(7, 7).getType());
        assertEquals(Color.WHITE, board.getPiece(7, 4).getColor());

        // Schwarze Figuren Reihe (row 0)
        assertEquals(PieceType.QUEEN, board.getPiece(0, 3).getType());
        assertEquals(PieceType.KING, board.getPiece(0, 4).getType());
        assertEquals(Color.BLACK, board.getPiece(0, 4).getColor());

        // Bauernreihen
        for (int col = 0; col < 8; col++) {
            assertEquals(PieceType.PAWN, board.getPiece(1, col).getType());
            assertEquals(Color.BLACK, board.getPiece(1, col).getColor());
            assertEquals(PieceType.PAWN, board.getPiece(6, col).getType());
            assertEquals(Color.WHITE, board.getPiece(6, col).getColor());
        }

        // Mittlere Reihen sind leer
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                assertNull(board.getPiece(row, col), "Feld " + row + "," + col + " sollte leer sein");
            }
        }
    }
}
