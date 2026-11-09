package ch.zhaw.schach.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Speichert und lädt den Spielzustand als JSON-Datei.
 * Speichert Brett-Position, aktuellen Spieler, Rochade-Status, En-Passant,
 * 50-Züge-Zaehler, Stellungswiederholung-History und Spielstatus.
 */
public class GameSaver {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Speichert den Spielzustand in eine JSON-Datei.
     *
     * @param game das aktuelle ChessGame
     * @param path der Dateipfad (z. B. "save.json")
     */
    public void save(ChessGame game, Path path) {
        JsonObject json = new JsonObject();

        // Brett-Position als 8x8 Array speichern
        JsonArray boardArray = new JsonArray();
        ChessBoard board = game.getBoard();
        for (int row = 0; row < 8; row++) {
            JsonArray rowArray = new JsonArray();
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    JsonObject pieceObj = new JsonObject();
                    pieceObj.addProperty("type", piece.getType().name());
                    pieceObj.addProperty("color", piece.getColor().name());
                    rowArray.add(pieceObj);
                } else {
                    rowArray.add(new com.google.gson.JsonNull());
                }
            }
            boardArray.add(rowArray);
        }
        json.add("board", boardArray);

        // Aktueller Spieler
        json.addProperty("currentPlayer", game.getCurrentPlayer().name());

        // Spielstatus
        json.addProperty("status", game.getStatus().name());

        // Rochade-Status
        JsonObject castling = new JsonObject();
        castling.addProperty("whiteKingMoved", game.isWhiteKingMoved());
        castling.addProperty("blackKingMoved", game.isBlackKingMoved());
        castling.addProperty("whiteKingRookMoved", game.isWhiteKingRookMoved());
        castling.addProperty("whiteQueenRookMoved", game.isWhiteQueenRookMoved());
        castling.addProperty("blackKingRookMoved", game.isBlackKingRookMoved());
        castling.addProperty("blackQueenRookMoved", game.isBlackQueenRookMoved());
        json.add("castling", castling);

        // En-Passant
        json.addProperty("enPassantRow", game.getEnPassantRow());
        json.addProperty("enPassantCol", game.getEnPassantCol());

        // 50-Züge-Regel
        json.addProperty("fiftyMoveCounter", game.getFiftyMoveCounter());

        // Stellungswiederholung-History
        JsonArray historyArray = new JsonArray();
        List<String> history = game.getPositionHistory();
        for (String hash : history) {
            historyArray.add(hash);
        }
        json.add("positionHistory", historyArray);

        // JSON in Datei schreiben
        try (FileWriter writer = new FileWriter(path.toFile())) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Spiel nicht speichern: " + e.getMessage(), e);
        }
    }

    /**
     * Lädt den Spielzustand aus einer JSON-Datei.
     *
     * @param path der Dateipfad (z. B. "save.json")
     * @return ein neues ChessGame mit dem geladenen Zustand
     */
    public ChessGame load(Path path) {
        JsonObject json;
        try (FileReader reader = new FileReader(path.toFile())) {
            json = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException("Konnte Spiel nicht laden: " + e.getMessage(), e);
        }

        // Neues Spiel erstellen
        ChessGame game = new ChessGame();
        ChessBoard board = game.getBoard();

        // Brett-Position laden
        JsonArray boardArray = json.getAsJsonArray("board");
        for (int row = 0; row < 8; row++) {
            JsonArray rowArray = boardArray.get(row).getAsJsonArray();
            for (int col = 0; col < 8; col++) {
                if (rowArray.get(col).isJsonNull()) {
                    board.setPiece(row, col, null);
                } else {
                    JsonObject pieceObj = rowArray.get(col).getAsJsonObject();
                    PieceType type = PieceType.valueOf(pieceObj.get("type").getAsString());
                    Color color = Color.valueOf(pieceObj.get("color").getAsString());
                    board.setPiece(row, col, new Piece(type, color));
                }
            }
        }

        // Aktueller Spieler laden
        Color currentPlayer = Color.valueOf(json.get("currentPlayer").getAsString());
        game.setCurrentPlayer(currentPlayer);

        // Spielstatus laden
        GameStatus status = GameStatus.valueOf(json.get("status").getAsString());
        game.setStatus(status);

        // Rochade-Status laden
        JsonObject castling = json.getAsJsonObject("castling");
        game.setWhiteKingMoved(castling.get("whiteKingMoved").getAsBoolean());
        game.setBlackKingMoved(castling.get("blackKingMoved").getAsBoolean());
        game.setWhiteKingRookMoved(castling.get("whiteKingRookMoved").getAsBoolean());
        game.setWhiteQueenRookMoved(castling.get("whiteQueenRookMoved").getAsBoolean());
        game.setBlackKingRookMoved(castling.get("blackKingRookMoved").getAsBoolean());
        game.setBlackQueenRookMoved(castling.get("blackQueenRookMoved").getAsBoolean());

        // En-Passant laden
        int enPassantRow = json.get("enPassantRow").getAsInt();
        int enPassantCol = json.get("enPassantCol").getAsInt();
        game.setEnPassant(enPassantRow, enPassantCol);

        // 50-Züge-Zaehler laden
        int fiftyMoveCounter = json.get("fiftyMoveCounter").getAsInt();
        game.setFiftyMoveCounter(fiftyMoveCounter);

        // Stellungswiederholung-History laden
        JsonArray historyArray = json.getAsJsonArray("positionHistory");
        List<String> positionHistory = game.getPositionHistory();
        positionHistory.clear();
        for (int i = 0; i < historyArray.size(); i++) {
            positionHistory.add(historyArray.get(i).getAsString());
        }

        return game;
    }
}
