package ch.zhaw.schach.model;

import ch.zhaw.schach.ai.AI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die AI-Klasse.
 * Prueft, dass die KI mindestens einen legalen Zug zurueckgibt.
 */
class AITest {

    @Test
    void getBestMove_returnsLegalMove() {
        ChessGame game = new ChessGame();
        AI ai = new AI(1);

        // KI soll einen Zug fuer Weiss berechnen
        Move move = ai.getBestMove(game);

        // Muss einen Zug zurueckgeben
        assertNotNull(move, "KI muss mindestens einen Zug zurueckgeben");

        // Zug muss im gueltigen Bereich sein
        assertTrue(move.getFromRow() >= 0 && move.getFromRow() < 8, "Von Reihe muss gueltig sein");
        assertTrue(move.getFromCol() >= 0 && move.getFromCol() < 8, "Von Spalte muss gueltig sein");
        assertTrue(move.getToRow() >= 0 && move.getToRow() < 8, "Nach Reihe muss gueltig sein");
        assertTrue(move.getToCol() >= 0 && move.getToCol() < 8, "Nach Spalte muss gueltig sein");

        // Zug muss von einer eigenen Figur ausgehen
        Piece piece = game.getBoard().getPiece(move.getFromRow(), move.getFromCol());
        assertNotNull(piece, "Von-Feld muss eine Figur haben");
        assertEquals(Color.WHITE, piece.getColor(), "KI muss eigene Figur ziehen");
    }
}
