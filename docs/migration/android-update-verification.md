# Release und Update: noch nicht freigegeben

Der Android-Debug-Viewer ist implementiert. Ein Update über eine tatsächlich veröffentlichte Flutter-App ist **nicht nachgewiesen**.

## Release-Guard

`android/key.properties` bleibt die lokale private Quelle für `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. Relative Keystore-Pfade beziehen sich wie zuvor auf `android/`, nicht auf `android/app/`. Keine neuen Release-Keystores wurden erzeugt. Debug verwendet die normale lokale Debug-Signatur.

Erst nach Artefaktprüfung werden diese öffentlichen Gradle-Properties gesetzt:

```properties
xkcd.previousVersionCode=<bestätigte veröffentlichte Altversion>
xkcd.releaseVersionCode=<größere neue Versionsnummer>
xkcd.releaseVersionName=<gewählte Release-Version ohne dev-Zusatz>
```

Ohne diese Prüfung bleibt die Standardversion `13 / 1.4.6-kmp-dev`. `:app:verifyReleaseConfiguration` verhindert Release-Builds mit fehlenden Signingfeldern, fehlendem Originalkeystore, unbekannter Altversion, nicht erhöhter Versionsnummer oder Entwicklungs-VersionName. Fehlertexte enthalten keine Passwörter. Release ist auf R8-Minifizierung und Resource-Shrinking vorbereitet, aber noch nicht als Artefakt geprüft.

2026-10-04: zehn Build-Logic-Tests erfolgreich. Drei neue Tests prüfen Pflichtfelder/Geheimnisse, relative Pfade/Versionsvergleich und fehlende Keystore-Dateien. Tests liefen vor Implementierung mit fehlendem Validator rot.

`.\gradlew.bat :app:assembleRelease` scheitert erwartungsgemäß am Guard:

```text
Release blocked: missing signing property storeFile in android/key.properties
```

## Benötigte lokale Artefakte

- Tatsächlicher Distributionsweg, etwa Play Store oder direkt verteilte APK.
- Alte veröffentlichte APK zur Prüfung von versionCode, Paket-ID und öffentlichem Zertifikat.
- Ursprüngliche Signingdateien lokal, nicht Passwörter im Chat. Bei Play App Signing muss der passende Store-Testkanal verwendet werden; Upload-Key und installierter App-Signing-Key sind nicht gleich.

Dann: Alt-APK mit apkanalyzer prüfen, Zertifikat mit apksigner vergleichen, dediziertes Testgerät mit Alt-App und geänderten Favoriten/Theme/Akzent/Datensparmodus vorbereiten, passende neue Release-APK mit `adb install -r` darüber installieren. Kein Uninstall, kein `pm clear`. Werte und Änderungen nach Neustart sowie minifizierte Lizenzseite prüfen.

## Weitere Abnahmeblocker

Abschließender Debug-Lauf nach Guard-Verdrahtung: `:shared:verifyLegacyResources :shared:jvmTest :app:assembleDebug :app:lintDebug` erfolgreich. Vergleich von `assets`, `LICENSE`, `CODE_OF_CONDUCT.md` und `PRIVACY_POLICY.md` mit Spec-Commit `2ccfc83` unverändert. `git diff --check` ohne Befund.

API 37 ist durch wiederholten Emulator-Systemabsturz blockiert, siehe [Gerätebericht](android-verification.md). API 23 und 36 wurden getestet. Flutter-Dateien bleiben bis zu den noch fehlenden Funktionsnachweisen erhalten; sie sind nicht Teil des Android-Root-Builds. Keine Release-, Merge- oder Push-Freigabe behauptet.
