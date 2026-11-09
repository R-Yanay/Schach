package ch.zhaw.schach.view;

import ch.zhaw.schach.model.ChessBoard;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * Schnittstelle zwischen GameController und der View.
 * GameController spricht nur über dieses Interface mit der View,
 * was die Trennung von Controller und View verbessert.
 */
public interface GameView {

    /** Aktualisiert das gesamte Brett. */
    void updateBoard(ChessBoard board);

    /** Setzt den Status-Text. */
    void setStatus(String status);

    /** Hebt ein Feld hervor (gelber Hintergrund) oder entfernt die Hervorhebung. */
    void highlightCell(int row, int col, boolean highlight);

    /** Hebt die Start- und Zielfelder des letzten Zugs hellgrün hervor. */
    void highlightLastMove(int fromRow, int fromCol, int toRow, int toCol);

    /** Entfernt die letzte-Zug-Hervorhebung und stellt die originalen Farben wieder her. */
    void clearLastMoveHighlight();

    /** Zeigt einen kleinen grünen Punkt für einen gültigen Zug. */
    void showValidMoveDot(int row, int col);

    /** Zeigt einen roten Ring für einen gültigen Capture-Zug. */
    void showValidCaptureDot(int row, int col);

    /** Entfernt den Punkt von einem Feld. */
    void clearDot(int row, int col);
}
