package ch.zhaw.schach.view;

import ch.zhaw.schach.controller.GameController;
import ch.zhaw.schach.model.BoardTheme;
import ch.zhaw.schach.model.ChessBoard;
import ch.zhaw.schach.model.Color;
import ch.zhaw.schach.model.Piece;
import ch.zhaw.schach.model.PieceStyle;
import ch.zhaw.schach.model.PieceType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import java.util.Optional;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.ViewBox;
import javafx.embed.swing.SwingFXUtils;

/**
 * Spielfenster mit Schachbrett, Figuren, Status-Anzeige und Hervorhebungen.
 * Implementiert GameView, damit der GameController die Anzeige steuern kann.
 */
class GameScreen implements GameView {

    private final Stage stage;
    private final RestartCallback restartCallback;

    private final BoardTheme boardTheme;
    private final PieceStyle pieceStyle;

    private final boolean flipped;

    private VBox rootContainer;
    private GridPane boardGrid;
    private StackPane boardStack;  // Wrapper für Overlay-Unterstützung
    private Label statusLabel;
    private Pane[][] cellPanes;
    private ImageView[][] cellImages;
    private Circle[][] moveDots;

    private GameController controller;

    /**
     * Wird aufgerufen, wenn der Benutzer "New Game" im Spielfenster klickt.
     */
    interface RestartCallback {
        void onRestart();
    }

    GameScreen(Stage stage, RestartCallback restartCallback,
               BoardTheme boardTheme, PieceStyle pieceStyle, boolean flipped) {
        this.stage = stage;
        this.restartCallback = restartCallback;
        this.boardTheme = boardTheme;
        this.pieceStyle = pieceStyle;
        this.flipped = flipped;
    }

    /**
     * Zeigt das Spielfenster mit dem gegebenen Brett.
     */
    void show(ChessBoard board) {
        rootContainer = new VBox(0);
        rootContainer.setPadding(new Insets(0));
        rootContainer.setAlignment(Pos.CENTER);

        // Menüleiste oben
        MenuBar menuBar = new MenuBar();
        Menu gameMenu = new Menu("Game");

        // Speichern Untermenü mit 3 Slots
        Menu saveMenu = new Menu("Save");
        for (int slot = 1; slot <= 3; slot++) {
            final int s = slot;
            MenuItem saveSlot = new MenuItem("Slot " + slot);
            saveSlot.setOnAction(e -> {
                if (controller != null) {
                    controller.saveGame(s);
                }
            });
            saveMenu.getItems().add(saveSlot);
        }

        // Laden Untermenü mit 3 Slots
        Menu loadMenu = new Menu("Load");
        for (int slot = 1; slot <= 3; slot++) {
            final int s = slot;
            MenuItem loadSlot = new MenuItem("Slot " + slot);
            loadSlot.setOnAction(e -> {
                if (controller != null) {
                    controller.loadGame(s);
                }
            });
            loadMenu.getItems().add(loadSlot);
        }

        MenuItem newGameItem = new MenuItem("New Game");
        newGameItem.setOnAction(e -> restartCallback.onRestart());

        gameMenu.getItems().addAll(saveMenu, loadMenu, newGameItem);
        menuBar.getMenus().add(gameMenu);
        rootContainer.getChildren().add(menuBar);

        // Status-Label (klein, über dem Brett)
        statusLabel = new Label("Chess");
        statusLabel.setFont(Font.font(12));
        statusLabel.setPadding(new Insets(4, 10, 4, 10));
        rootContainer.getChildren().add(statusLabel);

        // Schachbrett
        boardGrid = new GridPane();
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.setAlignment(Pos.CENTER);

        cellPanes = new Pane[8][8];
        cellImages = new ImageView[8][8];
        moveDots = new Circle[8][8];

        for (int visRow = 0; visRow < 8; visRow++) {
            for (int visCol = 0; visCol < 8; visCol++) {
                final int boardRow = flipped ? (7 - visRow) : visRow;
                final int boardCol = flipped ? (7 - visCol) : visCol;

                // Hintergrund-Feld
                Pane cellPane = new Pane();
                cellPane.setPrefSize(60, 60);

                boolean isLight = (boardRow + boardCol) % 2 == 0;
                cellPane.setStyle(String.format(
                        "-fx-background-color: %s; -fx-border-color: #333; -fx-border-width: 1;",
                        isLight ? boardTheme.getLightColor() : boardTheme.getDarkColor()
                ));

                // ImageView für die Figur (initial leer)
                ImageView cellImage = new ImageView();
                cellImage.setPreserveRatio(true);
                cellImage.setFitHeight(50);
                cellImage.setFitWidth(50);
                cellImage.setVisible(false);

                // Figur anzeigen mit gewähltem Style
                Piece piece = board.getPiece(boardRow, boardCol);
                if (piece != null) {
                    setPieceImage(cellImage, piece);
                }

                // Punkt für gültige Züge (unsichtbar)
                Circle dot = new Circle(10);
                dot.setVisible(false);
                dot.setManaged(false);

                // StackPane für Klick-Handler und Punkt
                StackPane stack = new StackPane(cellPane, cellImage, dot);
                stack.setPrefSize(60, 60);
                stack.setAlignment(Pos.CENTER);

                // Linksklick-Handler
                stack.setOnMouseClicked(e -> {
                    if (controller != null) {
                        controller.onCellClicked(boardRow, boardCol);
                    }
                });

                // Rechtsklick-Handler — Auswahl abbrechen
                stack.setOnMousePressed(e -> {
                    if (e.getButton().name().equals("SECONDARY") && controller != null) {
                        controller.cancelSelection();
                    }
                });

                // Arrays werden mit Brett-Koordinaten indiziert
                cellPanes[boardRow][boardCol] = cellPane;
                cellImages[boardRow][boardCol] = cellImage;
                moveDots[boardRow][boardCol] = dot;

                // GridPane bekommt visuelle Position
                boardGrid.add(stack, visCol, visRow);
            }
        }

        // StackPane als Wrapper um das Brett — ermöglicht Overlays ohne Layout-Verschiebung
        boardStack = new StackPane(boardGrid);
        boardStack.setStyle("-fx-background-color: transparent;");
        rootContainer.getChildren().add(boardStack);

        // Szene
        Scene scene = new Scene(rootContainer, 490, 540);
        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Setzt den GameController (wird nach dem Konstruktor gesetzt).
     */
    void setController(GameController ctrl) {
        this.controller = ctrl;
    }

    // -----------------------------------------------------------------
    // SVG rendering
    // -----------------------------------------------------------------

    private void setPieceImage(ImageView imageView, Piece piece) {
        try {
            String path = piece.getType().getSvgPath(piece.getColor(), pieceStyle);
            URL resource = getClass().getClassLoader().getResource(path);
            if (resource != null) {
                SVGLoader loader = new SVGLoader();
                SVGDocument document = loader.load(resource);

                int renderSize = 50;
                BufferedImage bufferedImage = new BufferedImage(renderSize, renderSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_STROKE_CONTROL,
                    java.awt.RenderingHints.VALUE_STROKE_PURE
                );
                document.render(null, g2d, new ViewBox(0, 0, renderSize, renderSize));
                g2d.dispose();

                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                imageView.setImage(image);
                imageView.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setVisible(false);
        }
    }

    // -----------------------------------------------------------------
    // GameView interface implementation
    // -----------------------------------------------------------------

    @Override
    public void updateBoard(ChessBoard board) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                updateCell(row, col, board.getPiece(row, col));
            }
        }
    }

    @Override
    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    @Override
    public void highlightCell(int row, int col, boolean highlight) {
        Pane cellPane = cellPanes[row][col];
        boolean isLight = (row + col) % 2 == 0;
        String baseColor = isLight ? boardTheme.getLightColor() : boardTheme.getDarkColor();
        if (highlight) {
            cellPane.setStyle(String.format(
                    "-fx-background-color: #FFD700; -fx-border-color: #333; -fx-border-width: 1;"
            ));
        } else {
            cellPane.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: #333; -fx-border-width: 1;",
                    baseColor
            ));
        }
    }

    @Override
    public void highlightLastMove(int fromRow, int fromCol, int toRow, int toCol) {
        highlightLastMoveCell(fromRow, fromCol);
        highlightLastMoveCell(toRow, toCol);
    }

    private void highlightLastMoveCell(int row, int col) {
        Pane cellPane = cellPanes[row][col];
        cellPane.setStyle(String.format(
                "-fx-background-color: #A0D468; -fx-border-color: #333; -fx-border-width: 1;"
        ));
    }

    @Override
    public void clearLastMoveHighlight() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                clearLastMoveCell(row, col);
            }
        }
    }

    private void clearLastMoveCell(int row, int col) {
        Pane cellPane = cellPanes[row][col];
        boolean isLight = (row + col) % 2 == 0;
        String baseColor = isLight ? boardTheme.getLightColor() : boardTheme.getDarkColor();
        cellPane.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: #333; -fx-border-width: 1;",
                baseColor
        ));
    }

    @Override
    public void showValidMoveDot(int row, int col) {
        Circle dot = moveDots[row][col];
        dot.setFill(javafx.scene.paint.Color.web("rgba(0, 180, 0, 0.5)"));
        dot.setVisible(true);
        dot.setManaged(true);
    }

    @Override
    public void showValidCaptureDot(int row, int col) {
        Circle dot = moveDots[row][col];
        dot.setFill(javafx.scene.paint.Color.web("rgba(255, 0, 0, 0.3)"));
        dot.setRadius(28);
        dot.setVisible(true);
        dot.setManaged(true);
    }

    @Override
    public void clearDot(int row, int col) {
        Circle dot = moveDots[row][col];
        dot.setVisible(false);
        dot.setManaged(false);
        dot.setRadius(10);
    }

    @Override
    public void showTimeoutDialog(Runnable retryAction, Runnable restartAction) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("AI Timeout");
        alert.setHeaderText("The AI is taking too long to think.");
        alert.setContentText("What would you like to do?");

        ButtonType retryButton = new ButtonType("Wait again");
        ButtonType newGameButton = new ButtonType("New Game");
        alert.getButtonTypes().setAll(retryButton, newGameButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == retryButton) {
                retryAction.run();
            } else if (response == newGameButton) {
                restartAction.run();
            }
        });
    }

    @Override
    public void restartGame() {
        restartCallback.onRestart();
    }

    @Override
    public int showLoadDialog(int failedSlot) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Load failed");
        alert.setHeaderText("Slot " + failedSlot + " is empty or corrupted.");
        alert.setContentText("Would you like to try another slot?");

        // Verfuegbare Slots sammeln (alle ausser dem fehlgeschlagenen)
        java.util.List<ButtonType> buttons = new java.util.ArrayList<>();
        for (int slot = 1; slot <= 3; slot++) {
            if (slot != failedSlot) {
                buttons.add(new ButtonType("Slot " + slot));
            }
        }
        buttons.add(new ButtonType("Cancel"));
        alert.getButtonTypes().setAll(buttons);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            String text = result.get().getText();
            if (text != null && text.startsWith("Slot ")) {
                return Integer.parseInt(text.substring(5));
            }
        }
        return -1;  // Cancelled
    }

    @Override
    public void showPromotionDialog(Color color, java.util.function.IntConsumer onSelected) {
        // Overlay über dem Brett via boardStack — verschiebt das GridPane nicht
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setVisible(false);
        overlay.setManaged(false);

        // Overlay deckt den boardStack (Brett-Wrapper) ab
        overlay.prefWidthProperty().bind(boardStack.widthProperty());
        overlay.prefHeightProperty().bind(boardStack.heightProperty());

        // Die 4 Promotion-Optionen
        PieceType[] options = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};
        String[] names = {"Queen", "Rook", "Bishop", "Knight"};

        // GridPane 2x2 für die Kacheln
        GridPane tileGrid = new GridPane();
        tileGrid.setHgap(15);
        tileGrid.setVgap(15);
        tileGrid.setAlignment(Pos.CENTER);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            VBox tile = new VBox(5);
            tile.setAlignment(Pos.CENTER);
            tile.setPrefSize(100, 110);
            tile.setStyle("-fx-background-color: #333333; -fx-background-radius: 8; -fx-border-color: #666; -fx-border-radius: 8;");
            tile.setCursor(javafx.scene.Cursor.HAND);

            // ImageView für die Figur — verwendet setPieceImage() statt duplizierter SVG-Logik
            ImageView imageView = new ImageView();
            imageView.setFitHeight(60);
            imageView.setFitWidth(60);
            imageView.setPreserveRatio(true);

            // Temporaer die Groesse anpassen fuer die Promotion-Kacheln (60 statt 50)
            Piece tempPiece = new Piece(options[i], color);
            setPieceImage(imageView, tempPiece);
            imageView.setFitHeight(60);
            imageView.setFitWidth(60);

            // Label mit dem Namen
            Label label = new Label(names[i]);
            label.setFont(Font.font(13));
            label.setTextFill(javafx.scene.paint.Color.WHITE);

            tile.getChildren().addAll(imageView, label);

            // Hilfsfunktion zum Schliessen des Overlays
            javafx.event.EventHandler<javafx.event.Event> closeOverlay = event -> {
                overlay.setVisible(false);
                overlay.setManaged(false);
                boardStack.getChildren().remove(overlay);
            };

            // Klick auf Kachel wählt die Figur
            tile.setOnMouseClicked(e -> {
                closeOverlay.handle(null);
                onSelected.accept(index);
            });

            // Rechtsklick bricht ab
            tile.setOnMousePressed(e -> {
                if (e.getButton().name().equals("SECONDARY")) {
                    closeOverlay.handle(null);
                    onSelected.accept(-1);
                }
            });

            // Grid-Position: 2x2 Layout
            int gridRow = i / 2;
            int gridCol = i % 2;
            tileGrid.add(tile, gridCol, gridRow);
        }

        overlay.getChildren().add(tileGrid);

        // Rechtsklick auf den dunklen Hintergrund bricht auch ab
        overlay.setOnMousePressed(e -> {
            if (e.getButton().name().equals("SECONDARY")) {
                overlay.setVisible(false);
                overlay.setManaged(false);
                boardStack.getChildren().remove(overlay);
                onSelected.accept(-1);
            }
        });

        // ESC-Taste bricht ab
        overlay.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                overlay.setVisible(false);
                overlay.setManaged(false);
                boardStack.getChildren().remove(overlay);
                onSelected.accept(-1);
            }
        });
        overlay.setFocusTraversable(true);

        // Overlay über dem Brett anzeigen (im StackPane, nicht im GridPane)
        boardStack.getChildren().add(overlay);
        overlay.setVisible(true);
        overlay.setManaged(true);
        overlay.requestFocus();
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private void updateCell(int row, int col, Piece piece) {
        ImageView cellImage = cellImages[row][col];
        if (piece != null) {
            setPieceImage(cellImage, piece);
        } else {
            cellImage.setImage(null);
            cellImage.setVisible(false);
        }
    }
}
