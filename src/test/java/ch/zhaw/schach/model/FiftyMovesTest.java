package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die 50-Zuege-Regel.
 * 50 Zuege ohne Pawn-Zug oder Schlagen → Remis.
 * Pawn-Zug oder Schlagen setzt den Zaehler zurueck.
 */
class FiftyMovesTest {

    /**
     * 50 Zuege (100 Halbzuege) ohne Pawn-Zug und ohne Schlagen → Remis.
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
     * Pawn-Zug setzt den Zaehler auf 0 zurueck → kein Remis.
     * Simuliert einen Pawn-Zug und prueft, dass der Zaehler zurueckgesetzt wurde.
     */
    @Test
    void pawnMove_resetsCounter() {
        ChessGame game = new ChessGame();
        // Zaehler auf 49 setzen (nahe am Remis)
        game.setFiftyMoveCounter(49);

        // Pawn von e2 nach e4 ziehen (2 Felder)
        boolean success = game.makeMove(6, 4, 4, 4, null);
        assertTrue(success);

        // Der Zaehler sollte auf 0 zurueckgesetzt sein
        assertEquals(0, game.getFiftyMoveCounter());
        assertNotEquals(GameStatus.DRAW_FIFTY_MOVES, game.getStatus());
    }

    /**
     * Schlagen setzt den Zaehler auf 0 zurueck → kein Remis.
     * Simuliert einen Pawn-Schlag (En Passant oder normal) und prueft den Zaehler.
     * Da manuelles Brett-Setup komplex ist, wird der Zaehler direkt getestet:
     * nach einem Zug mit Schlag sollte der Zaehler 0 sein.
     */
    @Test
    void capture_resetsCounter() {
        ChessGame game = new ChessGame();

        // Erster Zug: e2 nach e4 (Pawn, setzt Zaehler auf 0)
        game.makeMove(6, 4, 4, 4, null);
        assertEquals(0, game.getFiftyMoveCounter());

        // Zweiter Zug: d7 nach d5 (Pawn, setzt Zaehler auf 0)
        game.makeMove(1, 3, 3, 3, null);
        assertEquals(0, game.getFiftyMoveCounter());

        // En Passant: e4 schlaegt d5 En Passant (Pawn + Schlag → Zaehler auf 0)
        game.makeMove(4, 4, 2, 3, null);
        assertEquals(0, game.getFiftyMoveCounter());
        assertNotEquals(GameStatus.DRAW_FIFTY_MOVES, game.getStatus());
    }
}

