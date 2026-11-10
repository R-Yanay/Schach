package ch.zhaw.schach.model;

/**
 * Farbthemen für das Schachbrett.
 */
public enum BoardTheme {

    /** Klassisches Holzbrett (Standard) */
    WOOD("#F0D9B5", "#B58863"),

    /** Schwarz-Weiss Brett */
    MONOCHROME("#FFFFFF", "#777777"),

    /** Blau-Türkis Brett */
    OCEAN("#DEE3E9", "#8CA2AD");

    private final String lightColor;
    private final String darkColor;

    BoardTheme(String lightColor, String darkColor) {
        this.lightColor = lightColor;
        this.darkColor = darkColor;
    }

    public String getLightColor() {
        return lightColor;
    }

    public String getDarkColor() {
        return darkColor;
    }
}
