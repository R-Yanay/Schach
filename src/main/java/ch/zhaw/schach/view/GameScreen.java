package ch.zhaw.schach.view;

import ch.zhaw.schach.controller.GameController;
import ch.zhaw.schach.model.BoardTheme;
import ch.zhaw.schach.model.ChessBoard;
import ch.zhaw.schach.model.Color;
import ch.zhaw.schach.model.Piece;
import ch.zhaw.schach.model.PieceStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private GridPane boardGrid;
    private Label statusLabel;
    private StackPane[][] cellStacks;
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
        VBox root = new VBox(0);
        root.setPadding(new Insets(0));
        root.setAlignment(Pos.CENTER);

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
        root.getChildren().add(menuBar);

        // Status-Label (klein, über dem Brett)
        statusLabel = new Label("Chess");
        statusLabel.setFont(Font.font(12));
        statusLabel.setPadding(new Insets(4, 10, 4, 10));
        root.getChildren().add(statusLabel);

        // Schachbrett
        boardGrid = new GridPane();
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.setAlignment(Pos.CENTER);

        cellPanes = new Pane[8][8];
        cellImages = new ImageView[8][8];
        cellStacks = new StackPane[8][8];
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

                // Klick-Handler
                stack.setOnMouseClicked(e -> {
                    if (controller != null) {
                        controller.onCellClicked(boardRow, boardCol);
                    }
                });

                // Arrays werden mit Brett-Koordinaten indiziert
                cellPanes[boardRow][boardCol] = cellPane;
                cellImages[boardRow][boardCol] = cellImage;
                cellStacks[boardRow][boardCol] = stack;
                moveDots[boardRow][boardCol] = dot;

                // GridPane bekommt visuelle Position
                boardGrid.add(stack, visCol, visRow);
            }
        }

        root.getChildren().add(boardGrid);

        // Szene
        Scene scene = new Scene(root, 490, 540);
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
