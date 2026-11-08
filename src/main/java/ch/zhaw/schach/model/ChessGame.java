package ch.zhaw.schach.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Spiel-Logik: Züge ausführen, Spieler wechseln, Schach/Schachmatt/Patt erkennen.
 * Unterstützt Rochade, En-Passant und Figurenumwandlung.
 */
public class ChessGame {

    private final ChessBoard board;
    private final MoveValidator moveValidator;
    private Color currentPlayer = Color.WHITE;
    private GameStatus status = GameStatus.IN_PROGRESS;

    // Rochade-Tracking
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingRookMoved = false;  // Turm auf h1
    private boolean whiteQueenRookMoved = false; // Turm auf a1
    private boolean blackKingRookMoved = false;  // Turm auf h8
    private boolean blackQueenRookMoved = false; // Turm auf a8

    // En-Passant: letzte Bauer-Position, wenn 2 Felder gezogen
    private int enPassantRow = -1;
    private int enPassantCol = -1;

    // Letzter Zug (für Hervorhebung in der GUI)
    private int lastMoveFromRow = -1;
    private int lastMoveFromCol = -1;
    private int lastMoveToRow = -1;
    private int lastMoveToCol = -1;

    public ChessGame() {
        this.board = new ChessBoard();
        this.moveValidator = new MoveValidator();
    }

    /**
     * Führt einen Zug aus, wenn er gültig ist.
     * Gibt true zurück, wenn der Zug ausgeführt wurde.
     */
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) return false;

        // Check en passant
        if (piece.getType() == PieceType.PAWN &&
            fromRow == enPassantRow && toRow == enPassantRow + ((piece.getColor() == Color.WHITE) ? -1 : 1) &&
            toCol == enPassantCol &&
            board.getPiece(toRow, toCol) == null) {
            // En-Passant Capture
            if (!wouldBeInCheck(fromRow, fromCol, toRow, toCol)) {
                board.setPiece(fromRow, fromCol, null);
                // Gefangenen Bauer entfernen
                board.setPiece(enPassantRow, toCol, null);
                board.setPiece(toRow, toCol, piece);
                enPassantRow = -1;
                enPassantCol = -1;
                afterMove(piece, fromRow, fromCol, toRow, toCol);
                return true;
            }
            return false;
        }

        // Check castling
        if (piece.getType() == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            if (isValidCastling(fromRow, fromCol, toCol, piece.getColor())) {
                executeCastling(fromRow, fromCol, toCol, piece.getColor());
                afterMove(piece, fromRow, fromCol, toRow, toCol);
                return true;
            }
            return false;
        }

        // Normaler Zug – gültig?
        if (!moveValidator.isValidMove(board, fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        // Prüfen, ob der Zug den eigenen König ins Schach bringt
        if (wouldBeInCheck(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        // En-Passant-Variable zurücksetzen (wenn kein Bauer 2 Felder zieht)
        enPassantRow = -1;
        enPassantCol = -1;

        // Pawn moved 2 squares? Remember for en passant
        if (piece.getType() == PieceType.PAWN && Math.abs(toRow - fromRow) == 2) {
            enPassantRow = (fromRow + toRow) / 2;
            enPassantCol = fromCol;
        }

        // Figur bewegen
        board.setPiece(fromRow, fromCol, null);

        // Pawn promotion (auto to Queen)
        if (piece.getType() == PieceType.PAWN) {
            if ((piece.getColor() == Color.WHITE && toRow == 0) ||
                (piece.getColor() == Color.BLACK && toRow == 7)) {
                board.setPiece(toRow, toCol, new Piece(PieceType.QUEEN, piece.getColor()));
            } else {
                board.setPiece(toRow, toCol, piece);
            }
        } else {
            board.setPiece(toRow, toCol, piece);
        }

        afterMove(piece, fromRow, fromCol, toRow, toCol);
        return true;
    }

    /**
     * Nach jedem Zug: Rochade-Flags aktualisieren und Status prüfen.
     */
    private void afterMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        // Letzten Zug speichern
        lastMoveFromRow = fromRow;
        lastMoveFromCol = fromCol;
        lastMoveToRow = toRow;
        lastMoveToCol = toCol;
        // Rochade-Flags aktualisieren
        if (piece.getType() == PieceType.KING) {
            if (piece.getColor() == Color.WHITE) whiteKingMoved = true;
            else blackKingMoved = true;
        }
        if (piece.getType() == PieceType.ROOK) {
            if (fromRow == 7 && fromCol == 0) whiteQueenRookMoved = true;
            if (fromRow == 7 && fromCol == 7) whiteKingRookMoved = true;
            if (fromRow == 0 && fromCol == 0) blackQueenRookMoved = true;
            if (fromRow == 0 && fromCol == 7) blackKingRookMoved = true;
        }

        // Spieler wechseln
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;

        // Spielstatus aktualisieren
        updateStatus();
    }

    /**
     * Prüft, ob Rochade gültig ist.
     */
    private boolean isValidCastling(int fromRow, int fromCol, int toCol, Color color) {
        // König darf nicht gezogen haben
        if (color == Color.WHITE && whiteKingMoved) return false;
        if (color == Color.BLACK && blackKingMoved) return false;

        // König darf nicht im Schach stehen
        if (isKingInCheck(color)) return false;

        if (toCol > fromCol) {
            // Kurze Rochade (Königsseite)
            if (color == Color.WHITE && whiteKingRookMoved) return false;
            if (color == Color.BLACK && blackKingRookMoved) return false;
            // Felder d1/e1 oder d8/e8 müssen frei sein
            if (board.getPiece(fromRow, fromCol + 1) != null || board.getPiece(fromRow, fromCol + 2) != null) return false;
            // König darf nicht durch Schach ziehen
            if (isSquareAttacked(board, fromRow, fromCol + 1, color)) return false;
        } else {
            // Lange Rochade (Damenseite)
            if (color == Color.WHITE && whiteQueenRookMoved) return false;
            if (color == Color.BLACK && blackQueenRookMoved) return false;
            // Felder b1/c1/d1 oder b8/c8/d8 müssen frei sein
            if (board.getPiece(fromRow, fromCol - 1) != null ||
                board.getPiece(fromRow, fromCol - 2) != null ||
                board.getPiece(fromRow, fromCol + 1) != null) return false;
            // König darf nicht durch Schach ziehen
            if (isSquareAttacked(board, fromRow, fromCol - 1, color)) return false;
        }
        return true;
    }

    /**
     * Führt Rochade aus.
     */
    private void executeCastling(int fromRow, int fromCol, int toCol, Color color) {
        // König bewegen
        board.setPiece(fromRow, fromCol, null);
        Piece king = new Piece(PieceType.KING, color);

        if (toCol > fromCol) {
            // Kurze Rochade
            board.setPiece(fromRow, toCol, king);
            // Turm von h nach f
            Piece rook = board.getPiece(fromRow, 7);
            board.setPiece(fromRow, 7, null);
            board.setPiece(fromRow, 5, rook);
        } else {
            // Lange Rochade
            board.setPiece(fromRow, toCol, king);
            // Turm von a nach d
            Piece rook = board.getPiece(fromRow, 0);
            board.setPiece(fromRow, 0, null);
            board.setPiece(fromRow, 3, rook);
        }
    }

    /**
     * Prüft, ob der König der angegebenen Farbe im Schach steht.
     */
    private boolean isKingInCheck(Color color) {
        int kingRow = -1, kingCol = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        return isSquareAttacked(board, kingRow, kingCol, color);
    }

    /**
     * Prüft, ob ein Zug den eigenen König ins Schach bringen würde.
     */
    private boolean wouldBeInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        // Zug simulieren
        ChessBoard tempBoard = board.copy();
        Piece piece = tempBoard.getPiece(fromRow, fromCol);
        tempBoard.setPiece(fromRow, fromCol, null);
        tempBoard.setPiece(toRow, toCol, piece);

        // König finden
        int kingRow = -1, kingCol = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = tempBoard.getPiece(r, c);
                if (p != null && p.getType() == PieceType.KING && p.getColor() == piece.getColor()) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) break;
        }

        // Prüfen, ob der König bedroht wird
        return isSquareAttacked(tempBoard, kingRow, kingCol, piece.getColor());
    }

    /**
     * Prüft, ob ein Feld von der gegnerischen Farbe angegriffen wird.
     */
    public boolean isSquareAttacked(ChessBoard board, int row, int col, Color friendlyColor) {
        Color enemyColor = (friendlyColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece != null && piece.getColor() == enemyColor) {
                    if (moveValidator.isValidMove(board, r, c, row, col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Aktualisiert den Spielstatus (Schach, Schachmatt, Patt).
     */
    private void updateStatus() {
        // König finden
        int kingRow = -1, kingCol = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.getType() == PieceType.KING && p.getColor() == currentPlayer) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) break;
        }

        // Wenn kein König gefunden (z.B. manipuliertes Brett in Tests), Status nicht ändern
        if (kingRow == -1) return;

        boolean inCheck = isSquareAttacked(board, kingRow, kingCol, currentPlayer);

        // Prüfen, ob der aktuelle Spieler überhaupt Züge hat
        boolean hasValidMoves = hasAnyValidMoves(board, currentPlayer);

        if (inCheck && !hasValidMoves) {
            status = GameStatus.CHECKMATE;
        } else if (!inCheck && !hasValidMoves) {
            status = GameStatus.STALEMATE;
        } else if (inCheck) {
            status = GameStatus.CHECK;
        } else {
            status = GameStatus.IN_PROGRESS;
        }
    }

    /**
     * Prüft, ob eine Farbe noch gültige Züge hat.
     * Enthält Rochade und En-Passant.
     */
    private boolean hasAnyValidMoves(ChessBoard board, Color color) {
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && piece.getColor() == color) {
                    // Normale Züge
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (moveValidator.isValidMove(board, fromRow, fromCol, toRow, toCol)) {
                                // Prüfen, ob der Zug den König ins Schach bringt
                                ChessBoard tempBoard = board.copy();
                                tempBoard.setPiece(fromRow, fromCol, null);
                                tempBoard.setPiece(toRow, toCol, piece);

                                // König finden
                                int kingRow = -1, kingCol = -1;
                                for (int r = 0; r < 8; r++) {
                                    for (int c = 0; c < 8; c++) {
                                        Piece p = tempBoard.getPiece(r, c);
                                        if (p != null && p.getType() == PieceType.KING && p.getColor() == color) {
                                            kingRow = r;
                                            kingCol = c;
                                            break;
                                        }
                                    }
                                    if (kingRow != -1) break;
                                }

                                if (!isSquareAttacked(tempBoard, kingRow, kingCol, color)) {
                                    return true;
                                }
                            }
                        }
                    }

                    // Rochade prüfen (nur für König)
                    if (piece.getType() == PieceType.KING) {
                        // Kurze Rochade
                        if (fromCol + 2 < 8 && isValidCastling(fromRow, fromCol, fromCol + 2, color)) {
                            return true;
                        }
                        // Lange Rochade
                        if (fromCol - 2 >= 0 && isValidCastling(fromRow, fromCol, fromCol - 2, color)) {
                            return true;
                        }
                    }

                    // En-Passant prüfen (nur für Bauer)
                    if (piece.getType() == PieceType.PAWN && enPassantCol != -1) {
                        int direction = (color == Color.WHITE) ? -1 : 1;
                        int epToRow = fromRow + direction;
                        int epToCol = enPassantCol;
                        if (epToRow == enPassantRow + direction && Math.abs(epToCol - fromCol) == 1) {
                            if (!wouldBeInCheckEnPassant(fromRow, fromCol, epToRow, epToCol)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gibt alle gültigen Züge für eine Figur zurück.
     * Enthält Rochade, En-Passant, und filtert Züge die den eigenen König ins Schach bringen.
     */
    public List<Move> getValidMoves(int row, int col) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(row, col);
        if (piece == null) return moves;

        // Normaler Zug – MoveValidator
        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                if (moveValidator.isValidMove(board, row, col, toRow, toCol)) {
                    // Prüfen, ob der Zug den eigenen König ins Schach bringt
                    if (!wouldBeInCheck(row, col, toRow, toCol)) {
                        // Bauer-Umwandlung?
                        if (piece.getType() == PieceType.PAWN) {
                            if ((piece.getColor() == Color.WHITE && toRow == 0) ||
                                (piece.getColor() == Color.BLACK && toRow == 7)) {
                                moves.add(new Move(row, col, toRow, toCol, PieceType.QUEEN));
                            } else {
                                moves.add(new Move(row, col, toRow, toCol));
                            }
                        } else {
                            moves.add(new Move(row, col, toRow, toCol));
                        }
                    }
                }
            }
        }

        // Rochade hinzufügen (nur für König)
        if (piece.getType() == PieceType.KING) {
            // Kurze Rochade (König nach g1/g8 = 2 Felder nach rechts)
            if (isValidCastling(row, col, col + 2, piece.getColor())) {
                moves.add(new Move(row, col, row, col + 2));
            }
            // Lange Rochade (König nach c1/c8 = 2 Felder nach links)
            if (isValidCastling(row, col, col - 2, piece.getColor())) {
                moves.add(new Move(row, col, row, col - 2));
            }
        }

        // En-Passant hinzufügen (nur für Bauer)
        if (piece.getType() == PieceType.PAWN && enPassantCol != -1) {
            int direction = (piece.getColor() == Color.WHITE) ? -1 : 1;
            int epToRow = row + direction;
            int epToCol = enPassantCol;
            // En-Passant ist nur möglich, wenn der Gegner gerade 2 Felder gezogen hat
            // und der Bauer diagonal neben dem gezogenen Bauer steht
            if (epToRow == enPassantRow + direction && Math.abs(epToCol - col) == 1) {
                // Prüfen, ob En-Passant den eigenen König ins Schach bringt
                if (!wouldBeInCheckEnPassant(row, col, epToRow, epToCol)) {
                    moves.add(new Move(row, col, epToRow, epToCol));
                }
            }
        }

        return moves;
    }

    /**
     * Prüft, ob ein En-Passant-Zug den eigenen König ins Schach bringen würde.
     */
    private boolean wouldBeInCheckEnPassant(int fromRow, int fromCol, int toRow, int toCol) {
        ChessBoard tempBoard = board.copy();
        Piece piece = tempBoard.getPiece(fromRow, fromCol);
        tempBoard.setPiece(fromRow, fromCol, null);
        tempBoard.setPiece(toRow, toCol, piece);
        // Gefangenen Bauer entfernen
        tempBoard.setPiece(enPassantRow, toCol, null);

        // König finden
        int kingRow = -1, kingCol = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = tempBoard.getPiece(r, c);
                if (p != null && p.getType() == PieceType.KING && p.getColor() == piece.getColor()) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        return isSquareAttacked(tempBoard, kingRow, kingCol, piece.getColor());
    }

    /**
     * Erstellt eine tiefe Kopie des Spiels (für KI-Simulation).
     */
    public ChessGame copy() {
        ChessGame copy = new ChessGame();
        // Brett kopieren
        Piece[][] src = this.board.getBoard();
        Piece[][] dst = copy.board.getBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                dst[r][c] = src[r][c];
            }
        }
        copy.currentPlayer = this.currentPlayer;
        copy.status = this.status;
        copy.whiteKingMoved = this.whiteKingMoved;
        copy.blackKingMoved = this.blackKingMoved;
        copy.whiteKingRookMoved = this.whiteKingRookMoved;
        copy.whiteQueenRookMoved = this.whiteQueenRookMoved;
        copy.blackKingRookMoved = this.blackKingRookMoved;
        copy.blackQueenRookMoved = this.blackQueenRookMoved;
        copy.enPassantRow = this.enPassantRow;
        copy.enPassantCol = this.enPassantCol;
        return copy;
    }

    public ChessBoard getBoard() { return board; }
    public Color getCurrentPlayer() { return currentPlayer; }
    public GameStatus getStatus() { return status; }
    public MoveValidator getMoveValidator() { return moveValidator; }

    public int getLastMoveFromRow() { return lastMoveFromRow; }
    public int getLastMoveFromCol() { return lastMoveFromCol; }
    public int getLastMoveToRow() { return lastMoveToRow; }
    public int getLastMoveToCol() { return lastMoveToCol; }

    public boolean hasLastMove() {
        return lastMoveFromRow != -1;
    }
}
