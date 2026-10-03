# Migration von xkcdViewer zu Kotlin Multiplatform

## Status und Ziel

Der Benutzer hat den folgenden Architekturrahmen im Gespräch freigegeben. Dieses Dokument konkretisiert ihn und benötigt vor der Implementierungsplanung eine eigene Freigabe.

Flutter wird im bestehenden Repository durch Kotlin Multiplatform mit Compose Multiplatform ersetzt. Zunächst wird nur Android als ausführbare App fertiggestellt. Die Oberfläche und die fachliche Logik werden gemeinsam implementiert, damit später iOS ohne zweite Oberfläche ergänzt werden kann.

Die Beiträge der bisherigen Mitwirkenden bleiben erhalten. Übersetzungen, Contributor-Daten, Credits, Assets und Git-Historie werden nicht durch einen Projektneustart ersetzt.

## Umfang

Die Android-Version übernimmt die bestehenden Funktionen:

- Neuesten, vorherigen, nächsten und zufälligen Comic anzeigen.
- Einen Comic anhand seiner Nummer aufrufen. Keine Volltextsuche.
- Comic-Bilder zoomen und verschieben sowie den Alt-Text per Geste anzeigen.
- Favoriten hinzufügen, anzeigen, öffnen, einzeln entfernen und vollständig löschen.
- Comic-Links teilen, Bilder speichern und die Erklärung auf explainxkcd öffnen.
- Helles und dunkles Theme, Akzentfarbe und Datensparmodus einstellen.
- Bildcache anzeigen und löschen.
- Informationen zur App, Mitwirkende und Lizenzhinweise anzeigen.

Nicht zum Umfang gehören ein größeres Redesign, neue Produktfunktionen, ein vollständiger Offline-Katalog, Synchronisierung, Desktop, Web und eine ausführbare iOS-Version. Vorhandene Übersetzungsschlüssel für nicht implementierte Funktionen bedeuten keine Erweiterung des Umfangs.

## Erhaltung und Update-Kompatibilität

### Repository und Dateien

- Die Git-Historie wird nicht umgeschrieben.
- `assets/locale/*.json` bleibt die bearbeitbare Quelle aller vorhandenen Übersetzungen. Dateinamen, Schlüssel und bestehende Texte bleiben unverändert.
- Alle neun vorhandenen Locale-Dateien bleiben erhalten, einschließlich der bisher nicht aktivierten Sprachen. Die neue Ressourcenanbindung berücksichtigt alle vorhandenen Locale-Dateien.
- `assets/contributors.json`, `assets/fonts/`, vorhandene Launcher-Icons und weitere verwendete Assets bleiben erhalten. Notwendige Build-Kopien sind zulässig, die Originale werden nicht verschoben.
- `LICENSE`, `CODE_OF_CONDUCT.md`, `PRIVACY_POLICY.md` und Contributor-Credits bleiben erhalten. Technisch notwendige Änderungen an README und Build-Dokumentation sind zulässig.
- Der bestehende Text `built_by` mit Flutter-Nennung bleibt vorerst unverändert. Eine spätere redaktionelle Änderung benötigt eine eigene Entscheidung.
- Dart-Quellpfade können nicht als Kotlin-Quellstruktur erhalten bleiben. Technische Build- und Quellpfade dürfen gezielt geändert werden; Übersetzungs- und Contributor-Pfade nicht.

### App-Identität und Signierung

- Android behält `applicationId` und Namespace `de.stoupas.xkcd` sowie den Namen xkcdViewer.
- Die bestehende iOS-Bundle-ID `de.stoupas.xkcd` wird für die spätere Migration dokumentiert und nicht geändert.
- Android-Paketpfad, Manifest und Launcher-Ressourcen bleiben unter `android/app/`, soweit der moderne Build dies erlaubt.
- Eine neue veröffentlichte Version benötigt einen höheren `versionCode` als die tatsächlich zuletzt veröffentlichte Version. Der Repository-Wert 13 allein ist kein Beleg für diese Version.
- Vorhandene private Signing-Dateien bleiben an ihren bisherigen Orten und außerhalb von Git. `android/key.properties` kann weiterhin als lokale Konfiguration verwendet werden.
- Die im alten Build konfigurierte Debug-Signatur für Release wird nicht als belegte Veröffentlichungssignatur behandelt.
- Ein Release-Build darf bei fehlender Release-Konfiguration nicht still auf Debug-Signing zurückfallen.
- Kompatibilität wird gegen den tatsächlichen Distributionsweg geprüft. App-ID allein genügt nicht; die erforderliche Signatur beziehungsweise Signatur-Lineage muss passen.

### Lokale Nutzerdaten

Die Android-App importiert die bisherigen Favoriten und Einstellungen vor dem ersten schreibenden Zugriff auf den neuen Speicher.

Bekannte fachliche Schlüssel sind `favorites`, `accentColor` und `data_saver`. Die Theme-Einstellung wird vom damaligen Plugin `dynamic_theme` gespeichert und muss anhand dessen konkreter Implementierung ermittelt werden.

Der Import liest das tatsächliche Speicherformat der im Projekt gebundenen Flutter-Plugins. Datei, Präfixe und Listenserialisierung werden nicht aus den fachlichen Schlüsseln geraten. Er wird mit realistischen Legacy-Dateien getestet und anschließend über eine alte App-Installation geprüft.

Der Import ist wiederholbar sicher: Ein Abschlussmarker wird erst nach erfolgreicher Übernahme gesetzt. Bei einem Wiederanlauf entstehen keine doppelten Favoriten. Bestehende neue Einstellungen werden nicht durch einen erneuten Legacy-Import überschrieben. Fehlerhafte Einzelwerte werden übersprungen oder mit dokumentierten Standardwerten ersetzt; gültige Favoriten werden nicht verworfen. Alte Daten bleiben mindestens bis zum bestätigten erfolgreichen Import erhalten.

Der alte Bildcache ist nicht Teil der Nutzerdatenübernahme und darf neu aufgebaut werden. Bereits außerhalb des App-Speichers gespeicherte Bilder werden nicht gelöscht.

## Projektstruktur und Stack

```text
settings.gradle.kts         Gemeinsame Gradle-Projektdefinition
build.gradle.kts            Zentrale Plugin-Konfiguration
gradle/                     Wrapper und Versionskatalog
android/app/                Android-App-Modul
shared/                     KMP-Modul
  src/commonMain/           UI, ViewModels, Modelle und Repositories
  src/commonTest/           Plattformunabhängige Tests
  src/androidMain/          Android-Implementierungen der Schnittstellen
assets/                     Unveränderte Ressourcenquellen
ios/                        Historische Dateien für die spätere Umstellung
```

Der Gradle-Einstieg liegt künftig im Repository-Root. Das Android-Modul bleibt physisch unter `android/app/`. Die bestehenden Flutter-spezifischen Gradle-Dateien werden ersetzt.

Vorgesehener Stack:

| Aufgabe | Auswahl |
| --- | --- |
| Gemeinsame UI | Compose Multiplatform und Material 3 |
| Zustand | Multiplatform ViewModels, Coroutines, StateFlow |
| HTTP und JSON | Ktor Client, kotlinx.serialization |
| Bilder | Coil 3 mit KMP-kompatibler Netzwerkanbindung |
| Navigation | Multiplatform Navigation Compose |
| Einstellungen | Multiplatform Settings hinter einem Repository |
| Ressourcen | Compose Multiplatform Resources, aus bestehenden Quellen erzeugt |
| Tests | kotlin.test, Ktor MockEngine, Android-UI- und Migrationstests |

Es gibt zunächst keine Datenbank und keinen DI-Framework-Zwang. Abhängigkeiten werden über einen kleinen zentralen Einstieg manuell übergeben.

Konkrete Versionen werden im Implementierungsplan als kompatibler stabiler Satz festgelegt. Keine EAP-Abhängigkeiten als Standard. Der Build muss ohne Flutter-SDK funktionieren.

Androids bisheriges Minimum API 16 entfällt. Das neue Minimum ist mindestens API 21 und wird anhand des gesamten gewählten Dependency-Satzes geprüft. Compile- und Target-SDK werden auf einen dann aktuellen stabilen und für die Veröffentlichung geeigneten Stand gesetzt.

## Verantwortlichkeiten und Datenfluss

### Gemeinsamer Code

- `ComicApi`: lädt xkcd-JSON und bildet HTTP- beziehungsweise Parse-Fehler ab. Keine UI-Abhängigkeit.
- `ComicRepository`: verwaltet Comic-Aufrufe, zuletzt bekannte höchste Nummer und einen begrenzten Metadaten-Cache.
- `SettingsRepository`: stellt Einstellungen und Favoriten als beobachtbaren Zustand bereit.
- Screen-ViewModels: verarbeiten Benutzeraktionen und liefern explizite Lade-, Erfolgs-, Leer- und Fehlerzustände.
- Composables: rendern den Zustand und melden Aktionen zurück. Sie verwenden keine Android-Klassen direkt.
- Ressourcenadapter: liefert lokalisierte Texte, Fonts und Contributor-Daten aus den erhaltenen Assets.

Eine Benutzeraktion geht vom Screen zum ViewModel und von dort zum Repository. Das Ergebnis aktualisiert StateFlow. Die UI startet keine HTTP-Aufrufe während ihrer Darstellung. Mehrere schnelle Comic-Aufrufe dürfen nicht dazu führen, dass ein älteres Ergebnis eine neuere Auswahl überschreibt.

Favoriten speichern weiterhin Comic-IDs, keinen vollständigen Comic-Katalog. Ihr Metadatenabruf nutzt Cache und begrenzte Parallelität. Ein fehlerhafter Abruf darf die übrigen Favoriten nicht unzugänglich machen.

### Plattformschnittstellen

- `ShareService`: teilt den Comic-Link über die Systemfunktion.
- `ImageSaveService`: lädt beziehungsweise verwendet das Bild und speichert es über den passenden Android-Speicherweg.
- `BrowserService`: öffnet explainxkcd und Contributor-Links über den Plattformbrowser.
- Legacy-Import und Speicherinitialisierung: werden im Android-App-Einstieg ausgeführt.

Android implementiert diese Funktionen in `androidMain` beziehungsweise im App-Modul. Später ergänzt iOS eigene Implementierungen. Android-Context, Intent, MediaStore und andere Plattformtypen dürfen nicht in öffentliche gemeinsame Schnittstellen gelangen.

## UI und Verhalten

Die bestehenden Seiten und Bedienabläufe bleiben erkennbar. Material-3-Komponenten ersetzen Flutter-Komponenten; pixelgenaue Nachbildung ist kein Abnahmekriterium. Schrift, Credits und vorhandene Texte bleiben erhalten. Systemleisten, Insets und große Schriftgrößen werden berücksichtigt.

Die Comic-Ansicht trennt Zoom/Verschieben von der Geste für den Alt-Text. Der Alt-Text ist auch ohne präzise Wischgeste zugänglich. Breite und sehr hohe Bilder müssen benutzbar bleiben. Für Zoom wird eine KMP-fähige Lösung gewählt und früh auf Android überprüft, bevor die übrige UI darauf aufbaut.

Der Datensparmodus verwendet das Originalbild. Die vorhandene Regel für hochauflösende `_2x.png`-Bilder und ihre Ausnahmen wird übernommen. Fehlt die hochauflösende Datei, fällt der Bildlader auf die Original-URL zurück. Bilder speichern verwendet die erfolgreich aufgelöste Bildvariante.

Nummernsuche validiert die Eingabe vor dem Request. Zufallsauswahl erzeugt keine Nummer 0. Comic 404 wird als bekannter Sonderfall behandelt. Navigation an den Grenzen springt nicht unerwartet auf einen zufälligen Comic. Ladefehler zeigen einen verständlichen Zustand mit Wiederholungsmöglichkeit.

Theme- und Akzentfarben berücksichtigen Kontrast. Ein verworfener Farbdialog verändert keine gespeicherte Einstellung. Das Löschen des Bildcaches umfasst den neuen Speicher- und Diskcache; alte Cache-Zähler werden nicht nachgebildet.

## Ressourcen und Übersetzungen

Ein Gradle-Schritt liest die vorhandenen JSON-Dateien und erzeugt Compose-Ressourcen ausschließlich in generierten Build-Verzeichnissen. Er übernimmt die Werte ohne sprachliche Änderungen und behandelt XML-Sonderzeichen, Unicode, Zeilenumbrüche und Anführungszeichen korrekt.

Die Locale-Auswahl berücksichtigt die vorhandenen Sprach- und Regionskennungen. Fehlende Übersetzungen verwenden Englisch. Der ukrainische Sprachcode wird korrekt als `uk` erkannt; der alte Delegate mit `ua` wird nicht nachgebildet.

Ein automatisierter Ressourcencheck stellt sicher, dass vorhandene Schlüssel erreichbar bleiben und generierte Werte den Originalwerten entsprechen. Zusätzliche technische Fehlermeldungen werden als neue, klar abgegrenzte Ressourcen eingeführt, nicht als Änderungen bestehender Übersetzungen. Fehlende neue Übersetzungen verwenden den englischen Fallback.

Contributor-Daten und Lizenzhinweise bleiben im App-Informationsbereich erreichbar. Neue Bibliotheken erhalten zusätzlich ihre erforderlichen Lizenzhinweise; Flutter-Lizenzansichten werden nicht blind übernommen.

## Umstellung und spätere iOS-Ergänzung

Während der Umsetzung können Flutter-Referenzdateien vorübergehend im Repository bleiben. Der finale Android-Build benötigt keine Flutter-Dateien oder Flutter-Plugins. Nach überprüfter Funktionsübernahme werden ausschließlich identifizierte, nicht mehr benötigte Flutter-Dateien gezielt entfernt. Keine pauschale Bereinigung unbekannter oder lokaler Dateien.

Die alten iOS-Dateien werden in dieser Phase weder als funktionierende KMP-App ausgegeben noch ungeprüft gelöscht. Die README kennzeichnet iOS als noch nicht migriert. Später werden dort der KMP-App-Einstieg und die iOS-Plattformdienste eingerichtet. Die gemeinsame Oberfläche bleibt bestehen. iOS-Builds und Geräteprüfungen benötigen macOS und Xcode.

## Prüfungen und Abnahmekriterien

1. Android lässt sich vom Root-Projekt aus ohne Flutter-SDK bauen und installieren.
2. App-ID, Name, Launcher-Ressourcen und Credits entsprechen den Erhaltungsregeln.
3. Alle bestehenden Comic-, Favoriten-, Settings- und Informationsabläufe funktionieren auf Android.
4. Nummernsuche, Netzwerkfehler, Comic 404 und fehlende hochauflösende Bilder sind getestet.
5. Zoom, Verschieben und Alt-Text funktionieren gemeinsam, auch bei ungewöhnlichen Bildformaten.
6. Bestehende Locale-JSONs und Contributor-Daten bleiben unverändert; Ressourcenchecks decken Generierung und Fallback ab.
7. Legacy-Importtests decken gültige und beschädigte Daten, Wiederanlauf und vorhandene neue Einstellungen ab.
8. Ein Update über eine alte Installation erhält Favoriten und Einstellungen. Paketidentität und passende Signatur werden am tatsächlichen Artefakt geprüft.
9. Teilen, Bildspeichern, Browser und Cache-Löschen sind auf einem aktuellen Android-Gerät oder Emulator geprüft.
10. Ein Gerät beziehungsweise Emulator mit dem festgelegten Mindest-API-Level wird geprüft.
11. Release-Builds verwenden die bestätigte Signierung oder scheitern verständlich bei fehlender Konfiguration. Kein Debug-Fallback.
12. Der gemeinsame Code enthält keine direkten Android-UI- oder Plattformabhängigkeiten. Eine iOS-Abnahme wird nicht behauptet.

## Noch zu belegende Fakten und Freigabehürden

Diese Punkte sind keine offenen Produktentscheidungen, sondern verbindliche Prüfaufgaben für die Implementierungsplanung und Umsetzung:

- Tatsächlich veröffentlichte Android-Version, APK beziehungsweise App-Bundle und Distributionsweg identifizieren. Ohne diese Daten ist ein echtes Update nicht nachgewiesen.
- Verfügbarkeit und Kompatibilität des Release-Schlüssels prüfen, ohne Geheimnisse ins Repository oder in Diagnoseausgaben zu übernehmen. Fehlender Schlüssel blockiert die Release-Abnahme, nicht die Debug-Entwicklung.
- Speicherformat der gebundenen Versionen von `shared_preferences` und `dynamic_theme` prüfen und Legacy-Testdaten daraus ableiten.
- Dependency-Versionen, Mindest-API-Level, JDK und SDK-Voraussetzungen verifizieren und im Implementierungsplan konkret festlegen.
- Eine KMP-fähige Zoomlösung anhand der benötigten Gesten, Lizenz und Wartung auswählen.

## Ablauf und Freigaben

- [x] Bestehendes Projekt und Beiträge untersuchen.
- [x] Ziel und Erhaltungsregeln abstimmen.
- [x] Architekturrahmen für Android zuerst und gemeinsame UI freigeben lassen.
- [x] Migrationsspezifikation schreiben und intern auf Widersprüche prüfen.
- [ ] Benutzer prüft und bestätigt dieses Dokument.
- [ ] Implementierungsplan mit konkreten Versionen, Arbeitspaketen und Prüfkommandos erstellen.
- [ ] Benutzer prüft den Plan und entscheidet über die Ausführung.
- [ ] Migration implementieren und Abnahmekriterien nachweisen.

Die Freigabe des Architekturrahmens erlaubt dieses Dokument, aber noch keine Änderung am App-Code. Erst nach Dokumentfreigabe folgt die Implementierungsplanung.
