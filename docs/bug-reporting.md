# Bug reports from the Android app

The floating **Fehler melden / Report bug** button sends a structured text report to
`schnitzltoast95/Haulauf-Trainer-App` through GitHub's issue API. The reporter
does not need a GitHub account. The report includes the entered title and
description, optional reproduction steps and expected behavior, the screen and
training phase captured when the button was pressed,
app version, Android version, and device model.
Reports are public in the repository's Issues tab. Screenshots are not sent.
The button can be dragged; it follows with a soft spring, docks to the nearest
side after release, and remembers its side and height across app restarts.

## Configure a local build

Generate a **classic** personal access token with only the `public_repo` scope
using the dedicated reporting account. Do not add that account as a collaborator
to the app repository. In the project root's ignored `local.properties`, add:

```properties
haulauf.bugReportToken=YOUR_TOKEN_HERE
```

Keep any existing `sdk.dir` entry. Rebuild the APK after adding or rotating the
token. The button is hidden in builds without a configured token.
An isolated build may instead supply `HAULAUF_BUG_REPORT_TOKEN` as a temporary
environment variable, so the token file does not need to be copied into it.

The token is embedded in the APK and can be extracted. Its `public_repo` scope
is broader than issue creation and cannot be restricted to this repository;
keep the reporting account separate and without write access to the app code.
If it is abused, revoke the token, create a replacement, and ship a new APK.
