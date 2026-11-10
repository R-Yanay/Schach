package ch.zhaw.schach.view;

import ch.zhaw.schach.model.ChessBoard;
import ch.zhaw.schach.model.Color;
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

    /**
     * Zeigt einen Dialog, wenn die KI zu lange zum Nachdenken braucht.
     * Der Benutzer kann zwischen "Nochmal warten" und "Neues Spiel" wählen.
     *
     * @param retryAction wird ausgeführt, wenn der Benutzer nochmal warten möchte
     * @param restartAction wird ausgeführt, wenn der Benutzer ein neues Spiel starten möchte
     */
    void showTimeoutDialog(Runnable retryAction, Runnable restartAction);

    /**
     * Zeigt einen Dialog, wenn ein Slot leer oder beschädigt ist.
     * Der Benutzer kann einen anderen Slot zum Laden wählen oder abbrechen.
     *
     * @param failedSlot der Slot, der fehlgeschlagen ist (1-3)
     * @return der gewählte Slot (1-3) oder -1 wenn abgebrochen
     */
    int showLoadDialog(int failedSlot);

    /**
     * Zeigt einen Promotion-Dialog, wenn ein Bauer das Ende erreicht.
     * Der Benutzer wählt zwischen Queen, Rook, Bishop und Knight.
     * Die Auswahl wird über den Callback zurückgegeben.
     *
     * @param color die Farbe des promovierenden Bauers
     * @param onSelected wird mit der gewählten Figur aufgerufen (0=QUEEN, 1=ROOK, 2=BISHOP, 3=KNIGHT, -1=abgebrochen)
     */
    void showPromotionDialog(Color color, java.util.function.IntConsumer onSelected);

    /**
     * Beendet das aktuelle Spiel und kehrt zum Startmenü zurück.
     */
    void restartGame();
}
