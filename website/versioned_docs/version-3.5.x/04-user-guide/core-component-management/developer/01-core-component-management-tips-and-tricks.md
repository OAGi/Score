---
title: "Core Component Management Tips and Tricks"
sidebar_position: 1
---

If you have already started using connectCenter for CC management, this section may help increase your productivity.

1. To navigate to a particular node of the CC tree quickly, use the search box on the top of the CC tree.
   Note that 1) the currently selected node is the context node the search starts from, and 2) the match is a case-insensitive substring match of the node names.
   Camel-case input such as "EmployeeCount" is automatically split into separate words, and starting the search term with "/" searches from the root node instead of the selected node.
   See also [Search within a Core Component Tree](../03-search-and-browse-cc-library.md#search-within-a-core-component-tree).

2. Use multiple browser tabs to view, create or edit CCs.
   You can even use another tab to create a new CC to be used in the CC being edited in another tab.

3. Click on the ellipsis (the three dots) next to a node to open the context menu.
   You can find more shortcuts and macros such as:

    1. "Open in new tab", which opens the CC associated with the node in a new browser tab.
       This allows you to make changes or investigate some more details about that CC without leaving the top-level CC you are interested in.

    2. "Create ASCCP from this", a macro to create an ASCCP from an opened ACC.

    3. "Create OAGi Extension Component", a macro to create an OAGi Extension (see [Create OAGi Extension point for an ACC](./05-acc-management.md#create-oagi-extension-point-for-an-acc)).

    4. Faster tree expansion with "Expand 2" and "Expand 3", which means expanding the tree 2 or 3 levels in one click.

    5. "Where Used", which allows you to analyze where a particular CC is referenced, including when an ACC is used as a based ACC.

    6. "Copy Path", which copies the path of the node to the clipboard, "Show History", which opens the change history of the CC, "Comments", which opens the comment sidebar for the node, and "Tags", which assigns tags to the CC (see [Tagging CCs](./07-common-developer-cc-management-functions.md#tagging-ccs)).
