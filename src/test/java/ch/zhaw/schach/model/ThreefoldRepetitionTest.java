package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die dreimalige Stellungswiederholung.
 * Eine Stellung die 3 Mal auftritt → Remis.
 * Eine Stellung die 2 Mal auftritt → kein Remis.
 */
class ThreefoldRepetitionTest {

    /**
     * Stellung die 3 Mal auftritt → Remis durch dreimalige Wiederholung.
     * Nutzt getCurrentHash() um den echten Hash des Anfangsbretts zu erhalten
     * und positionHistory damit zu fuellen.
     */
    @Test
    void threefoldRepetition_isDraw() {
        ChessGame game = new ChessGame();

        // Den echten Hash des aktuellen Bretts holen
        String currentHash = game.getCurrentHash();

        // positionHistory mit demselben Hash 3 Mal fuellen
        List<String> history = game.getPositionHistory();
        history.clear();
        history.add(currentHash);
        history.add(currentHash);
        history.add(currentHash);

        game.updateStatus();
        assertEquals(GameStatus.DRAW_THREEFOLD_REPETITION, game.getStatus());
    }

    /**
     * Stellung die 4 Mal auftritt → auch Remis (mehr als 3 ist ebenfalls Remis).
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
     * Stellung die nur 2 Mal auftritt → KEIN Remis.
     * Zwei gleiche Hashes reichen nicht fuer Remis.
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
     * Unterschiedliche Stellungen → KEIN Remis.
     * 3 verschiedene Hashes sollten keine Wiederholung erkennen.
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
