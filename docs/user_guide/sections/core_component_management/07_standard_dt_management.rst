DT Management
~~~~~~~~~~~~~

DT Management allows users to create and manage Business Data Types (BDTs).
Data Types are managed on the separate "Data Type" page rather than on the Core Component page.

Find a DT
^^^^^^^^^

Open the "Data Type" page by clicking "Core Component" and then "View/Edit Data Type".
The Data Type page lists Data Types.
The current UI does not expose a separate CDT or BDT indicator in the list itself.
Like the Core Component page, it includes a Library selector and a Branch selector.
If the user has not chosen a library preference yet, connectCenter preselects the default library and shows it first in the selector.
The search bar searches by *DEN*, and Advanced Search provides additional filters such as State, Definition, Module, Deprecated, Commonly Used, Owner, Updater, Updated date, Namespace, and Tag.

**Tip**: Using the DT DEN pattern that is "Data Type. Type" along with the double quotes can help filter the research result down to mostly data types without having to use the Type filter.
For example, type "Amount. Type" (double quotes should be included) in the *DEN* field.

View detail of a DT
^^^^^^^^^^^^^^^^^^^

To view DT detail:

1. `Find a DT <#find-a-dt>`__.

2. Click on the DEN of the DT to open the "DT detail" page.

Create a new DT
^^^^^^^^^^^^^^^

Only BDT creation is supported.
The user cannot create a Core Data Type (CDT).
A BDT has to be based on either a CDT or another BDT.

1. If you are not already on, open the "Data Type" page by clicking
   the "View/Edit Data Type" menu item under the "Core Component"
   menu at the top of the page.

2. Click the "New Data Type" button near the top-right corner of the page.

3. The "Select based DT" page is opened.
   Click the checkbox in front of the desired DT to use as the base of the new DT.
   You can also use the search filters on the "Select based DT" page to find the desired base DT.

4. A new DT is created with revision #1.
   Its detail page is opened with default values populated.
   The new DT is in the WIP state.
   The developer may `edit the detail of the DT <#edit-detail-of-a-brand-new-dt>`__.

The "Select based DT" dialog is still scoped by the current library and branch context.
Depending on the selected branch, the list can also include imported base DTs from another library.
For example, a BDT in a business library can be created from a CDT supplied by the "CCTS Data Type Catalogue v3" library.

If the selected base DT is a library base DT, the new BDT inherits its content component, supplementary components (SCs), and default value domains from that base DT.
If the selected base DT is already a BDT, the new BDT inherits from that BDT instead.
The current DT creation flow does not create a new CDT and does not ask the user to choose a separate CCTS-versus-ISO specification option at creation time.

Edit detail of a brand new DT
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

This section describes DT editing when its revision number is 1.

1. Open the "DT detail" page according to `View detail of a
   DT <#view-detail-of-a-dt>`__. The DT has to be in the WIP state, and
   the current user has to be the owner to be editable. The fields in
   the details pane may be updated as follows.

   1. *Qualifier*: Qualifier field uses tokenize qualifiers. Each
      qualifier can be added as a string value after the qualifiers
      inherited from the base DT. A Qualifier can be removed by clicking
      the x icon. This field is required if the based DT already has a
      qualifier (however, it does not have to be the same or ends with
      the existing qualifier). Note: To add a qualifier in front of an
      existing qualifier, remove the existing qualifier first and type
      in each desired qualifier token and press Enter in the desired
      order. Alternatively, remove existing qualifier(s) and type in
      multiple qualifiers with the under bar and a space separator
      according to the CCTS specification and press Enter. For example,
      type in "Total\_ Tax" would result in two qualifiers.

   2. *Six Hexadecimal Identifier*. Click the renew icon to generate an
      identifier for this DT. This field is optional. If specified, this
      random number will be suffixed to the DT when it is serialized.

   3. *Namespace*. Select a standard namespace from the dropdown list.
      See the `Namespace Management <#namespace-management>`__ section
      to create a standard namespace if needed or how namespace may be
      used in connectCenter. *Namespace* is required.

   4. *Definition Source*. Specify the source of the definition. This is
      typically a URI but the field accepts a free form text.
      *Definition Source* is optional.

   5. *Definition*. Specify the description of the BCCP. *Definition* is
      optional but a warning is given if none is specified.

   6. *Content Component Definition*. Specify the definition of the DT’s
      Content Component value. This is typically a free form text.
      *Content Component Definition* is optional.

2. Click the "Update" button at the top right to save changes.

3. The developer may want to perform these other actions on the DT:

   1. `Edit Value Domain <#edit-value-domain>`__.

   2. `Add an SC to the DT <#add-an-sc-to-a-dt>`__.

   3. `Edit a newly added
      SC <#edit-details-of-a-brand-new-sc-in-the-dt-where-it-was-added>`__.

   4. `Remove a newly added SC from the DT <#remove-a-newly-added-sc-from-a-dt>`__.

   5. `Edit an inherited SC <#edit-details-of-an-existing-sc>`__.

Edit Value Domain
^^^^^^^^^^^^^^^^^

This section applies to value domain editing of both the DT content component and SCs.
Value domains inherited from the based DT or existing in the previous revision of the DT cannot be edited nor discarded.

1. On the `DT detail page <#view-detail-of-a-dt>`__ where the current
   user owns the DT, and the DT is in WIP state. Click on the root node
   of the DT or expand the root node on the DT tree and click on an SC
   node.

2. Click "Value Domain" in the detail pane on the right side to expand
   the "Value Domain" area. The following actions may be performed:

   1. Click the "Add" button to add a value domain.
      Since connectCenter has all possible Primitive value domains for DT content
      component and SC, only Code List or Agency ID List value domain types
      can be added. This is allowed only when there is a Token in the
      primitive value domain type. After clicking the "Add" button,
      select either Code List or Agency ID List value domain type and
      then select a specific Code List or agency ID List to use as
      the value domain. Finally, click the "Update" button on the top.

   2. *Set the default value domain*.
      Select the default value domain in the dropdown list.

   3. *Discard a newly added value domain*.
      To do so, click the checkbox in front of the desired value domain
      and click the "Discard" button. A value domain which is selected as
      the default value domain cannot be discarded. Change the default to
      another value domain first, then that default value domain can be
      discarded.

3. Click the "Update" button.

Add an SC to a DT
^^^^^^^^^^^^^^^^^

1. On the `DT detail page <#view-detail-of-a-dt>`__ where the current
   user owns the DT and the DT is in WIP state, expand the root node on
   the DT tree.

2. Click on the ellipsis next to the root node and select the menu item
   – "Add Supplementary Component". A new SC is added at the end of the
   DT tree. The default DEN of the SC node is in the format "[Data Type
   Term of the DT]. Property Term [a number]. [Representation Term]".

3. Click the SC on the tree to Edit details of a new SC.

Edit details of a brand new SC in the DT where it was added
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The section describes the case of editing an SC added to the DT.
The SC should have already been added in the latest revision of the DT.

1. On the `DT detail page <#view-detail-of-a-dt>`__ where the current
   user owns the DT, and the DT is in WIP state, expand the root node on
   the DT tree.

2. Click on an SC node of the DT. The detail of the SC is displayed in
   the detail pane on the right side of the page.

   1. *Property Term.* Property term should be unique across all SCs
      derived from this DT as well as within the DT itself. This field
      is required. It must be space-separated words, each with initial
      letter capitalized.

   2. *Representation Term*. Click on this field and select the
      representation term of the SC in the dropdown list. This field is
      required.

   3. *Cardinality.* The Cardinality field can be *Optional or
      Required.* To select the Cardinality of the SC, click the
      Cardinality field and select either *Optional* or *Required*.

   4. *Value Constraint*. Select *default* or *fixed value* constraint
      in the dropdown list and specify the value in the adjacent field.
      Value constraint is optional.

   5. *Value Domain*. See `Edit Value Domain <#edit-value-domain>`__.

   6. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field is free form text. *Definition
      Source* is optional.

   7. *Definition*. Specify the description of the SC. This is the
      definition of SC in the context of the DT. *Definition* is
      optional, but a warning is given if none is specified.

3. Click the "Update" button at the top right to accept changes.

**Note** that any changes to the SC are propagated to the corresponding SC in all DTs derived from this DT.
**Therefore, any restrictions put upon this SC will overwrite the restrictions in the derived DT.
However, editing an SC in the derived DT will not effect the same SC in the based DT.**

.. _remove-a-newly-added-sc-from-a-dt:

Remove a newly added SC from a DT
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Only a newly added SC can be removed.
SCs that are inherited from the based DT cannot be removed.
Removing an SC in a DT will also result in the removal of the same SC in derived DTs.

1. On the `DT detail page <#view-detail-of-a-dt>`__ where the current
   user owns the DT and the DT is in WIP state, expand the root node on
   the DT tree.

2. Click on the ellipsis next to any SC of the DT, if logically allowed,
   the "Remove" menu item is visible and active, select the menu item.

3. Click "Ok" in the confirmation dialog box, or "Cancel", if so
   desired.

.. _edit-details-of-an-existing-sc:

Edit details of an existing SC
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The section describes the case of editing an SC inherited from a based DT or when revising a DT.
The DT can be either a brand-new (revision #1) or revised DT (revision greater than #1).

1. On the `DT detail page <#view-detail-of-a-dt>`__ where the current user owns the DT,
   and the DT is in WIP state, expand the root node on the DT tree.

2. Click on an SC node of the DT. The detail of the SC is displayed in
   the detail pane on the right side of the page.

   1. *Property Term.* Property term cannot be changed.

   2. *Representation Term*. The Representation Term cannot be changed.

   3. *Cardinality.* Cardinality can be changed only if the current
      value is optional.

   4. *Value Constraint*. Select *default* or *fixed value* constraint
      in the dropdown list and specify the value in the adjacent field.
      Value constraint is optional.

   5. *Value Domain*. See `Edit Value Domain <#edit-value-domain>`__.

   6. *Definition Source*. Specify the source of the definition. This is
      typically a URI, but the field is free form text. *Definition
      Source* is optional.

   7. *Definition*. Specify the description of the SC. This is the
      definition of SC in the context of the DT. *Definition* is
      optional, but a warning is given if none is specified.

3. Click the "Update" button at the top right to accept changes.

**Note** that any changes to the SC are propagated to the corresponding SC in all DTs derived from this DT.
**Therefore, any restrictions put upon this SC will overwrite the restrictions in the derived DT**.

Delete a newly created DT
^^^^^^^^^^^^^^^^^^^^^^^^^

A DT whose revision number is 1 can be (marked) deleted.
The DT has to be in the WIP state and owned by the current user.
See `Delete a newly created CC <#delete-a-newly-created-cc>`__.

Restore a deleted DT
^^^^^^^^^^^^^^^^^^^^

See `Restore a deleted CC <#restore-a-deleted-cc>`__.

Revise a DT
^^^^^^^^^^^

A DT in the Published state can be revised where certain changes can be made.
Any developer user can revise a published DT.
He/she does not have to be its owner.
To do that:

1. `Find a DT <#find-a-dt>`__ in the Working branch.

2. `Open detail page of a DT <#view-detail-of-a-dt>`__ in the Published
   state.

3. Click the Revise button at the top-right corner of the page. The DT
   goes into the WIP state and its revision number increases by 1.

4. Only the following fields in the DT detail pane on the right may be
   updated.

   1. *Definition Source*. Specify the source of the definition. This is
      typically a URI but the field accepts a free form text.
      *Definition Source* is optional.

   2. *Definition*. Specify the description of the BCCP. *Definition* is
      optional, but a warning is given if none is specified.

   3. *Content Component Definition*. Specify the definition of the DT’s
      Content Component value. This is typically a free form text.
      *Content Component Definition* is optional.

5. Click the "Update" button at the top right to save changes.

6. The developer may want to perform these other actions on the DT:

   1. `Edit Value Domain <#edit-value-domain>`__.

   2. `Add an SC to the DT <#add-an-sc-to-a-dt>`__.

   3. `Edit details of a new
      SC <#edit-details-of-a-brand-new-sc-in-the-dt-where-it-was-added>`__.

   4. `Remove a newly added SC from the DT <#remove-a-newly-added-sc-from-a-dt>`__.

   5. `Editing details of an inherited
      SC <#edit-details-of-an-existing-sc>`__.

Cancel a DT revision
^^^^^^^^^^^^^^^^^^^^

See `Cancel a CC revision <#cancel-a-cc-revision>`__.

Change DT states
^^^^^^^^^^^^^^^^

See `Change CC states <#change-a-cc-state>`__.

Transfer ownership of a DT
^^^^^^^^^^^^^^^^^^^^^^^^^^

See `Transfer ownership of a CC <#transfer-ownership-of-a-cc>`__.

View history of changes to a DT
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

See `View Change History of a CC <#view-change-history-of-a-cc>`__.
