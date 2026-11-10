# Schach – JavaFX App

Ein Schachspiel mit grafischer Oberfläche, das du gegen einen Computer-Gegner (Minimax-KI) spielst.

## Features

- **Startbildschirm** mit konfigurierbaren Einstellungen:
  - Schwierigkeitsauswahl (Easy, Medium, Hard)
  - Spielerfarbe waehlen (Weiss oder Schwarz)
  - Figuren-Style waehlen (Alpha, Leipzig, Horsey) mit Vorschau-Bildern
  - Brett-Theme waehlen (Wood, Mono, Ocean) mit 2x2 Schachbrett-Muster
- **Grafisches Schachbrett** mit JavaFX (8x8 Felder, 3 Farbschemen)
- **Alle 6 Figuren-Typen** mit korrekten Bewegungsregeln
- **Klick-Steuerung**: Figur anklicken → Ziel-Feld anklicken
- **Rechtsklick**: Aktive Figurauswahl abbrechen
- **Alle Schach-Regeln**: Rochade (Kingside/Queenside unabhaengig), En-Passant, Bauer-Promotion (Overlay-Dialog mit 4 Figuren zur Auswahl), Schach/Schachmatt
- **Alle Remis-Regeln**:
  - Patt (Stalemate)
  - Unzureichendes Material (K vs K, K+B vs K, K+N vs K, K+B vs K+B, K+N vs K+N)
  - Dreimalige Stellungswiederholung (Hash-Vergleich)
  - 50-Zuege-Regel (100 Halbzuege ohne Bauerzug und ohne Schlagen)
- **Computer-Gegner** mit konfigurierbarem Minimax-Algorithmus (Tiefe 1–4)
- **Visuelle Hilfe**: Gruene Punkte fuer gueltige Zuege, rote Ringe fuer Schlaege, gruen Markierung fuer letzten Zug
- **Menueleiste** im Spielfenster: Game → Save (Slot 1/2/3), Load (Slot 1/2/3), New Game
- **Speichern/Laden** des Spielzustands als JSON (3 nummerierte Slots)
- **Fehlerbehandlung**:
  - KI-Timeout (30s): Dialog mit "Wait again" (60s) oder "New Game"
  - Speichern fehlgeschlagen: Automatischer Fallback zum naechsten Slot mit Benachrichtigung
  - Laden fehlgeschlagen: Dialog mit Slot-Wahl (Slot 2 / Slot 3 / Cancel)
  - Alle Fehler werden in `chess.log` protokolliert

## Voraussetzungen

- **Java 25** (oder hoeher)
- **Maven** fuer den Build
- **JavaFX 27.0.1** 

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

1. **Einstellungen waehlen** auf dem Startbildschirm:
   - Schwierigkeit: Easy, Medium oder Hard (blau hervorgehoben)
   - Spielerfarbe: White oder Black (orange hervorgehoben)
   - Figuren-Style: Alpha, Leipzig oder Horsey (lila hervorgehoben)
   - Brett-Theme: Wood, Mono oder Ocean (gruen hervorgehoben)
2. **Spiel starten**: Auf "New Game" klicken
3. **Figur auswaehlen**: Klicke auf eine eigene Figur (gelbe Markierung)
4. **Zug ausfuehren**: Klicke auf ein Feld mit gruenem Punkt (Zug) oder rotem Ring (Schlag)
5. **Bauer-Promotion**: Wenn ein Bauer das Ende erreicht, erscheint ein Overlay-Dialog direkt über dem Brett mit 4 Figuren zur Auswahl (Queen, Rook, Bishop, Knight). Rechtsklick oder ESC bricht die Auswahl ab.
6. **Computer zieht**: Nach deinem Zug denkt die KI nach und zieht automatisch
7. **Rechtsklick**: Aktive Figurauswahl abbrechen (Highlight und Punkte verschwinden)
8. **Speichern/Laden**: Ueber die Menueleiste "Game" → "Save" oder "Load" → Slot 1/2/3
9. **Neues Spiel**: Ueber "Game" → "New Game" zurueck zum Startbildschirm

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
│   ├── LoadException.java         # Exception: Spielstand kann nicht geladen werden
│   ├── Move.java                  # Zug (start, end, promotion)
│   ├── MoveValidator.java         # Gültige Züge berechnen
│   ├── Piece.java                 # Figur (Typ, Farbe)
│   ├── PieceStyle.java            # Enum: ALPHA, LEIPZIG, HORSEY (Figuren-Styles)
│   ├── PieceType.java             # Enum: PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
│   └── SaveException.java         # Exception: Spielstand kann nicht gespeichert werden
├── ai/
│   ├── AI.java                    # Minimax-Algorithmus mit Alpha-Beta-Pruning
│   └── Evaluator.java             # Bewertet Stellungen (Material + Position)
├── view/
│   ├── ChessView.java             # JavaFX Application-Einstieg, orchestriert Screens
│   ├── GameScreen.java            # Spielfenster mit Brett und Menueleiste
│   ├── GameView.java              # Interface: Vertrag zwischen Controller und View
│   └── StartScreen.java           # Startbildschirm mit Einstellungen
└── controller/
    └── GameController.java        # Verbindet GameView-Interface und Model
```

## Fehlerbehandlung

Das Projekt verwendet benutzerdefinierte Exceptions und Java-Standard-Exceptions, um Fehler abzufangen und dem Benutzer freundliche Meldungen zu zeigen.

### Benutzerdefinierte Exceptions

| Exception | Wird geworfen bei | Benutzer-Option |
|-----------|------------------|-----------------|
| **`SaveException`** | Speichern fehlschlägt (Festplatte voll, keine Berechtigung) | Automatischer Fallback: nächster Slot wird versucht. Bei Erfolg: "Slot 1 failed. Game saved to slot 2 instead." |
| **`LoadException`** | Laden fehlschlägt (Datei fehlt, leer, beschädigt) | Dialog erscheint: Benutzer wählt einen anderen Slot (Slot 2 / Slot 3) oder bricht ab |

### KI-Timeout (Standard-Java-Exceptions)

| Exception | Wird geworfen bei | Benutzer-Option |
|-----------|------------------|-----------------|
| **`TimeoutException`** | KI braucht länger als 30 Sekunden | Dialog: "Wait again" (KI neu mit 60s) oder "New Game" |
| **`InterruptedException`** | KI-Thread wird unterbrochen | Status: "AI calculation interrupted." |
| **`ExecutionException`** | Bug in der KI-Berechnung | Status: "AI error: ..." |

### Fehlerprotokoll

Jeder Fehler wird in `chess.log` protokolliert (Zeitstempel, Kategorie, Meldung, Ursache). Die Datei liegt im Projektordner neben den Spielstand-Dateien.

```
2026-11-10T14:05:23.456 [AI] Timeout after 30 seconds.
2026-11-10T14:06:12.789 [SAVE] Slot 1: Konnte Spielstand nicht speichern.
2026-11-10T14:07:45.012 [LOAD] Slot 2: Kein gespeicherter Spielstand in diesem Slot.
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
- **Fehlerbehandlung**: Benutzerdefinierte Exceptions (SaveException, LoadException) und Standard-Java-Exceptions (TimeoutException) mit try-catch, Dialogen und Fehlerprotokoll (chess.log)
- **Git**: Versionskontrolle für den Entwicklungsprozess

## Tests

57 JUnit-Tests in 9 Testdateien decken die Spiellogik ab:

| Testdatei | Tests | Abgedeckt |
|-----------|-------|-----------|
| ChessGameTest | 22 | Züge, Rochade (inkl. Kingside/Queenside Unabhängigkeit), En-Passant, Promotion (Queen, Rook), Schach, Schachmatt, Patt |
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

**Tests run: 57, Failures: 0, Errors: 0, BUILD SUCCESS**

## Lizenz

Dieses Projekt wurde als Übungprojekt erstellt und dient Demonstrationszwecken.
