# Android-first KMP Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. No execution method has been selected yet. Do not spawn agents before the user selects one or explicitly authorizes a review.

**Goal:** Flutter im vorhandenen Repository durch eine Android-App mit gemeinsamer KMP-Logik und Compose-Multiplatform-UI ersetzen, ohne Übersetzungen, Credits oder vorhandene Nutzerdaten zu verlieren.

**Architecture:** `:app` liegt weiterhin unter `android/app/`, `:shared` enthält gemeinsamen Code und Android-Plattformadapter. Der Build startet am Repository-Root. iOS bleibt eine dokumentierte spätere Migration; seine historischen Dateien werden nicht in den Android-Build einbezogen.

**Tech Stack:** Kotlin 2.4.20, Compose Multiplatform 1.12.1, AGP 9.3.3, Gradle 9.5.0, JDK 21, Ktor 3.6.0, Coil 3.6.3, ZoomImage 1.7.0, Multiplatform Settings 1.3.0.

**Spec:** `docs/superpowers/specs/2026-10-03-kmp-migration-design.md`

## Global Constraints

- Die Git-Historie wird nicht umgeschrieben.
- `assets/locale/*.json` bleibt die bearbeitbare Quelle aller vorhandenen Übersetzungen. Dateinamen, Schlüssel und bestehende Texte bleiben unverändert.
- `assets/contributors.json`, `assets/fonts/`, vorhandene Launcher-Icons und weitere verwendete Assets bleiben erhalten. Notwendige Build-Kopien sind zulässig, die Originale werden nicht verschoben.
- Der bestehende Text `built_by` mit Flutter-Nennung bleibt vorerst unverändert. Eine spätere redaktionelle Änderung benötigt eine eigene Entscheidung.
- Android behält `applicationId` und Namespace `de.stoupas.xkcd` sowie den Namen xkcdViewer.
- Die bestehende iOS-Bundle-ID `de.stoupas.xkcd` wird für die spätere Migration dokumentiert und nicht geändert.
- Ein Release-Build darf bei fehlender Release-Konfiguration nicht still auf Debug-Signing zurückfallen.
- Der Build muss ohne Flutter-SDK funktionieren.
- Neue Produktfunktionen, Volltextsuche, Sync und eine ausführbare iOS-App sind ausgeschlossen.
- Änderungen nur auf der freigegebenen Arbeitsbasis; unbekannte lokale Dateien und private Signing-Dateien bleiben unangetastet. Vor Ausführung Isolation mit `using-git-worktrees` prüfen. Eine Arbeitskopie im selben Repository erfüllt die Vorgabe, kein neues Produktrepository anzulegen.

## Review Focus

- Schnelle Comic-Wechsel bei langsamen Antworten: nur das Ergebnis der letzten Auswahl wird angezeigt. Test in Aufgabe 6.
- Prozessabbruch während des Imports und bereits vorhandene KMP-Werte: erneuter Start ist sicher und überschreibt keine neuen Einstellungen. Tests in Aufgabe 3.
- Unicode, Anführungszeichen, mehrzeilige Texte und regionslose Locale: Originalwerte bleiben lesbar und Sprache fällt passend zurück. Tests in Aufgabe 2.
- Pinch-Zoom, anschließend Einfinger-Pan und Gerätewechsel der Ausrichtung: keine versehentliche Alt-Text-Geste und kein Verlust der Comic-Auswahl. Tests in Aufgaben 5 und 7.
- Bilddownload scheitert nach Erstellung eines Galerieeintrags: keine leere Datei und keine falsche Erfolgsmeldung. Test in Aufgabe 8.

## Recherche, Versionen und Voraussetzungen

Stand 2026-10-03. Maven-Metadaten/POMs und Herstellerdokumentation bestätigen veröffentlichte stabile Versionen. Das ist noch kein Build-Nachweis dieses Projekts. Aufgabe 1 muss den gesamten Satz durch Auflösung, Manifest-Merge und einen echten Debug-Build prüfen.

| Bestandteil | Festlegung |
| --- | --- |
| Kotlin und Compose-Compiler-Plugin | 2.4.20, beide gleich |
| Compose Multiplatform | 1.12.1; Material 3 über dessen Dependency-Aliase |
| AGP / KMP-Android-Plugin | 9.3.3 / `com.android.kotlin.multiplatform.library` |
| Gradle / Build-JDK / JVM-Bytecode | 9.5.0 / 21 / 17 |
| Android SDK | minSdk 23, compileSdk 37, targetSdk 37, Build Tools 36.0.0 |
| Ktor und Ktor MockEngine | 3.6.0 |
| Coroutines Core und Test | 1.11.0 |
| kotlinx.serialization JSON | 1.11.0 |
| Coil Compose und Network Ktor 3 | 3.6.3 |
| ZoomImage Coil 3 | 1.7.0, Apache-2.0 |
| JetBrains Lifecycle ViewModel/Runtime Compose | 2.11.0 |
| JetBrains Navigation Compose | 2.9.2 |
| Multiplatform Settings und Test | 1.3.0 |
| Activity Compose / Browser | 1.13.0 / 1.10.0 |
| AboutLibraries Core und Android-Gradle-Plugin | 15.2.0 |
| JUnit / Android Test Runner / Test Ext JUnit | 4.13.2 / 1.7.0 / 1.3.0 |

Die Kotlin-Kompatibilitätstabelle nennt AGP bis 9.3.1; 9.3.3 ist der ausgewählte Patch derselben Reihe mit Windows/R8-Fixes. Dieser Unterschied wird in Aufgabe 1 praktisch geprüft, nicht als bereits zertifizierte Kombination behauptet. Keine Rückkehr zur alten KMP-Integration über `com.android.library`. Keine Preview-/Snapshot-Versionen als Ausweichlösung. Bei einem unlösbaren Versionskonflikt die konkrete Diagnose und einen stabilen Alternativsatz zur Freigabe vorlegen.

AGP verwendet im App-Modul built-in Kotlin, kein `org.jetbrains.kotlin.android`. Der Kotlin-Compiler wird über den zentralen KGP-Classpath auf 2.4.20 angeglichen, bevor das gleich versionierte Compose-Compiler-Plugin verwendet wird. Im Shared-Modul werden KMP, Serialization, Compose und das neue Android-KMP-Plugin angewandt. Android-Ressourcen und Hosttests werden ausdrücklich aktiviert. Ein JVM-Ziel dient ausschließlich lokalen gemeinsamen Tests, nicht einer Desktop-App; nur echte Android-Implementierungen liegen in `androidMain`.

Lokal vorhanden: JDK 17 und 21, SDK-Plattformen 36 und 37.0, Build Tools 36.0.0, Android CLI und adb. Das SDK 37.0 meldet leeren CodeName und PreviewSdkInt 0. Für die Ausführung `JAVA_HOME` pro Shell auf das vorhandene JDK 21 setzen, nicht globale Benutzereinstellungen verändern. Derzeit kein verbundenes Gerät. API-23- und API-37-Emulatoren werden erst bei der Ausführung angelegt beziehungsweise vorhandene genutzt.

Belegtes Legacy-Format aus den Pub-Archiven `shared_preferences-0.5.12+4` und `dynamic_theme-1.0.1`:

- Android-Datei `FlutterSharedPreferences`, Schlüsselpräfix `flutter.`.
- `flutter.favorites`: String mit Präfix `VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu`, danach Base64 eines Java-serialisierten `List<String>`. Frühere Varianten können ein String-Set enthalten.
- `flutter.accentColor`: Long mit ARGB-Wert, etwa `0xFF673AB7`; nicht mit `getInt()` lesen.
- `flutter.data_saver` und `flutter.isDark`: Boolean. Fehlendes Theme bedeutet dunkel.
- Plugin-Quellcode nicht ohne Lizenzhinweis kopieren. Decoder selbst schreiben, begrenzte erlaubte Klassen und Eingabegröße statt uneingeschränkter Java-Deserialisierung.

Referenzen: [Kotlin-Kompatibilität](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html), [AGP 9.3](https://developer.android.com/build/releases/agp-9-3-0-release-notes), [Android-KMP-Plugin](https://developer.android.com/kotlin/multiplatform/plugin), [Compose-Ressourcen](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html), [ZoomImage-Changelog](https://github.com/panpf/zoomimage/blob/main/CHANGELOG.md), [AboutLibraries](https://github.com/mikepenz/AboutLibraries), [Pub Shared Preferences](https://pub.dev/packages/shared_preferences/versions/0.5.12+4), [Pub Dynamic Theme](https://pub.dev/packages/dynamic_theme/versions/1.0.1).

## Dateiübersicht und Prüfkonventionen

Gemeinsames Paket `de.stoupas.xkcd`. In den Aufgaben gelten folgende exakte Verzeichnispräfixe:

- `C` = `shared/src/commonMain/kotlin/de/stoupas/xkcd/`
- `T` = `shared/src/commonTest/kotlin/de/stoupas/xkcd/`
- `A` = `shared/src/androidMain/kotlin/de/stoupas/xkcd/`
- `D` = `android/app/src/androidTest/kotlin/de/stoupas/xkcd/`

Jede folgende Dateiangabe mit diesen Präfixen bezeichnet den entsprechend zusammengesetzten Pfad. Die Verantwortung folgt dem Feature: `comic/`, `settings/`, `favorites/`, `info/`, `platform/`, `resources/` und `app/`.

Neue Build-Logik liegt unter `build-logic/`, Tests unter `build-logic/src/test/kotlin/de/stoupas/xkcd/build/`. Gradle-Aufrufe erfolgen im Repository-Root mit `./gradlew.bat` unter PowerShell. `:shared:jvmTest` führt die gemeinsamen Logiktests aus; Android-Plattformtests liegen im App-Modul und laufen über `:app:connectedDebugAndroidTest`. Keine UI-Tests gegen die öffentliche API; Testserver beziehungsweise injizierte Fakes verwenden.

Jede Aufgabe folgt Rot, Grün, Commit. Ein Test darf zunächst am fehlenden Produktionssymbol scheitern, nicht an einem unbemerkten Infrastrukturfehler. Neue Buildkonfiguration wird über TestKit beziehungsweise Build-Smoke-Checks geprüft. Commit jeweils nur die in der Aufgabe genannten eigenen Dateien, nie pauschal den gesamten Arbeitsbaum.

### Aufgabe 1: Flutter-unabhängiger Android-/KMP-Build

**Dateien:** Neu `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar`, `shared/build.gradle.kts`, `android/app/build.gradle.kts`, `C/app/XkcdApp.kt`, `C/app/AppIdentity.kt`, `T/app/BuildSmokeTest.kt`; ändern `.gitignore`, `android/app/src/main/kotlin/de/stoupas/xkcd/MainActivity.kt`, `android/app/src/main/AndroidManifest.xml`, `android/app/src/main/res/values/styles.xml`, `android/app/src/main/res/values-night/styles.xml`. Alte Groovy-Build-Dateien in `android/` gezielt durch den Root-Einstieg ersetzen, nicht parallel als zweiten aktiven Build behalten.

**Schnittstellen:** Produziert `@Composable fun XkcdApp(): Unit` als vorübergehenden Einstieg und die Gradle-Module `:app`, `:shared` mit `projectDir` für `:app` auf `android/app`.

- [ ] Test `BuildSmokeTest.appIdentity()` anlegen: `assertEquals("de.stoupas.xkcd", APP_ID)` und `assertEquals("xkcdViewer", APP_NAME)`. Konstanten in `C/app/AppIdentity.kt` erstellen. Einen APK-Smoke-Check im Aufgabenprotokoll definieren, der dieselben Werte und minSdk 23 prüft.
- [ ] Den Test vor der Implementierung ausführen; bei noch fehlendem Wrapper den fehlenden Build-Einstieg als erwartetes Rot dokumentieren.
- [ ] Den stabilen Versionssatz, Wrapper mit offizieller SHA-256-Prüfsumme, Modulzuordnung und Android-Testkonfiguration einrichten. `MainActivity` bleibt am bisherigen Pfad, wird `ComponentActivity` und ruft `XkcdApp()` auf. Nur Startertext anzeigen. Launcher-Icons erhalten; Flutter-Metadaten und Flutter-Themes ersetzen; `android:exported="true"` für die Launcher-Activity explizit setzen.
- [ ] `./gradlew.bat --version` ausführen. Erwartet Gradle 9.5.0 und JVM 21.
- [ ] `./gradlew.bat :shared:jvmTest :app:assembleDebug :app:lintDebug` ausführen. Erwartet `BUILD SUCCESSFUL`, keine Manifest-Minimum-Überschreibung und keine Flutter-SDK-Abfrage. Bibliotheks-Metadaten gegen minSdk 23 prüfen. Debug-APK unter `android/app/build/outputs/apk/debug/app-debug.apk` mit `apkanalyzer manifest print` prüfen.
- [ ] Nur die Änderungen dieser Aufgabe committen: `build: replace Flutter Android entry with KMP build`.

### Aufgabe 2: Originalressourcen als gemeinsame Compose-Ressourcen

**Dateien:** Neu `build-logic/settings.gradle.kts`, `build-logic/build.gradle.kts`, `build-logic/src/main/kotlin/de/stoupas/xkcd/build/GenerateLegacyResources.kt`, `build-logic/src/main/kotlin/de/stoupas/xkcd/build/LegacyResourceGenerator.kt`, `build-logic/src/test/kotlin/de/stoupas/xkcd/build/LegacyResourceGeneratorTest.kt`, `C/resources/AppResources.kt`, `C/resources/Contributor.kt`, `T/resources/ContributorTest.kt`, `shared/src/commonMain/composeResources/values/migration.xml`; ändern Root-Settings, `shared/build.gradle.kts`. Keine Änderungen unter `assets/`.

**Schnittstellen:** Produziert `fun generateLegacyResources(inputDir: Path, outputDir: Path): Unit`, Gradle-Tasks `:shared:generateLegacyResources`, `:shared:verifyLegacyResources`, `suspend fun loadContributors(): List<Contributor>` und `data class Contributor(val name: String, val profile: String)`. Compose-Res-Paket `de.stoupas.xkcd.resources`; Getter benutzen die generierten typisierten `Res.string`-Einträge.

- [ ] Generator-Tests schreiben: jede der neun Dateien hat 32 Schlüssel; `built_by` bleibt identisch; XML-Parsing der Ausgabe ergibt die Eingabewerte einschließlich `❤`, `&`, `<`, Anführungszeichen und Zeilenumbrüchen. Ein temporäres Fixture mit fehlendem `download` liefert den englischen Wert. Contributor-Parsing erhält Namen, Reihenfolge und Profil-URLs.
- [ ] `./gradlew.bat -p build-logic test` ausführen und fehlenden Generator als Rot bestätigen.
- [ ] Cachebare Generator-Task implementieren. Ausgabe ausschließlich `shared/build/generated/legacyComposeResources`: Englisch nach `values/` und Sprach-/Regionsverzeichnissen, zusätzlich Sprachfallback etwa `values-de/` für `de-AT`. Regionsverzeichnisse etwa `values-de-rDE/`; Fonts als `fira_mono_regular.ttf`, `fira_mono_medium.ttf`, `fira_mono_bold.ttf`; Contributor-JSON bytegleich nach `files/contributors.json`. Deterministisch sortieren; entfernte Quelldateien dürfen keine veralteten generierten Dateien hinterlassen.
- [ ] `compose.resources.customDirectory` über den Task-Output-Provider anbinden. Neue Texte nur in `migration.xml`: Fehlerdetails, Wiederholen, Speichern-Erfolg/-Fehler, Alt-Text öffnen/schließen, Lizenzseite und barrierefreie Beschriftungen. Bestehende Schlüssel nicht überlagern. Fehlende neue Übersetzungen verwenden Englisch.
- [ ] `./gradlew.bat -p build-logic test` und `./gradlew.bat :shared:verifyLegacyResources :shared:jvmTest :app:assembleDebug` ausführen. Erwartet alle Tests grün; `git diff -- assets` leer. Auf Android `de-AT`, `uk-UA`, `ja-JP` und unbekannte Sprache prüfen, sobald ein Testgerät verfügbar ist.
- [ ] Dateien dieser Aufgabe committen: `feat: generate shared resources from existing contributor assets`.

### Aufgabe 3: Dauerhafte Einstellungen und sicherer Flutter-Import

**Dateien:** Neu `C/settings/UserSettings.kt`, `C/settings/SettingsStorage.kt`, `C/settings/SettingsRepository.kt`, `C/settings/LegacySettings.kt`, `T/settings/SettingsRepositoryTest.kt`, `A/settings/AndroidSettingsStorage.kt`, `A/settings/FlutterPreferencesReader.kt`, `A/settings/LegacyStringListDecoder.kt`, `D/settings/LegacyPreferencesMigrationTest.kt`, `android/app/src/androidTest/assets/legacy_preferences.xml`; ändern App-Initialisierung.

**Schnittstellen:** `UserSettings(darkTheme: Boolean = true, accentArgb: Long = 0xFF673AB7, dataSaver: Boolean = false, favorites: List<Int> = emptyList())`; `SettingsStorage.read(): String?`, `suspend SettingsStorage.write(json: String): Result<Unit>`; `SettingsRepository(storage)` mit `state: StateFlow<UserSettings>`, `suspend initialize(legacy: LegacySettings): Result<Unit>`, `suspend setTheme(dark: Boolean): Result<Unit>`, `setAccent(argb: Long)`, `setDataSaver(enabled: Boolean)`, `toggleFavorite(id: Int)`, `removeFavorite(id: Int)`, `clearFavorites()`, jeweils suspend und `Result<Unit>`. `LegacySettings` hat dieselben vier nullable Felder. `FlutterPreferencesReader(context).read(): LegacySettings`.

- [ ] Gemeinsame Tests für Standardwerte, geordnete eindeutige Favoriten und Persistenzfehler schreiben. Erwartung `assertEquals(listOf(12, 25), state.favorites)` nach Import `12, 25, 12`; ungültige IDs werden verworfen, bekannte 404 nicht gespeichert. Ein bereits vorhandenes neues Theme bleibt nach Import unverändert.
- [ ] `./gradlew.bat :shared:jvmTest --tests "*SettingsRepositoryTest*"` ausführen und Rot bestätigen.
- [ ] Snapshot-Speicher `xkcd_settings` mit einem einzigen JSON-String `settings_snapshot` implementieren. Schema 1 enthält `migrationVersion = 1`, vorhandene Einstellungen und Favoriten. Import ergänzt nur fehlende Felder, nicht vorhandene neue Werte; gespeicherte Standardwerte zählen als vorhandene Werte. Mutex serialisiert Änderungen. Zustand erst nach erfolgreichem Schreiben aktualisieren. Android nutzt Multiplatform Settings für Reads und eine atomare `SharedPreferences.Editor.commit()`-Operation auf IO für den gesamten Snapshot; Fehlschlag wird zurückgegeben. Marker und Daten werden gemeinsam committed, Legacy-Daten nicht gelöscht.
- [ ] Android-Tests vor Decoderimplementierung ergänzen: Fixture des belegten Listenformats, früheres String-Set, Long-ARGB, beschädigtes Base64, falscher Datentyp, Commit-Fehler, Neustart nach unterbrochenem Import. Legacy-Datei bleibt unverändert. Maximal 1 MiB codierte Listendaten akzeptieren; Deserialisierung nur für nachgewiesene Listenklassen, String-Elemente und notwendige Arrays, keine Proxy-Klassen.
- [ ] Importer und Decoder implementieren. Fixture einmal mit dem Encoding-Algorithmus des Plugins erzeugen, als festes Fixture testen, nicht bei jedem Test mit dem Produktionsdecoder erzeugen. Kaltstart wartet vor Repository-Freigabe auf Import; bei Schreibfehler keine normalen Settings-Schreibzugriffe zulassen, verständlichen Wiederholungszustand zeigen.
- [ ] `./gradlew.bat :shared:jvmTest --tests "*SettingsRepositoryTest*"` und `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.stoupas.xkcd.settings.LegacyPreferencesMigrationTest` ausführen. Ohne Gerät Android-Nachweis als ausstehend markieren, nicht als bestanden.
- [ ] Dateien committen: `feat: preserve Flutter favorites and settings on Android updates`.

### Aufgabe 4: Comic-API und deterministische Navigation

**Dateien:** Neu `C/comic/Comic.kt`, `C/comic/ComicApi.kt`, `C/comic/KtorComicApi.kt`, `C/comic/ComicRepository.kt`, `C/comic/ComicNavigation.kt`, `T/comic/ComicApiTest.kt`, `T/comic/ComicRepositoryTest.kt`, `T/comic/ComicNavigationTest.kt`.

**Schnittstellen:** `@Serializable Comic(num: Int, title: String, alt: String, img: String, year: String, month: String, day: String)`; optionale bisherige JSON-Felder mit Standardwerten und `safe_title` korrekt abbilden. `ComicApi` bietet `suspend latest(): Result<Comic>` und `suspend byId(id: Int): Result<Comic>`. `ComicRepository(api)` bietet dieselben Funktionen, `suspend random(random: Random): Result<Comic>`, `latestKnownId: Int?`; `fun adjacentId(current: Int, delta: Int, latest: Int): Int`; `fun parseComicId(query: String): Int?`. Fehlerklasse `ComicFailure` unterscheidet ungültige Nummer, nicht gefunden, Netzwerk und ungültige Antwort.

- [ ] MockEngine-Tests schreiben: JSON mit unbekannten Feldern wird akzeptiert, ungültiges Pflichtfeld abgelehnt, 404 ergibt NotFound, 500 ergibt Fehler. Latest-Endpunkt `https://www.xkcd.com/info.0.json`, Nummern-Endpunkt `https://www.xkcd.com/12/info.0.json`. Navigationstest `assertEquals(405, adjacentId(403, 1, 500))`; Grenzen 1 und latest bleiben stehen. Eingaben leer, `abc`, 0, negativ und Integer-Überlauf werden abgelehnt. Zufall immer in 1..latest ohne 404.
- [ ] `./gradlew.bat :shared:jvmTest --tests "*Comic*Test*"` ausführen und Rot bestätigen.
- [ ] API/Repository implementieren, `ignoreUnknownKeys = true`, Request-Timeout 15 s. CancellationException nicht in einen Produktfehler umwandeln. LRU-Metadaten-Cache maximal 128 Einträge. Explizites Latest-Abrufen aktualisiert höchste Nummer trotz Cache; bekannte Comics können aus Cache gelesen werden. Ungültige Eingaben ohne Request ablehnen.
- [ ] Tests um wiederholte Cache-Aufrufe, Latest-Aktualisierung und Cache-Grenze ergänzen und ausführen. Erwartet keine versteckten zufälligen Fallbacks bei API-Fehlern.
- [ ] Dateien committen: `feat: add shared xkcd API and comic navigation`.

### Aufgabe 5: Bildvarianten und gemeinsamer Zoom-Viewer

**Dateien:** Neu `C/comic/ImageVariants.kt`, `C/comic/ComicViewer.kt`, `C/comic/ViewerGesturePolicy.kt`, `C/platform/ImageCacheService.kt`, `T/comic/ImageVariantsTest.kt`, `T/comic/ViewerGesturePolicyTest.kt`, `D/comic/ComicViewerTest.kt`, `A/platform/AndroidImageCacheService.kt`; Tests verwenden `android/app/src/androidTest/assets/viewer_wide.png` und `viewer_tall.png`.

**Schnittstellen:** `fun imageCandidates(comic: Comic, dataSaver: Boolean): List<String>`; `@Composable fun ComicViewer(comic: Comic, dataSaver: Boolean, onResolvedImage: (String) -> Unit, modifier: Modifier = Modifier)`; `fun shouldRevealAltText(pointerCount: Int, atBaseScale: Boolean, upwardDragDp: Float): Boolean`. `ImageCacheService` bietet `suspend stats(): CacheStats` und `suspend clear(): Result<Unit>`; `CacheStats(memoryBytes: Long, diskBytes: Long, entryCount: Int?)`. Null bedeutet nicht verlässlich ermittelbar, keinen erfundenen Zähler anzeigen.

- [ ] Tests schreiben: Nummer <1084, Datensparmodus und Ausnahmen `[1193, 1446, 1667, 1735, 1739, 1744, 1778]` ergeben nur Original. Sonst `_2x.png` vor Original. Alt-Text-Geste nur bei einem Pointer, Basiszoom und mindestens 48 dp nach oben; Pinch oder gezoomtes Pan ergeben false.
- [ ] `./gradlew.bat :shared:jvmTest --tests "*ImageVariantsTest*" --tests "*ViewerGesturePolicyTest*"` ausführen und Rot bestätigen.
- [ ] CoilZoomAsyncImage von ZoomImage hinter `ComicViewer` integrieren. Coil mit Ktor3, einem wiederverwendeten ImageLoader und explizitem Diskcache einrichten. Bei Ladefehler genau einmal Original versuchen. Nur die erfolgreich geladene aktuelle URL melden; nach Comic-Wechsel alten Erfolg nicht übernehmen. Zoom-/Alt-Text-Zustand pro Comic isolieren. Alt-Text zusätzlich mit zugänglicher Schaltfläche öffnen, scrollbar bei langen Texten.
- [ ] Android-Tests für Pinch, Pan, Alt-Text-Button und breite/hohe Fixtures schreiben; Fehler auf `_2x` liefert Original, Fehler auf beiden zeigt Wiederholen. Kein öffentlicher Netzwerkzugriff. Cache-Test befüllt Speicher und Disk, löscht beide, prüft danach erneut die Statistiken.
- [ ] Gemeinsame Tests und `./gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.stoupas.xkcd.comic.ComicViewerTest` ausführen. Geste früh praktisch prüfen, bevor Aufgabe 7 alle Seiten verbindet.
- [ ] Dateien committen: `feat: add shared zoomable comic viewer with image fallback`.

### Aufgabe 6: Comic-Zustand, Nummernsuche und Hauptseite

**Dateien:** Neu `C/comic/ComicUiState.kt`, `C/comic/ComicViewModel.kt`, `C/comic/ComicScreen.kt`, `C/comic/ComicSearchDialog.kt`, `T/comic/ComicViewModelTest.kt`, `D/comic/ComicScreenTest.kt`.

**Schnittstellen:** `ComicUiState(comic: Comic?, loading: Boolean, failure: ComicFailure?, resolvedImageUrl: String?)`. `ComicViewModel(comics: ComicRepository, settings: SettingsRepository)` mit `state: StateFlow<ComicUiState>` sowie `loadLatest()`, `loadAdjacent(delta: Int)`, `loadRandom()`, `select(id: Int)`, `retry()`, `resolvedImage(url: String)`. `@Composable fun ComicScreen(viewModel: ComicViewModel, settings: UserSettings, onFavorites: () -> Unit, onSettings: () -> Unit, onShare: (Comic) -> Unit, onSave: (Comic, String) -> Unit, onExplain: (Comic) -> Unit)`.

- [ ] ViewModel-Test mit kontrolliert verzögerter Fake-API schreiben: Auswahl 12 dann 25, Antwort 25 vor 12, am Ende `assertEquals(25, state.value.comic?.num)`. Weitere Tests für Fehler/Wiederholen, abgebrochenen Request, Favoritenzustand und invalidierte `resolvedImageUrl` beim Comic-Wechsel.
- [ ] `./gradlew.bat :shared:jvmTest --tests "*ComicViewModelTest*"` ausführen und Rot bestätigen.
- [ ] ViewModel mit Job-Cancellation und zusätzlicher Request-Identität implementieren. Alter Comic kann während Nachladen sichtbar bleiben, Aktionen dürfen nicht das falsche Bild speichern. Bei Kaltstart sichtbarer Ladezustand. Suchdialog validiert Nummer, ruft erst bei Bestätigung `select` auf. StateFlow lifecycle-aware sammeln; Rendern startet keine Requests.
- [ ] Hauptseite mit bisheriger Bedienstruktur implementieren: Titel/Datum, Nummernsuche, Favorit, Zufall, vorige/nächste Nummer, Menü für letzte Ausgabe, Erklärung, Favoriten, Einstellungen, Teilen und Speichern. Vorhandene Ressourcen und FiraMono nutzen. Speichern bis erfolgreicher Bildauflösung deaktivieren.
- [ ] Android-Tests mit Fake-Repositories ergänzen: leere Suche startet keinen Request, Fehler ist sichtbar und wiederholbar, Comic-Wechsel verwirft alte Bild-URL, Favorit bleibt nach Screen-Neuerstellung erhalten. Gemeinsame Tests und den gefilterten `ComicScreenTest` ausführen.
- [ ] Dateien committen: `feat: rebuild comic home and number lookup in shared Compose`.

### Aufgabe 7: Favoriten, Einstellungen, Credits und Navigation

**Dateien:** Neu `C/favorites/FavoritesViewModel.kt`, `C/favorites/FavoritesScreen.kt`, `T/favorites/FavoritesViewModelTest.kt`, `C/settings/SettingsScreen.kt`, `C/app/XkcdTheme.kt`, `C/app/AppRoute.kt`, `C/app/AppDependencies.kt`, `C/info/AboutScreen.kt`, `C/info/ContributorsScreen.kt`, `C/info/LicensesScreen.kt`, `C/platform/LicenseSource.kt`, `C/platform/ShareService.kt`, `C/platform/BrowserService.kt`, `C/platform/ImageSaveService.kt`, `D/app/AppNavigationTest.kt`, `D/resources/LocaleResolutionTest.kt`; ändern `C/app/XkcdApp.kt` und Android-App-Einstieg.

**Schnittstellen:** `FavoriteItem(id: Int, comic: Comic?, failure: ComicFailure?)`; `FavoritesViewModel(comics, settings)` liefert `items: StateFlow<List<FavoriteItem>>`, `loading: StateFlow<Boolean>`, `retry(id: Int)`. `LicenseSource` bietet `suspend readJson(): String`. `AppDependencies(comics: ComicRepository, settings: SettingsRepository, share: ShareService, save: ImageSaveService, browser: BrowserService, cache: ImageCacheService, licenses: LicenseSource)`; finale `@Composable fun XkcdApp(dependencies: AppDependencies)`. Services werden in Aufgabe 8 definiert; in dieser Aufgabe zunächst exakt diese Interfaces und Test-Fakes anlegen, keine Android-Implementierungen behaupten.

- [ ] Favoritentest schreiben: drei Favoriten, eine HTTP-Antwort fehlerhaft, andere zwei bleiben sichtbar; maximal vier Requests gleichzeitig; einmaliges Entfernen aktualisiert Zustand ohne erneutes vollständiges Laden. Leere Liste zeigt vorhandenes `nothing_here`. `./gradlew.bat :shared:jvmTest --tests "*FavoritesViewModelTest*"` als Rot ausführen.
- [ ] Favoritenmodell/-seite, Settings und Informationsseiten implementieren. Akzentdialog-Abbruch schreibt nichts. Cache-Angaben und Löschen über Service. Credits aus generiertem Contributor-JSON, vorhandene App-/Comic-/Icon-Lizenzen plus Bibliotheksseite. Theme erzeugt Material-3-Farben aus importiertem ARGB, passende Vordergrundfarben statt unlesbarer Kombinationen.
- [ ] Typisierte serialisierbare Routen `Home`, `Favorites`, `Settings`, `Contributors`, `About`, `Licenses`, `ComicDetail(id: Int)` mit Navigation Compose verbinden. Detailseite arbeitet mit ihrer eigenen ID; Zurück verändert nicht unerwartet den vorherigen Hauptcomic. ViewModels am jeweiligen Backstack-Eintrag halten. Route und Comic-ID über SavedStateHandle sichern; Zoom kann beim Prozessneustart zurückgesetzt werden.
- [ ] Instrumentierte Tests vor finaler Verdrahtung schreiben: Credits vollständig, Akzentdialog-Abbruch unverändert, Theme/DataSaver nach Neustart erhalten, Wechsel Favoriten/Detail/Zurück korrekt, Rotation behält ID und Route. Bei 200 % Schrift und kleinem Landscape-Fenster sind Kernaktionen erreichbar, Insets überdecken keine Buttons. Locale-Tests für alle neun Quelldateien sowie `de-AT`, `uk-UA`, unbekannte Sprache.
- [ ] Gemeinsame Tests und gefilterte Android-Tests `AppNavigationTest`, `LocaleResolutionTest` ausführen. Bei Fehlern keine neuen Übersetzungswerte in bestehende JSONs eintragen.
- [ ] Dateien committen: `feat: restore favorites settings credits and shared navigation`.

### Aufgabe 8: Android-Teilen, Browser und Bilder speichern

**Dateien:** In Aufgabe 7 angelegte Interfaces `C/platform/ShareService.kt`, `C/platform/BrowserService.kt`, `C/platform/ImageSaveService.kt` vervollständigen; neu `C/platform/PlatformFailure.kt`, `A/platform/AndroidShareService.kt`, `A/platform/AndroidBrowserService.kt`, `A/platform/AndroidImageSaveService.kt`, `A/platform/MediaStoreImageWriter.kt`, `A/platform/AndroidLicenseSource.kt`, `D/platform/PlatformServicesTest.kt`, `D/platform/ImageSaveServiceTest.kt`, `android/app/src/main/res/raw/aboutlibraries_keep.xml`; ändern App-Verdrahtung, Manifest und App-Gradle.

**Schnittstellen:** `suspend ShareService.shareComic(comic: Comic): Result<Unit>`; `suspend BrowserService.open(url: String): Result<Unit>`; `suspend ImageSaveService.save(comic: Comic, imageUrl: String): Result<Unit>`. Nutzerabbruch wird als `PlatformFailure.UserCancelled` zurückgegeben und in der UI ohne Fehler- oder Erfolgsmeldung behandelt; Coroutine-Cancellation wird weitergeworfen. URLs und Ergebnisse sind gemeinsame Typen, kein Uri/Intent/Context. `suspend MediaStoreImageWriter.write(bytes: ByteArray, mimeType: String, displayName: String): Result<Unit>` mit Android-interner Implementierung für Fehlerpfadtests.

- [ ] Service-Tests schreiben: Teilen benutzt `ACTION_SEND`, `text/plain` und `https://www.xkcd.com/12/`; Browser erklärt Nummer 12 über `https://www.explainxkcd.com/wiki/index.php/12`; fehlender Handler liefert Fehler. Bildtest injiziert Fehler nach MediaStore-Insert: kein übriggebliebener Eintrag, kein Erfolg. Nutzerabbruch eines Exportdialogs ist kein Speichern-Erfolg.
- [ ] Gefilterte `PlatformServicesTest` und `ImageSaveServiceTest` ausführen und fehlende Services als Rot bestätigen.
- [ ] Android-Implementierungen schreiben: Share-Chooser, Custom Tabs mit Browser-Fallback; URLs nur HTTP/HTTPS, unbekannte Schemata abweisen. Für API >=29 nach `Pictures/xkcdViewer` über MediaStore mit IS_PENDING und Cleanup speichern. Für API 23..28 `ACTION_CREATE_DOCUMENT` verwenden, ohne neue breite Speicherberechtigung. Über ActivityResult-Callback das Result erst nach Abschluss liefern; Abbruch gesondert behandeln. Content-Type/Dateiendung aus echtem Bild ableiten, keine unpassende `.png`-Endung erzwingen.
- [ ] AboutLibraries-Android-Plugin im App-Modul automatisch Metadaten erzeugen lassen, `AndroidLicenseSource` liest generiertes Raw-JSON; gemeinsame Lizenzseite verwendet AboutLibraries Core. Resource-Keep für `@raw/aboutlibraries`; native Abhängigkeiten und Font-/Icon-Hinweise zusätzlich prüfen. Erfolgs-/Fehlermeldungen über gemeinsame UI, kein Android-Toast in commonMain.
- [ ] `WRITE_EXTERNAL_STORAGE` entfernen; INTERNET behalten. VIBRATE nur behalten, wenn tatsächliches haptisches Feedback implementiert ist. Gesamten Bildspeicherweg auf API 23 und 37, Browser und Share-Chooser praktisch prüfen; Fehlerpfadtests ausführen.
- [ ] Dateien committen: `feat: add Android sharing image export and browser adapters`.

### Aufgabe 9: Release-Signierung und echter Update-Nachweis

**Dateien:** Neu `build-logic/src/main/kotlin/de/stoupas/xkcd/build/ReleaseConfiguration.kt`, `build-logic/src/test/kotlin/de/stoupas/xkcd/build/ReleaseConfigurationTest.kt`, `docs/migration/android-update-verification.md`; ändern `android/app/build.gradle.kts`.

**Schnittstellen:** `fun validateReleaseConfiguration(properties: Properties, baseDir: Path, versionCode: Int, previousVersionCode: Int?): ReleaseConfiguration`; Fehlertexte dürfen keine Passwörter enthalten. Release-Guard-Task `:app:verifyReleaseConfiguration` läuft nur für Release-Tasks, nicht beim Debug-Build.

- [ ] Tests schreiben: fehlende `storeFile`, `storePassword`, `keyAlias`, `keyPassword` verhindern Release; Debug bleibt baubar; relative storeFile-Pfade bleiben relativ zu `android/`, wie der alte SigningConfig-Code; keine Geheimnisse in Fehlermeldungen; versionCode muss über bestätigter Altversion liegen. `./gradlew.bat -p build-logic test` als Rot ausführen.
- [ ] Guard und Release-Konfiguration implementieren. `android/key.properties` unverändert als Quelle; keine neuen Keystores. Debug verwendet normale lokale Debug-Signatur. VersionName und Release-VersionCode erst anhand veröffentlichter Version festlegen; provisorische Debug-Version ist ausdrücklich nicht Release-fähig, wenn der Vergleich fehlt.
- [ ] Vom Benutzer den tatsächlichen Distributionsweg, ein altes APK und nötigen Signing-Zugang erfragen, sofern nicht bereits vorhanden. Keine Secrets im Chat anfordern; lokale private Dateien verwenden. `apkanalyzer` für Altversion und `apksigner verify --print-certs` für Vergleich nutzen. Nur öffentliche Zertifikat-Fingerprints dokumentieren.
- [ ] `./gradlew.bat :app:assembleRelease` ohne Signing-Konfiguration ausführen: erwarteter verständlicher Guard-Fehler, keine Debug-APK. Mit bestätigter Konfiguration `./gradlew.bat :app:assembleRelease :app:lintRelease` ausführen: erwarteter erfolgreicher Release-Build. Minifizierte App und Lizenzseite testen.
- [ ] Auf einem dedizierten Testgerät alte veröffentlichte APK installieren, Favoriten/Theme/Farbe/DataSaver ändern, dann neue passend signierte APK mit `adb -s <serial> install -r <new-apk>` darüber installieren. Kein Uninstall und kein `pm clear`. Alle Werte und weiteres Bearbeiten nach erneutem Start prüfen. Bei Store-App-Signing den passenden Store-Testkanal nutzen, nicht Upload-Key und App-Signing-Key verwechseln.
- [ ] Ergebnisse, Artefaktversionen und verbleibende Blocker dokumentieren. Ohne Alt-APK/Signatur nur Fixture-Import nachgewiesen; Release-/Update-Abnahme bleibt offen. Eigene Dateien committen: `build: preserve release signing and document update compatibility`.

### Aufgabe 10: Flutter entfernen und Android-Übergabe abschließen

**Dateien:** Ändern `README.md`, `.gitignore`; neu `.github/workflows/android.yml`, `docs/migration/android-verification.md`. Gezielt entfernen nach Freigabe der Funktionsnachweise: `lib/**/*.dart`, `test/widget_test.dart`, `integration_test/app_test.dart`, `integration_test/driver.dart`, `pubspec.yaml`, `pubspec.lock`, `.metadata`, `xkcd.iml`, `xkcd_android.iml`, `web/index.html`, `web/manifest.json`, identifizierte Flutter-Web-Icons, alte Gradle-Wrapper-Dateien unter `android/`. Vor Entfernung komplette getrackte Web-/Flutter-Dateiliste mit `git ls-files` prüfen. `ios/`, `assets/`, Credits, Screenshots und private Dateien nicht löschen.

**Schnittstellen:** Dokumentierter Root-Build und Android-Testworkflow. Keine neuen Produktinterfaces.

- [ ] Vor Löschung Checkliste und Ressourcen-Baseline prüfen: alle Android-Abläufe aus der Spec sind mindestens automatisiert beziehungsweise auf Gerät nachgewiesen; Assets vergleichen mit Spec-Commit `2ccfc83`. Release-Blocker dürfen dokumentiert offen bleiben, niemals als erledigt markiert werden.
- [ ] Getrackte Flutter-Reste einzeln prüfen und mit gezielten Dateioperationen entfernen. Historische iOS-Dateien ausdrücklich behalten; sie dürfen noch Flutter referenzieren, gelten aber als nicht migriert und werden nicht gebaut. Keine rekursive Löschung eines unklaren Verzeichnisses.
- [ ] README aktualisieren: Kotlin-/Compose-Stack, Root-Kommandos, JDK 21, API 23+, neun erhaltene Sprachen, Übersetzungsbeiträge weiterhin in `assets/locale/`, Signierungsbedingungen und iOS-Status. Alte Flutter-/Codemagic-Buildaussagen ersetzen, Contributor-Credits nicht reduzieren.
- [ ] CI für Push/PR einrichten: JDK 21, Android SDK 37, Gradle-Wrapper-Validierung, Generator- und gemeinsame Tests, Debug-Build und Lint. Keine Release-Secrets in PR-Jobs. Workflow-Aktionen auf geprüfte Releases oder Commit-SHAs pinnen. Device-Tests separat dokumentieren, nicht durch einen grünen Build vortäuschen.
- [ ] `./gradlew.bat -p build-logic test` ausführen.
- [ ] `./gradlew.bat clean :shared:verifyLegacyResources :shared:jvmTest :app:assembleDebug :app:lintDebug` ausführen. Erwartet erfolgreich ohne Flutter-SDK. `git diff 2ccfc83 -- assets LICENSE CODE_OF_CONDUCT.md PRIVACY_POLICY.md` bleibt leer.
- [ ] Gesamte Android-Instrumentierung auf API 23 und 37 mit `./gradlew.bat :app:connectedDebugAndroidTest` ausführen; Geräte explizit auswählen. Zoom, große Schrift, Landscape, Speichern und echter Update-Test separat im Prüfbericht nachweisen. Kein Anspruch auf iOS-Kompatibilität ohne späteren iOS-Build.
- [ ] `git diff --check` und finale Anforderungsprüfung gegen alle zwölf Spec-Abnahmekriterien ausführen. Wenn eine unabhängige Review vom Benutzer erlaubt ist, jetzt ausführen und konkrete Befunde abarbeiten.
- [ ] Eigene Abschlussdateien und gezielte Löschungen committen: `chore: remove retired Flutter Android tooling and document KMP app`.

## Übergabe und Freigaben

- [x] Spec vom Benutzer bestätigt.
- [x] Legacy-Formate und veröffentlichte Dependency-Versionen recherchiert.
- [x] Plan auf Spec-Abdeckung, Schnittstellen und Prüfwege selbst geprüft.
- [ ] Benutzer prüft diesen Plan und wählt eine Ausführungsmethode.
- [ ] Ausführungsumgebung und Arbeitsbasis vorbereiten.
- [ ] Aufgaben 1 bis 10 durchführen, jeweilige Prüfungen und Blocker protokollieren.

Empfehlung: native Ausführung in derselben Sitzung. Die Aufgaben bauen überwiegend auf gemeinsamen Interfaces auf; wechselnde Implementierer bieten hier weniger Nutzen als bei unabhängigen Features. Alternativ ist Subagent-Ausführung mit separater Aufgabenprüfung möglich, wenn der Benutzer dies ausdrücklich wählt. Eine unabhängige Schlussprüfung wird ebenfalls nur mit entsprechender Freigabe delegiert.

Nicht vor der Planfreigabe implementieren. Die vorläufige Android-Aufwandsspanne bleibt 12 bis 20 Personentage für KMP-/Compose-Erfahrene; fehlende Release-Artefakte blockieren den Update-Nachweis unabhängig vom Entwicklungsaufwand.
