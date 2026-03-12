Home Page
=========

The home page is the landing page after a user signs in.
It is also available by clicking the brand logo located at the top left of the screen.

The home page is a dashboard page.
Its content depends on the current user role and system configuration.
At the top of the page, the user can select a library by using the library selector.
If the user has not chosen a library preference yet, connectCenter preselects the default library and shows it first in the selector.
The dashboard data on the page is loaded for the selected library.

The home page can contain the following tabs:

1. "Core Components" for developers only.

2. "BIEs" for signed-in users.

3. "User Extensions".
   This tab is disabled when multi-tenant mode is enabled.

Core Components Tab
-------------------

The "Core Components" tab is displayed only to users with the developer role.
This tab provides analytics about core components and comprises the following panels:

-  "Total core components by states".
   This panel displays the number of core components by state.
   The states shown on this tab are WIP, Draft, Candidate, and other available component states shown by the system.

-  "My core components by states".
   This panel displays the number of core components owned by the current user by state.

-  "Core components by users and states".
   This panel displays the number of core components by user and by state.
   It includes a "User" filter.
   To narrow the results, click the "User" field and select one or more users.
   The values in the table are links to the "Core Component" page with matching filters applied.

-  "My recent core components".
   This panel displays recently updated core components for the current user.
   The table shows the Type, State, DEN, GUID, and update information.

Unlike the "BIEs" and "User Extensions" tabs, the "Core Components" tab does not provide a "Branch" selector on the home page.

BIEs Tab
--------

The "BIEs" tab provides analytics about BIEs and comprises the following panels:

-  "Total BIEs by states".
   This panel displays the number of all BIEs by state for the selected branch.
   The states shown on this tab are WIP, QA, and Production.

-  "My BIEs by states".
   This panel displays the number of BIEs owned by the current user by state for the selected branch.

-  "BIEs by users and states".
   This panel displays the number of BIEs by user and by state.
   It also includes a "User" filter.
   To narrow the results, click the "User" field and select one or more users.
   The values in the table are links to the "BIEs" page with matching filters applied.

-  "My recent BIEs".
   This panel displays recently updated BIEs for the current user.
   The table shows the State, DEN, Release, Business Contexts, and update information.

The "Branch" drop-down field on the "BIEs" tab allows filtering these panels by release.
The branch list includes an "All" option.
When "All" is selected, the summary panels on the tab show totals across all available releases.

User Extensions Tab
-------------------

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
