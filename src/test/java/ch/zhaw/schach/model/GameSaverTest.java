package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die JSON Speichern/Laden-Funktion.
 * Bestaetigt, dass ein Spielzustand korrekt gespeichert und geladen wird.
 */
class GameSaverTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoad_restoresBoard() {
        // Spiel starten und ein paar Zuege machen
        ChessGame game = new ChessGame();
        game.makeMove(1, 4, 3, 4, null);  // e2-e4
        game.makeMove(6, 3, 4, 3, null);  // e7-e5
        game.makeMove(0, 6, 2, 5, null);  // Sf1-g3

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
                    assertNull(p2, "Square " + row + "," + col + " should be empty");
                } else {
                    assertNotNull(p2, "Square " + row + "," + col + " should have a piece");
                    assertEquals(p1.getType(), p2.getType(), "Piece type on square " + row + "," + col);
                    assertEquals(p1.getColor(), p2.getColor(), "Piece color on square " + row + "," + col);
                }
            }
        }
    }

    @Test
    void saveAndLoad_restoresAllState() {
        // Spiel starten und Zuege machen
        ChessGame game = new ChessGame();
        game.makeMove(1, 4, 3, 4, null);  // e2-e4
        game.makeMove(6, 3, 4, 3, null);  // e7-e5

        // Speichern und laden
        GameSaver saver = new GameSaver();
        Path savePath = tempDir.resolve("test_state.json");
        saver.save(game, savePath);
        ChessGame loaded = saver.load(savePath);

        // Aktueller Spieler muss identisch sein
        assertEquals(game.getCurrentPlayer(), loaded.getCurrentPlayer(), "Current player");

        // Spielstatus muss identisch sein
        assertEquals(game.getStatus(), loaded.getStatus(), "Game status");

        // Castling-Status muss identisch sein
        assertEquals(game.isWhiteKingMoved(), loaded.isWhiteKingMoved(), "White king moved");
        assertEquals(game.isBlackKingMoved(), loaded.isBlackKingMoved(), "Black king moved");
        assertEquals(game.isWhiteKingRookMoved(), loaded.isWhiteKingRookMoved(), "White king rook moved");
        assertEquals(game.isWhiteQueenRookMoved(), loaded.isWhiteQueenRookMoved(), "White queen rook moved");
        assertEquals(game.isBlackKingRookMoved(), loaded.isBlackKingRookMoved(), "Black king rook moved");
        assertEquals(game.isBlackQueenRookMoved(), loaded.isBlackQueenRookMoved(), "Black queen rook moved");

        // En Passant muss identisch sein
        assertEquals(game.getEnPassantRow(), loaded.getEnPassantRow(), "En passant row");
        assertEquals(game.getEnPassantCol(), loaded.getEnPassantCol(), "En passant col");

        // 50-Zuege-Zaehler muss identisch sein
        assertEquals(game.getFiftyMoveCounter(), loaded.getFiftyMoveCounter(), "50-move counter");

        // Dreimalige Wiederholung History muss identisch sein
        assertEquals(game.getPositionHistory(), loaded.getPositionHistory(), "Threefold repetition history");
    }
}

