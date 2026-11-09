package ch.zhaw.schach.view;

import ch.zhaw.schach.model.BoardTheme;
import ch.zhaw.schach.model.Color;
import ch.zhaw.schach.model.PieceStyle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.ViewBox;
import javafx.embed.swing.SwingFXUtils;

/**
 * Startbildschirm mit Auswahl für Schwierigkeit, Spielerfarbe,
 * Figuren-Style und Brett-Theme.
 */
class StartScreen {

    private final Stage stage;
    private final SettingsCallback callback;

    private int selectedSearchDepth = 3;
    private Color selectedPlayerColor = Color.WHITE;
    private PieceStyle selectedPieceStyle = PieceStyle.ALPHA;
    private BoardTheme selectedBoardTheme = BoardTheme.WOOD;

    // Referenzen auf Buttons zum Hervorheben
    private Button easyBtn, mediumBtn, hardBtn;
    private Button whiteBtn, blackBtn;
    private Button alphaBtn, leipzigBtn, horseyBtn;
    private Button woodBtn, monochromeBtn, oceanBtn;

    /**
     * Schnittstelle, um die gewählten Einstellungen an ChessView zurückzugeben.
     */
    interface SettingsCallback {
        void onSettingsSelected(int searchDepth, Color playerColor,
                                PieceStyle pieceStyle, BoardTheme boardTheme);
    }

    StartScreen(Stage stage, SettingsCallback callback) {
        this.stage = stage;
        this.callback = callback;
    }

    /**
     * Zeigt den Startbildschirm mit allen Auswahl-Optionen.
     */
    void show() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c2c2c;");

        // Titel
        Text title = new Text("Chess");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(javafx.scene.paint.Color.web("#ffffff"));

        // --- Schwierigkeit ---
        Label difficultySubtitle = new Label("Select difficulty:");
        difficultySubtitle.setFont(Font.font(14));
        difficultySubtitle.setTextFill(javafx.scene.paint.Color.web("#cccccc"));
        HBox difficultyBox = new HBox(10);
        difficultyBox.setAlignment(Pos.CENTER);
        difficultyBox.getChildren().addAll(
                createDifficultyButton("Easy", 1),
                createDifficultyButton("Medium", 3),
                createDifficultyButton("Hard", 4));
        root.getChildren().addAll(difficultySubtitle, difficultyBox);

        // --- Farbe ---
        Label colorSubtitle = new Label("Play as:");
        colorSubtitle.setFont(Font.font(14));
        colorSubtitle.setTextFill(javafx.scene.paint.Color.web("#cccccc"));
        HBox colorBox = new HBox(10);
        colorBox.setAlignment(Pos.CENTER);
        colorBox.getChildren().addAll(
                createColorButton("White", Color.WHITE),
                createColorButton("Black", Color.BLACK));
        root.getChildren().addAll(colorSubtitle, colorBox);

        // --- Figuren-Style ---
        Label styleSubtitle = new Label("Piece style:");
        styleSubtitle.setFont(Font.font(14));
        styleSubtitle.setTextFill(javafx.scene.paint.Color.web("#cccccc"));
        HBox styleBox = new HBox(10);
        styleBox.setAlignment(Pos.CENTER);
        styleBox.getChildren().addAll(
                createStyleButton("Alpha", PieceStyle.ALPHA),
                createStyleButton("Leipzig", PieceStyle.LEIPZIG),
                createStyleButton("Horsey", PieceStyle.HORSEY));
        root.getChildren().addAll(styleSubtitle, styleBox);

        // --- Brett-Theme ---
        Label themeSubtitle = new Label("Board theme:");
        themeSubtitle.setFont(Font.font(14));
        themeSubtitle.setTextFill(javafx.scene.paint.Color.web("#cccccc"));
        HBox themeBox = new HBox(10);
        themeBox.setAlignment(Pos.CENTER);
        themeBox.getChildren().addAll(
                createThemeButton("Wood", BoardTheme.WOOD),
                createThemeButton("Mono", BoardTheme.MONOCHROME),
                createThemeButton("Ocean", BoardTheme.OCEAN));
        root.getChildren().addAll(themeSubtitle, themeBox);

        // Abstand
        Region spacer = new Region();
        root.getChildren().add(spacer);
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Start-Button
        Button startBtn = new Button("New Game");
        startBtn.setFont(Font.font(14));
        startBtn.setPrefWidth(140);
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 8 16;");
        startBtn.setOnAction(e -> callback.onSettingsSelected(
                selectedSearchDepth, selectedPlayerColor,
                selectedPieceStyle, selectedBoardTheme));
        root.getChildren().add(startBtn);

        // Standard-Hervorhebungen setzen
        updateDifficultyButtons(mediumBtn);
        updateColorButtons(whiteBtn);
        updateStyleButtons(alphaBtn);
        updateThemeButtons(woodBtn);

        Scene scene = new Scene(root, 400, 580);
        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // -----------------------------------------------------------------
    // Schwierigkeits-Buttons
    // -----------------------------------------------------------------

    private Button createDifficultyButton(String text, int depth) {
        Button btn = new Button(text);
        btn.setFont(Font.font(13));
        btn.setPrefWidth(90);
        btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 6 12;");

        if (text.equals("Easy")) easyBtn = btn;
        else if (text.equals("Medium")) mediumBtn = btn;
        else hardBtn = btn;

        btn.setOnAction(e -> {
            selectedSearchDepth = depth;
            updateDifficultyButtons(btn);
        });

        return btn;
    }

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

    // -----------------------------------------------------------------
    // Farb-Buttons
    // -----------------------------------------------------------------

    private Button createColorButton(String text, Color color) {
        Button btn = new Button(text);
        btn.setFont(Font.font(13));
        btn.setPrefWidth(100);
        btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 6 12;");

        if (text.equals("White")) whiteBtn = btn;
        else blackBtn = btn;

        btn.setOnAction(e -> {
            selectedPlayerColor = color;
            updateColorButtons(btn);
        });

        return btn;
    }

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

    // -----------------------------------------------------------------
    // Style-Buttons
    // -----------------------------------------------------------------

    private Image loadPreviewImage(PieceStyle style) {
        try {
            String path = "pieces/" + style.getFolderName() + "/wk.svg";
            URL resource = getClass().getClassLoader().getResource(path);
            if (resource != null) {
                SVGLoader loader = new SVGLoader();
                SVGDocument document = loader.load(resource);
                int previewSize = 30;
                BufferedImage bufferedImage = new BufferedImage(previewSize, previewSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_STROKE_CONTROL,
                    java.awt.RenderingHints.VALUE_STROKE_PURE
                );
                document.render(null, g2d, new ViewBox(0, 0, previewSize, previewSize));
                g2d.dispose();
                return SwingFXUtils.toFXImage(bufferedImage, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Button createStyleButton(String text, PieceStyle style) {
        Image preview = loadPreviewImage(style);
        ImageView previewIcon = new ImageView(preview);
        previewIcon.setFitHeight(24);
        previewIcon.setFitWidth(24);
        previewIcon.setPreserveRatio(true);

        HBox content = new HBox(6);
        content.setAlignment(Pos.CENTER);
        content.getChildren().add(previewIcon);
        Label textLabel = new Label(text);
        textLabel.setFont(Font.font(12));
        textLabel.setTextFill(javafx.scene.paint.Color.web("#ffffff"));
        content.getChildren().add(textLabel);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btn.setPrefWidth(100);
        btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 6 12;");

        if (text.equals("Alpha")) alphaBtn = btn;
        else if (text.equals("Leipzig")) leipzigBtn = btn;
        else horseyBtn = btn;

        btn.setOnAction(e -> {
            selectedPieceStyle = style;
            updateStyleButtons(btn);
        });

        return btn;
    }

    private void updateStyleButtons(Button selected) {
        Button[] buttons = {alphaBtn, leipzigBtn, horseyBtn};
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

    // -----------------------------------------------------------------
    // Theme-Buttons
    // -----------------------------------------------------------------

    private Button createThemeButton(String text, BoardTheme theme) {
        GridPane preview = new GridPane();
        preview.setHgap(0);
        preview.setVgap(0);
        int size = 2;
        int cellSize = 18;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Pane cell = new Pane();
                cell.setPrefSize(cellSize, cellSize);
                boolean isLight = (r + c) % 2 == 0;
                cell.setStyle("-fx-background-color: "
                        + (isLight ? theme.getLightColor() : theme.getDarkColor()) + ";");
                preview.add(cell, c, r);
            }
        }

        Label nameLabel = new Label(text);
        nameLabel.setTextFill(javafx.scene.paint.Color.web("#FFFFFF"));
        nameLabel.setFont(Font.font(12));

        VBox content = new VBox(4, preview, nameLabel);
        content.setAlignment(Pos.CENTER);

        Button btn = new Button();
        btn.setGraphic(content);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btn.setPrefWidth(90);
        btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                + "-fx-background-radius: 5; -fx-padding: 8 12;");

        if (text.equals("Wood")) woodBtn = btn;
        else if (text.equals("Mono")) monochromeBtn = btn;
        else oceanBtn = btn;

        btn.setOnAction(e -> {
            selectedBoardTheme = theme;
            updateThemeButtons(btn);
        });

        return btn;
    }

    private void updateThemeButtons(Button selected) {
        Button[] buttons = {woodBtn, monochromeBtn, oceanBtn};
        for (Button btn : buttons) {
            if (btn == selected) {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; "
                        + "-fx-background-radius: 5; -fx-padding: 8 12;");
            } else {
                btn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; "
                        + "-fx-background-radius: 5; -fx-padding: 8 12;");
            }
        }
    }
}
