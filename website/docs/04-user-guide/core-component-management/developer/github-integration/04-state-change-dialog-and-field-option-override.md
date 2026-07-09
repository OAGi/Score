---
title: "The state-change dialog and field-option override"
sidebar_position: 4
---

When the integration is enabled and you change a component's state, connectCenter opens a richer state-change dialog instead of the plain confirmation.
The same dialog is used for the *Update state* action on a detail page, for the bulk *Update* action on the core component and DT lists, and for *Cancel revision/amendment*.

The dialog title and confirmation text describe the transition — for example "Update state to 'Candidate'?" with "Are you sure you want to update the state to 'Candidate'?", and for a cancel it adds "Warning: all work done in this revision will be permanently removed and cannot be recovered.".
You confirm with the action button (typically "Update") or dismiss with "Cancel".

The GitHub section of the dialog only appears when at least one of the components you are changing has a linked issue.
If none do, the dialog falls back to a plain confirmation with no GitHub controls.
While linked issues load, the dialog shows "Loading linked GitHub issues…".

## What the dialog shows

For each component being changed, the dialog shows a header with the component type, its name, and the plain transition `<current state> → <to state>` (a cancel shows the destination as "Cancelled").
Below that it lists each linked issue with its `owner/repo#number` link, title, and state badge.

## The field-option override

If Projects board sync is available to you, the dialog also shows a GitHub-style *Projects* card for each component.
The card shows the board title, a status row labeled with the board field name (for example "Status"), and the *destination* board column the issue will move to.

When the destination column is one of the board's options, it is shown as an editable dropdown.
You can pick a different column to override where this component's linked issues move for this one transition.
The dropdown's tooltip reads: "{fieldName} — pick another to override where the linked issue(s) move".
If you do not change it, connectCenter uses the configured column for the new state.
An override you choose takes precedence over every default — including the per-state mapping, the cancel reset, and the remove-on-release behavior — and is applied even past the `Member review` gate column, because it is a deliberate choice.

The dialog shows only the *destination* column, not each issue's current column.

## The status-post comment editor

When you are connected and the component has linked issues, the dialog shows a GitHub-style comment box with *Write* and *Preview* tabs and a Markdown formatting toolbar.
The textarea is pre-filled with the rendered status post (for common transitions) and carries the placeholder "Leave a comment on the linked GitHub issues…".
A footer reminds you: "Markdown is supported — clear the comment to post nothing", alongside a character counter.

If the pre-fill cannot be generated, the box shows "Couldn't pre-fill the post — write the comment yourself or cancel.".
When you confirm, the comment you leave is posted verbatim to every linked issue; clearing it posts nothing.

## Permission warnings

The dialog warns you when a GitHub action cannot be performed with your account.

- If you cannot access an issue's repository, that issue shows: "No access to {owner}/{repo} with your GitHub account — a status comment can't be posted here.".

- If you do not have permission to write to the project board, the section shows: "Your GitHub account (@{login}) doesn't have permission to update the OAGi project board (you may not be an organization member or collaborator), so the linked issues' fieldOption won't be changed.".

If you connected your GitHub account before board sync was turned on, your token will not have the board-write permission and board moves will be silently skipped.
Disconnect and reconnect to grant the additional permissions.
