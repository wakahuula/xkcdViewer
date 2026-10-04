# Android/KMP-Grundlage, Stufen 1 und 2

Stand: 2026-10-04. Branch: `migration/kmp-android-foundation`.

Die Aufgaben 1 und 2 des [Implementierungsplans](../superpowers/plans/2026-10-03-kmp-migration.md) sind umgesetzt. Dies ist eine Entwicklungsgrundlage mit Starterbildschirm, noch kein funktionsfähiger Comic-Viewer. Flutter-Quellen und historische iOS-Dateien bleiben bis zur späteren Funktionsablösung erhalten.

## Verifikation

Ausgeführt mit JDK 21.0.12.1 und Gradle 9.5.0:

```powershell
.\gradlew.bat --version
.\gradlew.bat -p build-logic test --rerun-tasks
.\gradlew.bat :shared:verifyLegacyResources :shared:jvmTest :app:assembleDebug :app:lintDebug
```

- Beide Test-/Build-Aufrufe erfolgreich. Sieben Generator-Tests und sechs gemeinsame/JVM-Tests, keine Fehler.
- Die Generator-Tests wurden vor der Implementierung mit fehlenden Produktionssymbolen ausgeführt und scheiterten erwartungsgemäß. Dasselbe gilt für Contributor- und Ressourcenintegrationstests. Bei Aufgabe 1 fehlte zunächst der Root-Wrapper.
- Alle neun Originalübersetzungen mit je 32 Schlüsseln wurden nach der tatsächlichen Compose-Verarbeitung per `getString` gegen die Original-JSONs geprüft. Neue Texte sind daneben vorhanden.
- Sprachfallbacks für `de-AT`, `uk` und eine unbekannte Sprache funktionieren im JVM-Ressourcentest. Die Contributor-Liste wird aus dem gepackten Original geladen; Namen, Reihenfolge und Profil-URLs bleiben erhalten.
- Fonts und Contributor-JSON werden bytegleich kopiert. `git hash-object` stimmt für alle 18 Originaldateien unter `assets/` und den Launcher-Icon-Verzeichnissen mit Git überein. `git diff -- assets` ist leer.
- APK-Manifest mit `apkanalyzer manifest print` geprüft: Paket `de.stoupas.xkcd`, Name `xkcdViewer`, minSdk 23, targetSdk 37, `de.stoupas.xkcd.MainActivity`.
- APK: `android/app/build/outputs/apk/debug/app-debug.apk`.
- Lint erfolgreich mit fünf Warnungen: drei Versionshinweise und zwei Hinweise auf beibehaltene Legacy-Launch-Ressourcen. Keine Lint-Fehler.
- Release-Sperre separat geprüft. `:app:verifyReleaseConfiguration` scheitert mit der beabsichtigten Meldung, dass veröffentlichte Version und ursprüngliche Signierung zuerst geprüft werden müssen.

## Entscheidungen und Abweichungen

- Der vom Benutzer gewünschte Branch wird im bestehenden Checkout verwendet, keine Worktree. `master` bleibt unverändert; es gibt keine Dateisystemisolation zwischen Branches.
- Nur Aufgaben 1 und 2 gehören zu diesem Durchlauf. Aufgaben 3 bis 10 und die spätere iOS-Ausführung sind nicht umgesetzt.
- Aufgabenprotokoll und Testlogs werden mit nativen Dateiwerkzeugen statt den Bash-Helfern gepflegt. Die Planüberschriften sind deutsch; die Helfer erwarten englische Task-Überschriften. Die Nachweise stehen zusätzlich hier.
- Direkte Compose-Koordinaten ersetzen veraltete Dependency-Aliase. Material 3 wird auf die vom ursprünglichen Alias aufgelöste Version 1.9.0 festgelegt; Compose Multiplatform bleibt 1.12.1. Ein späteres Versionsupdate erfordert erneute Kompatibilitätsprüfung.
- Releases sind bis zur Verifikation der veröffentlichten Version und Signatur gesperrt. Debug bleibt ausführbar; für einen Release muss diese Sperre später bewusst ersetzt werden.
- `customDirectory` ersetzt den Compose-Standardordner. Eine `Sync`-Task führt deshalb Originalressourcen aus `build/generated/legacyComposeResources` und neue Texte aus `src/commonMain/composeResources` unter `build/generated/appComposeResources` zusammen. Beide Ausgaben sind ausschließlich Build-Artefakte. Dateikollisionen scheitern explizit.
- JVM-Ressourcentests benötigen `compose.desktop.currentOs`, weil die Systemumgebung die native Skiko-Themeerkennung aufruft. Diese Abhängigkeit gilt nur für JVM-Tests, nicht für Android und nicht als Desktop-Produktziel.
- `.gitignore` schließt Build-Logic-Caches aus und erlaubt das Quellpaket `de/stoupas/xkcd/build`, das sonst von der historischen Regel `build/` versteckt wird. `.gitattributes` legt Zeilenenden für Wrapper und Kotlin-Dateien fest; Original-JSONs werden nicht umgeschrieben.
- Der Benutzer möchte eine neue, zum Comic-Viewer passende Material-3-UI. Die PNGs dienen als Funktions-/Inhaltsreferenz. Font-Dateien bleiben erhalten, FiraMono wird nicht als globale Schrift der späteren UI vorgeschrieben.

## Review und offene Prüfungen

Eigenreview gegen die Aufgaben 1 und 2, die freigegebene Spezifikation und den Code-Review-Leitfaden durchgeführt. Keine unabhängige Agentenreview, da der Plan Delegation ohne ausdrückliche Freigabe untersagt. Das ersetzt keine unabhängige Review vor einem Merge.

Zurückgestellt:

- Die fünf Lint-Warnungen. Versionsupdates und Entfernung alter Launch-Ressourcen sind nicht Teil dieser Grundlage.
- Android-Gerätetests für `de-AT`, `uk-UA`, `ja-JP` und unbekannte Sprache sowie visuelle Prüfung des Starterbildschirms. Die JVM-Prüfung ist kein Nachweis für die Darstellung auf Android.
- Kein Upgrade- oder Nutzerdatenimport-Nachweis. Favoriten, Einstellungen und Legacy-Import folgen in späteren Aufgaben.
- Die tatsächliche veröffentlichte Version, Signatur und Vertriebswege sind noch nicht nachgewiesen. Kein Release oder produktives Update freigegeben.

Der bestehende uncommittete `implementation-handoff.md` wurde nicht verändert. Es wurde weder gemergt noch gepusht.
