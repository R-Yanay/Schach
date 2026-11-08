# Schach – JavaFX App

Ein Schachspiel mit grafischer Oberfläche, das du gegen einen Computer-Gegner (Minimax-KI) spielst.

## Features

- **Startbildschirm** mit Schwierigkeitsauswahl (Easy, Medium, Hard)
- **Grafisches Schachbrett** mit JavaFX (8×8 Felder, hell/dunkel)
- **Alle 6 Figuren-Typen** mit korrekten Bewegungsregeln
- **Klick-Steuerung**: Figur anklicken → Ziel-Feld anklicken
- **Alle Schach-Regeln**: Rochade, En-Passant, Figurenumwandlung, Schach/Schachmatt/Patt
- **Computer-Gegner** mit konfigurierbarem Minimax-Algorithmus (Tiefe 1–5)
- **Visuelle Hilfe**: Grüne Punkte zeigen gültige Züge, gelbe Markierung für ausgewählte Figur
- **New Game-Button** geht zurück zum Startbildschirm

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

1. **Schwierigkeit wählen**: Auf dem Startbildschirm Easy, Medium oder Hard auswählen (Button wird grün hervorgehoben)
2. **Spiel starten**: Auf "New Game" klicken
3. **Figur auswählen**: Klicke auf eine weiße Figur (gelbe Markierung)
4. **Zug ausführen**: Klicke auf ein grünes Feld (gültiger Zug)
5. **Computer zieht**: Nach deinem Zug denkt die KI nach und zieht automatisch
6. **Neues Spiel**: Auf "New Game" klicken, um zurück zum Startbildschirm zu gehen

## Architektur

```
Schach/src/main/java/ch/zhaw/schach/
├── Main.java                      # JavaFX Einstiegspunkt
├── model/
│   ├── ChessBoard.java            # 8x8 Brett, Figuren-Positionen
│   ├── ChessGame.java             # Spiel-Logik (Züge, Rochade, En-Passant)
│   ├── Color.java                 # Enum: WHITE, BLACK
│   ├── GameStatus.java            # Enum: IN_PROGRESS, CHECK, CHECKMATE, STALEMATE
│   ├── Move.java                  # Move (start, end, promotion)
│   ├── MoveValidator.java         # Validates moves
│   ├── Piece.java                 # Piece (type, color)
│   └── PieceType.java             # Enum: PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
├── ai/
│   ├── AI.java                    # Minimax-Algorithmus mit Alpha-Beta-Pruning
│   └── Evaluator.java             # Bewertet Stellungen (Material + Position)
├── view/
│   └── ChessView.java             # JavaFX Hauptfenster
└── controller/
    └── GameController.java        # Verbinzt View und Model
```

## Was ich gelernt habe

Bei diesem Projekt habe ich JavaFX kennengelernt und verstanden, wie man eine grafische Desktop-App mit Java baut. Im Gegensatz zu Spring Boot (Server-API) läuft eine JavaFX-App direkt auf dem Desktop mit einer visuellen Oberfläche. Die wichtigsten Konzepte waren `Application.start()` für den Einstieg, `GridPane` für das Schachbrett-Layout, Klick-Handler für die Interaktion, und der Minimax-Algorithmus für die Computer-KI. Die Drei-Schichten-Architektur (Model, View, Controller) hält den Code sauber und wartbar.

## Tests

35 JUnit-Tests decken die Bewegungsregeln aller Figuren, Rochade (inkl. Blockierung durch angegriffene Felder), En-Passant, Figurenumwandlung, Schach-Erkennung und Spielstatus ab.

```bash
mvn test
```

**Tests run: 35, Failures: 0, Errors: 0, BUILD SUCCESS**

## Lizenz

Dieses Projekt wurde als Übungprojekt erstellt und dient Demonstrationszwecken.
