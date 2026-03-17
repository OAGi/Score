Code List Management
~~~~~~~~~~~~~~~~~~~~

Code list management has its own page.
In connectCenter, code lists created by connectCenter developers are considered a kind of CC because they can be included in the release of standard Core Component.
Therefore, the Code List Management functionality can be accessed under the "Core Component" menu.

Find a code list
^^^^^^^^^^^^^^^^

To find a code list needed to work on, select "View/Edit Code List" under the "Core Component" menu on the top of connectCenter pages.
The "Code List" page is open that contains a table listing all code lists in a particular branch.
To manage developer code lists, ensure that *Working* is selected in the *Branch* filter.
Use search filters on the top of the table to find the desired code list.
All filters on the Code List have the same meaning as those described in `How to Search and Filter for a Core Component <#how-to-search-and-filter-for-a-core-component>`__, except that some filters do not exist on the Code List page and the Code List page has Name instead of DEN.
The `CC Specification <https://www.unece.org/cefact/codesfortrade/ccts_index.html>`__ does not have Code List as one of the Registry Classes and hence Code List does not have DEN specification.
Name of the code list a free form text.

View detail of a code list
^^^^^^^^^^^^^^^^^^^^^^^^^^

To view code list detail:

1. `Find a code list <#find-an-agency-id-list>`__.

2. Click on the code list name to open the Edit Code List page. Note:
   clicking somewhere else on a code list entry will display its textual
   definition.

Create a new code list
^^^^^^^^^^^^^^^^^^^^^^

To create a brand-new code list.

1. Open the "Code List" page by selecting "View/Edit Code List" under
   the "Core Component" menu on the top of connectCenter pages.

2. Ensure that "Working" is selected in the *Branch* filter.

3. Click the "New Code List" button at the top-right of the page.

4. `Detail of the code list can be updated including add/change code
   list values. <#edit-detail-of-a-brand-new-code-list>`__

Edit detail of a brand-new code list
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This section describes code list editing when its revision number is 1.

1. Make sure you are on the Working branch. Open the Edit Code List
   detail page according to `View detail of a code
   list <#view-detail-of-an-agency-id-list>`__. The code list has to be
   in the WIP state, and the current user has to be the owner to be
   editable. The fields in the detail pane may be updated as follows.

   1. *Name*. It is the name of the code list. The value should be
      space-separated set of words. Acronyms and plural words should be
      avoided. *Name* is required.

   2. *List ID*. This is a free form text representing external/global
      identifier of the code list. It is defaulted with a uniquely
      generated GUID, but it can be changed. *List ID* is required.

   3. *Agency ID List* and *Agency ID List Value*. This combination of
      this two fields represents an organization that owns and manages
      the code list. Currently, only one developer Agency ID List is
      allowed in a release, so there is always only one in the *Agency
      ID List* dropdown Select an Agency ID from the *Agency ID List
      value* drop down. If there is no desired agency ID value in the
      list, `revise the developer Agency ID
      List <#revise-an-agency-id-list>`__ and `add a new
      value <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list>`__.
      These two fields are required.

   4. *Version*. This field is a freeform text representing the version
      of the code list. The system will validate the combination of
      *List ID*, *Agency ID*, and *Version* is unique in the (working)
      branch data. *Version* is required.

   5. *Namespace*. Select a standard namespace from the dropdown list.
      See the `Namespace
      Management <#core-component-management-tips-and-tricks>`__ section
      to create a standard namespace if needed or how namespace may be
      used in connectCenter. *Namespace* is required.

   6. *Definition*. Specify the description of the code list.
      *Definition* is optional, but a warning is given if none is
      specified.

   7. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field is free form text. *Definition
      Source* is optional.

   8. *Deprecated*. The *Deprecated* checkbox is only applicable when
      the code list revision is higher than 1. Therefore, the field is
      locked.

2. Click the "Update" button at the top right to save changes.

3. The developer may also want to perform these other actions on the
   code list:

   1. `Add a brand-new code list value to the code
      list <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list>`__

   2. `Remove a brand-new code list value from the code
      list <#remove-a-brand-new-agency-id-list-value-from-the-agency-id-list>`__

   3. `Edit detail of a brand-new code list
      value <#edit-detail-of-a-brand-new-code-list-value>`__

   4. `Change a code list state <#change-an-agency-id-list-state>`__

   5. `Transfer ownership of a code
      list <#transfer-ownership-of-an-agency-id-list>`__

Add a brand-new code list value to the code list
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A code list value can be added to a code list that is in WIP state and is owned by the current user.

1. At the bottom of the `"Edit Code List"
   page <#view-detail-of-an-agency-id-list>`__, click the "Add" button.

2. A dialog pops up where the following fields can be edited.

   1. *Code*. The code value to be used in the message instance. *Code*
      is required.

   2. *Meaning*. Short name or short description of the code value.
      *Meaning* is required.

   3. *Definition*. Long description of the code value. *Definition* is
      optional.

   4. *Definition Source*. The source of the definition. This can be any
      text but usually a URI is specified. It is optional.

   5. *Deprecated*. The checkbox is disabled when a code value is newly
      added.

   6. For example, *Code* is "YYYY-MM-DDThh:mm:ssZ", *Meaning* is "UTC
      Date and Time", *Definition* is "ISO 8601 Date and Time extended
      format: YYYY-MM-DDThh:mm:ssZ with optional fraction of second
      allowed (YYYY-MM-DDThh:mm:ss,ssZ)".

3. Click the "Add" button at the bottom of the dialog. To get out of the
   dialog without adding the code value, hit the ESC button or click
   outside of the dialog.

4. Click the "Update" button at the top of the page.

Remove a brand-new code list value from the code list
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A code list value can be removed from a code list only when it is newly added to a brand-new code list or to a revised code list.
The code list must be in the WIP state and is owned by the current user.

1. In the "Code List Values" table on the `"Edit Code List"
   page <#view-detail-of-an-agency-id-list>`__, click the checkboxes in
   front of one or more code list values intended to be deleted. Only
   removable code list values have their checkboxes enabled.

2. Click the "Remove" button at the bottom of the page.

3. A confirmation dialog is displayed. Confirm or cancel the removal
   request.

4. Click the "Update" button at the top of the page.

Edit detail of a brand-new code list value
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This section describes the case when the code list value is brand-new, i.e., a code list value added to a revision 1 code list (brand-new code list) or to a revised code list (revision number more than 1).
The code list must be in the WIP state and is owned by the current user.
To edit the detail of a code list value:

1. In the Code List Values table on the `"Edit Code List"
   page <#view-detail-of-an-agency-id-list>`__, click on the row of the
   code list value to be updated.

2. The "Edit Code List Value" dialog is open. Detail of the code list
   value can be updated as described in `Add a brand-new code list value
   to the code
   list <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list>`__.

3. Click the "Save" button at the bottom of the dialog. To get out of
   the dialog without saving the changes, hit the ESC button or click
   outside of the dialog.

4. Click the "Update" button at the top of the page.

Revise a code list
^^^^^^^^^^^^^^^^^^

A developer code list in the Published state can be revised.
The current user does not have to be the owner of the code list.
To revise a code list:

1. Make sure you are on the *Working* branch. `Open the "Edit Code List"
   page <#view-detail-of-an-agency-id-list>`__ of a code list in the
   Published state.

2. Click the "Revise" button at the top-right corner of the page.

3. The "Edit Code List" page is refreshed with the code list whose
   revision number is incremented by 1.

4. `Detail of the code list can be updated including add/change code
   list values. <#edit-detail-of-a-revised-code-list>`__

Edit detail of a revised code list
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This section describes code list editing when its revision number is 2 or more.

1. Open the "Edit Code List detail" page according to `View detail of a
   code list <#view-detail-of-an-agency-id-list>`__. The code list has
   to be in the WIP state, and the current user has to be the owner to
   be editable. The fields in the detail pane may be updated as follows:

   1. *Name*. It is the name of the code list. The field is locked and
      not editable.

   2. *List ID*. This is a free form text representing external/global
      identifier of the code list. The field is locked, and change is
      not allowed.

   3. *Agency ID List* and *Agency ID List Value*. This combination of
      these two fields represents an organization that owns and manages
      the code list. These fields are locked, and change is not allowed.

   4. *Version*. This field is a freeform text representing the version
      of the code list. The system will validate the combination of
      *List ID*, *Agency ID*, and *Version* is unique in the (working)
      branch data. *Version* is required. The field can be changed

   5. *Namespace*. The field is locked and cannot be changed.

   6. *Definition*. Specify the description of the code list.
      *Definition* is optional but a warning is given if none is
      specified.

   7. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field accepts a free form text.
      *Definition Source* is optional.

   8. *Deprecated*. The deprecated checkbox allows the code list to be
      marked for deprecation.

2. Click the "Update" button at the top-right of the page to save
   changes.

3. The developer may also want to perform these other actions on the
   code list:

   1. `Add a brand-new code list value to the code
      list <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list>`__

   2. `Remove a brand-new code list value from the code
      list <#remove-a-brand-new-agency-id-list-value-from-the-agency-id-list>`__

   3. `Edit detail of a brand-new code list
      value <#edit-detail-of-a-brand-new-code-list-value>`__

   4. `Revise detail of a code list
      value <#revise-detail-of-an-agency-id-list-value>`__

   5. `Change a code list state <#change-an-agency-id-list-state>`__

   6. `Transfer ownership of a code
      list <#transfer-ownership-of-an-agency-id-list>`__

Revise detail of a code list value
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This section describes the case when the code list value has existed since the previous revision of the code list.
To revise the detail of a code list value:

1. In the Code List Values table on the `"Edit Code List"
   page <#view-detail-of-an-agency-id-list>`__ of a code list with
   revision number 2 or more, click on the row of the code list value to
   be updated.

2. The "Edit Code List Value" dialog is open. Detail of the code list
   value can be updated as follows.

   1. *Code*. Change is not allowed.

   2. *Meaning*: Change is allowed and the field is mandatory.

   3. *Definition*: Long description of the code value. *Definition* is
      optional.

   4. *Definition Source*: The source of the definition. This can be any
      text but usually a URI is specified. It is optional.

   5. *Deprecated*: Check the check box to deprecate the code list
      value.

3. Click the "Save" button at the bottom of the dialog. To get out of
   the dialog without saving the changes, hit the ESC button or click
   outside of the dialog.

4. Click the "Update" button at the top of the page.

Delete a brand-new code list
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A code list with revision #1 can be deleted.
Doing so will put the code list into the deleted state.
This signifies that the owner of the deleted code list no longer wants to use it.
This suggests that if the code list is used by another developer in another CC, he/she should consider using another code list.
It is recommended that the owner documents the reason for deletion in the Definition field before deleting.
Other developers (or the owner himself) can however restore the code list - see `Restore a deleted code list <#delete-a-brand-new-agency-id-list>`__.
To delete a code list:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find one or more Code Lists to
   delete. Make sure you are on the Working Branch. See `Find a code
   list <#find-an-agency-id-list>`__ for help in locating a code list.

2. There are two ways to delete a code list:

   1. Delete one or more code lists simultaneously.

      1. Check the checkbox in front of the code lists that are in the
         WIP state, have revision 1, and are owned by the current
         developer.

      2. The "Delete" button on top-right corner of the page is
         activated.

      3. Click the "Delete" button.

   2. Delete code list individually

      1. Click on the ellipsis in the last column of the code list
         entry. The code list must be in the WIP state, has revision 1,
         and is owned by the current developer. Click the "Delete" menu
         item in the pop-up menu.

      2. Alternatively, click the code list name to open its detail page
         and click the "Delete" button at the top-right corner of the
         page.

3. Confirm (or cancel) deletion on the pop-up dialog.

Restore a deleted code list
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Once a code list has been deleted, the ownership is delinquent from the current owner.
Any other developers can restore the code list to the WIP state and take the ownership.
To restore a code list.

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find one or more code lists to
   restore from deletion. See `Find a code
   list <#find-an-agency-id-list>`__ for help in locating a code list.
   There are two ways to restore a code list.

   1. Restore one or more code lists simultaneously:

      1. Check the checkbox in front of the code lists that are in the
         Deleted state.

      2. A "Restore" button is activated at the top-right corner of the
         page.

      3. Click the "Restore" button.

   2. Restore code list individually:

      1. Click on the ellipsis in the last column of the code list entry
         that is in the Deleted state. Click the "Restore" menu item in
         the pop-up menu.

      2. Alternatively, click on the name of a deleted code list to open
         its detail page and click the "Restore" button at the top-right
         corner of the page.

2. Confirm (or cancel) restoration on the pop-up dialog.

Cancel a code list revision
^^^^^^^^^^^^^^^^^^^^^^^^^^^

The developer who is the owner of a code list being revised can cancel the revision.
In this case, all changes to the code list are discarded.
Code list detail and its owner are rollbacked to the pre-revised state.
To cancel code list revision:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find a code list to restore
   from deletion. Make sure you are on the *Working* Branch. See `Find a
   code list <#find-an-agency-id-list>`__ for help in locating a code
   list.

2. Click on the name of the code list to open its detail page. The
   current user has to be the owner of the code list and the code list
   has to be the WIP state with a revision number greater than 1.

3. Click the "Cancel" button at the top-right of the page.

4. Confirm (or cancel) the revision cancellation.

Change a code list state
^^^^^^^^^^^^^^^^^^^^^^^^

The section covers the toggling between WIP, Draft, and Candidate states of the code list.
For the detailed meaning of these and other states, see `Change a CC state <#change-a-cc-state>`__.
The current user has to be the owner of the code list to toggle between these three states.
To change the code list state:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find a code list to work on.
   Make sure you are on the Working Branch. See `Find a code
   list <#find-an-agency-id-list>`__ for help in locating a code list.

2. Click on the name of the code list to work on.

3. Depending on the current state of the code list, click either the
   "Move to Draft", "Move to Candidate", or "Back to WIP" button at the
   top-right corner of the page.

4. Confirm (or cancel) the state change.

Transfer ownership of a code list
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To let another developer makes changes to a code list, the current owner has to transfer ownership of the code list to another developer.
Developer code lists can be transferred only to another developer.
To transfer ownership:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find a code list to transfer
   ownership. Make sure you are on the *Working* Branch. See `Find a
   code list <#find-an-agency-id-list>`__ for help in locating a code
   list.

2. There are two ways to transfer ownership of a code list:

   1. Click the transfer icon (icon with two opposite arrows) next to
      the owner of the code list, or

   2. Click on the ellipsis in the last column of the code list to
      transfer the ownership and select "Transfer Ownership" menu item
      in the pop-up menu.

3. A dialog is displayed to select a developer. Use the filter on the
   top to find the desired developer and check the checkbox in front of
   it.

4. Click the "Transfer" button.
