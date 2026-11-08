package ch.zhaw.schach.model;

/**
 * Das Schachbrett als 8x8 2D-Array.
 * Zeile 0 = Reihe 8 (schwarz), Zeile 7 = Reihe 1 (weiss).
 * Spalte 0 = a, Spalte 7 = h.
 */
public class ChessBoard {

    private final Piece[][] board;

    public ChessBoard() {
        board = new Piece[8][8];
        initializeStartPosition();
    }

    /**
     * Initialisiert die Startposition aller Figuren.
     */
    private void initializeStartPosition() {
        // Black pieces (row 8 = index 0)
        board[0][0] = new Piece(PieceType.ROOK, Color.BLACK);
        board[0][1] = new Piece(PieceType.KNIGHT, Color.BLACK);
        board[0][2] = new Piece(PieceType.BISHOP, Color.BLACK);
        board[0][3] = new Piece(PieceType.QUEEN, Color.BLACK);
        board[0][4] = new Piece(PieceType.KING, Color.BLACK);
        board[0][5] = new Piece(PieceType.BISHOP, Color.BLACK);
        board[0][6] = new Piece(PieceType.KNIGHT, Color.BLACK);
        board[0][7] = new Piece(PieceType.ROOK, Color.BLACK);

        // Black pawns (row 7 = index 1)
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Piece(PieceType.PAWN, Color.BLACK);
        }

        // White pawns (row 2 = index 6)
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Piece(PieceType.PAWN, Color.WHITE);
        }

        // White pieces (row 1 = index 7)
        board[7][0] = new Piece(PieceType.ROOK, Color.WHITE);
        board[7][1] = new Piece(PieceType.KNIGHT, Color.WHITE);
        board[7][2] = new Piece(PieceType.BISHOP, Color.WHITE);
        board[7][3] = new Piece(PieceType.QUEEN, Color.WHITE);
        board[7][4] = new Piece(PieceType.KING, Color.WHITE);
        board[7][5] = new Piece(PieceType.BISHOP, Color.WHITE);
        board[7][6] = new Piece(PieceType.KNIGHT, Color.WHITE);
        board[7][7] = new Piece(PieceType.ROOK, Color.WHITE);
    }

    /**
     * Gibt die Figur auf dem angegebenen Feld zurück, oder null wenn leer.
     */
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    /**
     * Setzt eine Figur auf das angegebene Feld.
     * Null entfernt die Figur vom Feld.
     */
    public void setPiece(int row, int col, Piece piece) {
        board[row][col] = piece;
    }

    /**
     * Gibt das gesamte Brett-Array zurück (für KI-Kopie).
     */
    public Piece[][] getBoard() {
        return board;
    }

    /**
     * Erstellt eine tiefe Kopie des Bretts.
     */
    public ChessBoard copy() {
        ChessBoard copy = new ChessBoard();
        Piece[][] copyBoard = copy.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                copyBoard[row][col] = board[row][col];
            }
        }
        return copy;
    }
}
