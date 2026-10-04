# Android-Migrationsnachweise

Fortsetzung auf `migration/kmp-android-foundation`, vom Benutzer am 2026-10-04 beauftragt. Kein Push und kein Merge. [Grundlage](foundation-verification.md), [Plan](../superpowers/plans/2026-10-03-kmp-migration.md), [Spezifikation](../superpowers/specs/2026-10-03-kmp-migration-design.md).

## Aufgabe 3, Einstellungen und Legacy-Import

2026-10-04, JDK 21, Gradle 9.5.0:

```powershell
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat :shared:jvmTest :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=de.stoupas.xkcd.settings.LegacyPreferencesMigrationTest'
```

Ergebnis: BUILD SUCCESSFUL, 14 JVM-Tests und vier Android-Tests. Gerät `pixel_test`, Android 16 / API 36. Tests vor den jeweiligen Implementierungen scheiterten an fehlenden Produktionssymbolen. Dies ist noch kein API-23-/API-37-Nachweis und kein Update über eine veröffentlichte Alt-App.

Geprüft:

- Standardwerte werden persistiert. Vor erfolgreicher Initialisierung sind Änderungen gesperrt.
- Favoriten bleiben geordnet und eindeutig. Ungültige Nummern und Comic 404 werden verworfen.
- Bereits gespeicherte neue Felder einschließlich expliziter Standardwerte werden nicht durch Legacy-Werte ersetzt.
- Snapshot und Importmarker werden zusammen geschrieben. Schreibfehler ändern nicht den beobachtbaren Zustand; Wiederanlauf importiert sicher.
- Änderungen von Theme, Akzent, Datensparmodus und Favoriten bleiben über Repository-Neustarts erhalten.
- Defekte oder zukünftige Snapshots werden nicht überschrieben.
- Android liest Long-ARGB und Boolean-Werte typgerecht; falsche Datentypen werden übersprungen. Ältere String-Sets werden deterministisch nach Nummer sortiert.
- Ein festes Fixture wurde einmal mit dem damaligen Base64-/Java-Serialisierungsalgorithmus erzeugt. Der Decoder prüft es, beschädigte und übergroße Eingaben sowie eine fremde serialisierte Klasse.
- Die originale Legacy-Preference-Datei bleibt bytegleich.

Entscheidung: Statt Java-Deserialisierung mit Klassenfreigabeliste wird ausschließlich das belegte `ArrayList<String>`-Wireformat gelesen. Keine Java-Klassen werden aus Eingabedaten instanziiert. Codierte Daten sind auf 1 MiB begrenzt, Anzahl und String-Referenzen werden validiert. Nicht belegte Listenimplementierungen werden nicht geraten; ein entsprechender realer Altbestand müsste als weiteres Fixture ergänzt werden.

Der App-Kaltstart wartet auf den Import. Ein Fehlschlag zeigt einen Wiederholungszustand und gibt die normale App nicht frei. Die alten Daten werden nicht gelöscht.

## Aufgabe 4, Comic-API und Navigation

2026-10-04: `:shared:jvmTest :app:assembleDebug` erfolgreich. 24 gemeinsame/JVM-Tests, keine Fehler. Die zehn neuen API-/Repository-/Navigationstests wurden vor der Implementierung mit fehlenden Symbolen ausgeführt.

Geprüft mit Ktor MockEngine und kontrollierten API-Fakes, ohne öffentlichen Netzwerkzugriff: Endpunkte, unbekannte JSON-Felder, HTTP 404/500, ungültige Pflichtfelder, Cancellation, Eingabevalidierung vor dem Request, Cache-Wiederverwendung, echte Latest-Aktualisierung, 128-Einträge-LRU-Grenze, Zufall ohne Nummer 0/404 und Navigation an Grenzen. Timeout ist auf 15 Sekunden begrenzt.

## Aufgabe 5, Zoom-Viewer und Bildcache

2026-10-04: `:shared:jvmTest :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest` erfolgreich. 27 JVM-Tests und acht Android-Tests auf API 36, keine Fehler.

Neue Viewer-Tests prüfen breite/hohe lokale PNG-Fixtures, `_2x`-Fehler mit genau einem Originalfallback, Fehler beider Varianten mit Wiederholen, Alt-Text-Schaltfläche, echtes Pinch-Zoom über einen zugänglichen Skalierungswert und anschließendes Pan ohne Alt-Text-Fehlauslösung. Speicher- und Diskcache werden befüllt und gemeinsam gelöscht; Statistik danach jeweils null Byte.

UI-Tests haben voneinander isolierte Caches. Ein erster Testlauf zeigte, dass der Coil-Standarddiskcache eine vorherige erfolgreiche Originalantwort in den Fehlerpfadtest übernahm; der Testaufbau wurde isoliert. Der echte Cache-Test verwendet weiterhin den produktiven Cache-Service.

ZoomImage wird per Comic und Datensparmodus neu gehalten. Bei Comic-Wechsel werden alte Image-Callbacks verworfen. Alt-Text ist zusätzlich unabhängig von Gesten zugänglich und scrollbar. Noch kein vollständiger App-Viewer-Screen oder API-23-/API-37-Nachweis.

## Noch offen

- Aufgaben 6 bis 10 und die vollständigen Material-3-Screens.
- API-23-/API-37-Geräteprüfungen.
- Veröffentlichte Altversion, Distributionsweg, passende Signatur und echter Update-Nachweis.
- Unabhängige Review benötigt ausdrückliche Freigabe gemäß Plan.
