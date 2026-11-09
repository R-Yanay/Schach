package ch.zhaw.schach.view;

import ch.zhaw.schach.controller.GameController;
import ch.zhaw.schach.model.BoardTheme;
import ch.zhaw.schach.model.ChessGame;
import ch.zhaw.schach.model.Color;
import ch.zhaw.schach.model.PieceStyle;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX-Hauptfenster.
 * Delegiert an StartScreen (Menü) und GameScreen (Spielfenster).
 */
public class ChessView extends Application {

    private Stage mainStage;
    private GameScreen gameScreen;

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        showStartScreen();
    }

    /**
     * Zeigt den Startbildschirm. Beim Klick auf "New Game" werden die
     * gewählten Einstellungen an startGame weitergegeben.
     */
    private void showStartScreen() {
        StartScreen startScreen = new StartScreen(mainStage, (searchDepth, playerColor, pieceStyle, boardTheme) -> {
            startGame(searchDepth, playerColor, pieceStyle, boardTheme);
        });
        startScreen.show();
    }

    /**
     * Startet das Spiel mit den gewählten Einstellungen.
     * Wenn Schwarz gewählt ist, wird das Brett gedreht (flipped = true).
     */
    private void startGame(int searchDepth, Color playerColor,
                           PieceStyle pieceStyle, BoardTheme boardTheme) {
        ChessGame game = new ChessGame();
        boolean flipped = (playerColor == Color.BLACK);

        gameScreen = new GameScreen(mainStage, this::showStartScreen,
                boardTheme, pieceStyle, flipped);
        gameScreen.show(game.getBoard());

        GameController controller = new GameController(game, gameScreen, searchDepth, playerColor);
        gameScreen.setController(controller);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
