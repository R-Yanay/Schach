# Schach – JavaFX App

Ein Schachspiel mit grafischer Oberfläche, das du gegen einen Computer-Gegner (Minimax-KI) spielst.

## Features

- **Startbildschirm** mit konfigurierbaren Einstellungen:
  - Schwierigkeitsauswahl (Easy, Medium, Hard)
  - Spielerfarbe wählen (Weiß oder Schwarz)
  - Figuren-Style wählen (Alpha, Leipzig, Horsey) mit Vorschau-Bildern
  - Brett-Theme wählen (Wood, Mono, Ocean) mit 2×2 Schachbrett-Muster
- **Grafisches Schachbrett** mit JavaFX (8×8 Felder, 3 Farbschemen)
- **Alle 6 Figuren-Typen** mit korrekten Bewegungsregeln
- **Klick-Steuerung**: Figur anklicken → Ziel-Feld anklicken
- **Alle Schach-Regeln**: Rochade, En-Passant, Bauer-Promotion, Schach/Schachmatt
- **Alle Remis-Regeln**:
  - Patt (Stalemate)
  - Unzureichendes Material (K vs K, K+B vs K, K+N vs K, K+B vs K+B, K+N vs K+N)
  - Dreimalige Stellungswiederholung (Hash-Vergleich)
  - 50-Züge-Regel (100 Halbzüge ohne Bauerzug und ohne Schlagen)
- **Computer-Gegner** mit konfigurierbarem Minimax-Algorithmus (Tiefe 1–4)
- **Visuelle Hilfe**: Grüne Punkte für gültige Züge, rote Ringe für Schläge, grüne Markierung für letzten Zug
- **Menüleiste** im Spielfenster: Game → Save (Slot 1/2/3), Load (Slot 1/2/3), New Game
- **Speichern/Laden** des Spielzustands als JSON (3 nummerierte Slots)

## Voraussetzungen

- **Java 25** (oder höher)
- **Maven** für den Build
- **JavaFX 27.0.1** (wird automatisch von Maven heruntergeladen)

## Projekt starten

### Mit Maven

```bash
mvn compile javafx:run
```

### In IntelliJ

1. Projekt öffnen
2. `Main.java` im Package `ch.zhaw.schach` öffnen
3. Auf "Run" klicken

## Bedienung

1. **Einstellungen wählen** auf dem Startbildschirm:
   - Schwierigkeit: Easy, Medium oder Hard (blau hervorgehoben)
   - Spielerfarbe: White oder Black (orange hervorgehoben)
   - Figuren-Style: Alpha, Leipzig oder Horsey (lila hervorgehoben)
   - Brett-Theme: Wood, Mono oder Ocean (grün hervorgehoben)
2. **Spiel starten**: Auf "New Game" klicken
3. **Figur auswählen**: Klicke auf eine eigene Figur (gelbe Markierung)
4. **Zug ausführen**: Klicke auf ein Feld mit grünem Punkt (Zug) oder rotem Ring (Schlag)
5. **Computer zieht**: Nach deinem Zug denkt die KI nach und zieht automatisch
6. **Speichern/Laden**: Über die Menüleiste "Game" → "Save" oder "Load" → Slot 1/2/3
7. **Neues Spiel**: Über "Game" → "New Game" zurück zum Startbildschirm

## Architektur

MVC-Muster (Model-View-Controller) mit Interface-Trennung zwischen Controller und View.

```
Schach/src/main/java/ch/zhaw/schach/
├── Main.java                      # JavaFX Einstiegspunkt
├── model/
│   ├── BoardTheme.java            # Enum: WOOD, MONOCHROME, OCEAN (Brett-Farben)
│   ├── ChessBoard.java            # 8x8 Brett, Figuren-Positionen
│   ├── ChessGame.java             # Spiel-Logik (Züge, Rochade, En-Passant, Remis)
│   ├── Color.java                 # Enum: WHITE, BLACK
│   ├── GameSaver.java             # JSON-Speichern/Laden des Spielzustands
│   ├── GameStatus.java            # Enum: IN_PROGRESS, CHECK, CHECKMATE, STALEMATE,
│   │                              #   INSUFFICIENT_MATERIAL, DRAW_THREEFOLD_REPETITION,
│   │                              #   DRAW_FIFTY_MOVES
│   ├── Move.java                  # Zug (start, end, promotion)
│   ├── MoveValidator.java         # Gültige Züge berechnen
│   ├── Piece.java                 # Figur (Typ, Farbe)
│   ├── PieceStyle.java            # Enum: ALPHA, LEIPZIG, HORSEY (Figuren-Styles)
│   └── PieceType.java             # Enum: PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
├── ai/
│   ├── AI.java                    # Minimax-Algorithmus mit Alpha-Beta-Pruning
│   └── Evaluator.java             # Bewertet Stellungen (Material + Position)
├── view/
│   ├── ChessView.java             # JavaFX Application-Einstieg, orchestriert Screens
│   ├── GameScreen.java            # Spielfenster mit Brett und Menüleiste
│   ├── GameView.java              # Interface: Vertrag zwischen Controller und View
│   └── StartScreen.java           # Startbildschirm mit Einstellungen
└── controller/
    └── GameController.java        # Verbinzt GameView-Interface und Model
```

## Was ich gelernt habe

Bei diesem Projekt habe ich JavaFX kennengelernt und verstanden, wie man eine grafische Desktop-App mit Java baut. Im Gegensatz zu Spring Boot (Server-API) läuft eine JavaFX-App direkt auf dem Desktop mit einer visuellen Oberfläche. Die wichtigsten Konzepte waren:

- **JavaFX**: `Application.start()` für den Einstieg, `GridPane` für das Schachbrett-Layout, SVG-Bilder für Figuren
- **MVC-Architektur**: Model, View, Controller-Trennung mit `GameView`-Interface als Vertrag zwischen Controller und View
- **Lambda-Ausdrücke und Callbacks**: Für Button-Klicks, Threads (KI-Berechnung im Hintergrund) und Screen-Übergänge
- **Method-References** (`this::showStartScreen`): Kurze Schreibweise für Callbacks
- **Minimax-Algorithmus**: Computer-KI mit konfigurierbarer Suchtiefe
- **JSON-Speicherung**: Spielzustand serialisieren und laden mit Gson-Bibliothek
- **JUnit-Tests**: Unit-Tests für Spiellogik, Remis-Regeln und Speichern/Laden
- **Git**: Versionskontrolle für den Entwicklungsprozess

## Tests

54 JUnit-Tests in 9 Testdateien decken die Spiellogik ab:

| Testdatei | Tests | Abgedeckt |
|-----------|-------|-----------|
| ChessGameTest | 19 | Züge, Rochade, En-Passant, Promotion, Schach, Schachmatt, Patt |
| MoveValidatorTest | 16 | Bewegungsregeln aller 6 Figuren-Typen |
| InsufficientMaterialTest | 4 | Unzureichendes Material (K vs K, K+B vs K, K+N vs K, K+B vs K+B, K+N vs K+N) |
| ThreefoldRepetitionTest | 5 | Dreimalige Stellungswiederholung (Hash-Vergleich) |
| FiftyMovesTest | 3 | 50-Züge-Regel (Zähler, Reset bei Bauerzug/Schlagen) |
| GameSaverTest | 2 | JSON-Speichern/Laden (Brett-Position und alle Status-Felder) |
| ChessBoardTest | 2 | Figuren setzen/lesen, Startposition |
| PieceTypeTest | 2 | SVG-Pfade und Figurennamen |
| AITest | 1 | KI gibt legalen Zug zurück |

```bash
mvn test
```

**Tests run: 54, Failures: 0, Errors: 0, BUILD SUCCESS**

## Lizenz

Dieses Projekt wurde als Übungprojekt erstellt und dient Demonstrationszwecken.
