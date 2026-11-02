---
title: "User Extensions Tab"
sidebar_position: 3
---

The "User Extensions" tab provides analytics about user extensions.
It is available to all users, but some of its panels are displayed only to users with the end-user role, as described below.

:::note
This tab is disabled when [multi-tenant mode](../administration/multi-tenant-management/01-multi-tenant-mode.md) is enabled.
In that case, the tab remains visible but is greyed out and cannot be opened.
:::

The figure below shows the "User Extensions" tab for a user with the end-user role:

![User Extensions tab for an end user showing the Total User Extensions by states, My User Extensions by states, User Extensions by users and states, and My unused extensions in BIEs panels](/img/user-guide/homepage_end_user_user_extensions_tab.png)

This tab comprises the following panels:

-  "Total User Extensions by states".
   This panel displays the number of all user extensions by state for the selected branch.
   The states shown on this tab include WIP, QA, Production, Deleted, and other available extension states shown by the system.

-  "My User Extensions by states".
   This panel displays the number of user extensions owned by the current user by state for the selected branch.
   This panel is displayed only to end users.

-  "User Extensions by users and states".
   This panel displays the number of user extensions by user and by state.
   It includes a "User" filter.
   To narrow the results, click the "User" field and select one or more users.
   The values in the table are links to the "Core Component" page with filters for user extension groups.

-  "My unused extensions in BIEs".
   This panel is displayed only to end users.
   It lists user extension content that exists in initialized extension nodes but is not enabled in top-level BIEs.
   The table shows the extension state, user extension name, update information, BIE state, top-level BIE, and association DEN.

:::note
Because user extensions are created and owned by end users, developers see a reduced version of this tab:
the "My User Extensions by states" and "My unused extensions in BIEs" panels are hidden for users with the developer role.
:::

## Branch selector

The "Branch" drop-down field at the top right of the "User Extensions" tab allows filtering these panels by release.
The branch list includes an "All" option.
When "All" is selected, links from some user extension summary values are disabled because those links require a specific release filter.

The "BIEs" and "User Extensions" tabs share the branch selection: changing the branch on one tab also applies to the other.
The selection is remembered in the browser and restored on the next visit.
