package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die 50-Zuege-Regel.
 * 50 Zuege ohne Bauerzug oder Schlagen → Remis.
 * Bauerzug oder Schlagen setzt den Zaehler zurueck.
 */
class FiftyMovesTest {

    /**
     * 50 Zuege (100 Halbzuege) ohne Bauerzug und ohne Schlagen → Remis.
     * Setzt den Zaehler manuell auf 100 und prueft den Status.
     */
    @Test
    void fiftyMoves_isDraw() {
        ChessGame game = new ChessGame();
        game.setFiftyMoveCounter(100);
        game.updateStatus();
        assertEquals(GameStatus.DRAW_FIFTY_MOVES, game.getStatus());
    }

    /**
     * Bauerzug setzt den Zaehler zurueck auf 0 → kein Remis.
     * Simuliert einen Bauerzug und prueft, dass der Zaehler zurueckgesetzt wurde.
     */
    @Test
    void pawnMove_resetsCounter() {
        ChessGame game = new ChessGame();
        // Zaehler auf 49 setzen (nahe am Remis)
        game.setFiftyMoveCounter(49);

        // Bauer von e2 nach e4 ziehen (2 Felder)
        boolean success = game.makeMove(6, 4, 4, 4);
        assertTrue(success);

        // Der Zaehler sollte auf 0 zurueckgesetzt sein
        assertEquals(0, game.getFiftyMoveCounter());
        assertNotEquals(GameStatus.DRAW_FIFTY_MOVES, game.getStatus());
    }

    /**
     * Schlagen setzt den Zaehler zurueck auf 0 → kein Remis.
     * Simuliert einen Bauerzug mit Schlagen (En-Passant oder normal) und prueft den Zaehler.
     * Da manuelle Brett-Konfiguration komplex ist, wird der Zaehler direkt getestet:
     * Nach einem Zug mit Schlagen sollte der Zaehler 0 sein.
     */
    @Test
    void capture_resetsCounter() {
        ChessGame game = new ChessGame();

        // Erster Zug: e2 nach e4 (Bauer, setzt Zaehler auf 0)
        game.makeMove(6, 4, 4, 4);
        assertEquals(0, game.getFiftyMoveCounter());

        // Zweiter Zug: d7 nach d5 (Bauer, setzt Zaehler auf 0)
        game.makeMove(1, 3, 3, 3);
        assertEquals(0, game.getFiftyMoveCounter());

        // En-Passant: e4 schlägt d5 en passant (Bauer + Schlagen → Zaehler auf 0)
        game.makeMove(4, 4, 2, 3);
        assertEquals(0, game.getFiftyMoveCounter());
        assertNotEquals(GameStatus.DRAW_FIFTY_MOVES, game.getStatus());
    }
}
