package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die "Unzureichendes Material"-Remis-Regel.
 * Prueft alle Remis-Faelle und negative Faelle (Matt noch moeglich).
 */
class InsufficientMaterialTest {

    /**
     * Hilfsmethode: Leert das gesamte Brett.
     */
    private void clearBoard(ChessGame game) {
        ChessBoard board = game.getBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.setPiece(r, c, null);
            }
        }
    }

    /**
     * Hilfsmethode: Setzt beide Koenige auf das Brett.
     */
    private void setKings(ChessGame game) {
        ChessBoard board = game.getBoard();
        board.setPiece(7, 4, new Piece(PieceType.KING, Color.WHITE));
        board.setPiece(0, 4, new Piece(PieceType.KING, Color.BLACK));
    }

    /**
     * Nur Koenige auf dem Brett → Remis.
     */
    @Test
    void onlyKings_isDraw() {
        ChessGame game = new ChessGame();
        clearBoard(game);
        setKings(game);
        game.updateStatus();
        assertEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());
    }

    /**
     * Einseitig unzureichendes Material → Remis.
     * K+B vs K und K+N vs K (Farbe spielt keine Rolle).
     */
    @Test
    void oneMinorPiece_isDraw() {
        ChessGame game = new ChessGame();
        clearBoard(game);
        setKings(game);

        ChessBoard board = game.getBoard();
        board.setPiece(6, 2, new Piece(PieceType.BISHOP, Color.WHITE));
        game.updateStatus();
        assertEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());

        board.setPiece(6, 2, null);
        board.setPiece(6, 3, new Piece(PieceType.KNIGHT, Color.WHITE));
        game.updateStatus();
        assertEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());

        board.setPiece(6, 3, null);
        board.setPiece(1, 2, new Piece(PieceType.BISHOP, Color.BLACK));
        game.updateStatus();
        assertEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());

        board.setPiece(1, 2, null);
        board.setPiece(1, 3, new Piece(PieceType.KNIGHT, Color.BLACK));
        game.updateStatus();
        assertEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());
    }

    /**
     * Beidseitig unzureichendes Material → Remis.
     * K+B vs K+B und K+N vs K+N.
     */
    @Test
    void bothWithMinor_isDraw() {
        ChessGame game = new ChessGame();
        clearBoard(game);
        setKings(game);

        ChessBoard board = game.getBoard();
        board.setPiece(6, 2, new Piece(PieceType.BISHOP, Color.WHITE));
        board.setPiece(1, 2, new Piece(PieceType.BISHOP, Color.BLACK));
        game.updateStatus();
        assertEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());

        board.setPiece(6, 2, null);
        board.setPiece(1, 2, null);
        board.setPiece(6, 3, new Piece(PieceType.KNIGHT, Color.WHITE));
        board.setPiece(1, 3, new Piece(PieceType.KNIGHT, Color.BLACK));
        game.updateStatus();
        assertEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());
    }

    /**
     * Matt noch moeglich → KEIN Remis.
     * K+R vs K (Turm kann matt setzen) und volles Spiel.
     */
    @Test
    void mattStillPossible_notDraw() {
        ChessGame game = new ChessGame();
        clearBoard(game);
        setKings(game);

        ChessBoard board = game.getBoard();
        board.setPiece(6, 2, new Piece(PieceType.ROOK, Color.WHITE));
        game.updateStatus();
        assertNotEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());

        board.setPiece(6, 2, new Piece(PieceType.PAWN, Color.WHITE));
        game.updateStatus();
        assertNotEquals(GameStatus.INSUFFICIENT_MATERIAL, game.getStatus());

        ChessGame fullGame = new ChessGame();
        assertEquals(GameStatus.IN_PROGRESS, fullGame.getStatus());
        assertNotEquals(GameStatus.INSUFFICIENT_MATERIAL, fullGame.getStatus());
    }
}
