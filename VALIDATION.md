# Gemeinsamer Teststand vom 29.10.2026

Dieser Stand enthält die bisherigen Verbesserungen aller App-Bereiche und die
sechs Nachbesserungen aus dem Impeccable-Audit. Alle 19 historischen
Standardübungen bleiben enthalten.

## Geprüftes Artefakt

- Datei: `Haulauf-Gesamtstand-2026-10-29.apk` im übergeordneten `outputs`-Ordner.
- Vollständiger Debug-Testbuild, kein signierter Produktionsrelease.
- App: **Haulauf Ablauf-Test**, Paket `com.example.myapplication.flowreview`.
- Version: `1.0`, VersionCode `1`.
- SHA-256: `245FC752A104556308F6DB7859AEB016D5FBE75F43B704C0F26A8C5222352B0C`.
- Gerät: A001, Android 16, 1080 × 2392.
- Basis: GitHub-main `dec2e57`; Quellstand: Commit mit diesem Bericht.

## Ergebnisse

- Gemeinsamer App-/Instrumentierungsbuild erfolgreich.
- **2 Unit-Tests bestanden**.
- **23 Android-Gerätetests bestanden**, vollständige Suite, 78,053 Sekunden.
- Anschließend ausschließlich die Home-Kennzahlen für große Schrift angepasst;
  **2 gezielte Nachtests bestanden**, 15,205 Sekunden: Home bei Schriftfaktor 2.0
  und kompletter Vorlagenablauf über die normale Startseite.
- Erneuter Build, Unit-Tests und Lint nach dieser letzten Anpassung erfolgreich.
- Android Lint: **0 Fehler, 0 Warnungen, 0 Hinweise**.

Die Suite prüft historische Quellen/Bilder, Vorlagen mit Bearbeiten/Kopie/
Umbenennen, Übungserstellung, Entwurfsspeicherung und Fehlerwiederholung,
Löschfolgen, Medienimport, große Trainingsaktionen und Dienstende.
Neun zusätzliche Audit-Tests prüfen verzögerte Speicher-Callbacks, Abbruch,
Audiofehler und -freigabe, die 300-Einträge-Bibliothek, beschriftete Controls und
schmale/große Layouts. Die Audio-Lifecycle-Prüfung läuft stumm; sie bestätigt
technische Wiedergabe und Freigabe, nicht die Hörbarkeit während Musik.

## Darstellungsprüfung

Normale Ansicht sowie in Compose auf 280/320 dp begrenzte Inhalte mit Schriftfaktor
2.0 und Trainingskonfiguration in einem 320 × 360-dp-Fenster wurden geprüft.
Die Systemschrift-Einstellung wurde dafür nicht verändert. Screenshots von
Konfiguration, Startseite, Kategorien, Steppern und Einstellungen wurden visuell
geprüft; gefundene Wortumbrüche und gequetschte Kennzahlen wurden korrigiert.
Der vollständige Hörtest mit TalkBack und physische Tablet-/Foldable-Tests fehlen.

## Performance-Evidenz

Bei 300 Übungen wurden vor der LazyColumn-Umstellung 300 Namen aufgebaut,
danach nur 4; der letzte Eintrag bleibt erreichbar. Einzelmessung bis Idle:
4304 ms vorher, 383 ms nachher. Keine statistische Benchmarkreihe.

## Reproduktion

```sh
./gradlew -I tools/flow-review.init.gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.example.myapplication.flowreview.test/androidx.test.runner.AndroidJUnitRunner
```

Lokale Protokolle und Bilder: `.impeccable/review/audit-*` (nicht versioniert).
[ANDROID_AUDIT.md](ANDROID_AUDIT.md) enthält die Nachprüfung;
[NEXT_STEPS.md](NEXT_STEPS.md) die noch nötigen Praxis- und Releaseprüfungen.
