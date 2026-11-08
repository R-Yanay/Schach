package ch.zhaw.schach.view;

import ch.zhaw.schach.controller.GameController;
import ch.zhaw.schach.model.ChessBoard;
import ch.zhaw.schach.model.ChessGame;
import ch.zhaw.schach.model.Color;
import ch.zhaw.schach.model.Piece;
import ch.zhaw.schach.model.PieceStyle;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX-Hauptfenster mit Startbildschirm und Schachbrett.
 */
public class ChessView extends Application {

    private GridPane boardGrid;
    private Label statusLabel;
    private StackPane[][] cellStacks; // 8x8 StackPane für Felder mit Punkten
    private Label[][] cellLabels; // 8x8 Label-Felder
    private Circle[][] moveDots; // 8x8 Punkte für gültige Züge
    private GameController controller;

    private Stage mainStage;
    private int selectedSearchDepth = 3; // default: Medium
    private Color selectedPlayerColor = Color.WHITE; // default: White
    private PieceStyle selectedPieceStyle = PieceStyle.OLDSCHOOL; // default: OldSchool

    // Board flip: true when playing as Black (board rotated 180°)
    private boolean flipped = false;

    // References to buttons for highlighting
    private Button easyBtn, mediumBtn, hardBtn;
    private Button whiteBtn, blackBtn;
    private Button oldSchoolBtn, chessComBtn, classicBtn;

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        showStartScreen();
    }

    /**
     * Zeigt den Startbildschirm mit Schwierigkeits-, Farb- und Style-Auswahl.
     */
    private void showStartScreen() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c2c2c;");

        // Titel
        Text title = new Text("Chess");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(javafx.scene.paint.Color.web("#ffffff"));

        // --- Difficulty section ---
        Label difficultySubtitle = new Label("Select difficulty:");
        difficultySubtitle.setFont(Font.font(14));
        difficultySubtitle.setTextFill(javafx.scene.paint.Color.web("#cccccc"));

        HBox difficultyBox = new HBox(10);
        difficultyBox.setAlignment(Pos.CENTER);
        easyBtn = createDifficultyButton("Easy", 1);
        mediumBtn = createDifficultyButton("Medium", 3);
        hardBtn = createDifficultyButton("Hard", 5);
        difficultyBox.getChildren().addAll(easyBtn, mediumBtn, hardBtn);

        // --- Color section ---
        Label colorSubtitle = new Label("Play as:");
        colorSubtitle.setFont(Font.font(14));
        colorSubtitle.setTextFill(javafx.scene.paint.Color.web("#cccccc"));

        HBox colorBox = new HBox(10);
        colorBox.setAlignment(Pos.CENTER);
        whiteBtn = createColorButton("White", ch.zhaw.schach.model.Color.WHITE);
        blackBtn = createColorButton("Black", ch.zhaw.schach.model.Color.BLACK);
        colorBox.getChildren().addAll(whiteBtn, blackBtn);

        // --- Piece Style section ---
        Label styleSubtitle = new Label("Piece style:");
        styleSubtitle.setFont(Font.font(14));
        styleSubtitle.setTextFill(javafx.scene.paint.Color.web("#cccccc"));

        HBox styleBox = new HBox(10);
        styleBox.setAlignment(Pos.CENTER);
        oldSchoolBtn = createStyleButton("OldSchool", PieceStyle.OLDSCHOOL);
        chessComBtn = createStyleButton("Chess.com", PieceStyle.CHESSCOM);
        classicBtn = createStyleButton("Classic", PieceStyle.CLASSIC);
        styleBox.getChildren().addAll(oldSchoolBtn, chessComBtn, classicBtn);

        // Start-Button
        Button startBtn = new Button("New Game");
        startBtn.setFont(Font.font(14));
        startBtn.setPrefWidth(140);
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 8 16;");
        startBtn.setOnAction(e -> startGame());

        root.getChildren().addAll(title, difficultySubtitle, difficultyBox,
                colorSubtitle, colorBox, styleSubtitle, styleBox, new Region(), startBtn);
        VBox.setVgrow((Region) root.getChildren().get(7), Priority.ALWAYS);

        // Set default highlights
        updateDifficultyButtons(mediumBtn);
        updateColorButtons(whiteBtn);
        updateStyleButtons(oldSchoolBtn);

        Scene scene = new Scene(root, 400, 520);
        mainStage.setTitle("Chess");
        mainStage.setScene(scene);
        mainStage.setResizable(false);
        mainStage.show();
    }

    /**
     * Erstellt einen Schwierigkeits-Button mit Highlight-Effekt.
     */
    private Button createDifficultyButton(String text, int depth) {
        Button btn = new Button(text);
        btn.setFont(Font.font(13));
        btn.setPrefWidth(90);
        btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 6 12;");

        btn.setOnAction(e -> {
            selectedSearchDepth = depth;
            updateDifficultyButtons(btn);
        });

        return btn;
    }

    /**
     * Erstellt einen Farb-Button (White/Black) mit Highlight-Effekt.
     */
    private Button createColorButton(String text, ch.zhaw.schach.model.Color color) {
        Button btn = new Button(text);
        btn.setFont(Font.font(13));
        btn.setPrefWidth(100);
        btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 6 12;");

        btn.setOnAction(e -> {
            selectedPlayerColor = color;
            updateColorButtons(btn);
        });

        return btn;
    }

    /**
     * Aktualisiert die Farbe der Schwierigkeits-Buttons.
     */
    private void updateDifficultyButtons(Button selected) {
        Button[] buttons = {easyBtn, mediumBtn, hardBtn};
        for (Button btn : buttons) {
            if (btn == selected) {
                btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; "
                        + "-fx-background-radius: 5; -fx-padding: 6 12;");
            } else {
                btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                        + "-fx-background-radius: 5; -fx-padding: 6 12;");
            }
        }
    }

    /**
     * Aktualisiert die Farbe der Farb-Buttons.
     */
    private void updateColorButtons(Button selected) {
        Button[] buttons = {whiteBtn, blackBtn};
        for (Button btn : buttons) {
            if (btn == selected) {
                btn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; "
                        + "-fx-background-radius: 5; -fx-padding: 6 12;");
            } else {
                btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                        + "-fx-background-radius: 5; -fx-padding: 6 12;");
            }
        }
    }

    /**
     * Erstellt einen Style-Button (OldSchool/Chess.com/Classic) mit Highlight-Effekt.
     */
    private Button createStyleButton(String text, PieceStyle style) {
        Button btn = new Button(text);
        btn.setFont(Font.font(12));
        btn.setPrefWidth(90);
        btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 6 12;");

        btn.setOnAction(e -> {
            selectedPieceStyle = style;
            updateStyleButtons(btn);
        });

        return btn;
    }

    /**
     * Aktualisiert die Farbe der Style-Buttons.
     */
    private void updateStyleButtons(Button selected) {
        Button[] buttons = {oldSchoolBtn, chessComBtn, classicBtn};
        for (Button btn : buttons) {
            if (btn == selected) {
                btn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; "
                        + "-fx-background-radius: 5; -fx-padding: 6 12;");
            } else {
                btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                        + "-fx-background-radius: 5; -fx-padding: 6 12;");
            }
        }
    }

    /**
     * Startet das Spiel mit der gewählten Schwierigkeit, Farbe und Style.
     * Wenn Schwarz gewählt ist, wird das Brett gedreht (flipped = true).
     */
    private void startGame() {
        ChessGame game = new ChessGame();
        flipped = (selectedPlayerColor == Color.BLACK);
        showGameScreen(game.getBoard());
        controller = new GameController(game, this, selectedSearchDepth, selectedPlayerColor);
    }

    /**
     * Zeigt das Spielfenster mit Schachbrett.
     * Wenn flipped=true (Schwarz), wird das Brett 180° gedreht angezeigt.
     */
    private void showGameScreen(ChessBoard board) {
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

        for (int visRow = 0; visRow < 8; visRow++) {
            for (int visCol = 0; visCol < 8; visCol++) {
                // Konvertiere visuelle Koordinaten zu Brett-Koordinaten
                final int boardRow = flipped ? (7 - visRow) : visRow;
                final int boardCol = flipped ? (7 - visCol) : visCol;

                Label cell = new Label();
                cell.setPrefSize(60, 60);
                cell.setAlignment(Pos.CENTER);
                cell.setFont(Font.font(40));

                // Feldfarbe: hell/dunkel abwechselnd (basierend auf Brett-Koordinaten)
                boolean isLight = (boardRow + boardCol) % 2 == 0;
                cell.setStyle(String.format(
                        "-fx-background-color: %s; -fx-border-color: #333; -fx-border-width: 1;",
                        isLight ? "#F0D9B5" : "#B58863"
                ));

                // Figur anzeigen mit gewähltem Style
                Piece piece = board.getPiece(boardRow, boardCol);
                if (piece != null) {
                    cell.setText(String.valueOf(piece.getType().getSymbol(piece.getColor(), selectedPieceStyle)));
                    // Schwarze Figuren dunkler färben für bessere Sichtbarkeit
                    if (piece.getColor() == ch.zhaw.schach.model.Color.BLACK) {
                        cell.setTextFill(javafx.scene.paint.Color.web("#1a1a1a"));
                    } else {
                        cell.setTextFill(javafx.scene.paint.Color.web("#ffffff"));
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

                // Klick-Handler: visuelle Koordinaten zurück zu Brett-Koordinaten
                stack.setOnMouseClicked(e -> {
                    if (controller != null) {
                        controller.onCellClicked(boardRow, boardCol);
                    }
                });

                // Arrays werden mit Brett-Koordinaten indiziert
                cellLabels[boardRow][boardCol] = cell;
                cellStacks[boardRow][boardCol] = stack;
                moveDots[boardRow][boardCol] = dot;

                // GridPane bekommt visuelle Position
                boardGrid.add(stack, visCol, visRow);
            }
        }

        root.getChildren().add(boardGrid);

        // Neustart-Button
        Button restartButton = new Button("New Game");
        restartButton.setFont(Font.font(14));
        restartButton.setPrefWidth(150);
        restartButton.setOnAction(e -> restartGame());
        root.getChildren().add(restartButton);

        // Szene
        Scene scene = new Scene(root, 520, 580);
        mainStage.setTitle("Chess");
        mainStage.setScene(scene);
        mainStage.setResizable(false);
        mainStage.show();
    }

    /**
     * Aktualisiert die Anzeige eines einzelnen Felds mit dem gewählten Style.
     */
    public void updateCell(int row, int col, Piece piece) {
        Label cell = cellLabels[row][col];
        if (piece != null) {
            cell.setText(String.valueOf(piece.getType().getSymbol(piece.getColor(), selectedPieceStyle)));
            if (piece.getColor() == ch.zhaw.schach.model.Color.BLACK) {
                cell.setTextFill(javafx.scene.paint.Color.web("#1a1a1a"));
            } else {
                cell.setTextFill(javafx.scene.paint.Color.web("#ffffff"));
            }
        } else {
            cell.setText("");
            cell.setTextFill(javafx.scene.paint.Color.BLACK);
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
     * Hebt die Start- und Zielfelder des letzten Zugs hellgrün hervor.
     */
    public void highlightLastMove(int fromRow, int fromCol, int toRow, int toCol) {
        highlightLastMoveCell(fromRow, fromCol);
        highlightLastMoveCell(toRow, toCol);
    }

    private void highlightLastMoveCell(int row, int col) {
        Label cell = cellLabels[row][col];
        cell.setStyle(String.format(
                "-fx-background-color: #A0D468; -fx-border-color: #333; -fx-border-width: 1;"
        ));
    }

    /**
     * Entfernt die letzte-Zug-Hervorhebung und stellt die originalen Farben wieder her.
     */
    public void clearLastMoveHighlight() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                clearLastMoveCell(row, col);
            }
        }
    }

    private void clearLastMoveCell(int row, int col) {
        Label cell = cellLabels[row][col];
        boolean isLight = (row + col) % 2 == 0;
        String baseColor = isLight ? "#F0D9B5" : "#B58863";
        cell.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: #333; -fx-border-width: 1;",
                baseColor
        ));
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
        dot.setFill(javafx.scene.paint.Color.web("rgba(0, 180, 0, 0.5)"));
        dot.setVisible(true);
        dot.setManaged(true);
    }

    /**
     * Zeigt einen roten Ring für einen gültigen Capture-Zug.
     */
    public void showValidCaptureDot(int row, int col) {
        Circle dot = moveDots[row][col];
        dot.setFill(javafx.scene.paint.Color.web("rgba(255, 0, 0, 0.3)"));
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
     * Geht zurück zum Startbildschirm.
     */
    public void restartGame() {
        showStartScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
