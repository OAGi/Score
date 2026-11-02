---
title: "About this Guide"
sidebar_position: 1
---

This user guide describes how to use connectCenter, one functional area per section.
It is a reference rather than a tutorial: the sections do not have to be read in
order, so feel free to jump straight to the feature you need.
Screenshots illustrate the key pages, but the guide is best read alongside a running
connectCenter instance — follow the instructions while interacting with the tool.

For the typographic conventions used throughout this guide, see
[Documentation conventions](../01-introduction/what-is-connectcenter.md#documentation-conventions).

## Before you start

- **No running instance yet?** [Installation with Docker](../02-getting-started/installation-docker.md)
  sets one up, and [First Steps](../02-getting-started/first-steps.md) walks you through
  signing in and finding your way around.
- **New to the CCS vocabulary?** Read [Key Concepts](../01-introduction/key-concepts.md)
  first — a short primer on the terms (Core Component, BIE, release, namespace, …) used
  on every page of this guide.

## Know your role

Which menus and actions the application offers — and which parts of this guide apply
to you — depend on your user role. connectCenter has two roles, **End User** and
**connectSpec Developer** ("Developer" for short), plus an **Admin** right that can be
granted to either:

- **Developers** maintain the standard library: Core Components, developer code lists
  and agency ID lists, releases, and modules.
- **End Users** profile the standard library into Business Information Entities (BIEs)
  for their business contexts, and can extend it with end-user Core Components, code
  lists, and agency ID lists.
- **Admins** additionally manage user accounts, application settings, and tenants.

See [Types of Users and Their Rights](./administration/01-types-of-users-and-their-rights.md)
for the complete rights matrix.

## What is in this guide

| Section | What it covers | Primarily for |
|---|---|---|
| [Administration](./administration/01-types-of-users-and-their-rights.md) | The two user roles (End User and Developer) and the Admin right, with the [per-entity rights matrix](./administration/01-types-of-users-and-their-rights.md); user account management ([create](./administration/02-create-a-user.md), [edit](./administration/03-update-users-information.md), [enable/disable, remove](./administration/04-enable-or-disable-user-account.md)); [password changes and admin password resets](./administration/05-password-management.md); [reviewing pending single sign-on (OpenID Connect) requests](./administration/08-using-single-sign-on.md); the [application settings page](./administration/06-application-settings.md) — feature toggles, SMTP, filename expressions, and web-page branding; [terminology selection](./administration/07-select-terminology.md) (CCTS vs connectSpec); and [multi-tenant management](./administration/multi-tenant-management/index.md) — tenants, tenant/user and tenant/business-context associations, and the feature restrictions of multi-tenant mode. | Admins |
| [Home Page](./home-page/index.md) | The dashboard shown after sign-in: the library and branch selectors and the statistics panels of the [Core Components tab](./home-page/01-core-components-tab.md) (developers only), the [BIEs tab](./home-page/02-bies-tab.md), and the [User Extensions tab](./home-page/03-user-extensions-tab.md) — totals by state, breakdowns by user, and recent and unused items, each linking to a pre-filtered list page. | All users |
| [Core Component Management](./core-component-management/01-core-component-in-brief.md) | The [CCS meta-model as implemented by connectCenter](./core-component-management/01-core-component-in-brief.md) (ACC, ASCCP, BCCP, DT, code lists, agency ID lists), [CC states and life cycles, ownership, branches, and releases](./core-component-management/02-key-concepts.md), and [searching and browsing the CC library](./core-component-management/03-search-and-browse-cc-library.md). The [developer walkthrough](./core-component-management/developer/index.md) covers editing every developer CC type, [namespaces](./core-component-management/developer/02-namespace-management.md), developer [code lists](./core-component-management/developer/09-code-list-management.md) and [agency ID lists](./core-component-management/developer/10-agency-id-list-management.md), the [connectSpec BOD macro](./core-component-management/developer/08-create-an-connectspec-bod.md), [release management](./core-component-management/developer/12-release-management.md), [module management](./core-component-management/developer/module-management/index.md) — serializing developer CCs into XML/JSON Schema files — and the [GitHub integration](./core-component-management/developer/github-integration/index.md) for linking GitHub issues to components. The [end-user walkthrough](./core-component-management/end-user/index.md) covers end-user CCs, which back BIE extensions: non-standard namespaces, amendments, and their state cycle. | Developers and End Users |
| [BIE Management](./bie-management/01-bie-in-brief.md) | Profiling Core Components into BIEs in the tree editor: [creating](./bie-management/06-manage-bie.md#create-a-bie) and [restricting](./bie-management/06-manage-bie.md#restrict-a-bie) BIEs, the [WIP/QA/Production review cycle](./bie-management/06-manage-bie.md#bie-review-process), [BIE extension](./bie-management/06-manage-bie.md#extend-a-bie), and deriving BIEs by [copy](./bie-management/06-manage-bie.md#copy-a-bie), [inheritance](./bie-management/06-manage-bie.md#bie-inheritance), and [reuse](./bie-management/06-manage-bie.md#bie-reuse); [business contexts with their context categories and schemes](./bie-management/05-manage-context.md); [end-user code lists](./bie-management/03-manage-end-user-code-lists.md) and [agency ID lists](./bie-management/04-manage-end-user-agency-id-lists.md); [expressing a BIE](./bie-management/06-manage-bie.md#bie-expression-generation) as XML Schema, JSON Schema, OpenAPI 3 template, spreadsheet, or Avro Schema; [uplifting BIEs to a newer release](./bie-management/06-manage-bie.md#uplift-a-bie); [BIE packages](./bie-management/07-manage-bie-package.md); [OpenAPI documents](./bie-management/08-manage-openapi-document.md); [business terms](./bie-management/09-manage-business-terms.md); and the [common search, filter, comment, and notification functions](./bie-management/10-common-functions.md). | End Users, and Developers standardizing BIEs |
