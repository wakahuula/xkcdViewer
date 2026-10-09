# Android-Abnahme vom 2026-10-09

Fortsetzung auf `compose-migration`, Ausgangscommit `7000ee5`. Signing wurde vom Benutzer ausdrücklich zurückgestellt. Keine Release-Schlüssel werden erzeugt, keine alten App-Daten gelöscht und keine Flutter-Dateien entfernt.

Nachtrag nach Benutzerfreigabe: Der unten dokumentierte Export-Rotationsfehler ist inzwischen korrigiert. Vier neue Regressionstests und der tatsächliche API-23-Systemdialogtest bestehen. Aktuelle Nachweise: [Export-Rotationsfix](android-export-rotation-2026-10-09.md). Die folgenden Testergebnisse und offenen Fehler beschreiben den ersten Abnahmedurchlauf vor diesem Fix.

## Arbeitsliste

- [x] Lokale Toolchain prüfen.
- [x] Bereitgestellte Alt-APK analysieren.
- [x] Build-Logic-Tests, Ressourcenprüfung, gemeinsame Tests, Debug-Build und Lint erneut ausführen.
- [x] Android-Instrumentierung auf API 37 und API 23 wiederholen.
- [x] Exportdialog bei Abbruch und Rotation prüfen. Rotationstest fehlgeschlagen, siehe Befund unten.
- [x] Build-Anleitung und CI ergänzen.
- [x] Abschließende Prüfungen dieses Durchlaufs und verbleibende Blocker dokumentieren.

## Toolchain

JDK Temurin 21.0.12.1, Gradle 9.5.0, SDK-Plattform 37.0 Revision 2, Build Tools 36.0.0 und Emulator 37.2.12 sind lokal vorhanden. Gradle verwendet nach ausdrücklichem Benutzerauftrag `JAVA_HOME` aus der Benutzerkonfiguration. SDK-Aufrufe und Gradle erhalten lokal `ANDROID_HOME=C:/Users/kosta/AppData/Local/Android/Sdk`; diese Pfade sind keine Projektvorgaben.

Der Wrapper wurde mit `gradlew.bat --version` ausgeführt. Die vorhandene `distributionSha256Sum` bleibt unverändert. Es wird kein Flutter-SDK verwendet.

## Bereitgestellte Alt-APK

Lokale Quelle: `C:/Users/kosta/Downloads/app-release.apk`, 7.506.246 Byte. Mit SDK Build Tools 36.0.0 analysiert:

```powershell
aapt dump badging C:/Users/kosta/Downloads/app-release.apk
apksigner verify --print-certs C:/Users/kosta/Downloads/app-release.apk
Get-FileHash C:/Users/kosta/Downloads/app-release.apk -Algorithm SHA256
```

| Merkmal | Ergebnis |
| --- | --- |
| Paket-ID | `de.stoupas.xkcd` |
| App-Name | `xkcdViewer` |
| versionName / versionCode | `1.3.0` / `7` |
| minSdk / targetSdk | `16` / `28` |
| Launcher | `de.stoupas.xkcd.MainActivity` |
| APK SHA-256 | `1865066b3bd30e972a04a102ac799fabc1e8585228365e6d04902db4703cb7ed` |
| Signaturzertifikat SHA-256 | `fee01d25f6eb684f7b96e1c3e2a969afb2ad6b9b979bd954183953b17ee3c6b7` |

`apksigner verify` endet erfolgreich, meldet aber nicht signaturgeschützte `META-INF/*.version`-Einträge der damaligen Android-Support-/Lifecycle-Bibliotheken. Das APK wird nicht verändert.

Das Artefakt ist älter als die Flutter-Version `1.4.6 / 13` im Repository. Der tatsächliche Distributionsweg und die zuletzt veröffentlichte Version sind noch nicht bestätigt. Die Release-Properties werden daher nicht aus diesem APK abgeleitet. Ein passendes öffentliches Zertifikat allein belegt weder verfügbares Signing noch einen erfolgreichen Update-Pfad.

## Erhaltungsprüfung

`git diff 2ccfc83 -- assets LICENSE CODE_OF_CONDUCT.md PRIVACY_POLICY.md` ist leer. Die ursprünglichen Ressourcen, Contributor-Daten und diese Texte bleiben unverändert.

## Lokaler Debug-Nachweis

```powershell
.\gradlew.bat :shared:verifyLegacyResources :shared:jvmTest :app:assembleDebug :app:lintDebug
```

Ergebnis: `BUILD SUCCESSFUL`, 35 JVM-Tests, keine Fehler. Lint meldet acht Warnungen: sechs Hinweise auf neuere AGP-/Kotlin-Versionen und zwei beibehaltene Legacy-Launch-Ressourcen. Keine Dependency-Upgrades im Rahmen dieser Abnahme.

`aapt dump badging` bestätigt für die Debug-APK App-ID `de.stoupas.xkcd`, App-Name `xkcdViewer`, Entwicklungs-Version `1.4.6-kmp-dev / 13`, minSdk 23, targetSdk/compileSdk 37 und die richtige Launcher-Activity. Das ist kein Release-Artefakt.

Der erste separate Build-Logic-Lauf scheiterte beim Hashen einer Kotlin-Compiler-Cachedatei. Ursache war die gleichzeitig gestartete Root-Kompilierung derselben inkludierten Build-Logic-Ausgaben. Die Root-Kompilierung lief erfolgreich durch. Der anschließende serielle Build-Logic-Lauf mit `gradlew.bat -p build-logic test --stacktrace` bestand alle zehn Tests. State-Tracking bleibt aktiviert.

## API-37-Instrumentierung

Google-Play-x86_64-Image API 37 Revision 6, Emulator 37.2.12, AVD `Medium_Phone_API_37.0`, Serial `emulator-5554`. Vor der Testinstallation war die App auf diesem AVD nicht vorhanden. Der frühere Emulator-Systemabsturz trat in diesem Lauf nicht auf.

```powershell
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat :app:connectedDebugAndroidTest --stacktrace
```

Der erste Lauf führte 22 Tests aus. Neun Compose-UI-Tests scheiterten bei der Espresso-Initialisierung mit `NoSuchMethodException: android.hardware.input.InputManager.getInstance`, noch vor ihren App-Prüfungen. Betroffen waren alle drei `AppNavigationTest`-, beide `ComicScreenTest`- und alle vier `ComicViewerTest`-Fälle. Die übrigen 13 Fälle bestanden.

`dependencyInsight` bestätigte Espresso 3.5.0 als transitive Abhängigkeit von Compose UI Test. Die offiziellen [Espresso-3.7.0-Release-Notes](https://developer.android.com/jetpack/androidx/releases/test#espresso-3.7.0) beschreiben den passenden Fix, `getSystemService` statt reflektivem `InputManager.getInstance`.

Espresso Core 3.7.0 wird deshalb explizit ausschließlich als `androidTestImplementation` gebunden. Keine Produktabhängigkeit wird aktualisiert, keine Tests übersprungen. Der vollständige Wiederholungslauf ist erfolgreich: **22 Tests, keine Fehler und keine Skips**. Dies ersetzt den früher offenen API-37-Instrumentierungsnachweis, nicht den Release-/Update- oder Export-Lifecycle-Nachweis.

## Praktischer API-23-Export

Eigenes Test-AVD `xkcd_api23_acceptance`, API-23-Google-APIs-x86_64-Image Revision 33, Serial `emulator-5556`. Die neue Debug-App wurde auf dem frischen AVD installiert; die Alt-APK wurde nicht installiert oder überschrieben.

Die vollständige Instrumentierung mit Espresso 3.7.0 und `ANDROID_SERIAL=emulator-5556` ist ebenfalls erfolgreich: XML-Bericht mit 22 Testfällen, **21 bestanden, ein erwarteter Skip**, keine Fehler. Der echte MediaStore-Test ist nur ab API 29 vorgesehen. Der Gradle-Reporter zählt den Skip zusätzlich und zeigt 23 Ergebnisse. Ein erster Lauf führte wegen eines ADB-Verbindungs-Timeouts im `AndroidAdditionalTestOutputPlugin.onBeforeAll` gar keine Tests aus. Nach erfolgreichem direkten ADB-Probeaufruf wurde derselbe unveränderte Testlauf wiederholt und bestand. Eine dauerhafte Infrastrukturkorrektur wird daraus nicht abgeleitet.

Comic 3308, `Juice`, wurde über die öffentliche xkcd-API geladen und mit Bild angezeigt. Die CLI-Layoutinstrumentierung funktioniert auf diesem Image nicht, daher wurden die folgenden Schritte anhand von Screenshots ausgeführt und visuell geprüft.

### Rotation im Exportdialog: fehlgeschlagen

1. Im Comic-Menü `Download` auswählen.
2. Im Systemdialog `Downloads` auswählen.
3. Bei offenem `ACTION_CREATE_DOCUMENT`-Dialog das Testgerät ins Querformat drehen.
4. `Save` bestätigen.
5. Die App zeigt weiterhin Comic 3308, speichert aber keine Bilddaten. `/sdcard/Download/xkcd_3308.png` bleibt mit **0 Byte** zurück, ohne Erfolgsmeldung.

Rotation wurde mittels `settings put system accelerometer_rotation 0` und `settings put system user_rotation 1` ausgelöst. Danach wurde die ursprüngliche automatische Rotation wieder aktiviert.

Der Code hält `pending` im Activity-lokalen `AndroidDocumentExporter`. Die von `XkcdApp` gestartete Coroutine ist an die Composition gebunden und wird bei Neuerstellung abgebrochen. Der neue Exporter hat keinen ausstehenden Auftrag; sein Result-Callback versucht nur noch Cleanup. Logcat bestätigt zusätzlich `UnsupportedOperationException: Delete not supported` aus `DocumentsProvider.delete`. Das Cleanup verwendet `ContentResolver.delete` statt der Dokument-API. Der Befund betrifft den tatsächlichen App-Export, nicht nur das Testframework.

### Ohne Rotation: erfolgreich

Ein zweiter Export ohne Rotation erzeugte `/sdcard/Download/xkcd_3308 (1).png`, **130.707 Byte**, und zeigte `Image saved`. Die Datei wurde per adb kopiert und als gültiges Comicbild visuell geöffnet. Die vorherige leere Datei blieb bestehen; sie wurde nicht versteckt oder als erfolgreicher Export gezählt.

### Abbrechen: erfolgreich

Der Exportdialog wurde erneut geöffnet und über Zurück abgebrochen. Die App kehrte ohne Erfolgs-/Fehlermeldung zur Comic-Ansicht zurück. Es entstand keine weitere Datei; beide zuvor erzeugten Testdateien blieben unverändert.

Der Rotationsfehler bleibt ein Abnahmeblocker. Eine Korrektur benötigt einen Activity-Neuerstellung überlebenden Exportauftrag und dokumentgerechtes Cleanup sowie Regressionstests. Hier wurde noch kein Exporter-Produktpatch vorgenommen.

## CI und Anleitung

`.github/workflows/android.yml` wurde ergänzt. Der Workflow verwendet JDK 21, SDK 37.0 und Build Tools 36.0.0. Er validiert Gradle-Wrapper, führt Build-Logic und Root-Build nacheinander aus und benötigt keine Signing-Secrets. Actions sind auf über ihre Release-Tags geprüfte Commit-SHAs festgelegt; Repository-Berechtigungen sind auf `contents: read` begrenzt.

Der Workflow wurde mit `actionlint 1.7.12` lokal geprüft, ohne Befund. ShellCheck/Pyflakes wurden dabei deaktiviert, da diese Werkzeuge lokal nicht installiert sind. Ein echter GitHub-Actions-Lauf und ein Linux-Build sind noch nicht nachgewiesen. Es wurde nichts gepusht.

README beschreibt nun den Root-Build, JDK/SDK, API 23+, Übersetzungsquellen, historischen Screenshot-/iOS-Status und die offenen Release-Bedingungen. Die Contributor-Verweise bleiben erhalten.

## Ausstehende Release-Nachweise

Release-Signierung, minifizierter Release-Build und echter Update-Test bleiben offen. Eine Debug-APK mit anderer Signatur wird nicht über die Alt-App installiert. Historische Testnachweise stehen in [android-verification.md](android-verification.md), Release-Bedingungen in [android-update-verification.md](android-update-verification.md).

## Abnahmekriterien der Spezifikation

| Kriterium | Aktueller Nachweis |
| --- | --- |
| 1. Flutter-unabhängiger Android-Build | Root-Debug-Build, APK-Installation und API-23-App-Start erfolgreich |
| 2. Identität, Ressourcen und Credits | APK-Metadaten, unveränderte Originaldateien und Ressourcen-/Contributor-Tests |
| 3. Alle vorhandenen Abläufe | Automatisierte Screens/Services bestanden, Export bei Rotation noch fehlerhaft |
| 4. Suche, Fehler, 404, Bildfallback | Gemeinsame und Android-Tests bestanden |
| 5. Zoom, Pan und Alt-Text | Viewer-Tests auf API 23 und API 37 bestanden |
| 6. Ressourcen und Locale-Fallback | Ressourcenprüfung sowie JVM-/Android-Locale-Tests bestanden |
| 7. Legacy-Import | Fixture- und Persistenzfehlertests bestanden, keine Behauptung eines echten Updates |
| 8. Update über alte Installation | Offen, Signierung auf Benutzerwunsch zurückgestellt |
| 9. Plattformaktionen und Cache | Automatisierte API-37-Nachweise bestanden, Export-Lifecycle weiter offen |
| 10. Mindest-API | API-23-Instrumentierung und praktischer App-Start/Export ohne Rotation bestanden |
| 11. Release-Signierung oder Guard | Bestehende Guard-Tests bestanden, echter Release nicht geprüft |
| 12. Gemeinsamer Code/iOS-Abgrenzung | Keine Änderungen an commonMain, iOS wird ausdrücklich nicht als migriert bezeichnet |

Keine vollständige Android-Abnahme und keine Flutter-Entfernung. GitHub-Actions-Ausführung, echter OS-Prozessneustart, Release-/Update-Test und Export-Rotationskorrektur stehen noch aus.

## Abschließender Nachweis dieses Durchlaufs

Nach der Espresso-Korrektur wurde die vollständige lokale CI-Build-/Testfolge ausgeführt:

```powershell
.\gradlew.bat -p build-logic test
.\gradlew.bat :shared:verifyLegacyResources :shared:jvmTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --stacktrace
```

Beide Aufrufe sind erfolgreich. Die Shared-JVM-Tests wurden erneut ausgeführt: 35 Tests, keine Fehler. Build-Logic behält die zehn erfolgreichen Testfälle; der letzte Aufruf verwendet den gültigen Up-to-date-Nachweis. Lint bleibt bei acht Warnungen, keine Fehler. `actionlint 1.7.12` und `git diff --check` sind ohne Befund. Der Vergleich der Erhaltungsdateien gegen `2ccfc83` bleibt leer.

Eigenreview von Workflow, Testabhängigkeit, README und Nachweisen durchgeführt. Keine unabhängige Agentenreview, kein Commit, Push oder Merge. Die vorhandenen Lint-Warnungen bleiben zurückgestellt. Der Export-Rotationsbefund ist ein wichtiger offener App-Fehler, keine erledigte Abnahmeaufgabe.
