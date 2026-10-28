# Haulauf Trainer

Historical fencing solo training app for Android. The app announces moves via audio so you can train reaction speed and rhythm **hands-free**, even with the screen locked and the phone in your pocket.

## Features

- **Quickstart** – Start training immediately with your last used preset
- **Training Setup** – Configure rounds, units per round, reaction interval (fixed or random range), pause between rounds, move selection
- **Training Screen** – Large move display, counters, progress bar, pause/resume, stop
- **Background Audio** – Foreground service keeps audio playing with screen locked
- **Custom Move Audio** – Replace default TTS with file picker or record your own
- **Advanced Settings** – No immediate repetition, metronome, end beep, volume sliders (hidden under "Erweiterte Einstellungen")

## Custom Audio Assets

Add optional custom sounds to `app/src/main/res/raw/`:

- **tick.wav** – Metronome tick (fallback: system tone)
- **window_end.wav** – End-of-window beep (fallback: system tone)

Move calls use Text-to-Speech (TTS) by default; customize via the "Audio" button per move in Training Setup.

## Build

Ensure Java (JDK 17 recommended) and Android SDK are installed, then:

```
./gradlew assembleDebug
```

Open in Android Studio for development.

Before the next signed release build, complete [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md), including the audio check on the release candidate.

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
