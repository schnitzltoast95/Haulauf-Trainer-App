# Nächste Schritte

Der gemeinsame Funktionsstand ist gebaut und automatisiert getestet. Ergebnisse
und APK-Prüfsumme stehen in [VALIDATION.md](VALIDATION.md).

## Erledigt: sechs Auditbefunde

Speicherschutz, Bedienhilfen-Kontext, asynchrone Audiovorbereitung, virtuelle
Übungsbibliothek, anpassbare Layouts und gemeinsame Farbrollen sind umgesetzt.
Nachweise und Grenzen: [ANDROID_AUDIT.md](ANDROID_AUDIT.md).

## 1. Praxisprüfung im Training

- Eine vollständige Einheit mit laufender Musik und gesperrtem Bildschirm testen:
  Ansagen, Start-/Fortsetzen-Countdown, Endton und optionales Metronom müssen hörbar
  bleiben. Gerät, Musik-App, Android-Version und Ergebnis notieren.
- Pause, Fortsetzen und Beenden mit den tatsächlich verwendeten Fechthandschuhen
  prüfen; große Tasten allein belegen die Touch-Erkennung nicht.
- Prüfen, dass Dienst und Benachrichtigung nach regulärem Ende und manuellem
  Beenden verschwinden. Das reguläre Ende ist bereits automatisiert abgedeckt.

## 2. Medien und unterbrochene Bearbeitung

- Echte Mikrofonaufnahme aufnehmen, anhören, speichern und nach App-Neustart erneut
  abspielen. Kurze/unterbrochene Aufnahmen sowie verweigerte Mikrofonfreigabe prüfen.
- Eine gültige eigene Audiodatei importieren und nach Neustart abspielen; zusätzlich
  Dateien aus einem Cloud-Dateianbieter testen.
- Rotation und Prozessneustart während Entwurf, Speichern und Medienimport prüfen;
  sicherstellen, dass bestätigte Medien erhalten bleiben und verworfene Entwürfe
  keine ungenutzten Dateien hinterlassen.
- Englische Oberfläche und TalkBack mit den neuen Fehleranzeigen praktisch prüfen.

## 3. Release vorbereiten

- Lint-Bereinigung abgeschlossen: 0 Fehler, 0 Warnungen, 0 Hinweise. Diesen Stand bei weiteren Änderungen erhalten.
- [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) vollständig durchgehen, einschließlich
  Stimmenwahl und hörbarer Audioausgabe des tatsächlichen Release-Artefakts.
- Versionsnummer erhöhen, Produktions-APK signieren und Installation beziehungsweise
  Update mit vorhandenen Nutzerdaten prüfen. Die aktuelle isolierte Test-App besitzt
  eigene Daten und ersetzt keine vorhandene Produktionsinstallation.
- Erst nach diesen Prüfungen den gemeinsamen Stand als Produktionsrelease freigeben.
