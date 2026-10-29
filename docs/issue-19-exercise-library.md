# Issue #19: Übungsbibliothek

## Verhalten

Die Bibliothek „Übungen“ bietet eine Suche nach Übungsnamen (ohne Beachtung der Groß-/Kleinschreibung) und Filter für Haue, Huten und Stiche. Karten zeigen Name, Kategorie und ein verfügbares Vorschaubild: bei Standardübungen die historische Illustration, sonst das erste persönliche Bild. Persönliche Ergänzungen werden gekennzeichnet. Ein Kartentipp öffnet zunächst die lesende Detailansicht; die Audioaktion spielt die Ansage ab.

Aus einem Trainingsentwurf öffnet „Übungen auswählen“ dieselbe Bibliothek mit Checkboxen. Checkboxen ändern ausschließlich die vorläufige Auswahl; ein Kartentipp öffnet weiterhin Details. „Alle auswählen“ beziehungsweise „Keine auswählen“ betrifft die aktuell sichtbaren Treffer. Der Übernehmen-Button nennt die Anzahl ausgewählter Übungen. Übernehmen aktualisiert den noch ungespeicherten Trainingsentwurf und entfernt Prioritäten abgewählter Übungen. Zurück verwirft die Auswahländerungen. Eine gespeicherte Trainingsvorlage ändert sich erst durch explizites Speichern des Trainings. In dieser Auswahlansicht werden keine Übungen erstellt oder bearbeitet.

Standardübungen behalten ihre ursprünglichen Beschreibungen, Illustrationen, Quellen und historischen Einordnungen einschließlich der Kennzeichnung moderner Stichübungen. Die Detailansicht trennt diese Inhalte von „Meine Ergänzungen“ und bietet aufklappbare Quellen. „Meine Ergänzungen bearbeiten“ öffnet einen eigenen Editor für persönliche Notizen, mehrere Bilder/GIFs/Videos und eine persönliche Audioansage. Eigene Übungen besitzen zusätzlich bearbeitbare Namen, Kategorien und Beschreibungen; beim Anlegen werden zunächst Name und Kategorie erfasst.

Notizen, Medien und Audio werden gemeinsam gespeichert oder verworfen. Ein fehlgeschlagener Speichervorgang lässt den Entwurf im Editor. Audio kann importiert oder aufgenommen und vorgehört werden. Beim Löschen einer eigenen Übung nennt die Bestätigung betroffene Trainings; gespeicherte Verweise werden bereinigt. Leere Trainings bleiben zur Korrektur bestehen. Erfolgreiches Löschen führt zurück zur Bibliothek.

Die Erweiterung verwendet weiterhin die bestehende dunkle/goldene Material-3-Gestaltung, Typografie, Formen und semantischen Farben. Sie führt keine neue visuelle Identität ein. Die kompakte Bibliothek hat eine einzelne Aktion zum Anlegen eigener Übungen; die Auswahlansicht besitzt stattdessen ihre Übernehmen-Aktion. Deutsche und englische Anzahltexte unterscheiden Einzahl und Mehrzahl.

## Datenübernahme und Dateiverwaltung

- `Move.withPersonalContent` setzt bei Standardübungen die mitgelieferten Originaldaten als Grundlage ein. Frühere abweichende Beschreibungen werden als persönliche Notizen dargestellt; eine unveränderte Originalbeschreibung erzeugt keine Notiz. Frühere Bilder werden persönliche Anhänge. Eigene Beschreibungen bleiben editierbar.
- Das vorhandene JSON wird um `notes` und eine geordnete `media`-Liste mit Pfad und Typ ergänzt. Fehlendes `notes` kennzeichnet ältere Daten; explizit leere neue Ergänzungen verhindern, dass gelöschte Altdaten wieder erscheinen. Die Übernahme erfolgt beim Zusammenführen der gespeicherten Daten; Speichern schreibt das neue Format.
- Neue Medien- und Audioimporte werden in das app-private Verzeichnis `filesDir/exercise-media` kopiert. Die Obergrenze beträgt pro Datei 200 × 1024 × 1024 Byte (in der Oberfläche „200 MB“). Leere, unlesbare oder nicht als passendes Bild/Video/Audio validierbare Dateien werden abgelehnt; fehlgeschlagene Teilkopien werden entfernt. Bei einer Mehrfachauswahl bleiben zuvor erfolgreich importierte Dateien im Entwurf, wenn ein späterer Import scheitert.
- Die neuen Kopien benötigen nach dem Import keinen Zugriff mehr auf das Quelldokument. Bereits gespeicherte alte externe Pfade werden dadurch nicht automatisch kopiert oder repariert.
- Beim Speichern oder Löschen werden ersetzte beziehungsweise entfernte Medien und Audioaufnahmen nur gelöscht, wenn sie nicht mehr in den gespeicherten Medien-/Audioeinträgen referenziert sind und ihr kanonischer Elternpfad das eigene `exercise-media`-Verzeichnis ist. Externe Altdateien werden nicht gelöscht. Ungespeicherte Importdateien werden beim Verlassen des Editors bereinigt; Konfigurationswechsel sind davon ausgenommen.

## Medien und Grenzen

Bilder und animierte GIFs lassen sich in der Detailansicht und vergrößert betrachten. Coil verwendet ab API 28 `ImageDecoderDecoder`, darunter `GifDecoder`. Videos öffnen in einer eigenen Ansicht mit Android-`VideoView` und `MediaController`; beim Hintergrundwechsel pausiert die Wiedergabe, beim Schließen wird sie beendet. Nicht verfügbare Bilder oder auf dem Gerät nicht abspielbare Videos zeigen einen Fehlerhinweis.

Die Wiedergabe hängt von den auf dem Android-Gerät unterstützten Formaten und Codecs ab. Ein erfolgreicher Metadatencheck beim Import garantiert nicht die Wiedergabe jedes Videos auf jedem Gerät. Für video-only eigene Übungen wird kein Videoframe als Bibliotheksvorschaubild erzeugt. Es gibt keine zugesicherte Wiederherstellung ungespeicherter Entwürfe oder Bereinigung verwaister Dateien nach einem abrupten Prozessabbruch. Die 200-MB-Grenze gilt pro Importdatei, nicht als Gesamtkontingent.

## Prüfung und Evidenz

Stand dieses Dokumentationspasses: Die XML-Berichte unter `app/build/test-results/testDebugUnitTest/` wurden gelesen. `ExerciseContentTest` weist vier Tests und `TrainingEngineTest` drei Tests aus, jeweils ohne Fehler, Fehlschläge oder übersprungene Tests (Berichtszeitstempel 2026-10-29, 23:14 UTC). Die vier Inhaltstests prüfen Altdatenübernahme, unveränderte Originaldaten, explizit leere Ergänzungen und eigene Beschreibungen mit geordneten Anhängen.

Die neuen Gerätetests in `ExerciseLibraryTest.kt` prüfen Suche/Kategoriefilter, getrennte Karten- und Checkboxaktionen, Schutz der Standardbeschreibung, Speichern/Verwerfen von Notizen, Übernehmen/Verwerfen der Trainingsauswahl, Dateikopien nach Entfernung der Quelle, Ablehnung ungültiger Dateien, Audio-Dateibereinigung und die Rückkehr zur Bibliothek nach dem Löschen. Nach den Reviewkorrekturen bestanden auf dem angeschlossenen Android-16-Gerät alle 20 Tests aus `ExerciseLibraryTest`, `LibraryPersistenceTest`, `LibraryFlowTest` und `HistoricalContentInstrumentedTest` (30.10.2026). Damit sind insbesondere Löschnavigation, Audio-Dateibereinigung und die bestehenden historischen Quellenlinks geprüft.

Vom Hauptlauf gemeldete manuelle Evidenz: Ein importiertes MP4 wurde abgespielt; wechselnde schwarze/goldene GIF-Frames bestätigten die Animation nach einem App-Neustart. Geräteaufnahmen liegen als ignorierte lokale Evidenz unter `.impeccable/review/`. Diese manuellen Prüfungen wurden im Dokumentationspass nicht erneut ausgeführt.

Weitere Geräteabdeckung: Zusätzlich können reale Medien unterschiedlicher Provider/Codecs, die Dateigrößengrenze, Hintergrundwechsel während der Videowiedergabe und die Verständlichkeit der Auswahl-/Entwurfsaktionen auf den Zielgeräten geprüft werden. Die weiterhin offenen Produktnachweise zu Handschuhbedienung, paralleler Musik und gesperrtem Bildschirm aus `PRODUCT.md` bleiben bestehen.
