package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer dreimalige Stellungswiederholung.
 * Eine Position, die 3 Mal auftritt → Remis.
 * Eine Position, die 2 Mal auftritt → kein Remis.
 */
class ThreefoldRepetitionTest {

    /**
     * Position, die 3 Mal auftritt → Remis durch dreimalige Wiederholung.
     * Verwendet getCurrentHash() fuer den echten Hash des Startbretts
     * und fueellt positionHistory damit.
     */
    @Test
    void threefoldRepetition_isDraw() {
        ChessGame game = new ChessGame();

        // Echten Hash der aktuellen Position holen
        String currentHash = game.getCurrentHash();

        // positionHistory 3 Mal mit demselben Hash fueellen
        List<String> history = game.getPositionHistory();
        history.clear();
        history.add(currentHash);
        history.add(currentHash);
        history.add(currentHash);

        game.updateStatus();
        assertEquals(GameStatus.DRAW_THREEFOLD_REPETITION, game.getStatus());
    }

    /**
     * Position, die 4 Mal auftritt → auch Remis (mehr als 3 ist auch Remis).
     */
    @Test
    void fourfoldRepetition_isDraw() {
        ChessGame game = new ChessGame();

        String currentHash = game.getCurrentHash();
        List<String> history = game.getPositionHistory();
        history.clear();
        history.add(currentHash);
        history.add(currentHash);
        history.add(currentHash);
        history.add(currentHash);

        game.updateStatus();
        assertEquals(GameStatus.DRAW_THREEFOLD_REPETITION, game.getStatus());
    }

    /**
     * Position, die nur 2 Mal auftritt → KEIN Remis.
     * Zwei identische Hashes reichen nicht fuer Remis.
     */
    @Test
    void twofoldRepetition_notDraw() {
        ChessGame game = new ChessGame();

        String currentHash = game.getCurrentHash();
        List<String> history = game.getPositionHistory();
        history.clear();
        history.add(currentHash);
        history.add(currentHash);

        game.updateStatus();
        assertNotEquals(GameStatus.DRAW_THREEFOLD_REPETITION, game.getStatus());
    }

    /**
     * Verschiedene Positionen → KEIN Remis.
     * 3 verschiedene Hashes sollten keine Wiederholung ausloesen.
     */
    @Test
    void differentPositions_notDraw() {
        ChessGame game = new ChessGame();

        List<String> history = game.getPositionHistory();
        history.clear();
        history.add("hash1");
        history.add("hash2");
        history.add("hash3");

        game.updateStatus();
        assertNotEquals(GameStatus.DRAW_THREEFOLD_REPETITION, game.getStatus());
    }

    /**
     * Volles Spiel am Anfang → KEINE dreimalige Wiederholung.
     */
    @Test
    void newGame_noRepetition() {
        ChessGame game = new ChessGame();
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNotEquals(GameStatus.DRAW_THREEFOLD_REPETITION, game.getStatus());
    }
}
