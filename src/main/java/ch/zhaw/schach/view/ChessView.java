package ch.zhaw.schach.view;

import ch.zhaw.schach.controller.GameController;
import ch.zhaw.schach.model.ChessBoard;
import ch.zhaw.schach.model.ChessGame;
import ch.zhaw.schach.model.Piece;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFX-Hauptfenster mit Schachbrett und Statusanzeige.
 */
public class ChessView extends Application {

    private GridPane boardGrid;
    private Label statusLabel;
    private StackPane[][] cellStacks; // 8x8 StackPane für Felder mit Punkten
    private Label[][] cellLabels; // 8x8 Label-Felder
    private Circle[][] moveDots; // 8x8 Punkte für gültige Züge
    private GameController controller;

    @Override
    public void start(Stage primaryStage) {
        // Hauptlayout
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        // Status-Label
        statusLabel = new Label("Chess");
        statusLabel.setFont(Font.font(16));
        root.getChildren().add(statusLabel);

        // Schachbrett
        boardGrid = new GridPane();
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.setAlignment(Pos.CENTER);

        cellLabels = new Label[8][8];
        cellStacks = new StackPane[8][8];
        moveDots = new Circle[8][8];
        ChessGame game = new ChessGame();
        ChessBoard board = game.getBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                final int r = row;
                final int c = col;

                Label cell = new Label();
                cell.setPrefSize(60, 60);
                cell.setAlignment(Pos.CENTER);
                cell.setFont(Font.font(40));

                // Feldfarbe: hell/dunkel abwechselnd
                boolean isLight = (row + col) % 2 == 0;
                cell.setStyle(String.format(
                        "-fx-background-color: %s; -fx-border-color: #333; -fx-border-width: 1;",
                        isLight ? "#F0D9B5" : "#B58863"
                ));

                // Figur anzeigen
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    cell.setText(piece.toString());
                    // Schwarze Figuren dunkler färben für bessere Sichtbarkeit
                    if (piece.getColor() == ch.zhaw.schach.model.Color.BLACK) {
                        cell.setTextFill(Color.web("#1a1a1a"));
                    } else {
                        cell.setTextFill(Color.web("#ffffff"));
                        // Weiße Figuren mit Schatten-Effekt
                        cell.setStyle(cell.getStyle() + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 2, 0, 0, 1);");
                    }
                }

                // Punkt für gültige Züge (unsichtbar)
                Circle dot = new Circle(10);
                dot.setVisible(false);
                dot.setManaged(false);

                // StackPane für Klick-Handler und Punkt
                StackPane stack = new StackPane(cell, dot);
                stack.setPrefSize(60, 60);

                // Klick-Handler
                stack.setOnMouseClicked(e -> {
                    if (controller != null) {
                        controller.onCellClicked(r, c);
                    }
                });

                cellLabels[row][col] = cell;
                cellStacks[row][col] = stack;
                moveDots[row][col] = dot;
                boardGrid.add(stack, col, row);
            }
        }

        root.getChildren().add(boardGrid);

        // Neustart-Button
        Button restartButton = new Button("New Game");
        restartButton.setFont(Font.font(14));
        restartButton.setPrefWidth(150);
        restartButton.setOnAction(e -> restartGame());
        root.getChildren().add(restartButton);

        // Controller erstellen
        controller = new GameController(game, this);

        // Szene
        Scene scene = new Scene(root, 520, 580);
        primaryStage.setTitle("Chess");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Aktualisiert die Anzeige eines einzelnen Felds.
     */
    public void updateCell(int row, int col, Piece piece) {
        Label cell = cellLabels[row][col];
        if (piece != null) {
            cell.setText(piece.toString());
            if (piece.getColor() == ch.zhaw.schach.model.Color.BLACK) {
                cell.setTextFill(Color.web("#1a1a1a"));
            } else {
                cell.setTextFill(Color.web("#ffffff"));
            }
        } else {
            cell.setText("");
            cell.setTextFill(Color.BLACK);
        }
    }

    /**
     * Aktualisiert das gesamte Brett.
     */
    public void updateBoard(ChessBoard board) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                updateCell(row, col, board.getPiece(row, col));
            }
        }
    }

    /**
     * Setzt den Status-Text.
     */
    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    /**
     * Hebt ein Feld hervor (gelber Rahmen) oder entfernt die Hervorhebung.
     */
    public void highlightCell(int row, int col, boolean highlight) {
        Label cell = cellLabels[row][col];
        boolean isLight = (row + col) % 2 == 0;
        String baseColor = isLight ? "#F0D9B5" : "#B58863";
        if (highlight) {
            cell.setStyle(String.format(
                    "-fx-background-color: #FFD700; -fx-border-color: #333; -fx-border-width: 1; -fx-background-radius: 2;"
            ));
        } else {
            cell.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: #333; -fx-border-width: 1;",
                    baseColor
            ));
        }
    }

    /**
     * Gibt das GridPane zurück (für Klick-Handler).
     */
    public GridPane getBoardGrid() {
        return boardGrid;
    }

    /**
     * Gibt die Zell-Labels zurück (für Klick-Handler).
     */
    public Label[][] getCellLabels() {
        return cellLabels;
    }

    /**
     * Zeigt einen kleinen grünen Punkt für einen gültigen Zug.
     */
    public void showValidMoveDot(int row, int col) {
        Circle dot = moveDots[row][col];
        dot.setFill(Color.web("rgba(0, 180, 0, 0.5)"));
        dot.setVisible(true);
        dot.setManaged(true);
    }

    /**
     * Zeigt einen roten Ring für einen gültigen Capture-Zug.
     */
    public void showValidCaptureDot(int row, int col) {
        Circle dot = moveDots[row][col];
        dot.setFill(Color.web("rgba(255, 0, 0, 0.3)"));
        dot.setRadius(28);
        dot.setVisible(true);
        dot.setManaged(true);
    }

    /**
     * Entfernt den Punkt von einem Feld.
     */
    public void clearDot(int row, int col) {
        Circle dot = moveDots[row][col];
        dot.setVisible(false);
        dot.setManaged(false);
        dot.setRadius(10);
    }

    /**
     * Startet ein neues Spiel.
     */
    public void restartGame() {
        ChessGame newGame = new ChessGame();
        // Controller neu erstellen
        controller = new GameController(newGame, this);
        // Brett zurücksetzen
        updateBoard(newGame.getBoard());
        // Alle Hervorhebungen entfernen
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                highlightCell(r, c, false);
                clearDot(r, c);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
