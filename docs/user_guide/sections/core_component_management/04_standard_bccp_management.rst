BCCP Management
~~~~~~~~~~~~~~~

Find a BCCP
^^^^^^^^^^^

See `Search and Browse CC Library <#search-and-browse-cc-library>`__ to find the BCCP needed.

**Tip**: How to find where a BCCP is used.
Use the *Where Used* function instead of the list page for this.
Open a Core Component detail page, click the ellipsis next to the target BCCP node, and choose "Where Used".
The returned dialog lists the ACCs that reference that BCCP.

View detail of a BCCP
^^^^^^^^^^^^^^^^^^^^^

`Find a BCCP <#find-a-bccp>`__.
Click on the BCCP DEN after the desired BCCP is found to open the BCCP detail page.
To understand the detail of the BCCP, see `Quick reference to different types of CCs <#quick-reference-to-different-types-of-ccs>`__.

**Tip**: You can open a BCCP detail page from within another Core Component tree.
On any BCCP node (regular-green font node), click on the ellipsis next to the node and select "Open in new tab".

Create a new BCCP
^^^^^^^^^^^^^^^^^

1. If you are not already on, open the "Core Component" page by clicking
   the "View/Edit Core Component" menu item under the "Core Component"
   menu at the top of the page. (note: depending on your circumstance,
   make sure the right branch is selected on top-left branch dropdown
   list).

2. Click on the plus sign near the top-right corner of the page.

3. Select "BCCP".

4. A BDT selection page is open. Check the check box in front of the
   desired BDT. You can use the Commonly Used BDTs are listed by
   default. The user can use other search filters to find the desired
   BDT. For explanation about different types of BDTs in connectCenter see
   `Types of BDTs in connectCenter <#types-of-bdts-in-connectcenter>`__.

5. A new BCCP is created with revision #1. Its detail page is open with
   default values populated. The new BCCP is in the WIP state. See also
   `Edit detail of a BCCP <#edit-detail-of-a-bccp>`__.

Edit detail of a BCCP
^^^^^^^^^^^^^^^^^^^^^

This section describes BCCP editing when its revision number is 1.

1. Open the BCCP detail page according to `View detail of a
   BCCP <#view-detail-of-a-bccp>`__. The BCCP has to be in the WIP state
   to be editable. The following fields can be updated.

   1. *Property Term*. Property Term should be space-separated words,
      each with initial letter capitalized Acronyms and plural should be
      avoided. For connectSpec, it should be what one would expect to see in
      the expression, except **the word "Identifier" which should always
      be spelled out**. For example, the name of a street should have a
      property term "Street Name", which would yield "Street Name. Name"
      as DEN. In other words, the data type term "Name" is not used in
      the expression generation. Naming pattern in connectSpec has data type
      term in the property term except when the data type term is Text
      (e.g., DEN of a description is "Description. Text" not
      "Description Text. Text". *Property Term* is required.

   2. *Nillable*. Nillable specifies whether a null value can be
      assigned in the instance data. *Nillable* is required but it is
      defaulted to false.

   3. *Deprecated*. Since this is a brand new BCCP, *Deprecated* is
      locked.

   4. *Value Constraint*. Select "Default" or "Fixed" value constraint
      in the drop down and specify the value in the adjacent text field.
      Note that "Fixed" value constraint and "Nillable" are mutually
      exclusive, i.e., "Nillable" cannot be true if there is a "Fixed"
      value constraint and vice versa. *Value Constraint* is optional.

   5. *Namespace*. Select a standard namespace from the dropdown list.
      See the `Namespace
      Management <#namespace-management>`__ section
      to create a standard namespace if needed or how namespaces may be
      used in connectCenter. *Namespace* is required.

   6. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field accepts free form text. *Definition
      Source* is optional.

   7. *Definition*. Specify the description of the BCCP. *Definition* is
      optional but a warning is given if none is specified.

2. Click the "Update" button at the top right to save changes.

Delete a newly created BCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Delete a newly created CC <#delete-a-newly-created-cc>`__.

Restore a deleted BCCP
^^^^^^^^^^^^^^^^^^^^^^

See `Restore a deleted CC <#restore-a-deleted-cc>`__.

Revise a BCCP
^^^^^^^^^^^^^

A BCCP in the Published state can be revised where certain changes can be made.
Any developer user can revise a published BCCP.
He/she does not have to be its owner.
To do that:

1. `Find a BCCP <#find-a-bccp>`__ in the Working branch.

2. `Open detail page of the BCCP <#view-detail-of-a-bccp>`__.

3. Click the "Revise" button at the top-right corner of the page. The
   BCCP goes into the WIP state and its revision number increases by 1.

4. The following fields can be updated.

   1. *Nillable*. It can only be updated from false (unchecked) to true
      (checked).

   2. *Deprecated*. It can only be updated from false (unchecked) to
      true (checked).

   3. *Value Constraint*. Select "Default" or "Fixed" value constraint
      in the dropdown list and specify the value in the adjacent field.
      Note that "Fixed" value constraint and "Nillable" are mutually
      exclusive, i.e., "Nillable" cannot be true if there is a fixed
      value constraint and vice versa. *Value constraint* is optional.

   4. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field accepts free form text. *Definition
      Source* is optional.

   5. *Definition*. Specify the description of the BCCP. *Definition* is
      optional but a warning is given if none is specified.

   6. *BDT*. The associated BDT can be changed.

5. Click the "Update" button at the top right to save changes.

Cancel a BCCP revision
^^^^^^^^^^^^^^^^^^^^^^

See `Cancel a CC revision <#cancel-a-cc-revision>`__.

Change BCCP states
^^^^^^^^^^^^^^^^^^

See `Change CC states <#change-a-cc-state>`__.

Transfer ownership of a BCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Transfer ownership of a CC <#transfer-ownership-of-a-cc>`__.

View history of changes to a BCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `View Change History of a CC <#view-change-history-of-a-cc>`__.
