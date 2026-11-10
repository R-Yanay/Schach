package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer das PieceType Enum.
 * Bestaetigt, dass SVG-Pfade und Namen korrekt berechnet werden.
 */
class PieceTypeTest {

    @Test
    void getSvgPath_returnsCorrectPath() {
        // Weiss King in Alpha Style
        String path = PieceType.KING.getSvgPath(Color.WHITE, PieceStyle.ALPHA);
        assertEquals("pieces/alpha/wK.svg", path);

        // Schwarz Pawn in Leipzig Style
        path = PieceType.PAWN.getSvgPath(Color.BLACK, PieceStyle.LEIPZIG);
        assertEquals("pieces/leipzig/bP.svg", path);

        // Weiss Knight in Horsey Style
        path = PieceType.KNIGHT.getSvgPath(Color.WHITE, PieceStyle.HORSEY);
        assertEquals("pieces/horsey/wN.svg", path);

        // Schwarz Queen in Alpha Style
        path = PieceType.QUEEN.getSvgPath(Color.BLACK, PieceStyle.ALPHA);
        assertEquals("pieces/alpha/bQ.svg", path);
    }

    @Test
    void getName_returnsCorrectName() {
        assertEquals("Pawn", PieceType.PAWN.getName());
        assertEquals("Rook", PieceType.ROOK.getName());
        assertEquals("Knight", PieceType.KNIGHT.getName());
        assertEquals("Bishop", PieceType.BISHOP.getName());
        assertEquals("Queen", PieceType.QUEEN.getName());
        assertEquals("King", PieceType.KING.getName());
    }
}
