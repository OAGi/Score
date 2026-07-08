---
title: "User Extensions Tab"
sidebar_position: 3
---

The "User Extensions" tab provides analytics about user extensions.
This tab is disabled when multi-tenant mode is enabled.

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

The "Branch" drop-down field on the "User Extensions" tab allows filtering these panels by release.
The branch list includes an "All" option.
When "All" is selected, links from some user extension summary values are disabled because those links require a specific release filter.
