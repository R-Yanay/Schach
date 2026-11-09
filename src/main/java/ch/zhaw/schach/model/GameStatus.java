package ch.zhaw.schach.model;

/**
 * Spielstatus.
 */
public enum GameStatus {
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE,
    INSUFFICIENT_MATERIAL,
    DRAW_THREEFOLD_REPETITION,
    DRAW_FIFTY_MOVES
}
