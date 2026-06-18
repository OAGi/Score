GitHub Integration
==================

connectCenter can link your core components to issues on GitHub and keep those issues informed as your work progresses.
The integration lets you do three things.
First, you can link one or more GitHub issues to a core component (ACC, ASCCP, BCCP, DT, code list, or agency ID list) so the issue and the component are connected.
Second, when you change a component's state, connectCenter can post a status comment — typically a change summary of what happened — onto each linked issue.
Third, if your administrator has turned on Projects board sync, changing a component's state can move the linked issue's card to a mapped column on a GitHub Projects v2 board.

Two prerequisites apply before you can use any of this.
The feature must be enabled by an administrator (see `Administrator setup <#administrator-setup>`__).
Each user connects their own personal GitHub account, and every comment and board move is made with that user's own GitHub token.
This means GitHub actions reflect *your* GitHub identity and *your* GitHub permissions.

All GitHub actions are best-effort.
A GitHub outage or a missing permission never blocks or rolls back a component state change — connectCenter simply skips the GitHub part and continues.

Connecting your GitHub account
------------------------------

You connect and disconnect GitHub from the *GitHub* box that appears on each component detail page.
There is no separate settings page, account page, or top-menu item for GitHub — the connection is managed entirely from this box.
Your connection is per user and global to your session, so once you connect, the connected state is shown on every component's GitHub box.

To connect your GitHub account:

1. Open the detail page of any core component (for example an ACC, ASCCP, BCCP, DT, code list, or agency ID list).

2. Find the *GitHub* box below the *Definition* field.

3. Click the "Connect GitHub" button.
   connectCenter redirects you to GitHub to authorize the connection, then returns you to the same page.

4. On the GitHub consent screen, review and grant the requested access.

Once connected, the box header shows the GitHub mark, the title "GitHub", and your account as ``@<login>`` in bold, followed by a "Disconnect" button.

.. Screenshot to add — capture media/github_integration_connect.png (the GitHub box header in
   both the disconnected "Connect GitHub" state and the connected "@login / Disconnect" state),
   then re-enable an image directive here.

To disconnect, click the "Disconnect" button in the box header.
Disconnecting removes your stored GitHub token from connectCenter.
Any issues already linked to your components remain visible in read-only form after you disconnect.

..

   Note that if your administrator turns on Projects board sync after you have already connected, you must disconnect and connect again so that connectCenter can request the additional GitHub permissions board sync needs.

Linking GitHub issues to a Core Component
-----------------------------------------

You link and unlink issues from the *GitHub* box on a component's detail page.
The box appears in the detail editor of every supported component type — ACC, ASCCP, BCCP, DT, code list, and agency ID list — directly below the *Definition* field.
It does not appear on the list or grid pages, only on the detail editors.
See `Core Component Management <#core-component-management>`__ for how to open a component's detail page.

The *GitHub* box is always shown when the integration is enabled, at minimum in read-only form.

.. Screenshot to add — capture media/github_integration_issue_box.png (the GitHub issue box on a
   component detail page showing one or more linked issues), then re-enable an image directive here.

What the box shows
~~~~~~~~~~~~~~~~~~~

For each linked issue, the box shows:

-  A link in the form ``owner/repo#number`` that opens the issue in a new browser tab.

-  An optional issue *type* chip, the issue *title*, and an optional *milestone* chip (dimmed if the milestone is closed).

-  The issue *state* badge (for example ``open`` or ``closed``).

-  Up to three *assignee* avatars (each with an ``@login`` tooltip).

-  The issue's GitHub *labels*, shown in their real GitHub colors.

While the issue data is loading the box shows "Loading issues…".
When no issues are linked it shows "No issues linked.".
Issue titles, states, and labels are refreshed live from GitHub when you open the page, so they stay current without a reload.

Linking an issue
~~~~~~~~~~~~~~~~~

You can link issues only when all of the following are true: the box belongs to the component you are viewing (not a tree-expanded child node), the component is in the **WIP** state, you are connected to GitHub, and you are the component's owner.
If any of these is not true, the box stays read-only and the *Link* controls do not appear.

To link an issue:

1. In the *GitHub* box, locate the input with the placeholder "Issue #, owner/repo#number, or URL".

2. Enter the issue in any of these forms:

   1. A bare issue number (for example ``123`` or ``#123``). connectCenter resolves it against the configured default repository, so for issues that live in that repository you only need the number — the repository is filled in for you.

   2. ``owner/repo#number`` (for example ``owner/repo#123``), to link an issue in a different repository.

   3. A full GitHub issue URL (for example ``https://github.com/owner/repo/issues/123``), which you can paste straight from your browser's address bar.

3. Press Enter, or click the "Link" button.

The same issue can be linked to any number of components, and a single component can have many linked issues.
Linking the same issue to the same component twice has no effect.

If you enter something connectCenter cannot parse, it shows: "Enter an issue number, owner/repo#number, or a GitHub issue URL.".
If the link cannot be created, it shows: "Failed to link the issue. Check the number/repository and that you have access.".

Unlinking an issue
~~~~~~~~~~~~~~~~~~~

To unlink an issue, click the unlink button (the **×** glyph, labeled "Unlink") on that issue's row.
As with linking, unlinking is available only when you own the component and it is in the **WIP** state.
Unlinking removes the connection in connectCenter; it does not delete or close the issue on GitHub.

What happens when a component changes state
-------------------------------------------

When you change a component's state — for example moving it from one stage toward release, or cancelling a revision — connectCenter can act on each linked issue.
Two independent, best-effort actions can run for each linked issue.

Status comments and change summaries
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

When you confirm a state change, connectCenter posts the comment you confirmed in the dialog — verbatim — onto every linked issue.
For common transitions the dialog pre-fills this comment with a rendered *change summary* describing what changed (for example a component moving to Candidate, being reverted to WIP, or a revision being cancelled).
You can freely edit this Markdown text before confirming.

A comment is posted only if the comment box is non-blank.
Clearing the comment box is how you opt out — if you clear it, nothing is posted.
Each state transition posts a brand-new comment; comments are not edited in place, so re-promoting a component after a revert posts another comment, by design, with each comment documenting one transition.

Projects board column sync
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If your administrator has enabled Projects board sync, each linked issue's card moves to a board column that matches the component's new state.
As a component moves through its lifecycle, the linked issue's card slides across the configured columns; backward transitions move it back as well.
The default mapping is:

.. list-table::
   :header-rows: 1
   :widths: 30 70

   * - Component state
     - Board column
   * - WIP
     - ``Implementing``
   * - Draft
     - ``Implemented``
   * - Candidate
     - ``Candidate``
   * - ReleaseDraft
     - ``Ready for release``
   * - Published
     - card removed from the board

A newly linked issue is placed in an initial column (the default option, ``New``), and a cancelled revision resets the card back to that initial column.
A column named ``Member review`` acts as a maintainer-owned gate: an automatic move never overwrites a card sitting there, so a reviewer's manual placement is preserved unless you explicitly override it (see below).
Board moves use your own GitHub token, so they require that you connected with the board-write permission.

The state-change dialog and field-option override
--------------------------------------------------

When the integration is enabled and you change a component's state, connectCenter opens a richer state-change dialog instead of the plain confirmation.
The same dialog is used for the *Update state* action on a detail page, for the bulk *Update* action on the core component and DT lists, and for *Cancel revision/amendment*.

The dialog title and confirmation text describe the transition — for example "Update state to 'Candidate'?" with "Are you sure you want to update the state to 'Candidate'?", and for a cancel it adds "Warning: all work done in this revision will be permanently removed and cannot be recovered.".
You confirm with the action button (typically "Update") or dismiss with "Cancel".

.. Screenshot to add — capture media/github_integration_state_change_dialog.png (the state-change
   dialog with a linked issue, the Projects override dropdown, and the comment editor), then
   re-enable an image directive here.

The GitHub section of the dialog only appears when at least one of the components you are changing has a linked issue.
If none do, the dialog falls back to a plain confirmation with no GitHub controls.
While linked issues load, the dialog shows "Loading linked GitHub issues…".

What the dialog shows
~~~~~~~~~~~~~~~~~~~~~~

For each component being changed, the dialog shows a header with the component type, its name, and the plain transition ``<current state> → <to state>`` (a cancel shows the destination as "Cancelled").
Below that it lists each linked issue with its ``owner/repo#number`` link, title, and state badge.

The field-option override
~~~~~~~~~~~~~~~~~~~~~~~~~~~

If Projects board sync is available to you, the dialog also shows a GitHub-style *Projects* card for each component.
The card shows the board title, a status row labeled with the board field name (for example "Status"), and the *destination* board column the issue will move to.

When the destination column is one of the board's options, it is shown as an editable dropdown.
You can pick a different column to override where this component's linked issues move for this one transition.
The dropdown's tooltip reads: "{fieldName} — pick another to override where the linked issue(s) move".
If you do not change it, connectCenter uses the configured column for the new state.
An override you choose takes precedence over every default — including the per-state mapping, the cancel reset, and the remove-on-release behavior — and is applied even past the ``Member review`` gate column, because it is a deliberate choice.

The dialog shows only the *destination* column, not each issue's current column.

The status-post comment editor
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

When you are connected and the component has linked issues, the dialog shows a GitHub-style comment box with *Write* and *Preview* tabs and a Markdown formatting toolbar.
The textarea is pre-filled with the rendered status post (for common transitions) and carries the placeholder "Leave a comment on the linked GitHub issues…".
A footer reminds you: "Markdown is supported — clear the comment to post nothing", alongside a character counter.

If the pre-fill cannot be generated, the box shows "Couldn't pre-fill the post — write the comment yourself or cancel.".
When you confirm, the comment you leave is posted verbatim to every linked issue; clearing it posts nothing.

Permission warnings
~~~~~~~~~~~~~~~~~~~~~

The dialog warns you when a GitHub action cannot be performed with your account.

-  If you cannot access an issue's repository, that issue shows: "No access to {owner}/{repo} with your GitHub account — a status comment can't be posted here.".

-  If you do not have permission to write to the project board, the section shows: "Your GitHub account (@{login}) doesn't have permission to update the OAGi project board (you may not be an organization member or collaborator), so the linked issues' fieldOption won't be changed.".

If you connected your GitHub account before board sync was turned on, your token will not have the board-write permission and board moves will be silently skipped.
Disconnect and reconnect to grant the additional permissions.

Administrator setup
-------------------

This subsection is for administrators enabling the integration.
All GitHub configuration is supplied to the backend through environment variables at deploy time.
None of these variables are pre-wired in the default deployment, so you must add them to your backend service's environment yourself.

GitHub OAuth App prerequisites
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Before turning the feature on, prepare the following on GitHub:

1. Create a GitHub OAuth App (under *Settings → Developer settings → OAuth Apps*) and obtain its **Client ID** and **Client secret**.

2. Set the OAuth App's *Authorization callback URL* to match your ``GITHUB_REDIRECT_URI`` (for example ``https://<your-host>/api/integration/github/callback``).

3. Choose a default repository that the issue-link box pre-fills.

4. If you want Projects board sync, prepare a Projects v2 board (org or user) with a single-select field (default name ``Status``) whose options include the configured column names.
   Users must grant the additional ``project`` and ``read:org`` scopes; connectCenter requests these automatically when board sync is on, and users who connected earlier must reconnect.

5. Configure a webhook on the repository or organization that delivers ``issues`` events to ``/api/integration/github/webhook``, using a secret that matches ``GITHUB_WEBHOOK_SECRET``.
   The webhook keeps cached issue metadata fresh; the integration still works without it, refreshing issue data when a page is opened.

The base OAuth scope is ``repo``, which covers reading issues and posting comments.
When board sync is on, connectCenter automatically also requests the ``project`` and ``read:org`` scopes at connect time.

Feature flags
~~~~~~~~~~~~~~

Two flags gate the integration:

-  ``SCORE_GITHUB_ENABLED`` (default ``false``) is the master switch.
   When it is off, no GitHub UI renders anywhere in connectCenter and no GitHub calls are made.
   Even when it is on, the feature stays hidden until both a client id and a client secret are supplied.

-  ``SCORE_GITHUB_PROJECT_ENABLED`` (default ``false``) is the board-sync sub-switch.
   Issue linking and commenting work with only the master switch and credentials.
   Turning this on additionally enables Projects v2 board column sync and the override dropdown, and causes the connect flow to request the ``project`` and ``read:org`` scopes.

Configuration keys
~~~~~~~~~~~~~~~~~~~

The following environment variables configure the integration.

.. list-table::
   :header-rows: 1
   :widths: 32 38 30

   * - Environment variable
     - Default
     - Controls
   * - ``SCORE_GITHUB_ENABLED``
     - ``false``
     - Master on/off switch for the whole integration.
   * - ``GITHUB_CLIENT_ID``
     - *(empty)*
     - OAuth App client id.
   * - ``GITHUB_CLIENT_SECRET``
     - *(empty)*
     - OAuth App client secret.
   * - ``GITHUB_OAUTH_SCOPE``
     - ``repo``
     - Base OAuth scope requested at connect time.
   * - ``SCORE_GITHUB_DEFAULT_REPO_URL``
     - ``https://github.com/OAGi/oagis``
     - Default repository the issue-link box offers.
   * - ``GITHUB_REDIRECT_URI``
     - ``http://localhost:4200/api/integration/github/callback``
     - OAuth callback URL; must match the OAuth App's registered callback.
   * - ``SCORE_WEB_BASE_URL``
     - ``http://localhost:4200``
     - The SPA base the callback returns the user to.
   * - ``GITHUB_WEBHOOK_SECRET``
     - *(empty)*
     - HMAC-SHA256 secret verifying inbound webhook payloads.
   * - ``SCORE_GITHUB_PROJECT_ENABLED``
     - ``false``
     - On/off for Projects v2 board column sync.
   * - ``SCORE_GITHUB_PROJECT_URL``
     - ``https://github.com/orgs/OAGi/projects/8``
     - The target Projects v2 board.
   * - ``SCORE_GITHUB_PROJECT_STATUS_FIELD``
     - ``Status``
     - Name of the board's single-select field to drive.
   * - ``SCORE_GITHUB_PROJECT_DEFAULT_FIELD_OPTION``
     - ``New``
     - Initial/reset column for newly linked issues and cancelled revisions.

The OAuth ``authorization-uri``, ``token-uri``, and ``api-base-url`` have no environment variables and are effectively fixed to GitHub.com.

**Tip**: The ``project-default-field-option`` should match the board field's *Default* option.
GitHub's API does not expose the field's Default, so connectCenter cannot read it automatically.
Leave this blank to skip placing newly linked issues in an initial column at all.

State-to-column mapping
~~~~~~~~~~~~~~~~~~~~~~~~~

The ``project-field-option-by-state`` map ties a component's destination state to a board column (a single-select option name).
Each entry can be overridden by its own environment variable.

.. list-table::
   :header-rows: 1
   :widths: 22 38 40

   * - Component state
     - Environment variable
     - Default column
   * - WIP
     - ``SCORE_GITHUB_PROJECT_FIELD_OPTION_WIP``
     - ``Implementing``
   * - Draft
     - ``SCORE_GITHUB_PROJECT_FIELD_OPTION_DRAFT``
     - ``Implemented``
   * - Candidate
     - ``SCORE_GITHUB_PROJECT_FIELD_OPTION_CANDIDATE``
     - ``Candidate``
   * - ReleaseDraft
     - ``SCORE_GITHUB_PROJECT_FIELD_OPTION_RELEASE_DRAFT``
     - ``Ready for release``

The column names you configure must match options of the board's single-select field.
Two behaviors are fixed and cannot be configured: a card is removed from the board only on release (when a component reaches **Published**) or on unlink, and the option ``Member review`` is always treated as a maintainer-owned gate that automatic moves never overwrite.
