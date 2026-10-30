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

## Release showcase video (required for every future release)

User preference recorded on 2026-10-30: every future release includes a short video showcasing its changes, using the installed **brag** skill.

- [ ] Locate and read the current `brag/SKILL.md` and follow its referenced planning, Hyperframes, validation and delivery instructions. Resolve supporting files relative to that skill directory; do not hard-code a plugin version. If the skill or rendering dependencies are unavailable, report the blocker rather than silently skipping the video.
- [ ] Compare the release commit with the previous published release. Choose the most useful changes actually included in this release; do not advertise open issues or planned features. For a maintenance release, show the improved behavior without inventing new features.
- [ ] Make a concise German storyboard, normally 15–25 seconds, with readable captions and 2–3 highlights where appropriate. Default to vertical format for this phone app, a polished tone and no voiceover unless requested.
- [ ] Show real screens or recordings from the matching release candidate on an Android device/emulator. Preserve the app's actual appearance; do not substitute an invented interface. Use demonstration content without private notes, personal media or notifications. Capture historical illustrations with their existing attribution/context and retain source links in accompanying copy when used.
- [ ] Follow brag's composition checks, render the MP4 and inspect the full result for accurate UI, readable text, pacing and audio levels. Pick a meaningful poster and bake it into frame zero as required by brag. Keep application sounds distinct from any soundtrack; use only appropriately licensed audio.
- [ ] Save the video, poster, storyboard and share copy under a version-specific local artifact directory outside the tracked source tree. Preserve previous releases' outputs.
- [ ] Upload the MP4 and poster alongside the signed APK and checksum. Link the showcase from the GitHub release notes, verify the uploaded assets and record their URLs in the release evidence. A release is not complete without its showcase unless the user explicitly waives it for that release.
