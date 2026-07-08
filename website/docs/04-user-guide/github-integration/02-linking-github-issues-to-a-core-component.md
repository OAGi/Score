---
title: "Linking GitHub issues to a Core Component"
sidebar_position: 2
---

You link and unlink issues from the *GitHub* box on a component's detail page.
The box appears in the detail editor of every supported component type — ACC, ASCCP, BCCP, DT, code list, and agency ID list — directly below the *Definition* field.
It does not appear on the list or grid pages, only on the detail editors.
See [Core Component Management](../core-component-management/01-core-component-in-brief.md) for how to open a component's detail page.

The *GitHub* box is always shown when the integration is enabled, at minimum in read-only form.

## What the box shows

For each linked issue, the box shows:

- A link in the form `owner/repo#number` that opens the issue in a new browser tab.

- An optional issue *type* chip, the issue *title*, and an optional *milestone* chip (dimmed if the milestone is closed).

- The issue *state* badge (for example `open` or `closed`).

- Up to three *assignee* avatars (each with an `@login` tooltip).

- The issue's GitHub *labels*, shown in their real GitHub colors.

While the issue data is loading the box shows "Loading issues…".
When no issues are linked it shows "No issues linked.".
Issue titles, states, and labels are refreshed live from GitHub when you open the page, so they stay current without a reload.

## Linking an issue

You can link issues only when all of the following are true: the box belongs to the component you are viewing (not a tree-expanded child node), the component is in the **WIP** state, you are connected to GitHub, and you are the component's owner.
If any of these is not true, the box stays read-only and the *Link* controls do not appear.

To link an issue:

1. In the *GitHub* box, locate the input with the placeholder "Issue #, owner/repo#number, or URL".

2. Enter the issue in any of these forms:

    1. A bare issue number (for example `123` or `#123`). connectCenter resolves it against the configured default repository, so for issues that live in that repository you only need the number — the repository is filled in for you.

    2. `owner/repo#number` (for example `owner/repo#123`), to link an issue in a different repository.

    3. A full GitHub issue URL (for example `https://github.com/owner/repo/issues/123`), which you can paste straight from your browser's address bar.

3. Press Enter, or click the "Link" button.

The same issue can be linked to any number of components, and a single component can have many linked issues.
Linking the same issue to the same component twice has no effect.

If you enter something connectCenter cannot parse, it shows: "Enter an issue number, owner/repo#number, or a GitHub issue URL.".
If the link cannot be created, it shows: "Failed to link the issue. Check the number/repository and that you have access.".

## Unlinking an issue

To unlink an issue, click the unlink button (the **×** glyph, labeled "Unlink") on that issue's row.
As with linking, unlinking is available only when you own the component and it is in the **WIP** state.
Unlinking removes the connection in connectCenter; it does not delete or close the issue on GitHub.
