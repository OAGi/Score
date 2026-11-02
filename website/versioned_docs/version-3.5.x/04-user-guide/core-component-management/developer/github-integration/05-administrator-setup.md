---
title: "Administrator setup"
sidebar_position: 5
---

This subsection is for administrators enabling the integration.
All GitHub configuration is supplied to the backend through environment variables at deploy time.
None of these variables are pre-wired in the default deployment, so you must add them to your backend service's environment yourself.

## GitHub OAuth App prerequisites

Before turning the feature on, prepare the following on GitHub:

1. Create a GitHub OAuth App (under *Settings → Developer settings → OAuth Apps*) and obtain its **Client ID** and **Client secret**.

2. Set the OAuth App's *Authorization callback URL* to match your `GITHUB_REDIRECT_URI` (for example `https://<your-host>/api/integration/github/callback`).

3. Choose a default repository that the issue-link box pre-fills.

4. If you want Projects board sync, prepare a Projects v2 board (org or user) with a single-select field (default name `Status`) whose options include the configured column names.
   Users must grant the additional `project` and `read:org` scopes; connectCenter requests these automatically when board sync is on, and users who connected earlier must reconnect.

5. Configure a webhook on the repository or organization that delivers `issues` events to `/api/integration/github/webhook`, using a secret that matches `GITHUB_WEBHOOK_SECRET`.
   The webhook keeps cached issue metadata fresh; the integration still works without it, refreshing issue data when a page is opened.

The base OAuth scope is `repo`, which covers reading issues and posting comments.
When board sync is on, connectCenter automatically also requests the `project` and `read:org` scopes at connect time.

## Feature flags

Two flags gate the integration:

- `SCORE_GITHUB_ENABLED` (default `false`) is the master switch.
  When it is off, no GitHub UI renders anywhere in connectCenter and no GitHub calls are made.
  Even when it is on, the feature stays hidden until both a client id and a client secret are supplied.

- `SCORE_GITHUB_PROJECT_ENABLED` (default `false`) is the board-sync sub-switch.
  Issue linking and commenting work with only the master switch and credentials.
  Turning this on additionally enables Projects v2 board column sync and the override dropdown, and causes the connect flow to request the `project` and `read:org` scopes.

## Configuration keys

The following environment variables configure the integration.

| Environment variable | Default | Controls |
| --- | --- | --- |
| `SCORE_GITHUB_ENABLED` | `false` | Master on/off switch for the whole integration. |
| `GITHUB_CLIENT_ID` | *(empty)* | OAuth App client id. |
| `GITHUB_CLIENT_SECRET` | *(empty)* | OAuth App client secret. |
| `GITHUB_OAUTH_SCOPE` | `repo` | Base OAuth scope requested at connect time. |
| `SCORE_GITHUB_DEFAULT_REPO_URL` | `https://github.com/OAGi/oagis` | Default repository the issue-link box offers. |
| `GITHUB_REDIRECT_URI` | `http://localhost:4200/api/integration/github/callback` | OAuth callback URL; must match the OAuth App's registered callback. |
| `SCORE_WEB_BASE_URL` | `http://localhost:4200` | The SPA base the callback returns the user to. |
| `GITHUB_WEBHOOK_SECRET` | *(empty)* | HMAC-SHA256 secret verifying inbound webhook payloads. |
| `SCORE_GITHUB_PROJECT_ENABLED` | `false` | On/off for Projects v2 board column sync. |
| `SCORE_GITHUB_PROJECT_URL` | `https://github.com/orgs/OAGi/projects/8` | The target Projects v2 board. |
| `SCORE_GITHUB_PROJECT_STATUS_FIELD` | `Status` | Name of the board's single-select field to drive. |
| `SCORE_GITHUB_PROJECT_DEFAULT_FIELD_OPTION` | `New` | Initial/reset column for newly linked issues and cancelled revisions. |

The OAuth `authorization-uri`, `token-uri`, and `api-base-url` have no environment variables and are effectively fixed to GitHub.com.

**Tip**: The `project-default-field-option` should match the board field's *Default* option.
GitHub's API does not expose the field's Default, so connectCenter cannot read it automatically.
Leave this blank to skip placing newly linked issues in an initial column at all.

## State-to-column mapping

The `project-field-option-by-state` map ties a component's destination state to a board column (a single-select option name).
Each entry can be overridden by its own environment variable.

| Component state | Environment variable | Default column |
| --- | --- | --- |
| WIP | `SCORE_GITHUB_PROJECT_FIELD_OPTION_WIP` | `Implementing` |
| Draft | `SCORE_GITHUB_PROJECT_FIELD_OPTION_DRAFT` | `Implemented` |
| Candidate | `SCORE_GITHUB_PROJECT_FIELD_OPTION_CANDIDATE` | `Candidate` |
| ReleaseDraft | `SCORE_GITHUB_PROJECT_FIELD_OPTION_RELEASE_DRAFT` | `Ready for release` |

The column names you configure must match options of the board's single-select field.
Two behaviors are fixed and cannot be configured: a card is removed from the board only on release (when a component reaches **Published**) or on unlink, and the option `Member review` is always treated as a maintainer-owned gate that automatic moves never overwrite.
