package ch.zhaw.schach;

import ch.zhaw.schach.view.ChessView;
import javafx.application.Application;

/**
 * Einstiegspunkt der Schach-App.
 * Startet die JavaFX-Anwendung.
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(ChessView.class, args);
    }
}
