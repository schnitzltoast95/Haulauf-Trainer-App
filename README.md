# Haulauf Trainer

Historical fencing solo training app for Android. The app announces moves via audio so you can train reaction speed and rhythm **hands-free**, even with the screen locked and the phone in your pocket.

## Features

- **Quickstart** – Start training immediately with your last used preset
- **Training Setup** – Configure rounds, units per round, reaction interval (fixed or random range), pause between rounds, move selection
- **Training Screen** – Glove-size pause/resume controls, with stop available after pausing; countdown when starting and resuming
- **Background Audio** – Foreground service keeps audio playing with screen locked
- **Custom Move Audio** – Replace default TTS with file picker or record your own
- **Training library** – Explicitly save changes to a template, create a copy, or start a one-off session without replacing the template
- **Exercise library** – Search and category filters; create custom moves; edit their metadata and manage personal media, notes and audio in separate editors
- **Explicit exercise saves** – Save or discard changes per section; original default descriptions, images and sources remain protected
- **Safe removal** – Deleting a custom move names affected templates and removes references; empty templates remain editable
- **Advanced Settings** – Collapsible no-immediate-repetition, metronome, end beep and start countdown; move weighting is separately revealed
- **Global audio levels** – Shared levels in Settings; existing per-training levels are retained until shared levels are selected and can be restored

## Custom Audio Assets

Add optional custom sounds to `app/src/main/res/raw/`:

- **tick.wav** – Metronome tick (fallback: system tone)
- **window_end.wav** – End-of-window beep (fallback: system tone)

Pass their compiled resource IDs as `tickResource = R.raw.tick` and
`windowEndResource = R.raw.window_end` when constructing `TrainingAudioManager`.
With no IDs supplied, the existing system-tone fallbacks remain active.

Move calls use Text-to-Speech (TTS) by default. Open an exercise from the exercise library to preview its announcement or stage an audio recording/file and save it in the separate announcement editor.

## Build

Ensure Java (the Gradle daemon toolchain is configured for JDK 21) and Android SDK are installed, then:

```
./gradlew assembleDebug
```

Open in Android Studio for development.

Before the next signed release build, complete [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md), including the audio check on the release candidate.

Every future release also includes a short German showcase video made with the `brag` skill: real app footage, the changes since the previous release, and a matching poster. Follow the video section of the checklist and publish these assets with the APK.

### Next release: 1.2.0 (versionCode 3)

This is preparation, not a published or fully approved release. The published 1.1.0 used versionCode 2; its historical checks are in [release-records/v1.1.0.md](release-records/v1.1.0.md). Track current release evidence separately in [release-records/v1.2.0.md](release-records/v1.2.0.md).

After the pre-build checks in RELEASE_CHECKLIST.md, build the regular `:app:assembleRelease` variant (without a review init script), then run `tools/sign-release.ps1 -Version 1.2.0` on the Windows build machine. The script checks the APK package/version, rejects versionCode 2 or lower, refuses to overwrite an existing release artifact and verifies the signing certificate against 1.1.0. Build Tools default to 36.0.0 and can be selected with `-BuildToolsVersion`.

The key and its DPAPI-protected password remain in `../release-signing/` outside this public repository; use `-SigningDirectory` for a different private location. Back up both privately. The DPAPI password file can only be decrypted by the same Windows account on the same installation; migrating to another PC also requires a separately secured, recoverable password. Never commit keys or passwords.

Use the same release key to update GitHub release 1.1.0 without removing user data. Only the older APK tracked in `app/release/` has a different certificate and requires a one-time uninstall. Do not uninstall a current release to work around an accidental debug-signing mismatch.

## Tests

Run the training unit tests, Android Lint, and build a matching app/test APK pair:

```
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
```

On an authorized Android test device, install both APKs from that build and run the instrumentation suite:

```
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -r com.example.myapplication.test/androidx.test.runner.AndroidJUnitRunner
```

The instrumentation APK must be paired with the debug app APK from the same build. A differently signed release APK cannot be updated with this debug APK; use a dedicated test device and remove only `com.example.myapplication` before installing when switching signatures.

## Isolated on-device review build

Build a separate app named **Haulauf Ablauf-Test** without replacing a release installation:

```sh
./gradlew -I tools/flow-review.init.gradle :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -r com.example.myapplication.flowreview.test/androidx.test.runner.AndroidJUnitRunner
```

This uses application ID `com.example.myapplication.flowreview`, with separate local data. The test suite creates its own temporary templates/exercises. Test APK and app APK must come from the same build. Device screenshots are written into the review app's external files directory. Use a review device for UI tests; they interact with the foreground screen.

Product requirements and the approved implementation scope are recorded in `PRODUCT.md` and `.impeccable/training-flow.md`.

## Combined version — 2026-10-29

All UI improvements are part of one app: home screen, training setup and controls,
training library, exercise library/editor, and shared audio settings. Each review
APK is a complete cumulative build, not a separate feature package. Installing a
new review APK updates the same **Haulauf Ablauf-Test** app and keeps its data.

The consolidated artifact is named `Haulauf-Gesamtstand-2026-10-29.apk`.
See [VALIDATION.md](VALIDATION.md) for its test results and fingerprint, and
[NEXT_STEPS.md](NEXT_STEPS.md) for the remaining work before a signed release.
