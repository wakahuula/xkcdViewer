# Export-Rotationsfix vom 2026-10-09

Der im [Abnahmebericht](android-acceptance-2026-10-09.md) reproduzierte Exportfehler ist korrigiert. Signing, Flutter-Dateien und gemeinsame Plattforminterfaces wurden nicht geändert.

## Umsetzung

- `AndroidAppModel` hält Download und Speichern im `viewModelScope`. Eine bei Activity-Neuerstellung abgebrochene UI-Coroutine beendet nur ihr Warten, nicht den angeforderten Schreibauftrag. Der ImageSaveService verwendet den Application-Context und wird erst mit dem ViewModel geschlossen.
- `AndroidDocumentExportState` hält das ausstehende Dialogergebnis und schreibt über den Application-ContentResolver. Er hält keine Activity oder Launcher-Referenz.
- `AndroidDocumentExporter` registriert den Activity-Result-Callback bei jeder Activity-Neuerstellung. Ein noch nicht gestarteter Dialogauftrag wird erst im gestarteten Lifecycle konsumiert, ein bereits gestarteter Dialog nicht erneut geöffnet.
- Cleanup verwendet `DocumentsContract.deleteDocument`. Ein verspätetes Dokument nach Auftragsabbruch sowie ein Dokument nach Schreibfehler werden darüber entfernt. Provider ohne Löschunterstützung bleiben Best Effort.
- Nach Rotation zeigt die neue Activity den Abschluss des zuvor angeforderten Speicherns als lokalisierten Toast. Ohne Rotation bleibt die bisherige Snackbar. Dialogabbruch zeigt keine Fehlermeldung.

Es gibt keinen `configChanges`-Workaround und keinen neuen Produkt- oder Testbibliotheksbedarf für diesen Fix. Prozess-Tod wird nicht als fortsetzbarer Export behandelt: Der ViewModel-Auftrag ist nur im laufenden Prozess erhalten. Ein nach Prozessneustart ohne zugehörigen Auftrag geliefertes Dokument wird nach Möglichkeit bereinigt. Ein echter OS-Prozessneustart ist noch nicht geprüft.

## Automatisierte Regressionstests

`AndroidDocumentExporterTest` prüft:

1. Activity-Neuerstellung bei ausstehendem Dialog und vollständige Datei nach Ergebnislieferung.
2. Abgebrochenen Auftrag mit verspätetem Dokument und Cleanup über die Dokument-API.
3. Schreibfehler mit Cleanup und Fehlerresultat.
4. Dialogabbruch mit anschließend erneut erfolgreichem Export.

Die ersten beiden Tests wurden gegen den ursprünglichen Produktcode ausgeführt und scheiterten jeweils mit Timeout: kein Abschluss nach Neuerstellung, keine Bereinigung des verspäteten Dokuments. Nach dem Fix bestehen sie. Der Testprovider implementiert den Dokumentprotokoll-Aufruf und echte Dateistreams, lehnt aber `ContentResolver.delete` ausdrücklich ab. Er wird ausschließlich mit der Instrumentierungs-APK installiert, nicht mit der Debug- oder Release-App.

Die Tests halten den Pickerstart mit einem Instrumentation-Monitor an und liefern das Ergebnis über die tatsächliche ActivityResultRegistry. Damit sind sie unabhängig von Netz und Bildschirmkoordinaten. Die tatsächliche Systemdialog-Rotation wurde zusätzlich praktisch geprüft.

Bei der Einrichtung der Tests gab es zunächst Testharness-Fehler, keinen Produktnachweis: falscher Dispatch-Parameter, eine nicht zulässige DocumentsProvider-Konfiguration und fehlende Kotlin-Runtime im separaten Test-APK-Prozess. Der Testprovider ist deshalb ein Java-ContentProvider mit Dokumentprotokoll. Ein beim ersten Timeout übrig gebliebener eigener Gradle-Prozess wurde gezielt beendet, nachdem er Testergebnisdateien gesperrt hielt. Erst danach wurde der oben beschriebene fachliche Rot-Nachweis erbracht.

## Geräte- und Buildnachweise

```powershell
$env:ANDROID_SERIAL = 'emulator-5556'
.\gradlew.bat :app:connectedDebugAndroidTest --stacktrace

$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat :shared:verifyLegacyResources :shared:jvmTest :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest --stacktrace
```

| Prüfung | Ergebnis |
| --- | --- |
| API 23, vollständige Android-Suite | 26 XML-Testfälle, 25 bestanden, ein erwarteter API-29+-MediaStore-Skip, keine Fehler |
| API 37, vollständige Android-Suite | 26 bestanden, keine Fehler oder Skips |
| Ressourcen, Shared-JVM, Debug-APK und Lint | `BUILD SUCCESSFUL`; 35 unveränderte JVM-Testfälle mit gültigem Up-to-date-Nachweis; acht bestehende Lint-Warnungen, keine Fehler |
| Debug-Manifest | Kein Testprovider enthalten |

### Praktischer Systemdialogtest auf API 23

Comic 3308 `Juice` laden, Menü `Download`, bei offenem Systemdialog ins Querformat drehen, `Save` bestätigen. Ergebnis:

- Rückkehr zur gleichen Comic-Ansicht, `Image saved` sichtbar.
- `/sdcard/Download/xkcd_3308 (2).png`, 130.707 Byte, als gültiges Bild geöffnet.
- SHA-256 `beed20c45278fcab52e4fdebaab0e4ad0a10e13b438c99e7527ad91b7539ff09`, identisch zum vorher ohne Rotation korrekt gespeicherten Bild.
- Erneutes Öffnen und Abbrechen des Systemdialogs kehrt ohne Fehlermeldung zur App zurück. Keine zusätzliche Datei.
- Ursprüngliche automatische Rotation des Test-AVD wiederhergestellt. Die alte 0-Byte-Datei aus dem Rot-Nachweis bleibt unverändert als historisches Testartefakt; keine pauschale Bereinigung alter Dateien.

Screenshots, kopierte PNGs, Rot-/Grün-Logs und Geräteberichte liegen lokal im genehmigten Temp-Verzeichnis `C:/Users/kosta/AppData/Local/Temp/opencode/` unter `xkcd-export-*`.

Eigenreview von Lebensdauer, Cancellation, Result-Routing und Cleanup durchgeführt. Keine unabhängige Agentenreview, kein Commit, Push oder Merge. Der Export-Rotationsblocker ist geschlossen; Release-/Update-Abnahme, echter Prozessneustart und GitHub-Actions-Ausführung bleiben offen.
