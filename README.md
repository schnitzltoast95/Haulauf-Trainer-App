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
