# Übergabeprotokoll für die KMP-Implementierung

Stand: 2026-10-03. Repository: `C:\DEV\workspaces\xkcdViewer`.

## Auftrag und aktueller Stand

xkcdViewer ist eine ältere Flutter-App. Sie soll im bestehenden Repository auf Kotlin Multiplatform umgestellt werden. Android wird zuerst fertiggestellt, iOS folgt später. UI und fachliche Logik sollen bereits gemeinsam in Compose Multiplatform entstehen.

Die bisherige Sitzung hat das Projekt untersucht, den Architekturrahmen abgestimmt und die Implementierung geplant. Es wurde noch kein App-Code geändert, kein KMP-Projekt angelegt und kein Flutter-Code entfernt. Die alte App wurde nicht gebaut oder auf einem Gerät ausgeführt.

Der Benutzer hat zuletzt ausdrücklich ein Übergabeprotokoll statt einer Ausführung in dieser Sitzung verlangt. Dieses Dokument ist keine Startfreigabe für die Implementierung.

### Freigaben

| Gegenstand | Status |
| --- | --- |
| Migration im bestehenden Repository | Bestätigt |
| Android zuerst, gemeinsame UI, iOS später | Bestätigt |
| Erhaltungsregeln und Architekturrahmen | Bestätigt |
| Migrationsspezifikation | Bestätigt |
| Implementierungsplan | Erstellt, nicht ausdrücklich bestätigt |
| Ausführungsmethode und Delegation | Nicht gewählt |
| Implementierung | Nicht begonnen |

Die Antwort „Nein, schreibe ein Übergabeprotokoll für die Implementierung“ ist nicht als Verwerfung der bereits bestätigten Architektur zu verstehen. Sie ersetzt die Aufforderung zur Wahl einer Ausführungsmethode durch einen Dokumentationsauftrag. Keine Zustimmung zum Implementierungsplan oder zu Subagents daraus ableiten.

## Verbindliche Dokumente und Git-Stand

Vor einer späteren Implementierung beide Dokumente vollständig lesen:

1. [Freigegebene Migrationsspezifikation](../superpowers/specs/2026-10-03-kmp-migration-design.md)
2. [Implementierungsplan mit zehn Aufgaben](../superpowers/plans/2026-10-03-kmp-migration.md)

Die Spezifikation definiert den Umfang und die Erhaltungsregeln. Der Plan konkretisiert Dateipfade, Schnittstellen, Versionsvorschläge und Prüfungen. Bei Widersprüchen hat die Spezifikation Vorrang. Dieses Protokoll fasst den Stand zusammen und erweitert den Umfang nicht.

Vorhandene Dokumentationscommits:

- `2ccfc83`: `docs: define Android-first KMP migration design`
- `542109f`: `docs: plan Android-first KMP migration`

Vor Erstellung dieses Protokolls war der Arbeitsbaum sauber und der aktuelle Branch `master`. Bei Übernahme erneut `git status --short` und `git log -5 --oneline` prüfen. Nicht annehmen, dass spätere Änderungen ebenfalls von dieser Sitzung stammen.

Der Erstellungs-Checkbox im Ablaufteil der Spezifikation ist noch offen, obwohl der Plan inzwischen vorliegt. Das ist ein Dokumentationsnachlauf, kein fehlendes Planartefakt. Die Planfreigabe und Ausführung bleiben tatsächlich offen.

## Unveränderliche Vorgaben

- Kein neues Produktrepository. Bestehende Git-Historie nicht umschreiben.
- `assets/locale/*.json` bleibt die Quelle der Übersetzungen. Dateinamen, Schlüssel und vorhandene Texte beibehalten.
- Alle neun Übersetzungsdateien mit jeweils 32 Schlüsseln erhalten: Deutsch, Englisch, Spanisch, Französisch, Ungarisch, Japanisch, Portugiesisch, Russisch und Ukrainisch.
- `assets/contributors.json`, Credits, Fonts, Launcher-Icons und Lizenzhinweise erhalten. Keine Contributor-Liste neu ausdenken oder kürzen.
- App-Name `xkcdViewer`, Android-App-ID und Namespace `de.stoupas.xkcd` beibehalten. Bestehende iOS-Bundle-ID ist ebenfalls `de.stoupas.xkcd`.
- Bestehende Favoriten, Theme, Akzentfarbe und Datensparmodus über das Update übernehmen.
- Private Signing-Dateien nicht verschieben, überschreiben oder committen. Keine neuen Release-Schlüssel als Ersatz erzeugen.
- Fehlendes Release-Signing darf nicht still zu Debug-Signing führen.
- Die Texte `built_by` nennen noch Flutter. Das ist bekannt und bleibt zunächst unverändert; sprachliche Anpassung nur nach eigener Entscheidung.
- Kein großes Redesign, keine Volltextsuche, kein Login, keine Synchronisierung und kein vollständiger Offline-Katalog.
- `ios/` zunächst als historische Referenz behalten und als nicht migriert kennzeichnen. Keine funktionierende iOS-App behaupten.
- Flutter erst nach geprüfter Android-Funktionsübernahme gezielt entfernen. Keine pauschalen Verzeichnislöschungen.

## Ausgangscode und Funktionen

Das Projekt umfasst 17 Dart-Dateien mit rund 1.500 nichtleeren Zeilen. Es gibt kein eigenes Backend und keine Anmeldung. Die wichtigsten Referenzen sind:

| Datei | Bedeutung |
| --- | --- |
| `lib/main.dart` | App-Einstieg, Routes, Theme und unterstützte Sprachen |
| `lib/api/comic_api_client.dart` | JSON-Endpunkte, Comic-Auswahl und Metadaten-Cache |
| `lib/models/comic_model.dart` | Favoriten, Teilen, Speichern und Ladezustand |
| `lib/widgets/comic_view.dart` | Zoom, Alt-Text-Geste und hochauflösende Bilder |
| `lib/widgets/comic_search_delegate.dart` | Suche ausschließlich nach Comic-Nummer |
| `lib/pages/home_page.dart` | Hauptbedienung und Menüs |
| `lib/pages/favorites_page.dart` | Favoritenliste und Detailansicht |
| `lib/pages/settings_page.dart` | Einstellungen, Cache und App-Informationen |
| `lib/pages/contributors_page.dart` | Mitwirkende und Profil-Links |
| `lib/utils/app_colors.dart` | Farben, Schrift und Akzentfarbe |
| `assets/locale/` | Übersetzungen, unverändert zu erhalten |
| `android/app/build.gradle` | Alte App-ID, Version und Signing-Konfiguration |

Bestehende Unit-/Integrationstests sichern die App nicht ab. Der Widget-Test ist auskommentiert, der Integrationstest prüft noch das Flutter-Counter-Beispiel. Neue Tests müssen tatsächliches App-Verhalten prüfen.

Bekannte Altverhalten nicht blind übernehmen: Zufall kann Nummer 0 auswählen, Ladefehler sind unvollständig behandelt, Grenzen können auf Zufall springen und ukrainische Locale wird im Delegate fälschlich als `ua` geprüft. Die Spezifikation erlaubt gezielte Korrekturen dieser Fälle.

## Zielstruktur und geplanter Stack

```text
settings.gradle.kts         Root-Einstieg für Gradle
build.gradle.kts            Zentrale Plugin-Konfiguration
gradle/                     Wrapper und Versionskatalog
android/app/                Android-App-Modul :app
shared/                     KMP-Modul :shared mit gemeinsamer UI und Logik
build-logic/                Ressourcengenerator und Release-Prüfungen
assets/                     Vorhandene Ressourcenquellen
ios/                        Später zu migrierende Plattformdateien
```

Common-Code enthält keine Android-Context-, Intent-, Uri- oder MediaStore-Typen. Teilen, Browser und Bildspeichern erhalten schmale gemeinsame Schnittstellen mit zunächst ausschließlich Android-Implementierungen. Ein JVM-Testziel bedeutet keine Desktop-App.

Im Plan vorgeschlagen:

- Kotlin und Compose-Compiler 2.4.20, Compose Multiplatform 1.12.1, Material 3.
- AGP 9.3.3, Gradle 9.5.0, Build-JDK 21, JVM-Bytecode 17.
- minSdk 23, compileSdk und targetSdk 37, Build Tools 36.0.0.
- Ktor 3.6.0, Coroutines 1.11.0, Serialization 1.11.0.
- Coil 3.6.3 mit Ktor3 und ZoomImage 1.7.0.
- Gemeinsame ViewModels/Lifecycle, Navigation Compose und Multiplatform Settings.
- AboutLibraries für neue Bibliothekslizenzen.

Die vollständigen Versionsangaben stehen im Plan. Veröffentlichte Artefakte wurden recherchiert, der gemeinsame Versionssatz wurde noch nicht gebaut. Die Kotlin-Tabelle nennt AGP bis 9.3.1; der gewählte Patch 9.3.3 benötigt den Build-Nachweis. Bei Konflikten keine Preview-Abhängigkeiten oder stillen Architekturwechsel einführen.

Der alte Android-Support ab API 16 entfällt. API 23 ist eine Konkretisierung des Plans aufgrund der aktuellen ZoomImage-Abhängigkeit, noch keine separat bestätigte Planentscheidung.

## Belegtes Preferences-Format

Das Format wurde anhand der tatsächlich gebundenen Pub-Versionen untersucht, nicht geraten:

- `shared_preferences` 0.5.12+4.
- `dynamic_theme` 1.0.1.
- Android-Preferences-Datei `FlutterSharedPreferences`, tatsächlicher XML-Dateiname `FlutterSharedPreferences.xml`.

| Schlüssel | Format |
| --- | --- |
| `flutter.favorites` | String mit Listenpräfix, anschließend Base64 einer Java-serialisierten `List<String>` |
| `flutter.accentColor` | Long-ARGB-Wert, kein Android-Int |
| `flutter.data_saver` | Boolean |
| `flutter.isDark` | Boolean; ohne gespeicherten Wert ist das bisherige Standardtheme dunkel |

Listenpräfix exakt:

```text
VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu
```

Frühere Versionen können Favoriten als String-Set hinterlassen. Der Import muss beide belegten Varianten berücksichtigen. Deserialisierung in ihrer Eingabegröße und den erlaubten Klassen begrenzen. Legacy-Quellcode nicht ohne zugehörige Lizenzhinweise kopieren.

Die importierten Daten und der Abschlussmarker sollen atomar in einem neuen Snapshot persistiert werden. Neue Werte dürfen bei Wiederanlauf nicht überschrieben werden. Bei Schreibfehler kein erfolgreicher Importmarker. Legacy-Daten nicht vor erfolgreichem Nachweis löschen.

Die heruntergeladenen Forschungsdateien liegen lokal unter `C:\Users\kosta\AppData\Local\Temp\opencode\xkcd-legacy-research\`. Sie sind nicht Teil des Repositorys und können in einer anderen Umgebung fehlen. Quellen bei Bedarf erneut aus den im Plan verlinkten Pub-Archiven laden.

## Ressourcen und Comic-Bilder

Der Build soll die bestehenden JSONs in generierte Compose-Ressourcen umwandeln. Originaldateien nicht konvertieren oder verschieben. Generierung muss Unicode, XML-Sonderzeichen, Anführungszeichen, Zeilenumbrüche und Sprach-/Regionsfallback testen. Fonts und Contributor-JSON werden aus ihren Originalpfaden eingebunden beziehungsweise in Build-Ausgaben kopiert.

Die bisherigen hochauflösenden Bilder werden ab Comic 1084 durch `_2x.png` adressiert. Ausnahmen sind 1193, 1446, 1667, 1735, 1739, 1744 und 1778. Der Datensparmodus nutzt das Original. Fehlende hochauflösende Varianten müssen auf das Original zurückfallen.

Der Viewer ist ein frühes technisches Risiko: Pinch und Pan dürfen die Alt-Text-Geste nicht versehentlich auslösen. Breite und sehr hohe Bilder sowie eine zugängliche Alternative zur Wischgeste prüfen, bevor alle Seiten auf dem Viewer aufbauen.

## Release und offene Nachweise

Noch nicht bekannt beziehungsweise nicht nachgewiesen:

1. Tatsächlich zuletzt veröffentlichte Version und `versionCode`.
2. Verfügbarkeit des veröffentlichten alten APKs und dessen Distributionsweg.
3. Vorhandener Release-Key und Kompatibilität zum veröffentlichten Zertifikat.
4. Echte Aktualisierung einer bestehenden Installation mit Erhalt der Nutzerdaten.
5. Erfolgreicher KMP-Build mit den geplanten Versionen.
6. Praktische Gesten-, Speicher- und UI-Tests auf Android.

Im Repository stehen `versionCode 13` und `versionName 1.4.6`. Das belegt nicht die letzte Veröffentlichung. Die alte Gradle-Konfiguration verwendet für Release tatsächlich Debug-Signing trotz vorhandener Release-Konfigurationslogik. Daraus nicht schließen, dass die veröffentlichte App ebenfalls diesen Schlüssel verwendet.

`android/key.properties` und Keystores sind ignoriert. Ihre Verfügbarkeit wurde nicht bestätigt. Passwörter weder im Chat anfordern noch in Logs oder Git übernehmen. Bei Google Play App Signing zwischen Upload- und App-Signing-Key unterscheiden.

Fehlende Release-Artefakte blockieren die Release-/Update-Abnahme, nicht die Debug-Entwicklung. Fixture-Importtests ersetzen keinen echten Update-Nachweis. Für den echten Test nur ein dediziertes Testgerät verwenden und niemals die Alt-App deinstallieren oder ihre Daten löschen.

## Ausführungsumgebung

Bisherige Umgebung ist Windows/PowerShell. Vorhanden sind Android CLI, adb, JDK 17 und 21 sowie SDK-Plattformen 36 und 37.0 mit Build Tools 36.0.0. `JAVA_HOME` zeigte bei der Untersuchung auf JDK 17; für den geplanten Build pro Shell JDK 21 auswählen. Keine globalen Einstellungen ungefragt ändern.

Bei der letzten Prüfung war kein Android-Gerät oder Emulator verbunden. API-23- und API-37-Geräte sind für die geplante Abnahme nötig. Die bestehenden Flutter-Builds wurden nicht ausgeführt. Neue Root-Gradle-Kommandos aus dem Plan funktionieren erst, nachdem Aufgabe 1 implementiert ist.

Bei Android-Geräteinteraktionen die Referenzen des `android-cli`-Skills lesen. iOS-Builds benötigen später macOS/Xcode und sind in dieser Phase ausgeschlossen.

## Einstieg für die nächste Implementierungssitzung

Nur bei einem ausdrücklichen Implementierungsauftrag starten. Falls der Benutzer lediglich dieses Protokoll vorlegt, zuerst den gewünschten Auftrag klären. Keine erneute grundlegende Architekturdiskussion beginnen, sofern keine widersprechenden Anforderungen oder technischen Blocker auftauchen.

1. Git-Status und lokale Repository-Instruktionen prüfen; Spezifikation und Plan lesen.
2. Klären, ob der Plan einschließlich Mindestversion/API 23 zur Ausführung freigegeben ist. Subagents nicht automatisch starten; nur nach entsprechender Auswahl oder ausdrücklicher Freigabe.
3. Arbeitsbasis sichern beziehungsweise isolieren. Kein neues Produktrepository erzeugen, keine vorhandenen Änderungen verwerfen.
4. Geplante Toolchain prüfen und mit Aufgabe 1 den ersten echten Build-Nachweis erbringen.
5. Aufgaben nacheinander durchführen und Ergebnisse in den Plan-Checkboxen beziehungsweise Prüfberichten dokumentieren.
6. Release-Artefakte früh erfragen, aber bei deren Fehlen den Debug-Umbau nicht unnötig blockieren.
7. Flutter erst nach geprüfter Übernahme entfernen; Assets gegen Commit `2ccfc83` vergleichen.
8. Bei Übergabe zwischen umgesetzt, getestet und durch externe Voraussetzungen blockiert unterscheiden.

### Reihenfolge der Arbeitspakete

1. Flutter-unabhängiger Android-/KMP-Build.
2. Generierte Ressourcen aus unveränderten Assets.
3. Einstellungen und Flutter-Import.
4. Comic-API und Navigation.
5. Bildvarianten und gemeinsamer Zoom-Viewer.
6. Hauptseite und Nummernsuche.
7. Favoriten, Settings, Credits und gemeinsame Navigation.
8. Android-Plattformdienste.
9. Release-Signing und echter Update-Nachweis.
10. Gezielte Flutter-Bereinigung, CI und Abschlussdokumentation.

## Mindestnachweise für den Abschluss

- Build ohne Flutter-SDK; gemeinsame Tests, Ressourcengenerator und Android-Lint erfolgreich.
- Original-Assets, Credits und Lizenzdateien unverändert.
- Funktionierende Hauptseite, Viewer, Favoriten, Einstellungen und Informationsseiten.
- Getesteter Legacy-Import einschließlich Wiederanlauf und beschädigter Daten.
- Android-Gerätetests für Gesten, Rotation, große Schrift, Teilen, Browser, Speichern und Cache-Löschen.
- Passende Release-Signierung und echter Update-Test oder klar dokumentierter externer Blocker.
- Keine Behauptung einer fertigen iOS-Version.

Die detaillierte Abnahme richtet sich nach den zwölf Kriterien der Spezifikation. Aktuell ist keines der Implementierungsergebnisse nachgewiesen; abgeschlossen sind ausschließlich Untersuchung und Dokumentation.
