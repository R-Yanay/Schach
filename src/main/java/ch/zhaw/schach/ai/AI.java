package ch.zhaw.schach.ai;

import ch.zhaw.schach.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Minimax-KI für Schach.
 * Die Suchtiefe ist konfigurierbar: 1 (Einfach), 3 (Mittel), 5 (Schwer).
 */
public class AI {

    private final int searchDepth;
    private final Evaluator evaluator;
    private final Random random;

    public AI(int searchDepth) {
        this.searchDepth = searchDepth;
        this.evaluator = new Evaluator();
        this.random = new Random();
    }

    /**
     * Berechnet den besten Zug für die angegebene Farbe.
     */
    public Move getBestMove(ChessGame game) {
        List<Move> allMoves = getAllMoves(game, game.getCurrentPlayer());
        if (allMoves.isEmpty()) return null;

        // Mische Züge für Abwechslung bei gleichen Bewertungen
        Collections.shuffle(allMoves, random);

        Color aiColor = game.getCurrentPlayer();
        boolean isMaximizing = (aiColor == Color.WHITE);

        Move bestMove = null;
        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : allMoves) {
            ChessGame simulatedGame = game.copy();
            if (simulatedGame.makeMove(move.getFromRow(), move.getFromCol(),
                    move.getToRow(), move.getToCol())) {
                int score = minimax(simulatedGame, searchDepth - 1,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, !isMaximizing);

                if (isMaximizing) {
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = move;
                    }
                } else {
                    if (score < bestScore) {
                        bestScore = score;
                        bestMove = move;
                    }
                }
            }
        }

        return bestMove != null ? bestMove : allMoves.get(0);
    }

    /**
     * Minimax-Algorithmus mit Alpha-Beta-Pruning.
     */
    private int minimax(ChessGame game, int depth, int alpha, int beta, boolean isMaximizing) {
        if (depth == 0 || game.getStatus() == GameStatus.CHECKMATE || game.getStatus() == GameStatus.STALEMATE) {
            return evaluator.evaluate(game.getBoard());
        }

        List<Move> moves = getAllMoves(game, game.getCurrentPlayer());

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                ChessGame simulatedGame = game.copy();
                if (simulatedGame.makeMove(move.getFromRow(), move.getFromCol(),
                        move.getToRow(), move.getToCol())) {
                    int eval = minimax(simulatedGame, depth - 1, alpha, beta, false);
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                ChessGame simulatedGame = game.copy();
                if (simulatedGame.makeMove(move.getFromRow(), move.getFromCol(),
                        move.getToRow(), move.getToCol())) {
                    int eval = minimax(simulatedGame, depth - 1, alpha, beta, true);
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) break;
                }
            }
            return minEval;
        }
    }

    /**
     * Gibt alle gültigen Züge für eine Farbe zurück.
     * Nutzt die effiziente getValidMoves-Methode pro Figur statt alle 64x64 Kombinationen zu prüfen.
     */
    private List<Move> getAllMoves(ChessGame game, Color color) {
        List<Move> moves = new ArrayList<>();
        ChessBoard board = game.getBoard();

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && piece.getColor() == color) {
                    moves.addAll(game.getValidMoves(fromRow, fromCol));
                }
            }
        }
        return moves;
    }
}
