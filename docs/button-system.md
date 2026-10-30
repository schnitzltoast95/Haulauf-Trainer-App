# Button-Konzept (Issue #24)

Buttons sind nach **Bedeutung und Einsatzort** vereinheitlicht, nicht nach einem einzigen Aussehen.

| Variante | Einsatz | Darstellung |
| --- | --- | --- |
| Primär | Nächster Schritt oder Speichern einer Bearbeitung | Gefüllter Button in der Theme-Primärfarbe |
| Sekundär | Alternative Aktion, Navigation, Vorschau oder Aufklappen | Umrissener Button mit Oberflächen-Textfarbe |
| Destruktiv | Training beenden oder eine andere folgenreiche Aktion | Umrissener Button mit Fehlerfarbe; Bestätigung bei Datenverlust |
| Start | Training von Startseite oder Konfiguration beginnen | Große goldene Startfläche mit Verlauf; deaktivierter Zustand ohne Verlauf |
| Training mit Handschuhen | Pause, Fortsetzen, Neustart und Beenden während der Einheit | Mindestens 96 dp hoch, volle Breite; Bedeutung bestimmt weiterhin primär/sekundär/destruktiv |
| Textaktion | Dialogbestätigung, Abbrechen oder untergeordnete Aktion im Textfluss | Material `TextButton`; für destruktive Aktionen `AppDestructiveTextButton` mit Fehlerfarbe und korrektem deaktiviertem Zustand |
| Icon-Aktion | Zurück, Schließen, Einstellungen und Menü in App-Leisten | Material `IconButton` mit verständlicher `contentDescription` |
| Auswahl | Kategorie, Filter oder Sprache mit auswählbarem Zustand | `FilterChip` beziehungsweise eindeutig markierte Textauswahl |
| Zahlensteuerung | Plus und Minus an Eingabefeldern | Eigene 48-dp-Steuerfläche mit zugänglichem Namen |

`AppActionButton`, `AppLaunchButton` und `AppDestructiveTextButton` in `AppButtons.kt` sind die gemeinsamen Compose-Komponenten. Normale Aktionen sind mindestens 56 dp hoch; die Startaktion und ihre benachbarte Speicheraktion in der Trainingskonfiguration mindestens 88 dp. Zusammenstehende Aktionen bekommen die gleiche Mindesthöhe. Bei schmalen Flächen oder großer Schrift stehen sie untereinander, damit deutsche und englische Beschriftungen vollständig bleiben. Die App nutzt die Farben und Zustände aus `MaterialTheme`; einzelne Screens sollen keine eigenen Button-Farben, Formen oder Innenabstände setzen.

Eine große Fläche ist eine bewusste Ausnahme für die Trainingsbedienung mit Handschuhen. Ihre tatsächliche Erkennbarkeit mit den verwendeten Handschuhen muss weiterhin am Gerät geprüft werden.
