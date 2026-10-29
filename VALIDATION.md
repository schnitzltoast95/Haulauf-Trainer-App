# Gemeinsamer Teststand vom 29.10.2026

Dieser Stand enthält sämtliche Änderungen an Startseite, Trainingskonfiguration,
Trainingsbedienung, Trainingsbibliothek, Übungsbibliothek und Übungseditor sowie
gemeinsamen Audiopegeln. Die 19 historischen Standardübungen bleiben enthalten.

## Geprüftes Artefakt

- Datei: `Haulauf-Gesamtstand-2026-10-29.apk`
- Vollständiger Debug-Testbuild, kein signierter Produktionsrelease.
- App: **Haulauf Ablauf-Test**, Paket `com.example.myapplication.flowreview`.
- Version: `1.0`, VersionCode `1`.
- SHA-256: `703BD4A9C75DD71D9D1857816F03162DB11B6ECBCBC96BC411B52714C40F0DF3`.
- Gerät: A001, Android 16, 1080 × 2392; Schriftfaktor 1.0.
- Basis: GitHub-main `dd5e23b`; geprüfter Quellstand ist der Commit mit diesem Bericht.

## Ergebnisse

- Gesamter Build erfolgreich, App und Instrumentierungs-APK aus demselben Build.
- **2 Unit-Tests bestanden**, keine Fehler oder übersprungenen Tests.
- **13 Android-Gerätetests bestanden**, komplette Instrumentierungssuite, 58,428 Sekunden.
- Android Lint: **0 Fehler, 0 Warnungen, 0 Hinweise**.
- `git diff --check`: ohne Befund.

Die Gerätetests umfassen historische Bilder und Quellen für alle Standardübungen,
Trainingsvorlagen mit Bearbeiten/Kopie/Umbenennen, Abbruch und Fehlerwiederholung,
Übungserstellung, gemeinsame Entwurfs-Speicherung, Löschfolgen, große
Pause-/Fortsetzen-Bedienung, fehlerhafte Medienimporte mit anschließendem gültigem
Bildimport sowie das Beenden des Vordergrunddienstes nach Trainingsabschluss.
Speicher-/Löschfehler und Dateiauswahl-Ergebnisse werden gezielt im Test simuliert.

Vorangegangene Prüfungen desselben funktionalen Stands decken zusätzlich den
Übungseditor bei Schriftfaktor 2.0 ab (vier UI- und zwei Speichertests bestanden,
Fehleranzeigen visuell geprüft). Die vollständige Suite dieses Berichts lief bei 1.0.

## Reproduktion

```sh
./gradlew -I tools/flow-review.init.gradle :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.example.myapplication.flowreview.test/androidx.test.runner.AndroidJUnitRunner
```

Lokale Rohprotokolle und Gerätebilder liegen unter `.impeccable/review/` und werden
nicht versioniert. APKs werden außerhalb des Quellrepositories bereitgestellt.
Offene praktische Prüfungen sind in [NEXT_STEPS.md](NEXT_STEPS.md) dokumentiert;
bestandene automatisierte Tests ersetzen diese Freigaben nicht.

## Lint-Bereinigung und Audit

Die 25 Warnungen und 9 Hinweise sind ohne Lint-Unterdr�ckungen bereinigt.
Nicht verwendete historische Scans liegen unver�ndert unter `docs/historical-reference/`;
die 19 verwendeten Standard�bungen sind durch die historische Testsuite abgedeckt.
Optionale Signaldateien werden jetzt �ber explizite Resource-IDs eingebunden
(siehe README); die bisher aktiven Systemton-Fallbacks bleiben erhalten.
Launcher-Icons nutzen die vorhandenen Vektoren statt uneinheitlicher Rastergr��en.
Auf Android 16 wurden normales und monochromes Icon zus�tzlich visuell gepr�ft.
Die Legacy-Icon-Darstellung auf Android 7/8 wurde nicht auf einem solchen Ger�t gepr�ft.

Das anschlie�ende Quellcode-Audit steht in [ANDROID_AUDIT.md](ANDROID_AUDIT.md):
12/20, sechs offene Befunde au�erhalb der Lint-Bereinigung.
