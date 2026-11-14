# Statistik und Abzeichen

## Verhalten

Die Statistikübersicht zeigt in dieser Reihenfolge die Wochenchallenge, den Trainingsüberblick und die drei am weitesten entwickelten Abzeichen. Der Trainingsüberblick enthält Übungsaufrufe, beendete Trainings, Häue insgesamt und Häue im laufenden Jahr.

Gezählt werden abgeschlossene Reaktionsfenster, auch aus abgebrochenen Trainings. Die App erkennt keine tatsächlichen Bewegungen. Die Daten werden lokal gespeichert; vor Einführung der Statistik absolvierte Trainings werden nicht nachträglich rekonstruiert.

## Abzeichen

- Vier Stufen pro Übung: 250, 1.000, 3.000 und 10.000 Aufrufe insgesamt. Verknüpfte Varianten zählen auch für ihre Grundübung.
- Rangfolge der drei Highlights: erreichte Stufen absteigend, dann Anzahl geteilt durch das nächste Stufenziel. Name und ID lösen Gleichstände deterministisch auf.
- Highlights stehen bei normaler Schrift nebeneinander. Bei großer Schrift oder wenig Breite werden zwei Spalten verwendet; die Seite bleibt scrollbar.
- Ein Highlight zeigt Wappen, Titel, Übungsname, gegebenenfalls erreichte Stufen sowie Fortschrittsbalken und „X von Y“.
- Einziger Zugang zur vollständigen Sammlung ist der zugängliche Pfeil neben „Deine Abzeichen“. Die Sammlung ist nach Übungenkategorien gegliedert. Antippen eines Abzeichens öffnet die vier Stufen in einer Detailansicht.
- Noch nicht begonnene Stufen zeigen in allen Ansichten „???“. Der erste Titel wird ab einem Aufruf sichtbar, weitere Titel nach dem ersten Aufruf oberhalb der jeweils vorigen Stufengrenze. Erreichte Titel bleiben sichtbar. Übungsnamen und Anforderungen bleiben immer lesbar. Dies gilt in beiden Sprachen.
- Die deutschen Titel beziehen sich auf Bewegung und Funktion der Techniken. Sie sind moderne Wortspiele, keine historischen Rangbezeichnungen. Alle Titel und ihre Herleitung stehen in [Abzeichentitel und Trainingsumfang](badge-names-and-thresholds.md).

## Gestaltung

Die Oberfläche verwendet das bestehende Schwarz/Gold, Material-Typografie, 16-dp-Ecken und die gemeinsamen Aktionsbuttons aus [Button-System](button-system.md). Kleine Vektorschilde zeigen das Kategoriezeichen und vier Stufenpunkte. Ab der ersten erreichten Stufe wird das Wappen goldfarben, davor bleibt es neutral. Es werden keine neuen Rastergrafiken benötigt.

Fortschrittsbalken auf Übersicht und Sammlung verwenden denselben Stil wie die Wochenchallenge, ohne separaten Endpunkt. „In Arbeit“ und der zusätzliche Sammlungsknopf entfallen.

## Prüfung

Die endgültige Oberfläche wurde in einer separaten Test-App auf Nothing A001 / Android 16 geprüft. Die drei Bildschirmtests decken Sammlung, Detailansicht, erreichte Titel, verborgene Titel und englische Beschriftungen ab. Die fünf Abzeichen-Unit-Tests prüfen Rangfolge, Varianten, Stufengrenzen und die Entdeckung der Titel. Frühere Layoutstände wurden zusätzlich mit Schriftfaktor 1,3 geprüft; der finale Stand mit normaler Schrift. Tablet-Darstellung wurde nicht auf einem Gerät geprüft.

Die separate Test-App kombiniert Statistik und Aussprachekorrektur. Ihre Paketkennung, Testdaten und Bezeichnung sind nicht Bestandteil der Produktänderung. Die ursprüngliche App-Installation wurde beim Gerätetest nicht ersetzt.
