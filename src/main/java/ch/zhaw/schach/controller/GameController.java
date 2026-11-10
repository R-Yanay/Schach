package ch.zhaw.schach.controller;

import ch.zhaw.schach.ai.AI;
import ch.zhaw.schach.model.*;
import ch.zhaw.schach.view.GameView;
import javafx.application.Platform;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Verbindet View und Model.
 * Speichert die ausgewaehlte Figur und fuehrt nur gueltige Zuege aus.
 * Die KI spielt gegen den Spieler – der Spieler kann Weiss oder Schwarz waehlen.
 */
public class GameController {

    private ChessGame game;
    private final GameView view;
    private final AI ai;
    private final Color playerColor; // Farbe des menschlichen Spielers

    private int selectedRow = -1;
    private int selectedCol = -1;

    public GameController(ChessGame game, GameView view, int searchDepth, Color playerColor) {
        this.game = game;
        this.view = view;
        this.ai = new AI(searchDepth);
        this.playerColor = playerColor;

        // Wenn Spieler Schwarz waehlt, zieht die KI (Weiss) zuerst
        if (playerColor == Color.BLACK) {
            makeAIMove();
        } else {
            updateStatus();
        }
    }

    /**
     * Wird aufgerufen, wenn der Benutzer auf ein Feld klickt.
     */
    public void onCellClicked(int row, int col) {
        // Bei Schachmatt oder Patt nichts mehr machen
        if (game.getStatus() == GameStatus.CHECKMATE || game.getStatus() == GameStatus.STALEMATE ||
            game.getStatus() == GameStatus.INSUFFICIENT_MATERIAL ||
            game.getStatus() == GameStatus.DRAW_THREEFOLD_REPETITION ||
            game.getStatus() == GameStatus.DRAW_FIFTY_MOVES) {
            return;
        }

        // Nur der menschliche Spieler kann klicken
        if (game.getCurrentPlayer() != playerColor) {
            return;
        }

        Piece clickedPiece = game.getBoard().getPiece(row, col);
        Color currentPlayer = game.getCurrentPlayer();

        // Wenn noch keine Figur ausgewählt ist
        if (selectedRow == -1 && selectedCol == -1) {
            // Nur eigene Figuren auswählbar
            if (clickedPiece != null && clickedPiece.getColor() == currentPlayer) {
                selectedRow = row;
                selectedCol = col;
                view.highlightCell(row, col, true);
                // Zeige mögliche Züge
                showValidMoves(row, col);
            }
            return;
        }

        // Wenn bereits eine Figur ausgewählt ist
        if (clickedPiece != null && clickedPiece.getColor() == currentPlayer) {
            // Neue eigene Figur auswählen – alte Auswahl aufheben
            view.highlightCell(selectedRow, selectedCol, false);
            clearValidMoves();
            selectedRow = row;
            selectedCol = col;
            view.highlightCell(row, col, true);
            showValidMoves(row, col);
            return;
        }

        // Zug ausführen – nur wenn gültig
        int fromRow = selectedRow;
        int fromCol = selectedCol;
        
        // Prüfen, ob dies eine Promotion ist
        Piece movingPiece = game.getBoard().getPiece(fromRow, fromCol);
        boolean isPromotion = (movingPiece != null && 
            movingPiece.getType() == ch.zhaw.schach.model.PieceType.PAWN &&
            ((movingPiece.getColor() == Color.WHITE && row == 0) ||
             (movingPiece.getColor() == Color.BLACK && row == 7)));
        
        if (isPromotion) {
            // Zeige Promotion-Dialog (Callback-basiert, blockiert nicht)
            final Color pieceColor = movingPiece.getColor();
            final int finalFromRow = fromRow;
            final int finalFromCol = fromCol;
            final int finalToRow = row;
            final int finalToCol = col;

            view.showPromotionDialog(pieceColor, choice -> {
                if (choice < 0 || choice > 3) {
                    // Abgebrochen
                    view.setStatus("Promotion cancelled.");
                    return;
                }

                // Wandle die Auswahl in PieceType um
                ch.zhaw.schach.model.PieceType promotionType;
                switch (choice) {
                    case 0: promotionType = ch.zhaw.schach.model.PieceType.QUEEN; break;
                    case 1: promotionType = ch.zhaw.schach.model.PieceType.ROOK; break;
                    case 2: promotionType = ch.zhaw.schach.model.PieceType.BISHOP; break;
                    default: promotionType = ch.zhaw.schach.model.PieceType.KNIGHT; break;
                }

                boolean success = game.makeMove(finalFromRow, finalFromCol, finalToRow, finalToCol, promotionType);
                view.highlightCell(finalFromRow, finalFromCol, false);
                clearValidMoves();
                selectedRow = -1;
                selectedCol = -1;

                if (success) {
                    // Letzten Zug hervorheben
                    view.clearLastMoveHighlight();
                    view.highlightLastMove(finalFromRow, finalFromCol, finalToRow, finalToCol);
                    // Brett komplett aktualisieren
                    view.updateBoard(game.getBoard());
                    updateStatus();

                    // KI-Zug auslösen, wenn das Spiel noch läuft
                    if (game.getStatus() == GameStatus.IN_PROGRESS || game.getStatus() == GameStatus.CHECK) {
                        makeAIMove();
                    }
                } else {
                    view.setStatus("Invalid move. Try again.");
                }
            });
        } else {
            // Normaler Zug (keine Promotion)
            boolean success = game.makeMove(fromRow, fromCol, row, col, null);
            view.highlightCell(fromRow, fromCol, false);
            clearValidMoves();
            selectedRow = -1;
            selectedCol = -1;
            
            if (success) {
                // Letzten Zug hervorheben
                view.clearLastMoveHighlight();
                view.highlightLastMove(fromRow, fromCol, row, col);
                // Brett komplett aktualisieren
                view.updateBoard(game.getBoard());
                updateStatus();
                
                // KI-Zug auslösen, wenn das Spiel noch läuft
                if (game.getStatus() == GameStatus.IN_PROGRESS || game.getStatus() == GameStatus.CHECK) {
                    makeAIMove();
                }
            } else {
                view.setStatus("Invalid move. Try again.");
            }
        }
    }

    /**
     * Bricht die aktuelle Figurauswahl ab.
     * Wird aufgerufen, wenn der Benutzer mit Rechtsklick auf ein Feld klickt.
     */
    public void cancelSelection() {
        // Wenn keine Figur ausgewählt ist, nichts tun
        if (selectedRow == -1 && selectedCol == -1) {
            return;
        }

        // Highlight der ausgewählten Figur entfernen
        view.highlightCell(selectedRow, selectedCol, false);
        // Gültige Zug-Punkte löschen
        clearValidMoves();
        // Auswahl zurücksetzen
        selectedRow = -1;
        selectedCol = -1;
        view.setStatus("Selection cancelled.");
    }

    /**
     * Lässt die KI einen Zug berechnen und ausführen.
     * Läuft in einem separaten Thread mit Timeout, damit die GUI nicht einfriert.
     * Wenn die KI zu lange braucht (30 Sekunden), wird die Berechnung abgebrochen.
     */
    private void makeAIMove() {
        makeAIMoveWithTimeout(30);
    }

    /**
     * Startet die KI-Berechnung erneut mit einem höheren Timeout (60 Sekunden).
     * Wird aufgerufen, wenn der Benutzer "Nochmal warten" im Timeout-Dialog wählt.
     */
    private void retryAIMove() {
        makeAIMoveWithTimeout(60);
    }

    /**
     * Führt die KI-Berechnung mit dem angegebenen Timeout durch.
     *
     * @param timeoutSeconds die maximale Wartezeit in Sekunden
     */
    private void makeAIMoveWithTimeout(int timeoutSeconds) {
        view.setStatus("Computer is thinking...");

        // Executor für die KI-Berechnung mit Timeout
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Move> future = executor.submit(new Callable<Move>() {
            @Override
            public Move call() {
                return ai.getBestMove(game);
            }
        });

        try {
            // Warte maximal timeoutSeconds auf den KI-Zug
            Move aiMove = future.get(timeoutSeconds, TimeUnit.SECONDS);
            Platform.runLater(() -> {
                if (aiMove != null) {
                    game.makeMove(aiMove.getFromRow(), aiMove.getFromCol(),
                            aiMove.getToRow(), aiMove.getToCol(), null);
                    view.clearLastMoveHighlight();
                    view.highlightLastMove(aiMove.getFromRow(), aiMove.getFromCol(),
                            aiMove.getToRow(), aiMove.getToCol());
                    view.updateBoard(game.getBoard());
                    updateStatus();
                } else {
                    // KI findet keinen gültigen Zug — Spiel sollte eigentlich vorbei sein
                    view.setStatus("Computer has no valid move. Game over.");
                }
            });
        } catch (TimeoutException e) {
            // KI braucht zu lange — Berechnung abbrechen und Dialog anzeigen
            future.cancel(true);
            logError("AI", "Timeout after " + timeoutSeconds + " seconds.", e);
            Platform.runLater(() -> {
                view.showTimeoutDialog(
                        this::retryAIMove,  // Nochmal warten: KI neu starten mit 60s
                        () -> view.restartGame()  // Neues Spiel: zurück zum Startmenü
                );
            });
        } catch (InterruptedException e) {
            logError("AI", "Calculation interrupted.", e);
            Platform.runLater(() -> {
                view.setStatus("AI calculation interrupted.");
            });
        } catch (ExecutionException e) {
            logError("AI", "Error during calculation: " + e.getCause().getMessage(), e.getCause());
            Platform.runLater(() -> {
                view.setStatus("AI error: " + e.getCause().getMessage());
            });
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Schreibt einen Fehler mit Zeitstempel und Kategorie in die Logdatei chess.log.
     * Die Datei wird im selben Ordner wie die Spielstand-Dateien erstellt.
     *
     * @param category die Fehlerkategorie, z. B. "SAVE", "LOAD", "AI"
     * @param message die Fehlermeldung
     * @param cause die ursprüngliche Exception (kann null sein)
     */
    private static void logError(String category, String message, Throwable cause) {
        String timestamp = LocalDateTime.now().toString();
        String causeInfo = (cause != null) ? " Cause: " + cause.getMessage() : "";
        String logLine = timestamp + " [" + category + "] " + message + causeInfo + "\n";

        try (FileWriter writer = new FileWriter("chess.log", true)) {
            writer.write(logLine);
        } catch (IOException e) {
            // Logging-Fehler kann nicht weiter geworfen werden, nur auf stderr ausgeben
            System.err.println("Failed to write to chess.log: " + e.getMessage());
        }
    }

    /**
     * Zeigt die möglichen Züge als kleine Punkte auf den Zielfeldern.
     */
    private void showValidMoves(int row, int col) {
        java.util.List<Move> moves = game.getValidMoves(row, col);
        for (Move move : moves) {
            int toRow = move.getToRow();
            int toCol = move.getToCol();
            Piece target = game.getBoard().getPiece(toRow, toCol);
            if (target == null) {
                view.showValidMoveDot(toRow, toCol);
            } else {
                view.showValidCaptureDot(toRow, toCol);
            }
        }
    }

    /**
     * Entfernt alle Zug-Hervorhebungen.
     */
    private void clearValidMoves() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                view.clearDot(r, c);
            }
        }
    }

    /**
     * Aktualisiert den Status-Text in der View.
     */
    private void updateStatus() {
        GameStatus status = game.getStatus();
        Color player = game.getCurrentPlayer();
        String playerText = (player == Color.WHITE) ? "White ♔" : "Black ♚";

        switch (status) {
            case CHECKMATE:
                Color winner = (player == Color.WHITE) ? Color.BLACK : Color.WHITE;
                String winnerText = (winner == Color.WHITE) ? "White" : "Black";
                // Zeige, ob der menschliche Spieler gewonnen hat
                if (winner == playerColor) {
                    view.setStatus("Checkmate! You win!");
                } else {
                    view.setStatus("Checkmate! Computer wins.");
                }
                break;
            case STALEMATE:
                view.setStatus("Stalemate! Draw.");
                break;
            case INSUFFICIENT_MATERIAL:
                view.setStatus("Draw! Insufficient material.");
                break;
            case DRAW_THREEFOLD_REPETITION:
                view.setStatus("Draw! Threefold repetition.");
                break;
            case DRAW_FIFTY_MOVES:
                view.setStatus("Draw! 50-move rule.");
                break;
            case CHECK:
                view.setStatus("Check! " + playerText + " is in danger.");
                break;
            case IN_PROGRESS:
            default:
                if (player == playerColor) {
                    view.setStatus("Your turn " + playerText);
                } else {
                    view.setStatus("Computer is thinking...");
                }
                break;
        }
    }

    /**
     * Gibt das ChessGame zurück.
     */
    public ChessGame getGame() {
        return game;
    }

    /**
     * Speichert den aktuellen Spielzustand in den angegebenen Slot (1-3).
     */
    public void saveGame(int slot) {
        GameSaver saver = new GameSaver();
        int originalSlot = slot;

        // Versuche, in den gewählten Slot zu speichern.
        // Falls das fehlschlägt, versuche automatisch den nächsten freien Slot.
        for (int attemptSlot = slot; attemptSlot <= 3; attemptSlot++) {
            Path savePath = Path.of("save" + attemptSlot + ".json");
            try {
                saver.save(game, savePath);
                if (attemptSlot == originalSlot) {
                    view.setStatus("Game saved to slot " + attemptSlot);
                } else {
                    view.setStatus("Slot " + originalSlot + " failed. Game saved to slot " + attemptSlot + " instead.");
                }
                return;
            } catch (SaveException e) {
                logError("SAVE", "Slot " + attemptSlot + ": " + e.getMessage(), e.getCause());
            } catch (Exception e) {
                logError("SAVE", "Slot " + attemptSlot + ": Unexpected error.", e);
            }
        }

        // Alle Slots haben fehlschlagen
        view.setStatus("Save failed: Could not save to any slot. Check disk space.");
    }

    /**
     * Lädt einen gespeicherten Spielzustand aus dem angegebenen Slot (1-3).
     * Wenn der Slot leer oder beschädigt ist, wird ein Dialog angezeigt,
     * der dem Benutzer anbietet, einen anderen Slot zu versuchen.
     */
    public void loadGame(int slot) {
        GameSaver saver = new GameSaver();
        Path savePath = Path.of("save" + slot + ".json");
        try {
            ChessGame loadedGame = saver.load(savePath);
            this.game = loadedGame;
            view.updateBoard(loadedGame.getBoard());
            updateStatus();

            // Wenn der geladene Spieler am Zug NICHT der menschliche Spieler ist,
            // muss die KI zuerst ziehen (z. B. gespeichert als Weiss, geladen als Schwarz).
            if (game.getCurrentPlayer() != playerColor
                    && game.getStatus() == GameStatus.IN_PROGRESS) {
                view.setStatus("Game loaded from slot " + slot + ". Computer moves first.");
                makeAIMove();
            } else {
                view.setStatus("Game loaded from slot " + slot);
            }
        } catch (LoadException e) {
            logError("LOAD", "Slot " + slot + ": " + e.getMessage(), e.getCause());
            // Zeige Dialog: Benutzer kann einen anderen Slot wählen
            Platform.runLater(() -> {
                int selectedSlot = view.showLoadDialog(slot);
                if (selectedSlot > 0) {
                    // Benutzer hat einen anderen Slot gewählt — lade diesen
                    loadGame(selectedSlot);
                } else {
                    // Benutzer hat abgebrochen
                    view.setStatus("Load cancelled.");
                }
            });
        } catch (Exception e) {
            logError("LOAD", "Slot " + slot + ": Unexpected error.", e);
            view.setStatus("Unexpected error while loading. Slot may be corrupted.");
        }
    }
}
