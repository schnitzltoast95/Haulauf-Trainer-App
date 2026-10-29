# Confirmed implementation brief

Mode: Operate. Native Android, Kotlin/Compose. Preserve existing dark/gold identity, historical content and exercise IDs.

Approved scope: separate one-off sessions, saved training templates, exercise library, and global audio preferences. Keep common configuration visible, advanced options collapsible. Explicit transactional saves for templates and exercise details/audio/image; cancel preserves originals. Support new/copy/rename/edit/delete templates and creation/category/edit of custom exercises. Explain removal impacts and retain empty templates with a recovery action. Start countdown remains configurable; pause is the primary glove-size training action, stop available after pause. Resume uses existing countdown.

Keep existing 19 default moves and historical attribution; custom changes can be restored. Global language/voice/levels are distinct from per-session end tone/metronome/countdown; preserve legacy audio values until the user chooses global levels.

Verify persistence, editing identity, cancelled changes, delete cleanup, empty selection recovery, and normal training lifecycle. Capture real Android screens including increased font size. No browser rendering used as native evidence.

No new visual world or image assets required. Source baseline: 47cde57. Local working copy is isolated from the earlier chat checkout.

2026-10-29 distill refinement: compact duration/countdown summary; exercise selection is an expandable entry at the top, with a two-line preview when closed. Rounds, calls, interval and conditional round pause precede advanced options. Existing category selection, batch selection, weighting and all advanced controls remain available; no settings or stored data removed.
