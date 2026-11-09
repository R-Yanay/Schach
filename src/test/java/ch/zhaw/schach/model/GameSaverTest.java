package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die JSON-Speichern/Laden-Funktion.
 * Prueft, dass ein Spielzustand korrekt gespeichert und geladen wird.
 */
class GameSaverTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoad_restoresBoard() {
        // Spiel starten und ein paar Zuege machen
        ChessGame game = new ChessGame();
        game.makeMove(1, 4, 3, 4);  // e2-e4
        game.makeMove(6, 3, 4, 3);  // e7-e5
        game.makeMove(0, 6, 2, 5);  // Sf1-g3

        // Speichern und laden
        GameSaver saver = new GameSaver();
        Path savePath = tempDir.resolve("test_save.json");
        saver.save(game, savePath);
        ChessGame loaded = saver.load(savePath);

        // Brett-Position muss identisch sein
        ChessBoard original = game.getBoard();
        ChessBoard loadedBoard = loaded.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p1 = original.getPiece(row, col);
                Piece p2 = loadedBoard.getPiece(row, col);
                if (p1 == null) {
                    assertNull(p2, "Feld " + row + "," + col + " sollte leer sein");
                } else {
                    assertNotNull(p2, "Feld " + row + "," + col + " sollte eine Figur haben");
                    assertEquals(p1.getType(), p2.getType(), "Figurtyp auf Feld " + row + "," + col);
                    assertEquals(p1.getColor(), p2.getColor(), "Figurfarbe auf Feld " + row + "," + col);
                }
            }
        }
    }

    @Test
    void saveAndLoad_restoresAllState() {
        // Spiel starten und Zuege machen
        ChessGame game = new ChessGame();
        game.makeMove(1, 4, 3, 4);  // e2-e4
        game.makeMove(6, 3, 4, 3);  // e7-e5

        // Speichern und laden
        GameSaver saver = new GameSaver();
        Path savePath = tempDir.resolve("test_state.json");
        saver.save(game, savePath);
        ChessGame loaded = saver.load(savePath);

        // Aktueller Spieler muss identisch sein
        assertEquals(game.getCurrentPlayer(), loaded.getCurrentPlayer(), "Aktueller Spieler");

        // Spielstatus muss identisch sein
        assertEquals(game.getStatus(), loaded.getStatus(), "Spielstatus");

        // Rochade-Status muss identisch sein
        assertEquals(game.isWhiteKingMoved(), loaded.isWhiteKingMoved(), "Weißer König bewegt");
        assertEquals(game.isBlackKingMoved(), loaded.isBlackKingMoved(), "Schwarzer König bewegt");
        assertEquals(game.isWhiteKingRookMoved(), loaded.isWhiteKingRookMoved(), "Weißer Königsturm bewegt");
        assertEquals(game.isWhiteQueenRookMoved(), loaded.isWhiteQueenRookMoved(), "Weißer Damenturm bewegt");
        assertEquals(game.isBlackKingRookMoved(), loaded.isBlackKingRookMoved(), "Schwarzer Königsturm bewegt");
        assertEquals(game.isBlackQueenRookMoved(), loaded.isBlackQueenRookMoved(), "Schwarzer Damenturm bewegt");

        // En-Passant muss identisch sein
        assertEquals(game.getEnPassantRow(), loaded.getEnPassantRow(), "En-Passant Reihe");
        assertEquals(game.getEnPassantCol(), loaded.getEnPassantCol(), "En-Passant Spalte");

        // 50-Züge-Zähler muss identisch sein
        assertEquals(game.getFiftyMoveCounter(), loaded.getFiftyMoveCounter(), "50-Züge-Zähler");

        // Stellungswiederholung-History muss identisch sein
        assertEquals(game.getPositionHistory(), loaded.getPositionHistory(), "Stellungswiederholung-History");
    }
}
