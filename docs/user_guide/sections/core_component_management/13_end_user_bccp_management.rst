BCCP Management
~~~~~~~~~~~~~~~

.. _find-a-bccp-1:

Find a BCCP
^^^^^^^^^^^

See `Search and Browse CC Library <#search-and-browse-cc-library>`__ to find the BCCP needed.
For more information about finding a BCCP see also `Find a BCCP <#find-a-bccp>`__.
Make sure you are on a published release branch for EUCC.

.. _view-detail-of-a-bccp-1:

View detail of a BCCP
^^^^^^^^^^^^^^^^^^^^^

See `View detail of a BCCP <#view-detail-of-a-bccp>`__.

.. _create-a-new-bccp-1:

Create a new BCCP
^^^^^^^^^^^^^^^^^

To create a new end user BCCP see `Create a new BCCP <#create-a-new-bccp>`__.

.. _edit-detail-of-a-bccp-1:

Edit detail of a BCCP
^^^^^^^^^^^^^^^^^^^^^

To edit an end user BCCP please see `Edit detail of a BCCP <#edit-detail-of-a-bccp>`__.

**Important!** when an end user is editing the details of a BCCP, only Non-standard Namespaces can be selected in the Namespace dropdown list.
See the `Non-standard namespace Management <#non-standard-namespace-management>`__ section to create a Non-standard namespace if needed or the `Namespace Management <#core-component-management-tips-and-tricks>`__ section about how namespaces are used in connectCenter.

.. _delete-a-newly-created-bccp-1:

Delete a newly created BCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Delete a newly created EUCC <#delete-a-newly-created-eucc>`__.

.. _restore-a-deleted-bccp-1:

Restore a deleted BCCP
^^^^^^^^^^^^^^^^^^^^^^

See `Restore a deleted EUCC <#restore-a-deleted-eucc>`__.

Amend a BCCP
^^^^^^^^^^^^

An end user BCCP in Production state can be amended where certain backwardly-compatible changes be made.
Any end user can amend a production BCCP.
He/she does not have to be its owner.
To do that:

1. `Find a BCCP <#find-a-bccp-1>`__ in the desired published Release
   branch.

2. `Open detail page of the BCCP <#view-detail-of-a-bccp-1>`__.

3. Click the Amend button at the top-right corner of the page. The BCCP
   goes into the WIP state and its revision number increases by 1.

4. The following fields can be updated.

   1. *Nillable*. It can only be updated from false (unchecked) to true
      (checked).

   2. *Deprecated*. It can only be updated from false (unchecked) to
      true (checked).

   3. *Value Constraint*. Select *default* or *fixed value* constraint
      in the dropdown list and specify the value in the adjacent field.
      Note that *fixed value* constraint and *nillable* are mutually
      exclusive, i.e., nillable cannot be true if there is a fixed value
      constraint and vice versa. Value constraint is optional.

   4. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field accepts a free form text.
      *Definition Source* is optional.

   5. *Definition*. Specify the description of the BCCP. *Definition* is
      optional but a warning is given if none is specified.

5. Click the "Update" button at the top right to save changes.

Cancel a BCCP amendment
^^^^^^^^^^^^^^^^^^^^^^^

See `Cancel an EUCC amendment <#cancel-an-eucc-amendment>`__.

.. _change-bccp-states-1:

Change BCCP states
^^^^^^^^^^^^^^^^^^

See `Change CC states <#change-a-cc-state>`__.

.. _transfer-ownership-of-a-bccp-1:

Transfer ownership of a BCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Transfer ownership of a EUCC <#transfer-ownership-of-an-eucc>`__.

.. _view-history-of-changes-to-a-bccp-1:

View history of changes to a BCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `View Change History of an EUCC <#view-change-history-of-an-eucc>`__.

.. _asccp-management-1:
