package ch.zhaw.schach.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests fuer die PieceType-Enum.
 * Prueft, dass SVG-Pfade und Namen korrekt berechnet werden.
 */
class PieceTypeTest {

    @Test
    void getSvgPath_returnsCorrectPath() {
        // Weißer König im Alpha-Stil
        String path = PieceType.KING.getSvgPath(Color.WHITE, PieceStyle.ALPHA);
        assertEquals("pieces/alpha/wK.svg", path);

        // Schwarzer Bauer im Leipzig-Stil
        path = PieceType.PAWN.getSvgPath(Color.BLACK, PieceStyle.LEIPZIG);
        assertEquals("pieces/leipzig/bP.svg", path);

        // Weißer Springer im Horsey-Stil
        path = PieceType.KNIGHT.getSvgPath(Color.WHITE, PieceStyle.HORSEY);
        assertEquals("pieces/horsey/wN.svg", path);

        // Schwarze Dame im Alpha-Stil
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
