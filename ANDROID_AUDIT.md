# Impeccable-Nachprüfung der Android-App — 29.10.2026

Die sechs Befunde aus dem Audit von Commit `55c1013` sind umgesetzt.
Die dunkle/goldene native Compose-Oberfläche und alle 19 historischen
Standardübungen bleiben erhalten. Dies ist eine technische Nachprüfung mit
Geräteevidenz, keine vollständige Barrierefreiheits- oder Release-Zertifizierung.

## Befunde und Nachweise

| Ursprünglicher Befund | Umsetzung | Prüfung |
|---|---|---|
| P1: Navigation während Speichern | Sichtbarer, nicht wegklickbarer Speicherzustand; Zurück, Start und Entwurfsänderungen abgesichert; späte Callbacks nach Verlassen ignoriert; Retry erhält Name, Ziel-ID und Entwurf | Verzögerter Erfolg/Fehler, Zurück, Wiederholung und Callback nach Entfernen des Editors |
| P1: Fehlender Bedienhilfen-Kontext | Namen für Slider und Switches; Werte für Slider; beschriftete Stepper-Felder und benannte Plus/Minus-Aktionen mit Button-Rolle | Semantik-Abfragen und Bedienung der Stepper; TalkBack-Hörprüfung bleibt offen |
| P2: Synchrone Audio-Vorbereitung | `AsyncUriPlayer` mit `prepareAsync`, zentralem einmaligem Freigeben bei Ende, Fehler oder Abbruch; `stopAll`/`release` stoppen eigene Dateien; bei Lesefehler wird der Übungsname gesprochen | Gültige, beschädigte und fehlende Datei; Abbruch während Vorbereitung und Wiedergabe; danach erneute erfolgreiche Wiedergabe |
| P2: Vollständiger Aufbau der Übungsliste | LazyColumn mit stabilen IDs, gespeichertem Such-/Filterzustand und Listenposition | 300 Übungen: zunächst 4 statt 300 Namen aufgebaut; letzter Eintrag erreichbar |
| P2: Kleine Fenster/große Schrift | Stepper stapeln Titel und Eingabe; Filter und Kategorien umbrechen; Home-Kennzahlen und Bibliotheksaktionen umbrechen; Mindesthöhen; Trainingsaktionen bei großer Schrift untereinander; Sekundenwert einzeilig | Gerätetests mit 280/320 dp, Schriftfaktor 2.0 und einem 320 × 360-dp-Fenster; Screenshots visuell geprüft |
| P3: Verteilte Weiß-/Grautöne | Gemeinsame Rollen für Karten, Kontrollflächen, Konturen, Primär-/Sekundärtext in den überarbeiteten Ansichten; Brandverlauf erhalten | Build, Lint und Vergleich normaler sowie großer Darstellung |

## Messung der Bibliothek

Eine lokale Debug-Einzelmessung mit 300 Übungen auf demselben A001/Android 16:
vorher 300 komponierte Namen und 4304 ms bis zum ersten Idle-Zustand;
nachher 4 Namen und 383 ms. Der belastbare strukturelle Befund ist die Begrenzung
auf sichtbare Einträge. Die Zeiten sind keine wiederholte Kaltstart-Benchmarkreihe
und belegen keine allgemeine Geschwindigkeitsgarantie.

## Vorsichtige Neubewertung

| Dimension | Vorher | Jetzt |
|---|---:|---:|
| Barrierefreiheit | 2/4 | 3/4 |
| Performance | 2/4 | 3/4 |
| Darstellung/Theming | 3/4 | 3/4 |
| Plattformkonformität | 3/4 | 3/4 |
| Anpassungsfähigkeit | 2/4 | 3/4 |
| **Gesamt** | **12/20** | **15/20** |

Die sechs konkreten Codebefunde sind geschlossen. Die verbleibenden Punkte sind
praktische Abnahmegrenzen: vollständiger TalkBack-Durchlauf, echte Handschuhe,
Musik bei gesperrtem Bildschirm, reale Mikrofon-/Cloud-Dateianbieter sowie
Rotation/Prozessneustart. Ein physisches Tablet oder Foldable wurde nicht geprüft.
Details und Release-Schritte stehen in [NEXT_STEPS.md](NEXT_STEPS.md), technische
Ergebnisse in [VALIDATION.md](VALIDATION.md).
