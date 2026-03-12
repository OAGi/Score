Manage End User Agency ID Lists
-------------------------------

Agency ID List management has its own page which can be accessed under the "Core Component" menu.
**End-user Agency ID Lists can be managed when a published release branch is selected in the Branch filter.**

There are two ways to create an Agency ID List.
The first way is to base it on (i.e., derive it from) a developer Agency ID List, where both restrictions and extensions are allowed.
An end-user Agency ID List cannot be based on another end-user Agency ID List.

The other way is to create one from scratch.
Both cases are handled as a brand-new Agency ID in WIP state and revision #1.

.. _find-an-agency-id-list-1:

Find an Agency ID List
~~~~~~~~~~~~~~~~~~~~~~

Open the "Agency ID List" page by choosing "View/Edit Agency ID List" from the "Core Component" menu.
Make sure that the desired published release branch is selected in the *Branch* filter.
Then use the search field and advanced search filters to locate the desired Agency ID List.

.. _view-detail-of-an-agency-id-list-1:

View detail of an Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view Agency ID List detail:

1. `Find an Agency ID List <#find-an-agency-id-list-1>`__.

2. Click on the Agency ID List name to open the Edit Agency ID List
   page. Note: clicking somewhere else on an Agency ID List entry will
   display its textual definition.

Create a brand-new Agency ID List without base
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To create a brand-new Agency ID List without base.

1. Open the "Agency ID List" page by selecting "View/Edit Agency ID
   List" under the "Core Component" menu on the top of connectCenter pages.

2. Ensure that the desired published release is selected in the *Branch*
   filter.

3. Click the "New Agency ID List" button at the top-right of the page.

4. `Detail of the Agency ID List can be updated including add/change
   Agency ID List
   values. <#edit-detail-of-a-brand-new-agency-id-list>`__

Create a brand-new Agency ID List based on a developer Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To create a brand-new Agency ID List without base.

1. Open the "Agency ID List" page by selecting "View/Edit Agency ID
   List" under the "Core Component" menu on the top of connectCenter pages.

2. Ensure that the desired published release is selected in the *Branch*
   filter.

3. Open a developer Agency ID List to `view detail of the Agency ID
   List <#view-detail-of-an-agency-id-list-1>`__.

4. `Click the "Derive Agency ID List based on this"
   button. <#edit-detail-of-a-brand-new-agency-id-list>`__

5. `Detail of the Agency ID List can be updated including add/change
   Agency ID List
   values. <#edit-detail-of-a-brand-new-agency-id-list>`__

It should be noted that the application simply copies the list values from the based Agency ID List, but the derivation relationship is maintained.
These agency ID list values are considered as brand-new values added by the user and they can be edited according to `Edit detail of a brand-new Agency ID List value <#edit-detail-of-a-brand-new-agency-id-list-value-1>`__.

Edit detail of a brand-new Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This section describes end user Agency ID List editing when its revision number is 1.

1. Make sure you are on a Published release branch. Open the Edit Agency
   ID List detail page according to `View detail of an Agency ID
   List <#view-detail-of-an-agency-id-list-1>`__. The Agency ID List has
   to be in the WIP state, and the current user has to be the owner to
   be editable. The fields in the detail pane may be updated as follows.

   1. *Name*. It is the name of the Agency ID List. The value should be
      space-separated set of words. Acronyms and plural words should be
      avoided. Serialization rules will concatenate or abbreviate words
      per naming and design rules. *Name* is required.

   2. *List ID*. This is a free form text intending for representing
      external/global identifier of the Agency ID List. It is defaulted
      with a uniquely generated GUID, but it can be changed. *List ID*
      is required.

   3. *Agency ID*. This field represents an organization that owns and
      manages the Agency ID List. This is a self-reference field.
      Available selections will depend on the Agency ID List values in
      the table at the bottom of the page. If there is no desired agency
      in the list, `add an Agency ID List
      value <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list-1>`__,
      click the "Update" button at the top right of the page and then the
      dropdown list will contain this added Agency ID List value. Note
      that if the value is set to unused, it will not show up for
      selection. *Agency ID* is optional.

   4. *Version*. This field is a freeform text representing the version
      of the Agency ID List. The system will validate the combination of
      *List ID*, *Agency ID*, and *Version* is unique in the selected
      branch data. *Version* is required.

   5. *Namespace*. Select an end user namespace from the dropdown list.
      See the `Non-standard Namespace
      Management <#non-standard-namespace-management>`__ section to
      create an end user namespace if needed or how namespace may be
      used in connectCenter. *Namespace* is required.

   6. *Definition*. Specify the description of the Agency ID List.
      *Definition* is optional, but a warning is given if none is
      specified.

   7. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field is free form text. *Definition
      Source* is optional.

   8. *Deprecated*. The *Deprecated* checkbox is only applicable when
      the Agency ID List revision is higher than 1. Therefore, the field
      is locked.

2. Click the "Update" button at the top right to save changes.

3. The developer may also want to perform these other actions on the
   code list:

   1. `Add a brand-new Agency ID List value to the Agency ID
      List <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list-1>`__

   2. `Remove a brand-new Agency ID List value from the Agency ID
      List <#remove-a-brand-new-agency-id-list-value-from-the-agency-id-list-2>`__

   3. `Edit detail of a brand-new Agency ID List
      value <#edit-detail-of-a-brand-new-agency-id-list-value-1>`__

   4. `Change an Agency ID List
      state <#change-an-agency-id-list-state-1>`__

   5. `Transfer ownership of an Agency ID
      List <#transfer-ownership-of-an-agency-id-list-1>`__

.. _add-a-brand-new-agency-id-list-value-to-the-agency-id-list-1:

Add a brand-new Agency ID List value to the Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Add a brand-new Agency ID List value to the Agency ID List <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list-1>`__.

.. _remove-a-brand-new-agency-id-list-value-from-the-agency-id-list-1:

Remove a brand-new Agency ID List value from the Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Remove a brand-new Agency ID List value from the Agency ID List <#remove-a-brand-new-agency-id-list-value-from-the-agency-id-list-1>`__.

.. _edit-detail-of-a-brand-new-agency-id-list-value-1:

Edit detail of a brand-new Agency ID List value
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Edit detail of a brand-new Agency ID List value <#edit-detail-of-a-brand-new-agency-id-list-value-1>`__.

Amend an Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~

An end user Agency ID List in the Production state can be amended.
The current user does not have to be the owner of the Agency ID List.
To amend an Agency ID List:

1. Make sure you are on a non-working branch. `Open the "Agency ID List"
   page <#view-detail-of-an-agency-id-list-1>`__ of an Agency ID List in
   the Production state.

2. Click the "Amend" button at the top-right corner of the page.

3. The "Edit Agency ID List" page is refreshed with the Agency ID List
   whose revision number is incremented by 1.

4. `Detail of the Agency ID List can be updated including add/change
   Agency ID List
   values. <#edit-detail-of-an-agency-id-list-during-its-amendment>`__

.. _edit-detail-of-an-agency-id-list-during-its-amendment:

Edit detail of an Agency ID List during its amendment
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Agency ID List must have revision number at least 2, is in WIP state and the current user should be the owner of the Agency ID List.

See `View detail of an Agency ID List <#view-detail-of-an-agency-id-list-1>`__.

The end user can edit detail of the Agency ID List according to

1. Open the "Edit Agency ID List detail" page according to `View detail
   of an Agency ID List <#view-detail-of-an-agency-id-list-1>`__, if it
   is not already opened. The Agency ID List has to be in the WIP state,
   and the current user has to be the owner to be editable. The fields
   in the detail pane may be updated as follows:

   1. *Name*. It is the name of the Agency ID List. The field is locked
      and not editable.

   2. *List ID*. This is a free form text representing external/global
      identifier of the Agency ID List. The field is locked, and change
      is not allowed.

   3. *Agency ID*. This field represents the organization that owns and
      manages the Agency ID List. The field is locked, and change is not
      allowed.

   4. *Version*. This field is a freeform text representing the version
      of the Agency ID List. The system will validate the combination of
      *List ID*, *Agency ID*, and *Version* is unique in the current
      branch data. Although the version should generally be different
      from prior revisions, connectCenter does not validate this. *Version* is
      required.

   5. *Namespace*. The field is locked and cannot be changed.

   6. *Definition*. Specify the description of the Agency ID List.
      *Definition* is optional but a warning is given if none is
      specified.

   7. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field accepts a free form text.
      *Definition Source* is optional.

2. Click the "Update" button at the top-right of the page to save
   changes.

The end user may also want to perform these other actions on the Agency ID List:

1. `Add a brand-new Agency ID List value to the Agency ID
   List <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list-1>`__

2. `Remove a brand-new Agency ID List value from the Agency ID
   List <#remove-a-brand-new-agency-id-list-value-from-the-agency-id-list-1>`__

3. `Edit detail of a brand-new Agency ID List
   value <#edit-detail-of-a-brand-new-agency-id-list-value-1>`__

4. `Revise detail of an Agency ID List
   value <#revise-detail-of-an-agency-id-list-value-1>`__

5. `Change an Agency ID List
   state <#change-an-agency-id-list-state-1>`__

6. `Transfer ownership of an Agency ID
   List <#transfer-ownership-of-an-agency-id-list-1>`__

.. _add-a-brand-new-agency-id-list-value-to-the-agency-id-list-2:

Add a brand-new Agency ID List value to the Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Add a brand-new Agency ID List value to the Agency ID List <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list-1>`__.

.. _remove-a-brand-new-agency-id-list-value-from-the-agency-id-list-2:

Remove a brand-new Agency ID List value from the Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Remove a brand-new Agency ID List value from the Agency ID List <#remove-a-brand-new-agency-id-list-value-from-the-agency-id-list-1>`__.

.. _edit-detail-of-a-brand-new-agency-id-list-value-2:

Edit detail of a brand-new Agency ID List value
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Edit detail of a brand-new Agency ID List value <#edit-detail-of-a-brand-new-agency-id-list-value-1>`__.

.. _revise-detail-of-an-agency-id-list-value-1:

Revise detail of an Agency ID List value
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Revise detail of an Agency ID List value <#revise-detail-of-an-agency-id-list-value-1>`__.

.. _delete-a-brand-new-agency-id-list-1:

Delete a brand-new Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To delete an Agency ID List, it should be in WIP state with revision number 1 and the current user should be the owner of the Agency ID List.

To delete an Agency ID List:

1. Go to the "Agency ID List" page by clicking the "View/Edit Agency ID
   List" menu item under the "Core Component" menu. Make sure you are on
   a non-working Branch.

2. Click the checkbox in front of the Agency ID List you want to delete.

3. At the top right of the page, click the "Delete" button.

4. A dialog is displayed to confirm your intention.

Cancel an Agency ID List amendment
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The end user who is the owner of the Agency ID List being amended can cancel the amendment.
In this case, all changes to the Agency ID List are discarded.
Agency ID List detail and its owner are rollbacked to the pre-revised state.
To cancel Agency ID List amendment:

1. `Open the "Edit Agency ID List"
   page <#view-detail-of-an-agency-id-list-1>`__ of an Agency ID List in
   WIP state and with revision number greater than 1 (i.e., a amended
   Agency ID List). The current user has to be the owner of the Agency
   ID List.

2. Click the "Cancel" button at the top-right of the page.

3. Confirm (or cancel) the amendment cancellation.

.. _change-an-agency-id-list-state-1:

Change an Agency ID List state
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The section covers the toggling between WIP, QA, and Production states of the Agency ID List.
For detailed meaning of these and other states, see `Change a CC state <#change-a-cc-state>`__.
The current user has to be the owner of the Agency ID List to toggle between these three states.
To change the Agency ID List state:

1. `Open the "Edit Agency ID List"
   page <#view-detail-of-an-agency-id-list-1>`__.

2. Depending on the current state of the Agency ID List, click either
   the "Move to QA", "Move to Production", or "Back to WIP" button at
   the top-right corner of the page.

3. Confirm (or cancel) the state change.

.. _transfer-ownership-of-an-agency-id-list-1:

Transfer ownership of an Agency ID List
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To let another end user make changes to an Agency ID List, the current owner has to transfer ownership of the Agency ID List to another end user.
End user Agency ID Lists can be transferred only to another end user.
To transfer ownership:

1. Go to the "Agency ID List" page by clicking the "View/Edit Agency ID
   List" menu item under the "Core Component" menu.

2. Find the Agency ID List you want to transfer its ownership. See `Find
   an Agency ID List <#find-an-agency-id-list-1>`__.

3. There are two ways to transfer ownership of an Agency ID List:

   1. Click the transfer icon (icon with two opposite arrows) next to
      the owner of the Agency ID List, or

   2. Click on the ellipsis in the last column of the code list to
      transfer the ownership and select "Transfer Ownership" menu item
      in the pop-up menu.

4. A dialog is displayed to select a developer. Use the filter on the
   top to find the desired developer and check the checkbox in front of
   it.

5. Click the "Transfer" button.
