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

    // Dreimalige Stellungswiederholung: speichert alle Brett-Hashes
    private final List<String> positionHistory = new ArrayList<>();

    // 50-Zuege-Regel: Zaehler fuer Zuege ohne Bauerzug oder Schlagen
    private int fiftyMoveCounter = 0;

    public ChessGame() {
        this.board = new ChessBoard();
        this.moveValidator = new MoveValidator();
        // Ersten Hash (Anfangsstellung) speichern
        positionHistory.add(getBoardHash());
    }

    /**
     * Führt einen Zug aus, wenn er gültig ist.
     * Gibt true zurück, wenn der Zug ausgeführt wurde.
     */
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) return false;

        // En-Passant prüfen
        if (piece.getType() == PieceType.PAWN &&
            fromRow == enPassantRow && toRow == enPassantRow + ((piece.getColor() == Color.WHITE) ? -1 : 1) &&
            toCol == enPassantCol &&
            board.getPiece(toRow, toCol) == null) {
            // En-Passant-Schlag
            if (!wouldBeInCheck(fromRow, fromCol, toRow, toCol)) {
                board.setPiece(fromRow, fromCol, null);
                // Gefangenen Bauer entfernen
                board.setPiece(enPassantRow, toCol, null);
                board.setPiece(toRow, toCol, piece);
                enPassantRow = -1;
                enPassantCol = -1;
                afterMove(piece, fromRow, fromCol, toRow, toCol, true);
                return true;
            }
            return false;
        }

        // Rochade prüfen
        if (piece.getType() == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            if (isValidCastling(fromRow, fromCol, toCol, piece.getColor())) {
                executeCastling(fromRow, fromCol, toCol, piece.getColor());
                afterMove(piece, fromRow, fromCol, toRow, toCol, false);
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

        // Bauer 2 Felder gezogen? En-Passant merken
        if (piece.getType() == PieceType.PAWN && Math.abs(toRow - fromRow) == 2) {
            enPassantRow = (fromRow + toRow) / 2;
            enPassantCol = fromCol;
        }

        // Figur bewegen
        Piece capturedPiece = board.getPiece(toRow, toCol);
        board.setPiece(fromRow, fromCol, null);

        // Bauer-Promotion (automatisch zur Dame)
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

        afterMove(piece, fromRow, fromCol, toRow, toCol, capturedPiece != null);
        return true;
    }

    /**
     * Nach jedem Zug: Rochade-Flags aktualisieren und Status prüfen.
     * @param isCapture true wenn eine Figur geschlagen wurde (inklusive En-Passant)
     */
    private void afterMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture) {
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

        // Stellungswiederholung: aktuellen Hash speichern
        positionHistory.add(getBoardHash());

        // 50-Zuege-Regel: Zaehler erhoehen oder zuruecksetzen
        boolean isPawnMove = (piece.getType() == PieceType.PAWN);
        if (isCapture || isPawnMove) {
            fiftyMoveCounter = 0;
        } else {
            fiftyMoveCounter++;
        }

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

        // Wenn kein König gefunden, Zug als ungültig behandeln
        if (kingRow == -1) return true;

        // Prüfen, ob der König bedroht wird
        return isSquareAttacked(tempBoard, kingRow, kingCol, piece.getColor());
    }

    /**
     * Prüft, ob ein Feld von der gegnerischen Farbe angegriffen wird.
     */
    public boolean isSquareAttacked(ChessBoard board, int row, int col, Color friendlyColor) {
        // defensively: ungültige Koordinaten abfangen
        if (row < 0 || row >= 8 || col < 0 || col >= 8) return false;

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
     * Aktualisiert den Spielstatus (Schach, Schachmatt, Patt, unzureichendes Material).
     * Wird nach jedem Zug aufgerufen; kann auch manuell für Tests genutzt werden.
     */
    public void updateStatus() {
        // Zuerst unzureichendes Material prüfen
        if (isInsufficientMaterial()) {
            status = GameStatus.INSUFFICIENT_MATERIAL;
            return;
        }

        // Dreimalige Stellungswiederholung prüfen
        if (isThreefoldRepetition()) {
            status = GameStatus.DRAW_THREEFOLD_REPETITION;
            return;
        }

        // 50-Züge-Regel prüfen (100 Halbzüge = 50 Züge beider Spieler)
        if (fiftyMoveCounter >= 100) {
            status = GameStatus.DRAW_FIFTY_MOVES;
            return;
        }

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
     * Prüft, ob auf dem Brett unzureichendes Material für ein Matt vorliegt.
     * Remis-Fälle: K vs K, K+N vs K, K+B vs K, K+B vs K+B, K+N vs K+N.
     */
    private boolean isInsufficientMaterial() {
        // Zähle alle Figuren pro Farbe (ohne Könige)
        int[] whitePieces = new int[6];  // PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
        int[] blackPieces = new int[6];

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null) {
                    int idx = p.getType().ordinal();
                    if (p.getColor() == Color.WHITE) {
                        whitePieces[idx]++;
                    } else {
                        blackPieces[idx]++;
                    }
                }
            }
        }

        // K vs K (nur Könige auf dem Brett)
        if (whitePieces[5] == 1 && blackPieces[5] == 1 &&
            whitePieces[0] == 0 && whitePieces[1] == 0 && whitePieces[2] == 0 &&
            whitePieces[3] == 0 && whitePieces[4] == 0 &&
            blackPieces[0] == 0 && blackPieces[1] == 0 && blackPieces[2] == 0 &&
            blackPieces[3] == 0 && blackPieces[4] == 0) {
            return true;
        }

        // K+B vs K (ein Läufer gegen König)
        if (whitePieces[5] == 1 && blackPieces[5] == 1 &&
            whitePieces[3] == 1 && whitePieces[0] == 0 && whitePieces[1] == 0 &&
            whitePieces[2] == 0 && whitePieces[4] == 0 &&
            blackPieces[0] == 0 && blackPieces[1] == 0 && blackPieces[2] == 0 &&
            blackPieces[3] == 0 && blackPieces[4] == 0) {
            return true;
        }

        // K vs K+B (König gegen einen Läufer)
        if (whitePieces[5] == 1 && blackPieces[5] == 1 &&
            blackPieces[3] == 1 && blackPieces[0] == 0 && blackPieces[1] == 0 &&
            blackPieces[2] == 0 && blackPieces[4] == 0 &&
            whitePieces[0] == 0 && whitePieces[1] == 0 && whitePieces[2] == 0 &&
            whitePieces[3] == 0 && whitePieces[4] == 0) {
            return true;
        }

        // K+N vs K (ein Springer gegen König)
        if (whitePieces[5] == 1 && blackPieces[5] == 1 &&
            whitePieces[2] == 1 && whitePieces[0] == 0 && whitePieces[1] == 0 &&
            whitePieces[3] == 0 && whitePieces[4] == 0 &&
            blackPieces[0] == 0 && blackPieces[1] == 0 && blackPieces[2] == 0 &&
            blackPieces[3] == 0 && blackPieces[4] == 0) {
            return true;
        }

        // K vs K+N (König gegen einen Springer)
        if (whitePieces[5] == 1 && blackPieces[5] == 1 &&
            blackPieces[2] == 1 && blackPieces[0] == 0 && blackPieces[1] == 0 &&
            blackPieces[3] == 0 && blackPieces[4] == 0 &&
            whitePieces[0] == 0 && whitePieces[1] == 0 && whitePieces[2] == 0 &&
            whitePieces[3] == 0 && whitePieces[4] == 0) {
            return true;
        }

        // K+B vs K+B (jeweils ein Läufer, keine anderen Figuren)
        if (whitePieces[5] == 1 && blackPieces[5] == 1 &&
            whitePieces[3] == 1 && blackPieces[3] == 1 &&
            whitePieces[0] == 0 && whitePieces[1] == 0 && whitePieces[2] == 0 &&
            whitePieces[4] == 0 &&
            blackPieces[0] == 0 && blackPieces[1] == 0 && blackPieces[2] == 0 &&
            blackPieces[4] == 0) {
            return true;
        }

        // K+N vs K+N (jeweils ein Springer, keine anderen Figuren)
        if (whitePieces[5] == 1 && blackPieces[5] == 1 &&
            whitePieces[2] == 1 && blackPieces[2] == 1 &&
            whitePieces[0] == 0 && whitePieces[1] == 0 && whitePieces[3] == 0 &&
            whitePieces[4] == 0 &&
            blackPieces[0] == 0 && blackPieces[1] == 0 && blackPieces[3] == 0 &&
            blackPieces[4] == 0) {
            return true;
        }

        return false;
    }

    /**
     * Erzeugt einen eindeutigen String fuer die aktuelle Stellung.
     * Enthalt: Brett-Position, Zugrecht, Rochade-Rechte, En-Passant-Feld.
     * Aehnlich der FEN-Notation.
     */
    private String getBoardHash() {
        StringBuilder sb = new StringBuilder();

        // Brett-Position: fuer jede Reihe, Figuren und leere Felder
        for (int r = 0; r < 8; r++) {
            int emptyCount = 0;
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount);
                        emptyCount = 0;
                    }
                    // Figur als Buchstabe: klein fuer Schwarz, gross fuer Weiss
                    char ch = p.getType().getName().charAt(0);
                    if (p.getColor() == Color.BLACK) {
                        ch = Character.toLowerCase(ch);
                    }
                    sb.append(ch);
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount);
            }
            // Reihe-Trenner
            if (r < 7) sb.append('/');
        }

        // Zugrecht
        sb.append(' ').append(currentPlayer == Color.WHITE ? 'w' : 'b');

        // Rochade-Rechte
        sb.append(' ');
        boolean hasCastling = false;
        if (!whiteKingMoved && !whiteKingRookMoved) { sb.append('K'); hasCastling = true; }
        if (!whiteKingMoved && !whiteQueenRookMoved) { sb.append('Q'); hasCastling = true; }
        if (!blackKingMoved && !blackKingRookMoved) { sb.append('k'); hasCastling = true; }
        if (!blackKingMoved && !blackQueenRookMoved) { sb.append('q'); hasCastling = true; }
        if (!hasCastling) sb.append('-');

        // En-Passant-Feld
        sb.append(' ');
        if (enPassantRow >= 0 && enPassantCol >= 0) {
            sb.append((char)('a' + enPassantCol));
            sb.append(8 - enPassantRow);
        } else {
            sb.append('-');
        }

        return sb.toString();
    }

    /**
     * Prueft, ob die aktuelle Stellung bereits 3 Mal aufgetreten ist.
     * Remis nach dreimaliger Stellungswiederholung.
     */
    private boolean isThreefoldRepetition() {
        String currentHash = getBoardHash();
        int count = 0;
        for (String hash : positionHistory) {
            if (hash.equals(currentHash)) {
                count++;
            }
        }
        // Auch den aktuellen Hash mitzaehlen (er wurde noch nicht zur History hinzugefuegt)
        // Da afterMove() den Hash NACH updateStatus() hinzufuegt, ist der aktuelle Hash
        // bereits in positionHistory enthalten (vom vorherigen Zug oder Konstruktor).
        return count >= 3;
    }

    /**
     * Gibt den aktuellen Brett-Hash zurueck (nur fuer Tests).
     */
    public String getCurrentHash() {
        return getBoardHash();
    }

    /**
     * Gibt die positionHistory zurueck (nur fuer Tests — originale Liste, keine Kopie).
     */
    public List<String> getPositionHistory() {
        return positionHistory;
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
        // Wenn kein König gefunden, Zug als ungültig behandeln
        if (kingRow == -1) return true;
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
        // positionHistory ist final, aber die Liste ist mutabel — Inhalt kopieren
        copy.positionHistory.clear();
        copy.positionHistory.addAll(this.positionHistory);
        copy.fiftyMoveCounter = this.fiftyMoveCounter;
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

    /**
     * Gibt den 50-Zuege-Zaehler zurueck (nur fuer Tests).
     */
    public int getFiftyMoveCounter() {
        return fiftyMoveCounter;
    }

    /**
     * Setzt den 50-Zuege-Zaehler (nur fuer Tests).
     */
    public void setFiftyMoveCounter(int value) {
        this.fiftyMoveCounter = value;
    }

    /**
     * Setzt den aktuellen Spieler (fuer Laden).
     */
    public void setCurrentPlayer(Color color) {
        this.currentPlayer = color;
    }

    /**
     * Setzt den Spielstatus (fuer Laden).
     */
    public void setStatus(GameStatus status) {
        this.status = status;
    }

    /**
     * Gibt die En-Passant-Reihe zurueck (fuer Speichern/Laden).
     */
    public int getEnPassantRow() {
        return enPassantRow;
    }

    /**
     * Gibt die En-Passant-Spalte zurueck (fuer Speichern/Laden).
     */
    public int getEnPassantCol() {
        return enPassantCol;
    }

    /**
     * Setzt die En-Passant-Position (fuer Laden).
     */
    public void setEnPassant(int row, int col) {
        this.enPassantRow = row;
        this.enPassantCol = col;
    }

    // Rochade-Getter (fuer Speichern)
    public boolean isWhiteKingMoved() { return whiteKingMoved; }
    public boolean isBlackKingMoved() { return blackKingMoved; }
    public boolean isWhiteKingRookMoved() { return whiteKingRookMoved; }
    public boolean isWhiteQueenRookMoved() { return whiteQueenRookMoved; }
    public boolean isBlackKingRookMoved() { return blackKingRookMoved; }
    public boolean isBlackQueenRookMoved() { return blackQueenRookMoved; }

    // Rochade-Setter (fuer Laden)
    public void setWhiteKingMoved(boolean value) { this.whiteKingMoved = value; }
    public void setBlackKingMoved(boolean value) { this.blackKingMoved = value; }
    public void setWhiteKingRookMoved(boolean value) { this.whiteKingRookMoved = value; }
    public void setWhiteQueenRookMoved(boolean value) { this.whiteQueenRookMoved = value; }
    public void setBlackKingRookMoved(boolean value) { this.blackKingRookMoved = value; }
    public void setBlackQueenRookMoved(boolean value) { this.blackQueenRookMoved = value; }

}
