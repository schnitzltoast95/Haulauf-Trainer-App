# Bug reports from the Android app

The floating bug icon opens the **Fehler melden / Report bug** form directly.
Submitting that form sends a structured text report to
`schnitzltoast95/Haulauf-Trainer-App` through GitHub's issue API. The reporter
does not need a GitHub account. The report includes the entered title and
description, optional reproduction steps and expected behavior, the screen and
training phase captured when the button was pressed,
app version, Android version, and device model.
Reports are public in the repository's Issues tab. Screenshots are not sent.
The button retains its purple appearance using the theme's `primaryContainer`
and `onPrimaryContainer` colors. It defaults to a compact 48 dp bug icon, flush
with an edge inside the safe drawing bounds. It can be dragged and follows with
a soft spring. On release it docks to the nearest of all four edges: left,
right, top or bottom. The chosen edge, horizontal and vertical position fractions,
and compact/expanded state persist across app restarts. Older saved left/right
positions remain the fallback when no new edge preference exists.
A text action in the form expands or minimizes the button. The expanded button
has a 12 dp edge margin, includes its label and a separate minimize arrow pointing
toward its docked edge. Opening the form or changing the button size does not
submit a report.

Validation: the menu-flow build passed seven unit tests and 27 device tests. A subsequent deletion-style adjustment passed its focused test. After restoring library thumbnails and implementing purple four-edge docking, both APK builds passed and the focused compact-report test passed (4.739 seconds), covering expansion, minimization and dragging to all four edges without submitting a report. Final normal and compact-screen captures were reviewed. A freely positioned overlay can cover underlying content; it can be moved to another edge.

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
