---
title: "Create an connectSpec BOD"
sidebar_position: 8
---

connectCenter includes a macro for creating an connectSpec BOD for a selected ASCCP. To create an connectSpec BOD:

1. Click on the "View/Edit Core Component" menu item under the "Core Component" menu, if you are not already on the "Core Component" page.

2. Click on the plus sign at the top-right of the page.

3. Select "Create OAGi BOD" Component.

4. A dialog opens up where a Verb can be selected on the left side and an ASCCP can be selected on the right side. Use the filters on the top find and select a Verb and an ASCCP. Multiple Verbs and multiple ASCCPs can be selected; and pairwise BODs will be created.

5. Click the "Create" button at the bottom of the page.

6. Four CCs are automatically created with appropriate structures and relationships. The tool opens the detail page of the ASCCP representing the BOD. It is more convenient to go back to the "Core Component" page to [manage states the four CC altogether](./07-common-developer-cc-management-functions.md#change-a-cc-state).

**Note**: Create connectSpec BOD macro needs some standard connectSpec CCs. If this macro is invoked when the database does not contain those CCs, it will fail.
