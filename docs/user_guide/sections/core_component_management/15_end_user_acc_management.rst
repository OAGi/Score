ACC Management
~~~~~~~~~~~~~~

.. _find-an-acc-1:

Find an ACC
^^^^^^^^^^^

See `Search and Browse CC Library <#search-and-browse-cc-library>`__ to find the ACC needed.
For more information about finding an ACC see `Find an ACC <#find-an-acc>`__.
Make sure you are on a published release branch for EUCC.

.. _view-detail-of-an-acc-1:

View detail of an ACC
^^^^^^^^^^^^^^^^^^^^^

See `View detail of an ACC <#view-detail-of-an-acc>`__.

.. _create-a-new-acc-1:

Create a new ACC
^^^^^^^^^^^^^^^^

See `Create a new ACC <#create-a-new-acc>`__.

.. _edit-detail-of-an-acc-1:

Edit detail of an ACC
^^^^^^^^^^^^^^^^^^^^^

To edit an ACC please see `Edit detail of an ACC <#edit-detail-of-an-acc>`__.

**Important!** when an end user is editing the details of an ACC, only Non-standard Namespaces can be selected in the Namespace dropdown list.
See the `Non-standard namespace Management <#non-standard-namespace-management>`__ section to create a Non-standard namespace if needed or the `Namespace Management <#core-component-management-tips-and-tricks>`__ section about how namespaces are used in connectCenter

.. _set-a-based-acc-1:

Set a based ACC
^^^^^^^^^^^^^^^

See `Set a based ACC <#set-a-based-acc>`__.

.. _remove-the-based-acc-1:

Remove the based ACC
^^^^^^^^^^^^^^^^^^^^

See `Remove the based ACC <#remove-the-based-acc>`__.

.. _add-a-property-to-an-acc-1:

Add a property to an ACC
^^^^^^^^^^^^^^^^^^^^^^^^

See `Add a property to an ACC <#add-a-property-to-an-acc>`__.

.. _remove-a-property-from-an-acc-1:

Remove a property from an ACC
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Remove a property from an ACC <#remove-a-property-from-an-acc>`__.

.. _edit-details-of-a-new-ascc-1:

Edit details of a new ASCC
^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Edit details of a new ASCC <#edit-details-of-a-new-ascc>`__.

.. _edit-details-of-a-new-bcc-1:

Edit details of a new BCC
^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Edit details of a new BCC <#edit-details-of-a-new-bcc>`__.

.. _order-the-propertiesassociations-1:

Order the properties/associations
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Order the properties/associations <#order-the-propertiesassociations>`__.

.. _delete-a-newly-created-acc-1:

Delete a newly created ACC
^^^^^^^^^^^^^^^^^^^^^^^^^^

An ACC whose revision number is 1 can be (marked) deleted.
The ACC has to be in the WIP state and owned by the current user.
See `Delete a newly created EUCC <#delete-a-newly-created-eucc>`__.

.. _restore-a-deleted-acc-1:

Restore a deleted ACC
^^^^^^^^^^^^^^^^^^^^^

See `Restore a deleted EUCC <#restore-a-deleted-eucc>`__.

Amend an ACC
^^^^^^^^^^^^

An ACC in Production state can be amended where certain backwardly-compatible changes can be made.
Any end user can amend a production ACC.
He/she does not have to be its owner.
To do that:

1. `Find an ACC <#find-an-acc-1>`__ in a published Release branch.

2. `Open the detail page of an ACC <#view-detail-of-an-acc-1>`__ in
   Production state.

3. Click the "Amend" button at the top-right corner of the page. The ACC
   goes into the WIP state and its revision number increases by 1.

4. Only the following fields in the ACC detail pane on the right may be
   updated.

   1. *Deprecated*. This can only be updated from false (unchecked) to
      true (checked). In other words, if the ACC was deprecated in the
      previous revision, it cannot be un-deprecated.

   2. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field accepts a free form text.
      *Definition Source* is optional.

   3. *Definition*. Specify the description of the BCCP. *Definition* is
      optional but a warning is given if none is specified.

5. Click the "Update" button at the top right to save changes.

6. The end user may want to perform these other actions on the ACC:

   1. `Set another ACC as a base of this ACC <#set-a-based-acc>`__.

   2. `Remove the based ACC <#remove-the-based-acc>`__.

   3. `Add a property to the ACC <#add-a-property-to-an-acc>`__ and edit
      the detail of the resulting `BCC <#edit-details-of-a-new-bcc>`__ or
      `ASCC <#edit-details-of-a-new-ascc>`__.

   4. `Remove a property from the
      ACC <#remove-a-property-from-an-acc-1>`__. Only the ASCC and BCC
      that are in revision 1 (i.e., added during the current revision)
      can be removed.

   5. `Order (i.e., change the sequence) the
      properties/associations <#order-the-propertiesassociations-1>`__.

   6. `Change the state of the ACC <#change-acc-states>`__.

   7. `Create an ASCCP from this ACC <#create-a-new-asccp>`__.

Refactor a property to a based ACC
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

End users cannot refactor a property to a based ACC which is owned by developers.
They can only refactor properties to a based ACC belonging to end users only.
Everything else is the same as detailed in `Refactor a property to a based ACC <#refactor-a-property-in-an-acc>`__.

Cancel an ACC amendment
^^^^^^^^^^^^^^^^^^^^^^^

See `Cancel an EUCC amendment <#cancel-an-eucc-amendment>`__.

.. _change-acc-states-1:

Change ACC states
^^^^^^^^^^^^^^^^^

See `Change EUCC states <#change-eucc-states>`__.

.. _transfer-ownership-of-an-acc-1:

Transfer ownership of an ACC
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Transfer ownership of a EUCC <#transfer-ownership-of-an-eucc>`__.

.. _view-history-of-changes-to-an-acc-2:

View history of changes to an ACC
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `View Change History of an EUCC <#view-change-history-of-an-eucc>`__.

.. _dt-management-1:
