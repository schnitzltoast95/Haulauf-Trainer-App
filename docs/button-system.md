# Button-Konzept (Issue #29)

Alle normalen Aktionen haben eine Mindesthöhe von 56 dp und eine Form mit 16 dp Ecken.
Längere Beschriftungen dürfen die Höhe vergrößern; nebeneinanderliegende Aktionen
werden auf schmalen Displays oder bei großer Schrift untereinander angeordnet.

| Element | Einsatz | Darstellung |
| --- | --- | --- |
| Primär | Die nächste Hauptaktion einer Ansicht, etwa Starten, Speichern oder Neu anlegen | Gefüllt in der Theme-Primärfarbe |
| Sekundär | Navigation, Bearbeiten, Auswahl und alternative Aktionen | Gefüllt in der erhöhten Oberflächenfarbe |
| Destruktiv | Beenden oder Löschen | Umrandet in der Fehlerfarbe; bei Datenverlust mit Bestätigung |
| Textaktion | Untergeordnete Aktion im Textfluss oder Dialog | Material `TextButton` |
| Icon-Aktion | Zurück, Schließen, Hinzufügen, Einstellungen, Vorschau und Menüs | Gemeinsamer `AppIconButton`, 48 dp Touchfläche ohne dauerhaften Hintergrund, zugänglicher Name und Material-Druckzustand |
| Zahlensteuerung | Plus und Minus an Zahlenfeldern | 56 dp, gleiche erhöhte Oberflächenfarbe und Eckenform; Eingabefeld mindestens 56 dp |
| Auswahl | Kategorie, Filter und Sprache | `FilterChip` oder klar markierte Textauswahl mit sichtbarem Zustand |

`AppActionButton` ist die gemeinsame Basis. `AppLaunchButton` nutzt dieselbe primäre
Darstellung und 56 dp Höhe. Nur die Steuerflächen **während einer laufenden Einheit**
nutzen `ActionSize.Glove` mit mindestens 96 dp und voller Breite. Dies gilt für Pause,
Fortsetzen, Neustart und Beenden. Ihre Bedeutung bestimmt weiterhin die Farbrolle.
Die Bedienbarkeit mit den tatsächlich verwendeten Fechthandschuhen ist am Gerät zu prüfen.

Buttons übernehmen Farben und Zustände aus `MaterialTheme`. Einzelne Screens setzen
keine eigenen Höhen, Formen oder Farben für dieselbe Aktionsrolle.
