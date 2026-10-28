# Release checklist

Complete the pre-build checks on an APK built from the source revision intended for release. Record the APK path, version, SHA-256, device, and result. A check from an earlier build does not replace this check.

## Before building the release

- [ ] Run `:app:testDebugUnitTest` and `:app:lintDebug`; resolve failures.
- [ ] Run the matching Android instrumentation tests on an authorized test device.
- [ ] **Audio must-check before the next release build:** Run a real training session on the candidate APK and listen to TTS move calls, countdown and end signals, and the metronome when enabled. Check pause/resume and a session with the screen locked. If custom audio is part of the release, play both an imported file and an in-app recording after restarting the app. Record the device, Android version, and result.
- [ ] Select Jonas where available, restart the app, and confirm that training calls still use his voice. Check that imported move audio remains unchanged and that the previous standard voice can be restored.
- [ ] Open a default move from each category (Hau, Hut, Stich). Check that its historical image loads offline, its context note is visible, and both source links open the matching original scan or text.
- [ ] Confirm that the foreground notification and training service disappear after both normal completion and manual stop.

## Release artifact

- [ ] Build and sign the release APK; verify package ID, version code/name, signing certificate, and SHA-256.
- [ ] Install and launch that exact signed APK on a test device. Keep its result separate from tests of the debug APK.
- [ ] Confirm that an audible move call plays from the signed APK.
