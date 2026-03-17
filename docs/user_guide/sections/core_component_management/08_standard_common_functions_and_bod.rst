Common Developer CC Management Functions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The user guide in this section commonly applies to BCCP, ASCCP, and ACC.

Delete a newly created CC
^^^^^^^^^^^^^^^^^^^^^^^^^

A CC with revision #1 can be deleted.
Doing so will put the CC into the deleted state.
This signifies that the owner of the deleted CC does not want to use it anymore.
It suggests that if the CC is used by another developer, he/she should consider using another CC.
It is recommended that the owner documents the reason for deletion in the *Definition* field before deleting.
Other developers (or the owner himself) can however restore the CC - See `Restore a deleted CC <#restore-a-deleted-cc>`__.
To delete a CC:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more CCs to delete. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a CC.

2. There are two ways to delete a CC:

   1. Delete one or more CCs simultaneously.

      1. Check the checkbox in front of one or more CCs that are in the
         WIP state, have revision 1, and owned by the current developer.

      2. A trash icon is displayed at the top-right corner of the page.

      3. Select the trash icon.

   2. Delete a CC individually:

      1. Click on the DEN of the CC to open its detail page. The CC must
         be in the WIP state, has revision 1, and is owned by the
         current developer.

      2. Click the "Delete" button at the top-right corner.

3. Confirm (or cancel) the deletion on the pop-up dialog.

Restore a deleted CC
^^^^^^^^^^^^^^^^^^^^

Once a CC has been deleted, the ownership is delinquent from the current owner.
Any other developers can restore the CC to the WIP state and take the ownership.
To restore a CC.

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more CCs to restore from deletion. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a CC.
   There are two ways to restore a CC.

   1. Restore one or more CCs simultaneously:

      1. Check the checkbox in front of the CCs that are in the Deleted
         state.

      2. A trash can with an arrow icon inside is displayed at the
         top-right corner of the page.

      3. Select the icon.

   2. Restore CC individually:

      1. Click on the DEN of a deleted CC to open its detail page.

      2. Click the "Restore" button at the top-right corner of the page.

2. Confirm (or cancel) the restoration on the pop-up dialog.

Cancel a CC revision
^^^^^^^^^^^^^^^^^^^^

The developer who is the owner of a CC being revised can cancel the revision.
In this case, all changes to the CC are discarded.
CC detail and its owner are rollbacked to the pre-revised state.
To cancel a CC revision:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find the CC to
   cancel its revision. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a CC.

2. Click on the DEN of the CC to open its detail page. The current user
   has to be the owner of the CC and the CC has to be the WIP state with
   revision number greater than 1.

3. Click the "Cancel" button at the top-right of the page.

4. Confirm (or cancel) the revision cancellation.

Change a CC state
^^^^^^^^^^^^^^^^^

The section covers the toggling between WIP, Draft, and Candidate CC states.
For detailed meaning of these and other states, see `CC States <#cc-states>`__.
The current user has to be the owner of the CC to toggle between these three states.
To change the CC state:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more CCs to change the state. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a CC.

2. There are two ways to change state of a CC:

   1. Change the state of one or more CCs in one shot.

      1. Check the checkbox in front of one or more CCs that are in the
         same state and owned by the current developer.

      2. Arrow icons are displayed at the top-right corner of the page.
         Depending on the current state of the selected CCs; left,
         right, or both arrow icons are displayed.

      3. Click the left arrow icon to retract the state or click the
         right arrow icon to advance the state.

   2. Change the state of CC individually:

      1. Open the CC detail page, clicking the DEN of the CC in WIP,
         Draft, or Candidate state that is owned by the current
         developer.

      2. Depending on the current state of the CC, click either the
         "Move to Draft", "Move to Candidate", or "Back to WIP" button
         at the top-right corner of the page.

   3. Confirm (or cancel) the state change.

Transfer ownership of a CC
^^^^^^^^^^^^^^^^^^^^^^^^^^

To let another developer makes changes to a CC, the current owner has to transfer ownership of the CC to another developer.
Developer CC can be transferred only to another developer.
To transfer ownership:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more CCs to transfer ownership. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a CC.

2. There are two ways to transfer ownership of a CC:

   1. Transfer ownership of one or more CCs simultaneously.

      1. Check the checkbox in front of one or more CCs that are owned
         by the current developer and are in the WIP state.

      2. The double-arrow icon is displayed at the top-right corner of
         the page.

      3. Click the double-arrow icon.

      4. A dialog is displayed to select a developer. Use the filter on
         the top to find the desired developer and check the checkbox in
         front of it.

      5. Click the "Transfer" button.

   2. Transfer ownership of a CC individually:

      1. On the "Core Component" page, when a CC is in the WIP state and
         is owned by the current developer, there is a double-arrow icon
         next to the username of the owner.

      2. Click the double-arrow icon.

      3. A dialog is displayed to select a developer. Use the filter on
         the top to find the desired developer and check the checkbox in
         front of it.

      4. Click the "Transfer" button.

.. _view-change-history-of-a-cc:

View Change History of a CC
^^^^^^^^^^^^^^^^^^^^^^^^^^^

1. On any `CC detail page <#how-to-read-a-core-component>`__, click on the ellipsis of a
   node in the CC tree in the left pane. Select the "Show History" menu
   item.

2. A new browser tab is opened showing a list of changes that have
   occurred.

3. Check any two checkboxes in the list. The "Compare" button is
   activated.

4. Click the "Compare" button.

5. A diff dialog is open. Older copy is shown on the left and the newer
   one is shown on the right. Fields that were changed are highlighted
   in blue. Things that were newly added are highlighted green.

6. Click anywhere outside the dialog or hit the "Esc" key to close the
   diff dialog.

Purge a CC
^^^^^^^^^^

Purge allows for permanently discarding CCs, Code Lists and Agency ID lists from connectCenter.
Such entities along with their logs and comments can never be restored back.

Any developer can purge any CC providing that it is in the Deleted state.
See `Delete a newly created CC <#delete-a-newly-created-cc>`__.
However, if the CC is still used by another CC (as a property, base, or role of an ASCCP) that is not in the Deleted state, it cannot be purged.
For instance, if you want to purge a deleted BCCP which is still used as a property of an ACC.
The ACC should be deleted first.

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more CCs to delete. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a CC.

2. There are two ways to purge a CC:

   1. Delete one or more CCs simultaneously.

      1. Check the checkbox in front of one or more CCs that are
         deleted.

      2. A trash icon with a "Purge" toolbox is displayed at the
         top-right corner of the page.

      3. Click this trash icon.

   2. Delete a CC individually:

      1. Click on the DEN of the CC to open its detail page. The CC must
         be in the Deleted state.

      2. Click the "Purge" button at the top-right corner.

3. A confirmation dialog is displayed. Confirm or cancel the purge
   request.

Tagging CCs
^^^^^^^^^^^

The tagging feature in connectCenter allows users to label and categorize core components for better management.
With this feature, users can assign custom tags to core components making it easier to locate and work with them later.
To label the tag to core components:

1. Click on the ellipsis of a node in the CC tree located in the left pane
   on any CC detail page.

2. Next, select the "Tag" option from the menu that appears.

   1. To apply the tag to the core component, choose the desired tag from the list
      of available tags in the submenus.

   2. To add a new tag, first click on the "Edit Tags" menu item. Then, scroll
      down to the bottom of the dialog box. Next, type in the "Name", "Text Color",
      "Background Color", and "Description (Optional)" fields for the new tag.
      Finally, click on the "Add" button to create the new tag.

   3. To edit existing tags, click on the "Edit Tags" menu item. Then, update
      the "Name", "Text Color", "Background Color", and "Description (Optional)"
      fields of the tag. Finally, click on the "Update" button to save the changes.

   4. To discard existing tags, click on the "Edit Tags" menu item and then
      click on the "Discard" button located below the tag properties.

Export ASCCPs
^^^^^^^^^^^^^

"Export ASCCPs" allows users to download the schema expression of the selected ASCCP(s)
as either XML Schema or JSON Schema.
To export ASCCPs:

1. Select the ASCCP(s) in the list by clicking the checkbox(es). The export icon button
   will appear on the upper right hand corner of the screen.

2. Click the export icon button.

3. Choose either "XML Schema" or "JSON Schema" from the menu.

Create an connectSpec BOD
~~~~~~~~~~~~~~~~~~~~~~~~~

connectCenter includes a macro for creating an connectSpec BOD for a selected ASCCP.
To create an connectSpec BOD:

1. Click on the "View/Edit Core Component" menu item under the "Core
   Component" menu, if you are not already on the "Core Component" page.

2. Click on the plus sign at the top-right of the page.

3. Select "Create OAGi BOD" Component.

4. A dialog opens up where a Verb can be selected on the left side and
   an ASCCP can be selected on the right side. Use the filters on the
   top find and select a Verb and an ASCCP. Multiple Verbs and multiple
   ASCCPs can be selected; and pairwise BODs will be created.

5. Click the "Create" button at the bottom of the page.

6. Four CCs are automatically created with appropriate structures and
   relationships. The tool opens the detail page of the ASCCP
   representing the BOD. It is more convenient to go back to the "Core
   Component" page to `manage states the four CC
   altogether <#change-a-cc-state>`__.

**Note**: Create connectSpec BOD macro needs some standard connectSpec CCs.
If this macro is invoked when the database does not contain those CCs, it will fail.
