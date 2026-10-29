# Impeccable-Audit der Android-App — 29.10.2026

## Plattformurteil

**Bestanden: native Android-App.** Compose/Material-3-Komponenten, NavHost,
System-Zurück, Dialoge und Insets bilden eine native Oberfläche. Kein Web-Port.
Nachbesserungsbedarf besteht bei expliziten Bedienhilfen-Semantiken, schmalen
Layouts und dem Schutz laufender Speichervorgänge.

## Prüfgrundlage und Grenzen

Quellcodeprüfung aller Oberflächen: Startseite, Trainingskonfiguration und -ablauf,
Trainingsbibliothek, Übungsbibliothek, Übungseditor, Detaildialoge und Einstellungen.
Bewertung nach Impeccable `audit.native.md` und `android.md`, Modus Operate.
Die bestehende dunkle/goldene Gestaltung bleibt verbindlich; ein heller Modus
oder Dynamic Color wird nicht ungefragt als Produktanforderung eingeführt.

Geräteevidenz: automatisierte Abläufe auf A001/Android 16, aktuelle sowie zuvor
geprüfte Screenshots desselben Layouts bei Schriftfaktor 1.0, 1.3 und im Editor
2.0. Kein vollständiger TalkBack-Durchlauf, kein Tablet/Foldable und kein
Performance-Profiling. Die folgenden Scores sind deshalb eine vorsichtige
Quellcodebewertung, keine Zertifizierung dieser ungetesteten Eigenschaften.

## Audit Health Score

| Dimension | Score | Wichtigster Befund |
|---|---:|---|
| Barrierefreiheit | 2/4 | Regler, Schalter und Stepper benötigen eindeutige Kontextbeschriftungen |
| Performance | 2/4 | Synchrone Audiovorbereitung; Übungsliste ohne LazyColumn |
| Darstellung/Theming | 3/4 | Etablierter dunkler Stil; mehrere feste Farben außerhalb des Themes |
| Plattformkonformität | 3/4 | Native Komponenten; Navigation während Speichern nicht ausreichend geschützt |
| Anpassungsfähigkeit | 2/4 | Feste Breiten und einzeilige Filter begrenzen kleine Fenster/große Schrift |
| **Gesamt** | **12/20** | **Akzeptabel; gezielte Nacharbeit erforderlich** |

Sechs Befunde: **0 P0, 2 P1, 3 P2, 1 P3**. Die bereinigten Lint-Meldungen zählen
nicht erneut als offene Befunde. Ein sauberer Lint-Bericht beweist keine vollständig
barrierefreie oder unter allen Bedingungen flüssige App.

## Detaillierte Befunde

### [P1] Bedienelemente brauchen eindeutigen Kontext für TalkBack

- Kategorie: Barrierefreiheit.
- Stellen: `ui/SettingsScreen.kt:226,369,432`, `ui/TrainingSetupScreen.kt:725`,
  `ui/StepperComponents.kt:48,69` (Pfade relativ zu `app/src/main/java/com/example/myapplication`).
- Befund: Beschriftungen und Slider/Switch stehen als getrennte Geschwister im
  Layout. Es gibt keine explizite semantische Zuordnung. Stepper verwenden nur
  „+“/„−“ und ein Zahlenfeld ohne Feldbezeichnung; ähnliche Kontrollen wiederholen sich.
- Auswirkung: Beim direkten Fokus auf eine Kontrolle fehlt der eindeutige Zweck.
  Der tatsächlich vorgelesene Text muss zusätzlich mit TalkBack bestätigt werden.
- Empfehlung: Kontextbeschriftungen, Button-Rollen und Zustandsbeschreibungen
  ergänzen; Schalterzeilen semantisch zusammenfassen, ohne doppelte Aktionen.
- Regel: Android-Bedienhilfen verlangen verständlich benannte Aktionen und Zustände.
- Passender Befehl: `$impeccable harden Barrierefreiheit der Einstellungen und Trainingskonfiguration`.

### [P1] Trainingseditor lässt Verlassen während Speichern zu

- Kategorie: Plattformkonformität / Fehlerzustände.
- Stelle: `ui/TrainingEditor.kt:25–29,52`.
- Befund: `busy` verhindert weitere Speicheraufrufe, aber weder Zurück noch
  „Verwerfen“ sind währenddessen gesperrt. Ein späterer Erfolgs-Callback navigiert
  weiterhin zur Bibliothek.
- Auswirkung: Verwerfen kann nach bereits angestoßenem Speichern irreführend sein;
  ein später Callback kann eine gerade gewählte andere Seite wieder verlassen.
- Empfehlung: Aktive Speicherung sichtbar machen, konkurrierende Navigation
  absichern und verzögerte Erfolgs-/Fehler-Callbacks gezielt testen.
- Passender Befehl: `$impeccable harden Trainingseditor`.

### [P2] Synchrone Vorbereitung eigener Audioansagen

- Kategorie: Performance.
- Stellen: `audio/TrainingAudioManager.kt:118–136`, `ui/HaulaufViewModel.kt:307–311`.
- Befund: Vorschau startet im Main-Scope; MediaPlayer verwendet synchrones
  `prepare()`. Ein langsamer oder problematischer Datenzugriff kann die UI blockieren.
- Empfehlung: Asynchrone Vorbereitung mit sauberer Fehler-/Abbruchbehandlung;
  langsame und defekte Dateien prüfen. Eine tatsächliche Blockierdauer wurde nicht gemessen.
- Passender Befehl: `$impeccable optimize Audiovorschau`.

### [P2] Übungsbibliothek baut alle Einträge sofort auf

- Kategorie: Performance.
- Stelle: `ui/MoveAudioScreen.kt:120–148`.
- Befund: Scrollbare Column mit `visible.forEach`, statt einer wiederverwendenden
  LazyColumn. Bei 19 Standardübungen wurde kein Ruckeln nachgewiesen; viele eigene
  Übungen erhöhen aber den Aufwand proportional zur Listenlänge.
- Empfehlung: LazyColumn mit stabilen Übungs-IDs; Such-/Filterzustand und Position
  erhalten. Mit einer größeren Bibliothek messen.
- Passender Befehl: `$impeccable optimize Übungsbibliothek`.

### [P2] Feste Breiten und Filterzeilen begrenzen die Anpassung

- Kategorie: Anpassungsfähigkeit.
- Stellen: `ui/StepperComponents.kt:48–50,91`, `ui/MoveAudioScreen.kt:138`,
  `ui/HomeScreen.kt:123,147,227,350`.
- Befund: Stepper reserviert 192 dp neben dem Titel; Filter bleiben in einer Zeile;
  mehrere Startseitenbuttons besitzen feste Höhen. Es gibt keine breite
  Fensteranordnung. Das sind konkrete Layoutgrenzen, kein nachgewiesener Ausfall
  auf sämtlichen genannten Geräteklassen.
- Empfehlung: Bei wenig Platz stapeln/umbrechen, Mindesthöhen verwenden und
  kleine Fenster, Querformat und große Schrift zusammen prüfen.
- Passender Befehl: `$impeccable adapt kleine Fenster und große Schrift`.

### [P3] Farbrollen sind nicht durchgängig zentralisiert

- Kategorie: Theming.
- Stellen: `ui/SettingsScreen.kt`, `ui/StepperComponents.kt`, `ui/theme/Theme.kt`.
- Befund: Wiederholte feste Weiß-/Grautöne ergänzen vorhandene Theme-Rollen.
  Die tatsächlich ausgelieferte App verwendet absichtlich das dunkle Theme.
- Empfehlung: Bei der nächsten Überarbeitung feste UI-Farben in semantische Rollen
  überführen. Kein heller Modus allein zur Erhöhung des Audit-Scores.
- Passender Befehl: `$impeccable polish Farbrollen`.

## Muster und positive Befunde

Die verbleibenden Probleme betreffen überwiegend ältere gemeinsame Controls und
asynchrone Vorgänge; der gehärtete Übungseditor kann als Vorbild für sichtbare
Fehlerzustände dienen. Positiv: beschriftete Navigationsicons, 96-dp-Trainingsaktionen,
scrollbare Inhalte, IME-Behandlung im Übungseditor, explizite Entwürfe, Fehlerwiederholung,
stabile IDs in der Trainingsbibliothek und erhaltene historische Quellen.
Die App-Icons besitzen jetzt eine monochrome Variante; der aktuelle technische
Lint-Lauf meldet keine Warnungen oder Hinweise.

## Empfohlene Reihenfolge

1. **P1:** `$impeccable harden Trainingseditor`.
2. **P1:** `$impeccable harden Barrierefreiheit der Einstellungen und Trainingskonfiguration`.
3. **P2:** `$impeccable optimize Audiovorschau und Übungsbibliothek`.
4. **P2:** `$impeccable adapt kleine Fenster und große Schrift`.
5. Abschließend `$impeccable polish` für die überarbeiteten Oberflächen.

Die Schritte lassen sich einzeln oder gemeinsam beauftragen. Danach das Audit
wiederholen; TalkBack und echte Trainingspraxis bleiben notwendige Geräteprüfungen.
