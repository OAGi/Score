---
title: First Steps
sidebar_position: 3
---

You have connectCenter running — the frontend on the host port you mapped in
[Installation with Docker](installation-docker.md) (the production
`docker-compose.yml` maps the web UI to host port `4200`). This page walks you
through opening the app, signing in, and finding your way around so you can
start working with Core Components and BIEs.

## Open the application

Point your browser at the frontend URL for your deployment. For the production
`docker-compose.yml` in this repository, that is the host the web container is
mapped to on port **4200**, for example:

```text
http://localhost:4200/
```

If you used a different port mapping (for example a local dev server), use that
instead. See [Installation with Docker](installation-docker.md) and the
[Development Setup](../06-contributing/development-setup.md) guide for
the exact ports your setup exposes.

## Sign in

The first screen is the sign-in page, titled **"Sign in to NIST/OAGi
connectCenter"**. It has two ways to authenticate:

- **Local sign-in** — enter your **Username** and **Password**, then click
  **Sign in**.
- **Single Sign-On (SSO)** — if your administrator configured one or more
  OpenID Connect identity providers (see
  [OIDC Setup](oidc-setup.md)), a **"Sign in with
  &lt;provider&gt;"** button appears below the local form. The first SSO
  sign-in with an unknown account creates a *pending* request that an
  administrator must review and link. See the SSO section of the
  [Administration guide](../04-user-guide/administration/08-using-single-sign-on.md).

Once you are signed in, your account name and role appear at the **top-right**
of the page, for example `yourname (developer)` or `yourname (end-user)`.

### What account do I use?

The `oagi1docker/srt-repo` database image comes preloaded with seed data that
includes a default administrator account:

| Field | Value |
| --- | --- |
| **Username** | `oagis` |
| **Password** | `oagis` |

This account has the **Admin** right, so you can use it to set up a fresh
deployment and create working accounts for your team (see
[Create your first user](#create-your-first-user) below).

:::warning[Change the default password immediately]
The default credentials are public knowledge. Right after your first sign-in,
click your account name at the **top-right**, choose **Settings**, and set a
new password in the **Change password** section. See
[Administration → Change password](../04-user-guide/administration/05-password-management.md#change-password).
:::

If your deployment does not use the seed data, or the default account has
already been changed, ask whoever installed connectCenter for an account, or
have an administrator create one for you.

## Choose a library and release

connectCenter organizes content into **libraries**, and each library contains
one or more **releases** (also shown as "Branch" in some screens).

- A **library selector** appears at the top of the [Home Page](../04-user-guide/home-page/index.md).
  If you have not chosen a library yet, connectCenter pre-selects the **default
  library** (or the first available one) and loads the dashboard for it.
- On the **BIEs** and **User Extensions** dashboard tabs, a **Branch**
  drop-down lets you filter the panels by release. It includes an **All**
  option that aggregates across releases.

Set the library/release you want to work in before creating or editing content,
since most pages load their data for the current selection.

## Find your way around

After signing in you land on the **Home Page** dashboard. The top menu bar gives
you access to the main areas of the product. Which menus you see depends on your
role (developer vs. end user), the **Admin** right, tenant mode, and enabled
feature flags.

| Top menu | What it is for |
| --- | --- |
| **BIE** | View/Edit, Create, Copy, Uplift, and Express BIEs; manage BIE Packages, OpenAPI Documents (end users), Reuse Reports, and Code Lists used in BIEs. |
| **Context** | View/Edit Context Category, Context Scheme, and Business Context. |
| **Core Component** | View/Edit Core Component, Data Type, Code List, Agency ID List, Release, and Namespace. (Shown as **Browse Standard** for end users when *Browse Standard mode* is enabled.) |
| **Module** | View/Edit Module Set and Module Set Release. (Hidden in multi-tenant mode.) |
| **Library** | View/Edit Library. (Hidden in multi-tenant mode.) |
| **Admin** | Account, Transfer Ownership, Pending SSO, and Tenant. (Shown only to users with the **Admin** right.) |
| **Help** | About and User Guide. |
| **Account menu** (your name, top-right) | Switch terminology (connectSpec / CCTS), open **Settings**, and **Logout**. |

:::tip
The label next to your name — `(developer)` or `(end-user)` — tells you your
role, which determines what you can see and do. For a full breakdown of roles
and rights, see the
[Administration guide](../04-user-guide/administration/01-types-of-users-and-their-rights.md).
:::

The Home Page dashboard itself shows analytics tabs that depend on your role:

- **Core Components** — developer-only analytics about CC states and ownership.
- **BIEs** — analytics about BIE states, filterable by branch (release).
- **User Extensions** — analytics about user extensions (disabled in
  multi-tenant mode).

See the [Home Page guide](../04-user-guide/home-page/index.md) for a description of
each panel.

## Create your first user

If you are setting up a fresh deployment, an account with the **Admin** right
must create the working accounts. From an admin account:

1. On the top menu, open **Admin** → **Account**.
2. Click **New Account**.
3. Fill in **Login ID** (required, cannot be changed later), and optionally
   **Name** and **Organization**.
4. Leave **Standard Developer** unchecked for an End User, or check it to create
   a Standard Developer.
5. Check **Admin** to grant the new user the Admin right.
6. Enter and confirm a **Password** (at least five characters), then click
   **Create**.

Full step-by-step instructions, including enabling/disabling accounts and
resetting passwords, are in
[Administration → Create a user](../04-user-guide/administration/02-create-a-user.md).

## Where to go next

- [Home Page](../04-user-guide/home-page/index.md) — understand the dashboard you
  land on.
- [Administration](../04-user-guide/administration/01-types-of-users-and-their-rights.md) — manage users, roles,
  application settings, multi-tenant mode, and SSO.
- [Core Component Management](../04-user-guide/core-component-management/01-core-component-in-brief.md) —
  develop and browse the standard library.
- [BIE Management](../04-user-guide/bie-management/01-bie-in-brief.md) — profile and express
  Business Information Entities.
- [Architecture Overview](../06-contributing/architecture.md) — how the modules fit
  together.
