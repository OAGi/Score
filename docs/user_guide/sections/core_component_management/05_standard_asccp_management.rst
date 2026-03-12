ASCCP Management
~~~~~~~~~~~~~~~~

Find an ASCCP
^^^^^^^^^^^^^

See `Search and Browse CC Library <#search-and-browse-cc-library>`__ to find the ASCCP needed.

**Tip**: How to find where an ASCCP is used.
Use the *Where Used* function instead of the list page for this.
Open a Core Component detail page, click the ellipsis next to the target ASCCP node, and choose "Where Used".
The returned dialog lists the ACCs that reference that ASCCP.

View detail of an ASCCP
^^^^^^^^^^^^^^^^^^^^^^^

`Find an ASCCP <#find-an-asccp>`__.
Click on the ASCCP DEN after the desired ASCCP is found to open the ASCCP detail page.
To understand the detail of the ASCCP, see `Quick reference to different types of CCs <#find-the-usages-a.k.a.-where-used-of-a-core-component>`__.

**Tip**: You can open an "ASCCP detail" page from within another Core Component tree.
On any ASCCP node (bolded blue font node), click on the ellipsis next to the node and select "Open in new tab".

Create a new ASCCP
^^^^^^^^^^^^^^^^^^

There are two ways to create a new ASCCP.

1. Create an ASCCP from scratch.

   1. If you are not already on, open the "Core Component" page by
      clicking the "View/Edit Core Component" menu item under the "Core
      Component" menu at the top of the page.

   2. Click on the plus sign near the top-right corner of the page.

   3. Select "ASCCP".

   4. The ACC selection page is open. Check the check box in front of
      the desired ACC. The user can use other search filters to find the
      desired ACC. Certain types of ACCs are excluded from the list
      including "Extension", "User Extension Group", "Embedded",
      "OAGIS10 Nouns", and "OAGIS10 BODs". For explanation about these
      different types in connectCenter see `Component
      Types <#component-types>`__.

   5. A new ASCCP is created with revision #1. Its detail page is open
      with default values populated. The new ASCCP is in the WIP state.
      The developer may `edit the detail of the
      ASCCP <#edit-detail-of-an-asccp>`__.

2. Create an ASCCP from an ACC:

   1. `Open ACC detail page <#view-detail-of-an-acc>`__ where the
      current user is the owner of the ACC and the ACC is in the WIP
      state.

   2. Click the ellipsis next to the root node of the ACC tree in the
      left pane.

   3. Select "Create ASCCP from this" menu item.

   4. An ASCCP is created with default values. In this case, the
      property term is defaulted to the same as the ACC’s object class
      term. The ASCCP is in WIP state. See also `Edit detail of an
      ASCCP <#edit-detail-of-an-asccp>`__.

Edit detail of an ASCCP
^^^^^^^^^^^^^^^^^^^^^^^

This section describes ASCCP editing when its revision number is 1.

1. Open the ASCCP detail page according to `View detail of an
   ASCCP <#view-detail-of-an-asccp>`__. The ASCCP has to be in the WIP
   state to be editable. The following fields can be updated.

   1. *Property Term*. Property Term should be space-separated words,
      each with initial letter capitalized. Acronyms and plural words
      should be avoided. For connectSpec, it should be what one would expect
      to see in the expression. For example, a "Customer Party" ASCCP
      which uses the "Party" ACC should have a property term "Customer
      Party" (not just "Customer" as *CustomerParty* is expected in the
      expression), which would yield "Customer Party. Party" as DEN. In
      other words, the object class term "Party" is not used in the
      expression generation.

   2. *Nillable*. Nillable specifies whether a null value can be
      assigned in the instance data. *Nillable* is required, but it is
      defaulted to false.

   3. *Deprecated*. Since this is a brand new ASCCP. The deprecated
      field is locked.

   4. *Reusable*. This flag supports the notion of local element
      expression in XML Schema. It is required but it is defaulted to
      true, which makes the ASCCP analogous to the global element in XML
      Schema. There are two consequences when an ASCCP is set to not
      reusable – 1) the application will allow only one ASCC to use the
      ASCCP; and 2) there can be multiple ASCCPs with the same property
      term in a single release. In connectSpec, the Data Area component in a
      BOD is expressed as a local element. In such situation, the ASCCP
      corresponding to a Data Area would have this flag set to false.

   5. *Namespace*. Select a standard namespace from the dropdown list.
      See the `Namespace
      Management <#core-component-management-tips-and-tricks>`__ section
      to create a standard namespace if needed or how namespace may be
      used in connectCenter. *Namespace* is required.

   6. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field is free form text. Definition
      Source is optional.

   7. *Definition*. Specify the description of the ASCCP. *Definition*
      is optional, but a warning is given if none is specified.

2. Click the "Update" button at the top right to save changes.

Delete a newly created ASCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Delete a newly created CC <#delete-a-newly-created-cc>`__.

Restore a deleted ASCCP
^^^^^^^^^^^^^^^^^^^^^^^

See `Restore a deleted CC <#restore-a-deleted-cc>`__.

Revise an ASCCP
^^^^^^^^^^^^^^^

An ASCCP in the Published state can be revised where certain changes can be made.
Any developer user can revise a published ASCCP.
He/she does not have to be its owner.
To do that:

1. `Find an ASCCP <#find-an-asccp>`__ in the Working branch.

2. `Open detail page of the ASCCP <#view-detail-of-an-asccp>`__.

3. Click the "Revise" button at the top-right corner of the page. The
   ASCCP goes into the WIP state and its revision number increases by 1.

4. The following fields can be updated.

   1. *Nillable*. It can only be updated from false (unchecked) to true
      (checked).

   2. *Deprecated*. It can only be updated from false (unchecked) to
      true (checked).

   3. *Reusable*. It can only be updated from false (unchecked) to true
      (checked). If the reusable is changed to true, it means that there
      can be multiple ASCCs using the ASCCP. However, it may cause
      release invalidation if this results in multiple reusable ASCCP
      with the same property term.

   4. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field is free form text. Definition
      Source is optional.

   5. *Definition*. Specify the description of the ASCCP. *Definition*
      is optional but a warning is given if none is specified.

5. Click the "Update" button at the top right to save changes.

Cancel an ASCCP revision
^^^^^^^^^^^^^^^^^^^^^^^^

See `Cancel a CC revision <#cancel-a-cc-revision>`__.

Change ASCCP states
^^^^^^^^^^^^^^^^^^^

See `Change CC states <#change-a-cc-state>`__.

Transfer ownership of an ASCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Transfer ownership of a CC <#transfer-ownership-of-a-cc>`__

View history of changes to an ASCCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `View Change History of a CC <#view-change-history-of-a-cc>`__.
