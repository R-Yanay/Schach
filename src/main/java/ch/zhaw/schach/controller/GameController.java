package ch.zhaw.schach.controller;

import ch.zhaw.schach.ai.AI;
import ch.zhaw.schach.model.*;
import ch.zhaw.schach.view.ChessView;
import javafx.application.Platform;

/**
 * Verbinzt View und Model.
 * Speichert die ausgewählte Figur und führt nur gültige Züge aus.
 * Nach jedem Spieler-Zug (Weiß) denkt die KI nach und zieht als Schwarz.
 */
public class GameController {

    private final ChessGame game;
    private final ChessView view;
    private final AI ai;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public GameController(ChessGame game, ChessView view, int searchDepth) {
        this.game = game;
        this.view = view;
        this.ai = new AI(searchDepth);
        updateStatus();
    }

    /**
     * Wird aufgerufen, wenn der Benutzer auf ein Feld klickt.
     */
    public void onCellClicked(int row, int col) {
        // Bei Schachmatt oder Patt nichts mehr machen
        if (game.getStatus() == GameStatus.CHECKMATE || game.getStatus() == GameStatus.STALEMATE) {
            return;
        }

        // Nur Weiß (Spieler) kann klicken – Schwarz ist die KI
        if (game.getCurrentPlayer() != Color.WHITE) {
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
        boolean success = game.makeMove(selectedRow, selectedCol, row, col);
        view.highlightCell(selectedRow, selectedCol, false);
        clearValidMoves();
        selectedRow = -1;
        selectedCol = -1;

        if (success) {
            // Brett komplett aktualisieren
            view.updateBoard(game.getBoard());
            updateStatus();

            // Trigger AI move if game is still running
            if (game.getStatus() == GameStatus.IN_PROGRESS || game.getStatus() == GameStatus.CHECK) {
                makeAIMove();
            }
        } else {
            view.setStatus("Invalid move. Try again.");
        }
    }

    /**
     * Lässt die KI einen Zug berechnen und ausführen.
     * Läuft in einem separaten Thread, damit die GUI nicht einfriert.
     */
    private void makeAIMove() {
        view.setStatus("Computer is thinking...");

        new Thread(() -> {
            Move aiMove = ai.getBestMove(game);
            if (aiMove != null) {
                Platform.runLater(() -> {
                    game.makeMove(aiMove.getFromRow(), aiMove.getFromCol(),
                            aiMove.getToRow(), aiMove.getToCol());
                    view.updateBoard(game.getBoard());
                    updateStatus();
                });
            }
        }).start();
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
                view.setStatus("Checkmate! " + winnerText + " wins.");
                break;
            case STALEMATE:
                view.setStatus("Stalemate! Draw.");
                break;
            case CHECK:
                view.setStatus("Check! " + playerText + " is in danger.");
                break;
            case IN_PROGRESS:
            default:
                view.setStatus("" + playerText + " to move");
                break;
        }
    }

    /**
     * Gibt das ChessGame zurück.
     */
    public ChessGame getGame() {
        return game;
    }
}
