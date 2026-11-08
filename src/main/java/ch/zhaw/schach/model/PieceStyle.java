package ch.zhaw.schach.model;

/**
 * Verschiedene visuelle Stile für Schachfiguren.
 * Alpha = Altmodisches klassisches Design
 * Leipzig = Modernes sauberes Design
 * Horsey = Kreatives verspieltes Design
 */
public enum PieceStyle {
    ALPHA("Alpha"),
    LEIPZIG("Leipzig"),
    HORSEY("Horsey");

    private final String displayName;

    PieceStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gibt den Ordnernamen für die SVG-Dateien dieses Stils zurück.
     */
    public String getFolderName() {
        return this.name().toLowerCase();
    }
}
