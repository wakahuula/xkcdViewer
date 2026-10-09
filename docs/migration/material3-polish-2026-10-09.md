# Material 3 polish, 2026-10-09

The user approved targeted reader and settings refinements while retaining Material 3. This is not a navigation or architecture migration.

## Changes

- Favorites are directly available from the reader toolbar. The comic's favorite toggle is next to its title, with a filled heart for saved comics.
- Comic metadata and title use a smaller header. Random navigation uses a text button rather than a prominent filled button.
- Images are top-aligned in a stable zoom viewport. At fit scale, the alt-text button follows the image's natural aspect ratio; while zoomed, the button moves below the viewport. The button is measured before the image so large labels can wrap without clipping.
- Settings use consistent section labels and spacing. Theme and data-saver rows are toggleable with switch semantics, rather than requiring a tap on the switch itself.
- Android system-bar styles now react to live theme changes. API 23-25 use a dark navigation-bar scrim in light mode because dark navigation icons are unavailable there.

Existing actions, image fallback, retry, zoom, alt-text gestures, themes and persistence remain in scope for regression verification. Signing, release/update acceptance and Flutter removal remain deferred.

## Verification

The four initial UI regression tests failed against the original implementation for the intended reasons, then passed after the changes. Two system-bar regressions also failed before their fixes on API 23. An additional wide-image pinch test checks that zoom remains active after the viewport expands and does not open alt text.

The API-23 full run completed successfully with no failures and the expected API-29+ MediaStore skip. Shared JVM tests, legacy resources, debug assembly and lint also completed successfully. Lint retains eight warnings.

The independent review found that resizing the zoom viewport interrupted double-tap animations and constrained the large-font label. New tests reproduced both problems: double-tap stopped at 101%, and a tall-image label at fourfold font scale could not exceed 64 dp. The stable viewport and button-first measurement fix both regressions. The live system-bar test also now covers both theme directions and navigation icons on supported APIs. A second review found no Critical or Important issues.

Final sequential runs on API 37 and API 23 completed successfully with 34 passed tests and one expected skip each. API 37 skips the legacy navigation-scrim test; API 23 skips the API-29+ MediaStore test. The Gradle progress reporter counts a skip twice and reports 36 results for 35 test cases. The 35 shared JVM tests, resource validation, debug build and lint passed, with eight lint warnings and no errors.

One preceding API-37 full run failed in the unchanged export test `cancelledCallerDeletesLateDocumentThroughDocumentApi` because ActivityScenario remained PAUSED instead of reaching CREATED. All four export tests passed on a separate rerun, followed by a successful full suite. No export implementation or tests were changed for this result; the transient lifecycle failure remains recorded rather than treated as a diagnosed product defect.

Commands used, with JDK 21 and the Android SDK configured:

```powershell
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat :shared:verifyLegacyResources :shared:jvmTest :app:assembleDebug :app:lintDebug :app:connectedDebugAndroidTest
$env:ANDROID_SERIAL = 'emulator-5556'
.\gradlew.bat :app:connectedDebugAndroidTest
```

Final logs: `xkcd-polish-api37-reviewed-retry.log` and `xkcd-polish-api23-reviewed.log` in the local evidence directory below. Before the user-requested commit and push, both full device suites passed again with the same expected skips; these logs are `xkcd-polish-precommit-api37.log` and `xkcd-polish-precommit-api23.log`.

## Visual inspection

Actual API-23 reader, overflow menu and settings screens were inspected in dark and light mode. The system-bar legibility problem was reproduced during this inspection and checked again after the fix.

Local screenshots and verification logs are stored in `C:\Users\kosta\AppData\Local\Temp\opencode\`, with the prefix `xkcd-polish-`. Screenshots are device captures, not mockups. Existing large-font and landscape instrumentation coverage is retained; this is not a claim of manual tablet, foldable or screen-reader acceptance.
