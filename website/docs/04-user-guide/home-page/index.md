---
title: "Home Page"
sidebar_position: 0
---

The home page is the landing page after a user signs in.
It is also available by clicking the brand logo located at the top left of the screen.

The home page is a dashboard page.
It provides summary statistics about core components, BIEs, and user extensions, organized into tabs.
Its content depends on the current user role and the system configuration.

The figure below shows the home page for a user with the developer role, with the "Core Components" tab selected:

![Home page for a developer showing the library selector and the Core Components, BIEs, and User Extensions tabs](/img/user-guide/homepage_core_components_tab.png)

## Library selector

At the top of the page, the user can select a library by using the library selector.

![Home page with the library selector expanded, listing the available libraries](/img/user-guide/homepage_library_selector.png)

If the user has not chosen a library preference yet, connectCenter preselects the default library and shows it first in the selector.
The selection is remembered in the browser and restored on the next visit.
The dashboard data on the page is loaded for the selected library.

## Tabs

The home page can contain up to three tabs.
Which tabs are shown depends on the current user role and the system configuration:

| Tab | Availability | Description |
|-----|--------------|-------------|
| ["Core Components"](./01-core-components-tab.md) | Users with the developer role only. The tab is hidden for end users. | Summary statistics about core components. |
| ["BIEs"](./02-bies-tab.md) | All users. | Summary statistics about BIEs. |
| ["User Extensions"](./03-user-extensions-tab.md) | All users, unless multi-tenant mode is enabled. | Summary statistics about user extensions. |

:::note
The "User Extensions" tab is disabled when [multi-tenant mode](../administration/multi-tenant-management/01-multi-tenant-mode.md) is enabled.
In that case, the tab remains visible but is greyed out and cannot be opened.
:::

For example, a user with the end-user role sees only the "BIEs" and "User Extensions" tabs:

![Home page for an end user showing only the BIEs and User Extensions tabs](/img/user-guide/homepage_end_user_bies_tab.png)

The "BIEs" and "User Extensions" tabs also provide a "Branch" selector for filtering their panels by release; see the corresponding sections for details.
