# Haulauf

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Bestehende native Android-App mit Kotlin und Jetpack Compose (Material 3), lokaler Speicherung über DataStore, Android-Sprachsynthese und eigenen Audioaufnahmen. Mindestversion im bestehenden Projekt: Android 7 / API 24.

## Projektquelle

Repository: https://github.com/schnitzltoast95/Haulauf-Trainer-App

Die aktive Arbeitskopie liegt in diesem Repository. Der zuvor leere übergeordnete Ordner war nicht die App-Codebasis. Ausgangspunkt dieser Überarbeitung ist Commit `47cde57` aus der bestehenden lokalen Arbeitskopie.

## Users

Menschen, die historisches Fechten betreiben und Haue und Huten alleine üben möchten. Während des Trainings tragen sie gegebenenfalls dicke Handschuhe.

## Product Purpose

Haulauf übernimmt beim Solotraining die Ansagen eines Trainers: Die App ruft zufällige Haue und Huten auf, die der Trainierende ausführen beziehungsweise einnehmen soll. Nutzer sollen ihre Trainingseinheiten konfigurieren und selbstständig starten und stoppen können.

Erfolg bedeutet, eine konfigurierte Einheit anhand hörbarer Ansagen absolvieren zu können, ohne ständig auf das Display sehen zu müssen.

## Operating Context

- Solotraining im historischen Fechten.
- Zufällige gesprochene Ansagen geben die nächste Aktion vor.
- Start und Stopp müssen mit dicken Handschuhen bedienbar sein.
- Die Ansagen sollen parallel zu laufender Musik hörbar sein.
- Das Training muss bei gesperrtem Bildschirm weiterlaufen.

## Capabilities and Constraints

### Bestätigte Anforderungen

- Konfigurierbare Trainingseinheiten.
- Zufällige Auswahl und Ansage von Hauen und Huten.
- Große, leicht erreichbare Start- und Stoppflächen für die Bedienung mit Handschuhen.
- Audioausgabe und Trainingsablauf bei gesperrtem Bildschirm.
- Hörbare Ansagen während der Musikwiedergabe.
- Native Android-App.

### Bestätigter Gesamtentwurf

- Häufige Einstellungen direkt, seltene Möglichkeiten unter einem aufklappbaren Bereich „Erweitert“.
- Bestehende 19 Standardübungen in den Kategorien Haue, Huten und Stiche erhalten; eigene Übungen können hinzugefügt werden.
- Bestehendes Rundensystem, Ansagen pro Runde, feste oder zufällige Abstände und Rundenpausen beibehalten.
- Countdown ist einstellbar und bleibt auch beim Fortsetzen erhalten.
- Im laufenden Training ist Pause die dominante, große Aktion. Fortsetzen und Beenden folgen im pausierten Zustand.
- Die nächste Einheit, gespeicherte Trainingsvorlagen und die Übungsbibliothek sind getrennte Konzepte.
- Gespeicherte Trainings können gestartet, bearbeitet, kopiert, umbenannt und gelöscht werden. Änderungen benötigen explizites Speichern; ein einmaliger Start verändert die Vorlage nicht.
- Übungsdetails, Audio und Bild werden als gemeinsamer Entwurf bearbeitet und gemeinsam gespeichert oder verworfen. Neue eigene Übungen benötigen zunächst Name und Kategorie.
- Eigene Anpassungen an Standardübungen sind wiederherstellbar; historische Quellen und die Kennzeichnung moderner Stichübungen bleiben erhalten.
- Vor dem Löschen eigener Übungen werden betroffene Trainings genannt; Verweise werden bereinigt. Leere Trainings bleiben erhalten und bieten eine Korrekturmöglichkeit.
- Sprache, Stimme und gemeinsame Audiopegel gehören zu den App-Einstellungen; Endton, Metronom und Countdown zum Training. Alte gespeicherte Pegel dürfen durch die Umstellung nicht stillschweigend verloren gehen.

### Weiterhin offene Nachweise

- Tatsächliche Touch-Erkennung mit den verwendeten dicken Handschuhen. Große Flächen allein garantieren sie nicht.
- Verhalten mit verschiedenen Musik-Apps und Android-Herstellern bei gesperrtem Bildschirm muss praktisch geprüft werden; die Anforderungen bleiben verbindlich.
- Verteilung einer produktiven Release-Version.

## Brand Commitments

Projektname: Haulauf. Weitere Marken- oder Gestaltungsvorgaben sind noch nicht festgelegt.

## Evidence on Hand

Vorhandene Android-Codebasis mit historischen Beschreibungen, Bildern, Quellenhinweisen, Audiofunktionen und Tests. Die historische Einordnung ist in `HISTORICAL_SOURCES.md` dokumentiert. Neue Trainingsabläufe wurden vom Nutzer im Shape-Dialog bestätigt. Prüfberichte und Geräteaufnahmen der Umsetzung liegen in der Arbeitskopie unter `.impeccable/review/`.

## Begriffe in der Oberfläche

- **Übung / Exercise:** ein auswählbarer Hau, eine Hut oder ein Stich; die Bibliothek heißt „Übungen“ / „Exercises“.
- **Ansage / Call:** ein einzelner Aufruf während des Trainings. „Ansagen pro Runde“ und „Ansagen insgesamt“ zählen Aufrufe, nicht verschiedene Übungen.
- **Training / Training:** eine konfigurierbare Einheit; gespeicherte Einheiten stehen unter „Trainings“. „Nächstes Training“ bezeichnet die auf der Startseite bereitstehende Einheit.
- **Anhören / Listen:** Vorschau einer Ansage. **Training beenden / End training:** die laufende Einheit verlassen; **Pause / Pause** unterbricht sie zum späteren Fortsetzen.

## Product Principles

- Training soll sich über hörbare Ansagen steuern lassen, ohne dauernden Blick auf das Display.
- Die Bedienung muss die tatsächliche Trainingssituation mit dicken Handschuhen berücksichtigen.
- Bildschirmsperre und parallele Musik sind Kernbedingungen des Trainingsbetriebs.
- Nutzer sollen ihre Trainingseinheiten an ihre Übungen anpassen können; genaue Optionen bleiben bis zur Abstimmung offen.

## Accessibility & Inclusion

Besonders große und eindeutig bedienbare Start-/Stoppflächen sind eine bestätigte Anforderung. Die praktische Bedienbarkeit mit den verwendeten Handschuhen sowie die Verständlichkeit der Ansagen während Musik müssen auf echter Hardware überprüft werden. Weitere nutzerspezifische Anforderungen sind noch nicht erhoben.
