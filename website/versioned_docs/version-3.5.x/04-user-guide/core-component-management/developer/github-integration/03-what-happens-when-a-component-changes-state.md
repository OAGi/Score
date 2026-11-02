---
title: "What happens when a component changes state"
sidebar_position: 3
---

When you change a component's state — for example moving it from one stage toward release, or cancelling a revision — connectCenter can act on each linked issue.
Two independent, best-effort actions can run for each linked issue.

## Status comments and change summaries

When you confirm a state change, connectCenter posts the comment you confirmed in the dialog — verbatim — onto every linked issue.
For common transitions the dialog pre-fills this comment with a rendered *change summary* describing what changed (for example a component moving to Candidate, being reverted to WIP, or a revision being cancelled).
You can freely edit this Markdown text before confirming.

A comment is posted only if the comment box is non-blank.
Clearing the comment box is how you opt out — if you clear it, nothing is posted.
Each state transition posts a brand-new comment; comments are not edited in place, so re-promoting a component after a revert posts another comment, by design, with each comment documenting one transition.

## Projects board column sync

If your administrator has enabled Projects board sync, each linked issue's card moves to a board column that matches the component's new state.
As a component moves through its lifecycle, the linked issue's card slides across the configured columns; backward transitions move it back as well.
The default mapping is:

| Component state | Board column |
| --- | --- |
| WIP | `Implementing` |
| Draft | `Implemented` |
| Candidate | `Candidate` |
| ReleaseDraft | `Ready for release` |
| Published | card removed from the board |

A newly linked issue is placed in an initial column (the default option, `New`), and a cancelled revision resets the card back to that initial column.
A column named `Member review` acts as a maintainer-owned gate: an automatic move never overwrites a card sitting there, so a reviewer's manual placement is preserved unless you explicitly override it (see below).
Board moves use your own GitHub token, so they require that you connected with the board-write permission.
