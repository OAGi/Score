DT Management
~~~~~~~~~~~~~

A non-working branch should be selected in order for the end users to manage end-user DTs.

.. _find-a-dt-1:

Find a DT
^^^^^^^^^

See `Find a DT <#find-a-dt>`__.

.. _view-detail-of-a-dt-1:

View detail of a DT
^^^^^^^^^^^^^^^^^^^

See `View detail of a DT <#view-detail-of-a-dt>`__.

.. _create-a-new-dt-1:

Create a new DT
^^^^^^^^^^^^^^^

See `Create a new DT <#create-a-new-dt>`__.

Edit detail of a DT
^^^^^^^^^^^^^^^^^^^

See `Edit detail of a DT <#edit-detail-of-a-brand-new-dt>`__.
Note that for the Namespace field of an end-user DT, a non-standard namespace should be selected.
See the `Non-standard Namespace Management <#non-standard-namespace-management>`__ section to create a non-standard namespace if needed or how namespace may be used in connectCenter

.. _edit-value-domain-1:

Edit Value Domain
^^^^^^^^^^^^^^^^^

See `Edit Value Domain <#edit-value-domain>`__.

.. _add-an-sc-to-a-dt-1:

Add an SC to a DT
^^^^^^^^^^^^^^^^^

See `Add an SC to a DT <#add-an-sc-to-a-dt>`__.

Edit details of a new SC
^^^^^^^^^^^^^^^^^^^^^^^^

See `Edit details of a new SC <#edit-details-of-a-brand-new-sc-in-the-dt-where-it-was-added>`__.

.. _remove-a-newly-added-sc-from-a-dt-1:

Remove a newly added SC from a DT
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Remove a newly added SC from a DT <#remove-a-newly-added-sc-from-a-dt>`__.

.. _edit-details-of-an-existing-sc-1:

Edit details of an existing SC
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Edit details of an existing SC <#edit-details-of-an-existing-sc>`__.

.. _delete-a-newly-created-dt-1:

Delete a newly created DT
^^^^^^^^^^^^^^^^^^^^^^^^^

A DT whose revision number is 1 can be (marked) deleted.
The DT has to be in the WIP state and owned by the current user.
See `Delete a newly created EUCC <#delete-a-newly-created-eucc>`__.

.. _restore-a-deleted-dt-1:

Restore a deleted DT
^^^^^^^^^^^^^^^^^^^^

See `Restore a deleted EUCC <#restore-a-deleted-eucc>`__.

Amend a DT
^^^^^^^^^^

A DT in the Production state can be revised where certain changes can be made.
Any end user can amend a DT that is in the Production state.
He/she does not have to be its owner.
To do that:

1. `Find a DT <#find-a-dt-1>`__ in a non-working branch.

2. `Open detail page of a DT <#view-detail-of-a-dt-1>`__ in the
   Production state.

3. Click the "Amend" button at the top-right corner of the page. The DT
   goes into the WIP state; and its revision number increases by 1.

4. Only the following fields in the DT detail pane on the right may be
   updated.

   1. *Definition Source*. Specify the source of the definition. This is
      typically a URI but the field accepts a free form text.
      *Definition Source* is optional.

   2. *Definition*. Specify the description of the BCCP. *Definition* is
      optional but a warning is given if none is specified.

   3. *Content Component Definition*. Specify the definition of the DT’s
      Content Component value. This is typically a free form text.
      *Content Component Definition* is optional.

7. Click the "Update" button at the top right to save changes.

8. The end user may want to perform these other actions on the DT:

   1. `Edit Value Domain <#edit-value-domain>`__.

   2. `Add an SC to the DT <#add-an-sc-to-a-dt>`__.

   3. `Edit details of a new
      SC <#edit-details-of-a-brand-new-sc-in-the-dt-where-it-was-added>`__.

   4. `Remove a newly added SC from the DT <#remove-a-newly-added-sc-from-a-dt>`__.

   5. `Editing details of an existing
      SC <#edit-details-of-an-existing-sc>`__.

Cancel a DT amendment
^^^^^^^^^^^^^^^^^^^^^

See `Cancel an EUCC amendment <#cancel-an-eucc-amendment>`__.

.. _change-dt-states-1:

Change DT states
^^^^^^^^^^^^^^^^

See `Change EUCC states <#change-eucc-states>`__.

.. _transfer-ownership-of-a-dt-1:

Transfer ownership of a DT
^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Transfer ownership of a EUCC <#transfer-ownership-of-an-eucc>`__.

.. _view-history-of-changes-to-a-dt-1:

View history of changes to a DT
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `View Change History of an EUCC <#view-change-history-of-an-eucc>`__.

Common End User CC Management Functions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The user guide in this section commonly applies to end user BCCP, ASCCP, and ACC.

Delete a newly created EUCC
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Similar to `Delete a newly created CC <#delete-a-newly-created-cc>`__, an end user can put an EUCC into the deleted state.
The EUCC has to have revision #1.
This signifies that the owner of the deleted CC does not want to use it anymore.
It suggests that if the EUCC is used by another end user, he/she should consider using another component.
It is recommended that the owner documents the reason for deletion in the *Definition* field before deleting.
Other end users (or the owner himself) can however restore the EUCC – See `Restore a deleted EUCC <#restore-a-deleted-eucc>`__.
To delete a EUCC:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more EUCCs to delete. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating an
   EUCC. Individual EUCC management sections also contain additional
   tips to search for EUCCs.

2. There are two ways to delete a EUCC.

   1. Delete one or more EUCCs simultaneously.

      1. Check the checkbox in front of one or more EUCCs that are in
         the WIP state, have revision 1, and are owned by the current
         end user.

      2. A trash icon is displayed at the top-right corner of the page.

      3. Click the trash icon.

   2. Delete an EUCC individually.

      1. Click on the DEN of the EUCC to open its detail page. The EUCC
         must be in the WIP state, has revision 1, and is owned by the
         current end user.

      2. Click the "Delete" button at the top-right corner.

3. Confirm (or cancel) the deletion on the pop-up dialog.

Restore a deleted EUCC
^^^^^^^^^^^^^^^^^^^^^^

Once an EUCC is deleted, the ownership is delinquent from the current owner.
Any other end users can restore the EUCC to the WIP state and take the ownership.
To restore an EUCC:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more EUCCs to restore from deletion. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a
   EUCC. There are two ways to restore an EUCC.

   1. Restore one or more EUCCs simultaneously.

      1. Check the checkbox in front of the EUCCs that are in the
         Deleted state.

      2. A trash can with an arrow icon inside is displayed at the
         top-right corner of the page.

      3. Click the icon.

   2. Restore an EUCC individually.

      1. Click on the DEN of a deleted EUCC to open its detail page.

      2. Click the "Restore" button at the top-right corner of the page.

2. Confirm (or cancel) the restoration on the pop-up dialog.

Cancel an EUCC amendment
^^^^^^^^^^^^^^^^^^^^^^^^

The end user who is the owner of a EUCC being amended can cancel the amendment.
In this case, all changes to the EUCC are discarded.
EUCC detail and its owner are rollbacked to the pre-amendment state.
To cancel an EUCC amendment:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find the EUCC
   to cancel its amendment. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating the
   desired EUCC. Individual EUCC management sections also contain
   additional tips to search for EUCCs.

2. Click on the DEN of the EUCC to open its detail page. The current
   user has to be the owner of the EUCC and the EUCC has to be the WIP
   state with revision number greater than 1.

3. Click the "Cancel" button at the top-right of the page.

4. Confirm (or cancel) the amendment cancellation on the pop-up dialog.

Change EUCC states
^^^^^^^^^^^^^^^^^^

The section covers the toggling between WIP, QA, and Production EUCC states.
For detailed meaning of these and other states, see `EUCC States <#end-user-cc-states>`__.
The current user has to be the owner of the EUCC to toggle between these three states.
To change the EUCC state:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more EUCCs to change the state. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a
   EUCC. Individual EUCC management sections contain additional tips to
   search for EUCCs.

2. There are two ways to change state of a EUCC:

   1. Change the state of one or more EUCCs simultaneously.

      1. Check the checkbox in front of one or more CCs that are in the
         same state and owned by the current end user.

      2. Arrow icons are displayed at the top-right corner of the page.
         Depending on the current state of the selected EUCC; left,
         right, or both arrow icons are displayed.

      3. Click the left arrow icon to retract the state or click the
         right arrow icon to advance the state.

   2. Change the state of EUCC individually:

      1. Open the "Core Component detail" page by clicking the DEN of
         the EUCC in WIP or QA, that is owned the current end user.

      2. Depending on the current state of the EUCC, click either the
         "Move to QA", "Move to Production", or "Back to WIP" (from QA)
         button at the top-right corner of the page.

   3. Confirm (or cancel) the state change in the pop-up dialog.

Transfer ownership of an EUCC
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

To let another end user, make changes to an EUCC, the current owner has to transfer the ownership of the EUCC to another end user.
EUCC can be transferred only to another end user.
To transfer ownership:

1. Go to the "Core Component" page by clicking the "View/Edit Core
   Component" menu item under the "Core Component" menu. Find one or
   more EUCCs to transfer ownership. See `Search and Browse CC
   Library <#search-and-browse-cc-library>`__ for help in locating a
   EUCC. Individual EUCC management sections contain additional tips to
   search for EUCCs.

2. There are two ways to transfer ownership of an EUCC:

   1. Transfer ownership of one or more EUCCs simultaneously.

      1. Check the checkbox in front of one or more EUCCs that are owned
         by the current end user and are in the WIP state.

      2. The double-arrow icon is displayed at the top-right corner of
         the page.

      3. Click the double-arrow icon.

      4. A dialog is displayed to select an end user to transfer the
         ownership to. Use the filter on the top to find the desired end
         user and check the checkbox in front of it.

      5. Click the "Transfer" button.

   2. Transfer ownership of a EUCC individually.

      1. On the "Core Component" page, when a EUCC is in the WIP state
         and is owned by the current end user, there is a double-arrow
         icon next to the username of the owner.

      2. Click the double-arrow icon.

      3. A dialog is displayed to select an end user. Use the filter on
         the top to find the desired end user and check the checkbox in
         front of it.

      4. Click the "Transfer" button.

View Change History of an EUCC
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `View Change History of a CC <#view-change-history-of-a-cc>`__.
