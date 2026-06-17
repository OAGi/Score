Manage BIE
----------

It is recommended that the user reads first the BIE in brief section (`BIE in brief <#bie-in-brief>`__).

BIE States
~~~~~~~~~~

A BIE can be in WIP, QA or Production state.
WIP state means that the BIE is still being changed or in fluid condition.
QA generally means that the BIE is ready to be reviewed or tested.
Finally, Production means that the BIE is already in use in a deployment.

The figure below shows life cycle states of BIEs.
When a BIE is created for the first time, it is placed in the WIP state.
Allowed transitions are from WIP to QA, from QA back to WIP and from QA to Production state.
A BIE can be discarded (i.e., permanently removed from the database) when it is in WIP state.

.. image:: media/image9.jpg
   :alt: Diagram Description automatically generated
   :width: 5.05208in
   :height: 1.23958in

The creator of a BIE is its first owner.
The ownership can be transferred.
Only the current BIE owner is authorized to change its detail and state.
All other users can view the details of a BIE only if the BIE is in QA or Production state.

The table below summarizes the actions and authorizations in each BIE state.

+-------------+----------------------------------------+----------------------------------------+
| Role State  | Current Owner                          | Other Users                            |
+=============+========================================+========================================+
| WIP         | Restrict the BIE.                      |                                        |
|             |                                        |                                        |
|             | Change its state to QA.                |                                        |
|             |                                        |                                        |
|             | Copy the BIE.                          |                                        |
|             |                                        |                                        |
|             | Express it.                            |                                        |
|             |                                        |                                        |
|             | Extend it if the owner is an end user. |                                        |
|             |                                        |                                        |
|             | Transfer ownership.                    |                                        |
|             |                                        |                                        |
|             | Uplift it.                             |                                        |
|             |                                        |                                        |
|             | Discard it.                            |                                        |
+-------------+----------------------------------------+----------------------------------------+
| QA          | View its details.                      | View its details.                      |
|             |                                        |                                        |
|             | Change its state back to WIP or        | Copy the BIE.                          |
|             | advance to Production.                 |                                        |
|             |                                        | Express it.                            |
|             |                                        |                                        |
|             | Copy the BIE.                          | Uplift it.                             |
|             |                                        |                                        |
|             | Express it.                            |                                        |
|             |                                        |                                        |
|             | Uplift it.                             |                                        |
+-------------+----------------------------------------+----------------------------------------+
| Production  | View its details.                      | View its details.                      |
|             |                                        |                                        |
|             | Copy the BIE and Express it.           | Copy the BIE.                          |
|             |                                        |                                        |
|             | Reuse it under another BIE.            | Express it.                            |
|             |                                        |                                        |
|             | Uplift it.                             | If the BIE is owned by a developer can |
|             |                                        | be reused by anyone. If it is owned by |
|             |                                        | an end user can be reused by end users |
|             |                                        | only.                                  |
|             |                                        |                                        |
|             |                                        | Uplift it.                             |
+-------------+----------------------------------------+----------------------------------------+

A BIE state change can also depend on inheritance and reuse relationships.
For example, when a BIE is based on another BIE, or when another BIE is inherited from it,
connectCenter may require the related BIEs to be moved to compatible states as well.

A Note About the BIE Visibility
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In the BIE page, users can view the list of all the BIEs that have been created so far by any user.
However, access to a BIE depends on its state as described in the table of the `BIE States <#bie-states>`__ section.

Create a BIE
~~~~~~~~~~~~

To create a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "Create BIE" from the drop-down list.

3. On the returned "Create BIE – Select Business Contexts" page, choose
   one or multiple Business Contexts to associate with the BIE by
   clicking the corresponding checkboxes. The logic that is applied
   between different Business Contexts is defined by the logical
   operator "OR". That is, a BIE can be meaningful and used in any of
   the assigned business contexts. If the desired Business Context does
   not exist, the user should exit this page by going to another page,
   e.g., `Create a Business Context <#create-a-business-context>`__,
   first and restart the whole Create BIE process. The user can also
   open another browser tab and create a Business Context, use the
   browser refresh button on the previous browser tab and the new
   Business Context will show up for selection. The user MAY use another
   browser tab to `Search and view Business Context
   detail <#search-and-view-business-context-detail>`__ to help with the
   selection.

4. Click the "Next" button.

5. On the upper middle side of the table on the returned "Create BIE –
   Select Top-Level Concept page", select the release on which you want
   to base your BIE in the *Branch* drop-down list.

6. Select an ASCCP from which the BIE is derived to become the root node
   (aka, root element) of the BIE. Developers can select
   Published/Developer ASCCPs only while end users can select
   Published/Developer ASCCPs or end user ASCCP that are in Production
   state. You can find the ASCCP via:

   1. Bottom pagination bar.

   2. Sorting the results by clicking on a column, such as the "DEN" or
      "Updated on" columns. Clicking the column name multiple times will
      toggle between the ascending and descending sorting.

   3. Search function which allows you to find an ASCCP via its Property
      Term, Definition and Module. The Module is a physical file that
      the ASCCP has been or to be serialized to in that particular
      release. When the Module is specified, only ASCCP whose Module
      path matches the specified string will be returned. It provides an
      additional filter or the user may simply enter a search string in
      the Module field and browse for an ASCCP. For example, a user
      using connectSpec standard may wish to search only ASCCPs serialized in
      the *Components.xsd*. In such case, the user can simply enter
      "Component" in the Module field (despite longer actual module
      path). The user should not enter a file extension (because of the
      syntax-independent purpose within connectCenter, the file extension is not
      kept but depends on what syntax to serialize to). As a second
      example, the user may enter "Noun" in the Module field. In this
      case, connectCenter will look for an ASCCP that is an connectSpec noun (because
      all connectSpec noun files have "Noun" in its module path). See also
      `How to Search and Filter for a Core
      Component <#how-to-search-and-filter-for-a-core-component>`__
      section which describes how the CC search works. These two search
      fields have the same behavior as the corresponding CC search
      fields.

   4. For *Owner*, *Updater*, *Updated start date* and *Updated end
      date* search filters, see `How to use Search
      Filters <#how-to-use-search-filters>`__.

..

   **Tips**: For the user who may have the knowledge of XML Schema and
   wonder what is and why he/she has to select an ASCCP, ASCCP
   corresponds to xsd:element in XML Schema, which is the only construct
   that can be instantiated. In an actual integration, a BIE is going to
   be instantiated; hence, we need an ASCCP at the root and ASCCP
   Property Term is the name of the element.

   **Tips**: Click on blank space in the ASCCP row will display its
   definition if exists.

7. Select an ASCCP via clicking its corresponding checkbox.

8. Click "Create".

9. The BIE is created in the WIP State; and the page where you can edit
   the BIE is returned. At this stage the user can `Restrict the
   BIE <#restrict-a-bie>`__ or `Extend the BIE <#extend-a-bie>`__.

Restrict a BIE
~~~~~~~~~~~~~~

To restrict a BIE, it has to be in the WIP State.
If you are not already on the page where you can edit a BIE, you need to first `Search and retrieve a BIE <#search-and-retrieve-a-bie>`__.

The BIE edit page is divided into the two panes: the left one depicting the BIE tree, namely the *Tree* pane, and the right one showing the details of a selected node of the BIE, namely the *Details* pane.

A BIE tree is a data structure in which different types of BIEs are organized hierarchically.
Each node in the BIE tree has names that are displayed in a few different font types corresponding to different types of the BIEs.
We will not go into the detail of different BIE types, because the user should not have to, i.e., connectCenter aim is for BIE to serve business analysts and they should have minimal concerns about modeling constructs.

For the BIE editing purpose, what matters is only two different kinds of nodes, one that is a container of other nodes and cannot directly have a value and the other that can have a value.
The latter is apparent with the fields on the right, *Details* pane.
It has extra fields to provide the value restriction (aka, value domain), such as fixed, primitive type, or code list allowed in the instance.
Except for the root node, before you can customize the fields in its *Details* pane, the node **NEED TO be enabled** either by using the checkbox in the BIE tree or by selecting the node on the BIE tree and checking the *Used* checkbox on the top of the *Details* Pane.
Some nodes are enabled by default because it is a mandatory node.

**Tips:** Toggle the "Hide unused" checkbox at the top-right corner of the page to hide or display the unused nodes of a BIE.

**Important:** After the node is already enabled, **BE SURE** to click on the node in the BIE tree you want to customize its details.
The *Details* pane display details of the node highlighted in the tree.
Just clicking the checkbox on a node does not bring up the details of the node on the right-side; it is still showing the details of the last node selected, **be careful** with that as you might end up editing the details of the previous node you selected and not the last node you enabled (Note that this behavior allows the UI to work faster.
Additionally, some users prefer to browse through the tree and check/enable the nodes he/she wants and return later to edit their details).

**Tips:** Notice the breadcrumb on top of the tree.
It shows the focus of the *Details* pane.

Below, we explain the fields in the detail pane of the few types of nodes described earlier.

**The root node:**

.. list-table::
   :widths: 30 70

   * - *Name* (Uneditable)
     - The name of the root data element (e.g., BOD, Noun, Common Components) that is displayed at the top of the detail pane. This is the same as its corresponding CC and is not editable.
   * - *Release*
     - The release of the core component (e.g., 10.6) that the root node is derived from.
   * - *State*
     - The state of the BIE (e.g., WIP, QA or Production).
   * - *Owner*
     - The user that currently owns the BIE.
   * - *Business Context* (Mandatory)
     - At least one Business Context must be assigned to a BIE. Multiple different business contexts can also be assigned to a BIE. These business contexts are joined by the logical connective OR so that it means the BIE can be used in multiple contexts. Add a business context by starting to type the business context name. A list of matching business context will appear for selection. Remove a business context by clicking on the X mark next to the business context. There is no need to click the "Update" button.
   * - *Version* (Optional)
     - Version number you want to assign to the root BIE. It can be in any format your organization chooses. When the *Version* is set and there is a "Version Identifier" direct child in the root BIE, the *Fixed Value Constraint* is automatically assigned to the "Version Identifier" node. The Fixed Value Constraint of the "Version Identifier" node is also set to be the same as the Version field, but it can still be changed. The value of the Version field of the root node drives the Fixed Value Constraint of the "Version Identifier" node, but not the opposite.
   * - *Status* (Optional)
     - This is a free text field typically used for the detailed BIE development status in addition to the built-in statuses described in the `BIE Review Process <#bie-review-process>`__. For example, while the BIE is in the WIP or QA state, an organization may wish to capture detailed statuses, such as Data Architect Review, Data Architect Approve, Development and Testing, Development Review, and Testing Completed.
   * - *Inverse Mode* (Optional)
     - In *Inverse Mode*, all disabled nodes under the root BIE are processed as an enabled node. For example, the user could turn this mode on when it needs to enable all components in the tree.
   * - *Legacy Business Term* (Optional)
     - Other names of the data element commonly known in the context. For example, the user may wish to capture a BOM BIE that is commonly known as Super BOM, Engineering BOM, etc. in its context. At present the application supports only one Business Term field; however, the user may use a semicolon to separate multiple Business Terms.
   * - *Remark* (Optional)
     - Remark is a free-form text field that can be used to capture comments that are not a part of the semantic definition, such a reminder or note the BIE editor would like to make. For example, the user may wish to take a note "Need to discuss this with Scott."
   * - *Context Definition* (Optional but highly recommended)
     - This field captures the context-specific semantic definition of the BIE in natural language. It may describe in detail how or in what situation the BIE should or should not be used. For example, "This BOM BIE is for capturing super BOM (aka Model BOM) that represents all possible options and configurations of a product". Implementation detail that should be considered by developer can be placed here as well, including mapping details.

**Descendant Nodes that cannot have a value but children nodes in an instance:**

.. list-table::
   :widths: 30 70

   * - *Name* (Uneditable)
     - Name of the data element the BIE node represents. Name is also displayed at the top of the detail pane. This is not editable.
   * - *Used* (Required)
     - Checkbox indicating that the node is enabled or disabled. This is the same as the checkbox on the tree.
   * - *Min* (Required)
     - Minimal number of allowed occurrences for the node in an instance data. This field is defaulted to the same as its corresponding CC the node is based on. The value of this field has to be within the range of the CC’s Min and the current *Max*.
   * - *Max* (Required)
     - Maximum number of allowed occurrences for the node in the instance data. This field is defaulted to the same as its corresponding CC the node is based on. Max cannot be more than the defaulted value. To specify unbounded occurrences, type ‘-1’ or ‘unbounded’. Typing in ‘-1’ also puts the field back to the default value.
   * - *Nillable* (Required)
     - Indicate whether a NULL value can be assigned to the node in the instance data. The default value of the field is the one assigned to Core Component from which the node is derived. Note that in different syntax expressions nullifying a node may be expressed differently, and certain syntaxes may not support it.
   * - *Business Term* (Optional)
     - Other names of the data element commonly known in the context. For example, the user may wish to capture that a BOM BIE is commonly known as Super BOM, Engineering BOM, etc. in its context. At present the application support one Business Term field; however, the user may use a semicolon to separate multiple Business Terms. Alternatively, because connectCenter does not have a function to do mapping, an organization may wish to designate Business Term for manually capturing mapping to the BIE.
   * - *Remark* (Optional)
     - *Remark* is a free-form text field that can be used to capture comments that are not part of the semantic definition, such as a reminder or note the BIE editor would like to make. For example, the user may wish to take a note "Need to discuss this with Scott."
   * - *Context Definition* (Optional but highly recommended. Required in some situations)
     - This field should be used for capturing a context-specific semantic definition of the BIE in natural language. The context-specific semantic definition is based on the *Association Definition*, *Component Definition*, and *Type Definition*, which describes the general, context-independent purpose of the data element as described in the next row. For example, this field may describe in detail how or in what situation the BIE should or should not be used, as in "This BOM BIE is for capturing super BOM (aka Model BOM) that represents all possible options and configurations of the product." Implementation detail that should be considered by developer can be placed here as well, including mapping details.
   * - *Association Definition*, *Component Definition*, and *Type Definition* (Uneditable)
     - These three fields are for informative purposes. They display the canonical CCs’ definitions, from which the BIE node is derived. A single node in the BIE tree has three definitions (some of which may be blank because they were not specified) because the BIE tree simplifies the view from the canonical CC model. Generally, the user should interpret these three definitions as follows. The Association Definition adds to the Component Definition additional explanation when the corresponding **reusable** component is used within the parent BIE node. Similarly, the Component Definition adds to the Type Definition when the corresponding **reusable** type is used to define the component. For example, a type can be an Address Type, and a component can be a Home Address or another component Work Address, both of which uses the reusable Address Type. Both Home Address and Work Address component should have their own Component Definitions, one saying "It is the residential address" and the other saying "It is the address where businesses are conducted." Both Home Address and Work Address components may be associated (used) with an Employee type resulting in two associations, which have corresponding two Association Definitions. The Association Definition of the Home Address may say "An employee may have multiple home addresses, one of which must be designated as primary." Therefore, a user may wish to see all three definitions of the CCs, from which the Home Address BIE (under the Employee BIE) is derived, including the Association Definition, the Component Definition (definition of the Home Address component itself), and Type Definition (from the Address type). One can imagine that the Home Address component may also be associated with a Contact type, where a different Association Definition of the Home Address may be specified.

**Descendant nodes that can have a value in the instance:**

.. list-table::
   :widths: 30 70

   * - *Name* (Uneditable)
     - Name of the data element the BIE node represents. It is also displayed at the top of the detail pane. This is not editable.
   * - *Used* (Required)
     - Checkbox indicating that the node is enabled or disabled. This is the same as the checkbox on the tree.
   * - *Min* (Required)
     - Minimal number of allowed occurrences for the node in the instance data. This field is defaulted to the same as its corresponding CC. The value of this field has to be within the range of the CC’s Min and the current *Max*.
   * - *Max* (Required)
     - Maximum number of allowed occurrences for the node in the instance data. This field is defaulted to the same as its corresponding CC. Max cannot be more than the defaulted value. To specify unbounded occurrences, type ‘-1’ or ‘unbounded’. Typing in ‘-1’ also puts the field back to the default value.
   * - *Nillable* (Required)
     - Indicator whether a NULL value can be assigned to the node in the instance data. The default value of the field is the one assigned to Core Component from which the node is derived. Note that in different syntax expressions nullifying a node may be expressed differently, and certain syntaxes may not support it. It should be noted that some nodes do not have Nillable, because they are considered meta-data of the parent node. In other words, because the value in the parent node would be ambiguous without it, it will never be nullified in an exchange. This can be a node designated as an *Attribute* Entity Type in the CC and the CC’s Supplementary Component nodes.
   * - *Value Constraint* (Optional)
     - This field is used for specifying the default or the fixed value of the selected node. Note that these two values are mutually exclusive, namely you can specify only one of the two. *Default Value* indicates the value that should be assumed when a value is not specified in an instance of the BIE. *Fixed Value* can be used to restrict the valid value of the data element to one and only one fixed value. When the node name is "Version Identifier" and is directly under the root BIE, its *Fixed Value Constraint* field is automatically assigned and reflects the value of the *Version* field of the root node. However, the value constraint of the "Version Identifier" can still be changed to any other value. This does not cause any change to the Version field of the root node though.
   * - *Business Term* (Optional)
     - Other names of the data element commonly known in the context. For example, the user may wish to capture that an Identifier BIE of a Person BIE is also known as Social Security Number or Driver License Number in its context. At present the application support one Business Term field; however, the user may use a semicolon to separate multiple Business Terms. Alternatively, because connectCenter does not have a function to do mapping, an organization may wish to designate Business Term for manually capturing mapping to the BIE.
   * - *Remark* (Optional)
     - Remark is a free form text field that can be used to capture comments that are not part of the semantic definition, such as a reminder or note the BIE editor would like to make. For example, the user may wish to take a note "Need to discuss this with Scott."
   * - *Example* (Optional)
     - It is a free form text field that can be used to provide a data instance example such as a date. The user should specify only one value. It may be serialized as part of a schema or used for an example instance generation function.
   * - *Value Domain Restriction* (Required)
     - This field is required; however, it is defaulted to the same as that of the CC the BIE is derived from. There are three subfields within the *Value Domain Restriction*. They are: *Business Data Type*, which is not editable and indicates the semantics of the business data type used by the BIE node; *Value Domain Type*, which is used to indicate how the value domain will be restricted and changes the choices in the next field; and *Value Domain*. If selected *Value Domain Type* is *Primitive*, then this field gives the user the list of primitives to choose, e.g., integer, string, token, etc. The available choice depends on the Business Data Type and the primitive specified in the CC model. For example, if the primitive in the CC model is Integer, then only those primitives that are restrictions of Integer are available. If the Business Data Type is Date Time, all primitives related to date and time are available. Those primitives started by ‘xbt’ are primitives defined in connectSpec. If selected *Value Domain Type* is *Code*, then this field gives the user the choice of the applicable code list. Applicable code list depends on the primitive and code list derived from the CC model. If the BIE node uses primitive in the CC model, any code list is allowed. However, if the BIE node uses a particular code list (e.g., Language Code) in the CC model, then only code lists that are based on the Language Code are available for selection. When a developer (rather than an end user) edits the BIE, any end-user code list shown in the *Value Domain* list appears greyed out and cannot be selected; a developer can therefore see an end-user code list already assigned to the node but cannot assign or re-assign it, while developer code lists remain selectable. If selected *Value Domain Type* is *Agency*, then this field gives the user the choices of agency identification lists.
   * - *Context Definition* (Optional but highly recommended. Required in some situations)
     - This field should be used for capturing a context-specific semantic definition of the BIE in natural language. The context-specific semantic definition is based on the Association Definition and Component Definition, which describe the general, context-independent purpose of the data element as described in the next row. For example, this field may describe in detail how or in what situation the BIE should or should not be used, as in "The Tax Amount for internet order should always be zero unless the buyer address is in Maryland. In that case, Maryland tax rate shall apply." Implementation detail that should be considered by developer can be placed here as well, including mapping details.
   * - *Association Definition* and *Component Definition* (Uneditable)
     - These two fields are for informative purposes. They display the canonical CCs’ definitions, from which the BIE node is derived. A single node in the BIE tree has these two definitions because the BIE tree simplifies the view from the canonical CC model, and the tool simplifies the view of this type of BIE node even further, which is why the Type Definition is not included in this case. It should be noted that some definitions are blank because they were not specified in the CC model. Generally, the user should interpret these two definitions as follows. The *Association Definition* adds to the *Component Definition* additional explanation when the corresponding **reusable** component is used within the parent CC node. For example, a reusable component can be a Tax Amount, which has its own *Component Definition*, saying "Tax Amount is the amount charged by the government on top of the sales price." The Tax Amount component may be associated with an Invoice Line. Hence, an *Association Definition* can be provided in the model for the relationship between the Invoice Line and the Tax Amount. The Association Definition may be "Tax Amount on the invoice line item." Note that for some nodes in this category, only the Component Definition is present. This is normal. For users familiar with CC specification, it is because these are derived from Supplementary Components that do not reuse types.

In summary, a BIE is edited by enabling a BIE node in the BIE tree to be used and then changing its details.
To do so:

1. Expand the BIE tree by clicking the triangle icon in front of the
   tree node.

2. Click the name of the tree node you want to change its detail.

3. Click the "Used" checkbox, either on the tree or the detail pane, in
   order for this component to be used.

..

   **Tips**:

-  The user can keep expanding the BIE tree until the node he/she would
   like to enable appear and only check the particular node. All the
   ancestor nodes are automatically enabled. Also, a BIE tree can be
   very huge, containing hundreds of thousands of nodes. The `Search
   within the BIE Tree <#search-within-the-bie-tree>`__ function can be
   helpful. The user can click the ellipsis icon located next to a BIE
   node to open the node’s context menu. Then, the user can then click
   "Enable Children" option to enable all the children of this node in
   one shot (i.e., to enable their "Used" checkbox). The user can also
   click "Set Children Max Cardinality to 1" option to set the Max
   Cardinality to 1 for all children node of BIE node in focus.

4. Scroll down detail pane on the right side and find the field you want
   to change its value.

5. Enter a new value in the field.

6. Click "Update" when finish.

7. Click on the "Round Arrow" icon located at the top right of the
   detail pane to reset the values of the BIE node back to their initial
   ones retrieved from the corresponding Core Component.

Note that you have to click "Update" in order for the BIE to be updated and for the changes to be saved.
Each time you click "Update" a confirmation message appears at the bottom of the page informing you that the BIE has successfully updated.
Although it is not necessary to click "Update" for every change to the BIE node, it is recommended that the user click the "Update" button frequently.
If the server response is slow either due to network tardiness or server loads, the user might want to click the "Update" button less frequently.

Note that while end users can create and restrict a BIE which is derived from an end user ASCCP that is in Production state, its editability may change over time.
This is because the ASCCP or any of its descendant CCs may be amended while the BIE is being edited.
In other words, end user CCs may change to WIP, QA or Deleted state anytime.
The BIE nodes, whose underlying CC is an end user CC not in Production state, will not be editable.
These BIE nodes, including their children, are shown in the BIE tree, but they are not editable.
They become editable when the underlying CC is moved in the Production state again (the BIE page has to be refreshed or reopened).
In case that a CC node is Deprecated, the corresponding BIE node can be still edited, but it is flagged in grey color.

Extend a BIE
~~~~~~~~~~~~

BIE extension fundamentals
^^^^^^^^^^^^^^^^^^^^^^^^^^

Extension is generally an connectSpec architectural concept.
The `UN/CEFACT Core Component Specification (CCS) <https://www.unece.org/fileadmin/DAM/cefact/codesfortrade/CCTS/CCTS-Version3.pdf>`__ has no provision for extension.
Almost all connectSpec complex components (i.e., connectSpec `ACC <#aggregate-core-component-acc>`__) have their own extension points.
It is a practical design that allow standard users to add content specific or proprietary to the organization adopting the standard.
For example, extension may be appropriate for adding specific fields needed in reference components such as Purchase Order Reference (possibly because the organization found that another call back to retrieve specific information about the purchase order to achieve its integration objective does not meet its performance criterion).

While in connectCenter, an extension is invoked on a BIE node, the content is added to the CC.
Consequently, the added content shows up on current and future BIEs that are based on the same CC.
For example, if end user A had invoked BIE Extension on and added content to the Extension node of the Purchase Order Line BIE that is based on the Purchase Order Line CC in Release 10.6, and another Purchase Order Line BIE created later on by end user B on the same release will also see the content added by end user A.

When the user invokes a BIE extension function, a few types of Core Components (CCs) are created behind the scene including a User Extension Group ASCC, a User Extension Group ASCCP and a User Extension Group ACC (UEGACC).
These CCs are hidden in the CC view, except the UEGACC.

The purpose of the ASCC and the ASCCP is only to allow the UEGACC to be added to the Extension component, as illustrated below.
In this illustration, the Application Area Extension ACC is the Extension component of the Application Area ACC (the Application Area ACC is not included in the illustration).

The reason connectCenter creates the Application Area User Extension Group ACC is so that revisions can be made to the extension without revising the (standard) Application Area Extension ACC.
When the user edits the extension, i.e., adding/removing the data elements via BCCs or ASCCs, he/she is actually editing the UEGACC.
DEN pattern of the UEGACC is the concatenation of the [Name of the BIE node parent to the extension node] and the string "User Extension Group. Details".
The user may open UEGACC in the Core Component to make edits.
See `End User Core Component Management <#life-cycle-dependency-between-eucc-and-bie-extension>`__. data elements appear as if they were direct children of the Application Area Extension BIE.

.. image:: media/image10.jpg
   :alt: Diagram Description automatically generated
   :width: 6.5in
   :height: 4.07778in

It should be noted that BIEs corresponding to these CCs are hidden from the user in the BIE view.
Data elements added to the UEGACC will appear as if they were direct children of the Application Area Extension BIE.

Basic BIE extension
^^^^^^^^^^^^^^^^^^^

This section provides a guide to extend a BIE, when it is not being extended by another user or it has never been extended within the CC release the BIE bases on.
See `Advanced BIE extension <#advanced-bie-extension>`__ for guides about these other situations.

BIE extension is accessible only by end users.
Developers can neither extend a BIE nor use the associations of the extensions created by end users when they restrict a BIE.
Developers can only view BIE extensions in the Core Component list page (see `How to Search and Filter for a Core Component <#how-to-search-and-filter-for-a-core-component>`__).
In order for a BIE to be extended, it has to be in WIP State.
In addition, a BIE node can be extended only if it has an Extension child node.
A BIE node has an Extension child if its corresponding CC has the Extension child component (as designed by the standard architect).

To start, if the top-level BIE you would like to extend is not already opened, see `Search and retrieve a BIE <#search-and-retrieve-a-bie>`__ to open it.

On the page where you can edit a BIE\ *,* expand the BIE tree until you see the desired Extension point (a node with the name Extension) of a BIE node you would like to extend.
Click on the ellipsis icon located next to the Extension component as shown in the illustration below.

The context menu appears showing two options related to BIE Extension – "Creating ABIE Extension Globally" and "Create BIE Extension Locally".
Once you have chosen to create an extension, you can append (i.e., create an association to) an ASCCP or a BCCP to the extension.
**Important**: Click Update to save BIE before invoking extension as unsaved BIE data will be loss.

A global BIE extension means that the added BCCP will appear globally in all BIE’s extensions.
The user cannot add an ASCCP to a global BIE extension since it will create a cyclical structure (this is because most ASCCP also contains the global extension).
Compared to a global BIE extension,, a local BIE extension exists only in the component you have selected to extend (for example, in the above illustration added data elements will be specific to the Allocate Resources component).

Global extension is a design/feature of the connectSpec standard.
Making such extension is generally very rare as any added data element will appear in all Extension components.
One example situation could be when an entire enterprise architecture decided that all (extensible) data component should have a UUID.

To create an extension:

After either "Create ABIE Extension Locally" or "Create ABIE Extension Globally" context menu item is invoked, connectCenter displays the corresponding UEGACC (see the `BIE extension fundamentals <#bie-extension-fundamentals>`__ section).
This is actually a Core Component editing view.
Without going into details of this view, the user can keep expanding the tree to see generic data elements already exist in any extension component.
These are non-semantic extension data elements.
However, the user can add semantic data element to the UEGACC, see `Add a property to an ACC <#add-a-property-to-an-acc>`__.
The user may also want to construct additional End User Core Component (EUCC) and use them in the UEACC.
See `End User Core Component Management <#life-cycle-dependency-between-eucc-and-bie-extension>`__.

After additional data elements have been added to the UEGACC, they cannot be used or edited in the BIE while the UEGACC is still in the WIP state.
Indeed, the UEGACC can be in three states as described below.
Only when it is in the Production state, then its content can be used in BIEs:

1. WIP state that allows for appending and removing data elements
   (ASCCPs or BCCPs). In this state, no other user can invoke the
   extension on the BIE with the same underlying Core Component. Other
   users can however view the current content of the UEGACC by open it
   from the "View/Edit Core Component" under the "Core Component" menu.
   Current content of the UEGACC also shows up in corresponding BIE
   extensions; however, the content cannot used or edited in the BIE.

2. QA state that allows other users to review and provide their comments
   (see `Commenting <#commenting>`__). In this state, the UEGACC cannot
   be changed. However, the current owner of the CC can transition the
   state back to WIP for further editing or to Production state. Similar
   as in the WIP state, no other user can invoke BIE extension that uses
   the same UEACC; and the content of the UEGACC can be viewed by other
   users in the Core Component view and also in the corresponding BIE
   extensions but it can not be used or edited in the BIE.

3. Production state. In this state, the revision is permanently made to
   the UEGACC, it is like a commit in a version control. The
   significance of this state to the BIE development is that the content
   of the UEGACC can be used in corresponding BIE extensions. Note
   however that if the UEACC uses any EUCC and if the EUCC is not the
   Production state, the BIE node corresponding to the EUCC still cannot
   be viewed, used, or edited (see `Life-cycle dependency between EUCC
   and BIE
   extension <#life-cycle-dependency-between-eucc-and-bie-extension>`__).
   Also, in this state, the BIE Extension can be invoked again on the
   BIE node that relies on the UEGACC. This would result in increasing
   in the revision number of the UEGACC and only backwardly compatible
   changes can be made (see also `Advanced BIE
   extension <#advanced-bie-extension>`__). See `BIE
   States <#bie-states>`__ and `BIE Review
   Process <#bie-review-process>`__ for additional information about BIE
   states.

There are two ways to open the UEGACC page again (if you have left the page).
First, open by invoking BIE Extension on the same extension node in the BIE as described in `Basic BIE extension <#bie-extension-fundamentals>`__.
This option is available only to the current owner of the UEGACC.
The other way is by the Core Component page.
In this case, the user clicks the "View/Edit Core Component" menu item under the "Core Component" menu (see `How to Search and Filter for a Core Component <#how-to-search-and-filter-for-a-core-component>`__ and see `BIE extension fundamental <#bie-extension-fundamentals>`__ for the UEGACC DEN pattern).
This option is available to both the current owner and other users.

Advanced BIE extension
^^^^^^^^^^^^^^^^^^^^^^

Case 1: The UEGACC is being edited by another user, i.e., it is in WIP.

The user encounters this situation when he/she tries to extend a BIE node, which uses the same Extension core component (or strictly speaking the UEGACC, see `BIE extension fundamentals <#advanced-bie-extension>`__) as another BIE node also being extended.
For example, while user A is extending the Application Area node within an Acknowledge BOM BIE, the respective Application Area UEGACC is being Edited.
If user B invokes the extension on the Application Area node within a Show Shipment BIE, user B will receive a message indicating that extension to the Application Area is being made by another user and that user B cannot perform an extension to the BIE at this time.

Case 2: Similar to case 1, but the Extension core component is in QA state.

With respect to the example given in Case 1, user B will receive a message indicating that the extension to the Application Area is being made by another user and asking whether he/she would like to review the extension.
If the user answers yes, then the UEGACC is displayed.
The user B can provide comments about what changes he/she might want to the extension (see `Commenting <#commenting>`__).

Case 3: The BIE has been extended before.

This case means that there is already a revision of associated UEGACC in the Production state (i.e., a revision of UEGACC) before the current BIE Extension invocation.
In this case, connectCenter will open the UEGACC for amendment.
The user can update the UEGACC as described in `Amend an ACC <#amend-an-acc>`__ – except setting/removing a based ACC.
To continue the example made in Case 1, in this case, user A has already moved the Application Area UEGACC to the Production state.
If user B then invokes the extension on the Application Area BIE, this will result in the amendment of the Application Area UEGACC where its revision number will increment by 1.
For the amendment, only backwardly compatible changes can be made.
For example, User B cannot remove any associations added earlier by user A.
He can only edit existing associations such as deprecating them.
He can, of course, add additional associations.
It should be noted that, alternative to invoking the BIE Extension on the same component, the user can also `amend <#amend-an-acc>`__ the UEGACC directly.

Life-cycle dependency between EUCC and BIE extension
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

As indicated earlier, a BIE extension, i.e., an UEGACC may use other EUCCs at some of its descendant nodes.
The states of these EUCCs (note that UEGACC is also a kind of EUCC) can change independently, e.g., some may be in WIP, some in QA, and some in Production, and those in Production may also be amended at any time by any end user causing it to go back to WIP. connectCenter ensures the consistency between the BIE contents derived from these EUCCs while they are still changing by two mechanisms; 1) blocking BIE from modification while the corresponding EUCC is not in the Production state and 2) only allowed backwardly compatible changes to the EUCC if the EUCC has revision number 2 or more.

Because of the first mechanism, BIE nodes corresponding to EUCCs that are not in the Production state are either blocked from expanding or from making any changes in the BIE tree.
For example, if an UEGACC owned by user A uses an EUCC that is in WIP and is owned by user B, User A will not be able to profile the EUCC in UEGACC until user B moves the EUCC into the Production state.
And if another user C happens to amend the EUCC while user A is profiling the extension, user A will be profiling based on the pre-amendment version until user refresh the BIE page.
If after refreshing the BIE page, the EUCC is still in WIP or QA, user A will not be able to make further profiling until the EUCC is moved into the Production state again.

Search and retrieve a BIE
~~~~~~~~~~~~~~~~~~~~~~~~~

To find and retrieve a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit BIE" from the drop-down list.

3. Search for the BIE you want to edit via any of these options:

   1. Bottom navigation bar.

   2. Sorting the results by the terms exist in the first row of the
      table, viz *State*, *Property Term*, *Owner*, *Business Contexts*,
      *Version*, *Status*, *Business Term*, *Remark* and *Updated on*.

   3. The *Property Term* and *Business Context* search fields. See also
      `How to use the Search field in
      general <#how-to-use-the-search-field-in-general>`__.

   4. The *State*, *Owner*, *Updater*, *Updated start date* and *Updated
      end date* filters. See also `How to use Search
      Filters <#how-to-use-search-filters>`__.

   5. The *Branch* filter that allows for filtering BIEs based on their
      release. To do this:

      1. Choose the release you would like to view from the "Branch"
         drop-down list near the top-left of the page. It should be
         noted that any change in the *Branch* filter of a BIE-related
         page (i.e., the "BIE", "Create a BIE", "Copy BIE" and "Express
         BIE" pages) is stored and used across all BIE-related pages.
         The "Code List" page is also BIE-related only if it is visited
         via "View/Edit Code List" under the "BIE" menu.

4. Click the checkbox locate next to the BIE you want to edit. Or, if
   you want to view the BIE details, click on its Property Term or its
   GUID rather than clicking its corresponding checkbox.

Search within the BIE Tree
~~~~~~~~~~~~~~~~~~~~~~~~~~

In the BIE detail page, the user can search the BIE tree.
This allows the user to quickly locate the desired BIE node within possibly hundreds of thousands of nodes by the node label.
To use the search within the BIE Tree.

1. On the *Edit BIE* page, click on a node within the BIE tree to set
   the scope of the search. Selecting a lower level node in the tree
   will narrow down the scope of the search and also return the result
   faster.

2. Input a search term near the top-left corner of the page and hit the
   "Enter" key or click the magnifying glass icon. It should be noted
   that node labels are space separated words and ID is spelled out as
   Identifier.

3. If there is any match with the search term, the number of matches is
   displayed next to the search term. Use the adjacent Up/Down arrow
   icons to step through the search result.

4. Use the "Exclude SCs" checkbox in order to skip (i.e., exclude) the
   BBIE_SCs from the searching function. When it is checked (i.e.,
   enabled) the searching function does not consider the BBIE_SC nodes.

Discard a BIE
~~~~~~~~~~~~~

Discarding a BIE permanently remove it from the database and CANNOT be undone.
In order for a user to discard a BIE, he/she has to be the owner of the BIE and the BIE has to be in WIP state.
A BIE in QA or Production state cannot be discarded.

To discard a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "View/Edit BIE" from the drop-down list.

3. `Search <#search-and-retrieve-a-bie>`__ and select one or more the
   BIEs you want to discard by clicking on the corresponding checkboxes.

4. Click the "Discard" button at the top-right of the page.

5. A Modal Dialog is displayed where you need to confirm your intention
   that the BIEs are going to be permanently removed.

BIE Review Process
~~~~~~~~~~~~~~~~~~

BIE review process supports the collaborative aspect of the BIE development.
This is enabled by three BIE development states, namely WIP, QA and Production.
They may be used in the following fashion.

1. A BIE developer creates a BIE and keeps it in the WIP state. He is
   the BIE **owner** and he is the only one having access to edit the
   BIE in this state. Other users cannot view the BIE details nor edit
   it in the WIP state. Other users can only see that the BIE exists and
   is in the WIP state.

2. When he is done with the BIE development, he transitions it to the QA
   state. In this state, other users, such as other subject matter
   experts, developers, data or enterprise architect can access, review,
   and provide offline comments for the BIE.

3. For the BIE developer to make changes after receiving comments,
   he/she takes the BIE back to the WIP state. To change a BIE’s state
   from QA to WIP, the BIE developer has to retrieve the BIE (see
   `Search and retrieve a BIE <#search-and-retrieve-a-bie>`__ ) and then
   click "Back to WIP" at the top-right of *Edit BIE* page. Steps 2 and
   3 can be revisited to complete the BIE review cycles. In addition to
   the BIE state, the *Status* field in the root BIE node may be used
   for capturing detailed BIE development states, as it is a free text
   field. For example, the BIE developer may set the status field to
   ‘Architecture Review’ in the first few cycles of shuttling between
   the WIP and QA state. Then, he/she may set the status field to
   ‘Implementation Test’, the next time he put the BIE to the QA state
   and then ‘Final Review/Approval’.

4. Once there are no more comments, the BIE developer himself may move
   the BIE to the Production state. To do so, BIE developer retrieves
   the BIE (see `Search and retrieve a
   BIE <#search-and-retrieve-a-bie>`__) that is already in the QA state
   and clicks "Move to Production". Alternatively, an organization may
   designate a user, such as an enterprise architect to be the solely
   responsible user for the BIE life-cycle management. In such a case,
   the BIE developer would `Transfer BIE Ownership (Making BIE editable
   by another
   user) <#transfer-bie-ownership-making-bie-editable-by-another-user>`__
   to the enterprise architect first who makes a final decision whether
   he wants to move the state of the BIE to the Production state. The
   BIE developer or the enterprise architect may use the status field to
   indicate a detailed state such as Production. They may also use the
   version field in the root BIE to communicate the BIE revision, e.g.,
   "OAGIS_10.4_BIE_1.0.0". Once the BIE is in the Production state, it
   can no longer be changed or discarded. To make a new revision of the
   BIE, see `Copy a BIE <#copy-a-bie>`__.

Transfer BIE Ownership (Making BIE editable by another user)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A BIE can be edited only by the current owner.
BIE ownership transfer may be used, for example, during the `BIE Review Process <#bie-review-process>`__, or when the owner of the BIE leaves the organization or wants to change the authorship so that another user can edit it.
In order to transfer the ownership of a BIE, the BIE has to be in WIP state.
Transferring the ownership of a BIE is allowed either between developers or between end users.
That is, a developer cannot transfer the ownership to an end user and vice versa.

Note that if the current BIE owner has left and no one knows the password of that user, someone with the connectSpec developer role can reset the user’s password, log in as that user, and transfer the ownership.

To transfer the ownership of a BIE:

1. `Search and retrieve a BIE <#search-and-retrieve-a-bie>`__. Stay on
   the "BIE" page.

2. On the *BIE* page click on the icon |image3| in the *Owner* column.
   Otherwise, click on the ellipsis located in the last column of the
   BIE entry and select "Transfer Ownership" option in context menu.

3. A new Modal Dialog is returned where all the users to whom the BIE
   can be transferred are displayed. Only users with the same role are
   available.

4. Select the desired user to transfer the ownership of the BIE by
   clicking anywhere in the row. You may also use the *Login ID*, *Name*
   and *Organization* search fields to search for the desired user (see
   also `How to use the Search field in
   general <#how-to-use-the-search-field-in-general>`__).

5. Click "Transfer".

You may also click "Cancel" to return to "BIE" page.

Copy a BIE
~~~~~~~~~~

The user might want to use this function, for example, when a BIE has already been in the Production state (see `BIE Review Process <#bie-review-process>`__) or when the user would like to create a new BIE in another Business Context and does not want to start from scratch.
A BIE can be copied when it is in QA or Production state.
A BIE in WIP can be copied by its owner only.
To copy a BIE:

1. On the top menu, select "BIE".

2. Then, select "Copy BIE".

3. Search for the desired Business Contexts to associate to the new BIE
   on the next page. To do so, you can use:

   1. *Name* search field (see also `How to use the Search field in
      general <#how-to-use-the-search-field-in-general>`__)

   2. *Updater*, *Updated start date* and *Updated end date* filters
      (see `How to use Search Filters <#how-to-use-search-filters>`__).

..

   A user can view the details of a Business context before selecting
   it. To do so, he can click the Name of the Business Context and its
   detail will be displayed in a new tab.

   If the desired Business Context does not exist, the user should first
   exit this page by going to another page, e.g., `Create a Business
   Context <#create-a-business-context>`__, and restart the whole BIE
   copying process. The user can also open another browser tab and
   create a Business Context, use the browser refresh button on the
   previous browser tab and the new Business Context will show up for
   selection.

4. Select one or multiple Business Contexts to associate with the BIE by
   clicking the corresponding checkboxes.

5. Click "Next".

6. `Search for a BIE <#search-and-retrieve-a-bie>`__ you want to copy.
   Select the BIE by clicking its corresponding checkbox. In this page,
   all BIEs that are allowed to be copied by the current user are
   listed. These BIEs can be in any release. The resulted BIE will
   belong to the same release as the selected BIE.

7. Click the "Copy" button at the bottom of the page. The *BIE* page is
   returned where you can see the new BIE created and which is set in
   the WIP state and in the release, which is the same with the source
   BIE. If the user wants to Edit/Update the BIE he can continue editing
   the BIE (`see also Restrict a BIE <#restrict-a-bie>`__). Note that
   until this BIE is successfully copied, you may notice that it is in
   the Initializing state. This state is used as an indicator that the
   BIE is being copied rather than as an actual state. The user has to
   refresh the page or just click the "Search" button to see whether the
   copying is finished, i.e., the Initializing state changes to WIP.

Note that when a developer copies a BIE owned by an end user, any descendant BIEs in the added by the end user to the Extension BIEs are ignored (i.e., BIEs based on User Extension Group CCs are ignored).
This is because a developer cannot extend a BIE nor use the associations of the extensions created by end users (see also `Extend a BIE <#extend-a-bie>`__).

BIE inheritance
~~~~~~~~~~~~~~~

BIE inheritance allows a user to create a new top-level BIE that is based on another top-level BIE.
The source BIE is the base BIE, and the new one is the inherited BIE.
Unlike copying, inheritance keeps an explicit base relationship between the two BIEs.

In the current UI, an inherited BIE can be recognized in the following places:

- In the "View/Edit BIE" list, the DEN column shows a *Based on:* line under the inherited BIE.
- In the BIE tree, inherited nodes show the inheritance icon with the tooltip "Inherited".
- On the edit page, inherited nodes provide an additional tab labeled *Inherits from ...* that shows the base BIE details in read-only form.

When an inherited BIE is created, it is initialized from the base BIE.
The inherited BIE keeps the same release, business contexts, version, and status as the base BIE at the time of creation.
The new inherited BIE is then owned by the user who created it and starts in the WIP state after the creation finishes.

The inherited BIE remains editable.
At the same time, the base relationship is preserved so connectCenter can keep the inherited structure aligned with the base BIE.
In practice, this means the inherited BIE can keep its own changes, while base-driven constraints and inherited content can still affect it when the base BIE is updated.

Create an inherited BIE
~~~~~~~~~~~~~~~~~~~~~~~

To create an inherited BIE:

1. On the top menu, click "BIE".

2. Choose "View/Edit BIE".

3. Search for the BIE you want to use as the base BIE.
   See `Search and retrieve a BIE <#search-and-retrieve-a-bie>`__.

4. Click the ellipsis on the right side of the desired BIE row.

5. Click "Create Inherited BIE".
   This menu item is enabled only when the current user is allowed to inherit from that BIE.
   Developers can create an inherited BIE from a developer-owned BIE.
   End users can create an inherited BIE from an end-user-owned BIE.

6. Wait for the confirmation message.
   connectCenter shows a message that the request has been received and that the user should check back shortly.

7. Click "Search" or refresh the page after a short wait.

8. Find the newly created inherited BIE in the list.
   The new BIE shows a *Based on:* line in the DEN column.
   During creation, it may briefly appear in the Initiating state before it becomes WIP.

9. Click the DEN of the inherited BIE to open its edit page.

10. Review the inherited content.
    The BIE tree marks inherited nodes with the inheritance icon.
    For an inherited node, the *Inherits from ...* tab shows the corresponding base BIE details in read-only form.

BIE reuse
~~~~~~~~~

BIE reuse allows for a top-level BIE to be reused (called reused BIE) under another top-level BIE (called reusing BIE).
The reused BIE can be in any state and owned by any user.
However, in order for a reusing BIE to be moved in the Production state, all its reused BIE nodes should be in Production state.
It should also be noted that normally, a user who is not the owner of a BIE cannot view its details or express it while it is in WIP state. connectCenter skips this constraint in case of a reused BIE.
That is, a user can view the detail and express a reusing BIE that reuses some BIEs in WIP state and owned by a different user.

A target node is a BIE node that the user would like to assign a reuse.
The target-node must be an aggregate node (i.e., the node with bold-blue font).
An aggregate node is an Association Business Information Entity (ASBIE).
An ASBIE represents a complex business characteristic and it is derived from an Association Core Component (ASCC) in a specific business context.
Similar to an ASCC, it consists of the Associating ABIE based on an ASCC, the ASBIE Property (ASBIEP) based on an ASCCP and so it can be reusable (see also `Make a BIE reusable <#make-a-bie-reusable>`__).

*Developer can reuse a BIE owned by another developer only, while end user BIE can reuse a BIE owned either by a developer or an end user.
Developer BIEs won't show up for reuse selection when BIE being edited is an end user BIE.*

To reuse a BIE under another top-level BIE:

1. Expand the BIE tree

2. Click on the ellipsis located next to a target aggregate node – the
   target node.

3. Click on the "Reuse BIE" option.

4. In the pop-up dialog, all BIEs that can be used on the target node
   are displayed for selection (only production top-level BIEs that
   reference the same ASCCP as the target node are allowed). To narrow
   down the results, you can filter the BIEs based on the *Owner*,
   *Updater*, the *Updated start date* and *Updated end date* (see also
   `How to use Search Filters <#how-to-use-search-filters>`__) or the
   *Business Context* search field (see also `How to use the Search
   field in general <#how-to-use-the-search-field-in-general>`__). Note
   that clicking on the BIE will open up its detail in another browser
   tab where you can inspect every detail of the BIE.

5. Select a BIE by clicking its corresponding checkbox.

6. Click the "Select" button.

7. On the confirmation dialog returned, confirm that you are okay that
   the details of descendant nodes of the target BIE node will be lost.

At this point, the target node is replaced by the selected BIE - a recycling icon indicating it is a BIE reuse node.
You can view the details of the corresponding top-level reused BIE in another tab by clicking on this icon.

The details of the reused BIE and its descendants cannot be changed.
However, on the detail pane of a reused BIE node, you can change the details of the association to the reused top-level BIE, namely the *Min* and *Max* *Cardinality*, *Context Definition*, *Used* and *Nillable* fields.

Make a BIE reusable
~~~~~~~~~~~~~~~~~~~~

This function offers the ability to make an aggregate BIE node (the node with the bold-blue font) within a (source) top-level BIE reusable.
That is, to create a top-level BIE from a descendant node of a source top-level BIE.
The source top-level BIE can be in any state if it is owned by the current user.
If it is owned by another user, it must be in the QA or Production state.
The interested node may contain a `reuse BIE <#bie-reuse>`__ node; the resulting top-level BIE will also reuse those BIEs.

To make a BIE reusable:

1. `Search and retrieve a BIE <#search-and-retrieve-a-bie>`__.

2. Expand the tree of the top-level BIE.

3. Click on the ellipsis located next to an aggregate BIE node (a node
   with bold-blue font).

4. Click on the "Make BIE Reusable" option.

5. Click the "Make" button in the returned confirmation dialog.

6. A new message box is prompted asking if the user wants to proceed to "View/Edit BIE" page to see the newly created BIE.

7. Click the "Go to View/Edit BIE page" option.

At this point the BIE list page is returned where the new BIE is listed.
Until this BIE is successfully created, it is in the Initiating state; a state that indicates that the BIE is being created.
Once the creation process is finished, the BIE goes to the WIP state (refresh the page or click the "Search" button to see if the creation is done).
This BIE inherits the Business Contexts and release association of its source top-level BIE.
At this stage, the user can still make changes to the BIE.

BIE Expression generation
~~~~~~~~~~~~~~~~~~~~~~~~~

The user uses BIE expression generation to represent a BIE in a chosen syntax.
The current page supports XML Schema, JSON Schema, OpenAPI 3 templates, Open Document Spreadsheet, and Avro Schema.

The user can generate an expression of a BIE when at least one of the following conditions is met:

-  The user owns the BIE, or

-  The BIE belongs to another user, but it is in the QA or Production
   state.

To generate a BIE expression:

1. On the top menu of the page, click "BIE".

2. Choose "Express BIE" from the menu items.

3. If necessary, choose the desired Library using the selector at the top of the page.
   If the user has not chosen a library preference yet, connectCenter preselects the default library and shows it first in the selector.

4. Choose the desired release from the "Branch" drop-down list near the
   top-left of the page.

5. Find the BIE, from which you want to generate an expression. See Step
   3 in `Search and retrieve a BIE <#search-and-retrieve-a-bie>`__ for
   help with finding a BIE.

6. Select the BIE by using the checkbox in the first column. Multiple
   BIEs can be selected. For each BIE, you may choose the Business
   Context you want to include in the name of the downloaded file. Click
   on the down arrow icon at the "Business Context" column and select
   the desired Business Context (the default is set to the first
   business context assigned to the BIE. Checkbox "Include a business
   context in the filename" below at the page allows to include or
   exclude the business context from the file name). The filename of the
   downloaded file will be in the format [BIE Property Term]-[Business
   Context]. If no Business Context is selected the filename of the
   downloaded file will be only the property term of the BIE. The checkbox
   "Include a version in the filename" allows to include or exclude the
   version of the BIE from the file name. The filename of the downloaded
   file will be in the format [BIE Property Term]-[Version]. When both
   the "Include a business context in the filename" and "Include a
   version in the filename" are checked the filename of the downloaded
   file will be in the format [BIE Property Term]-[Business
   Context]-[Version]

7. Choose what annotations to generate along with the BIE structure definition.
   The available options depend on the selected expression type.

   -  *BIE Definition*: The generated expression includes the Context Definition
      specified on each BIE node.

   -  *BIE CCTS Meta Data*: Available only for XML Schema.
      The generated schema includes BIE information such as Dictionary Entry Name,
      Object Class Term Name, and Business Term according to the Core Component Specification.
      If *Include CCTS_Definition Tag* is also selected, the BIE Definition content is duplicated
      into the CCTS_Definition element as part of the CCTS metadata.

   -  *Include CCTS_Definition Tag*: Available only for XML Schema, and only when
      *BIE CCTS Meta Data* is selected.

   -  *BIE GUID*: Available only for XML Schema.
      The generated schema includes the BIE GUID.

   -  *Business Context*: Available only for XML Schema.
      The generated schema includes the details of the BIE's Business Context.

   -  *BIE OAGi/connectCenter Meta Data*: Available only for XML Schema.
      The generated schema includes connectCenter-specific information such as version,
      state, status, and remark.
      If *Include WHO Columns* is also selected, ownership and timestamp information is included.

   -  *Include WHO Columns*: Available only for XML Schema, and only when
      *BIE OAGi/connectCenter Meta Data* is selected.

   -  *Based CC Meta Data*: Available for XML Schema and JSON Schema.
      The generated expression includes information from the Core Component
      on which the BIE is based.

8. Select the expression type and related options.

   |bie_expression_format|

   -  *XML Schema*: Generates the BIE as XML Schema.
      This option also supports *Separate file references for reused schemas*.
      When that option is selected, reused schemas are emitted as separate referenced files.

   -  *JSON Schema*: Generates the BIE as JSON Schema.
      The *Version* field allows the user to choose either `2020-12` or `Draft-04`.
      The option *Separate file references for reused schemas* is available only for `2020-12`.
      When selected, reused schemas are emitted as separate referenced files.
      JSON Schema also supports *Make as an array*, *Include Meta Header*, and
      *Include Pagination Response*.
      If *Include Meta Header* or *Include Pagination Response* is selected,
      the schema package option is automatically limited to individual files.

   -  *OpenAPI 3 (Template)*: Generates the BIE as an OpenAPI template.
      The *Version* field allows the user to choose either `3.1` or `3.0`.
      The *Format* field allows the user to choose either `YAML` or `JSON`.
      The user can enable a GET operation template, a POST operation template, or both.
      Each selected operation template provides additional options such as
      *Make as an array*, *Suppress a root property*, and *Include Meta Header*.
      The GET operation template also supports *Include Pagination Response*.

   -  *Open Document Spreadsheet (supports CSV)*: Generates the BIE as a spreadsheet-style document.
      The *Format* field allows the user to choose `ODS`, `FODS`, or `XLSX`.

   -  *Avro Schema*: Generates the BIE as Avro Schema.

9. Select the *Schema File Option*.

   -  *Put all schemas in the same file*: Generates one root expression document.
      If *Separate file references for reused schemas* is selected and reused schemas exist,
      the downloaded result contains the root file and the referenced files together.

   -  *Put each schema in an individual file*: Generates one file for each selected schema.
      In this case, a zip file containing the generated files is downloaded.

   The options *Open Document Spreadsheet* and *Avro Schema* use individual files only.

10. Click "Generate".
    The generated file is automatically downloaded, typically to the "Downloads" folder
    in the user profile folder of your computer.

Find reused BIE
~~~~~~~~~~~~~~~

connectCenter allows for finding a BIE or BIEs are reused.
This could be very handy particularly during the `uplifting <#uplift-a-bie>`__.
There are two ways to retrieve this information – find reuse for an individual BIE or get a full report for the entire repository.

To find reuses for an individual BIE:

1. On the top menu of the page, click "BIE".

2. Choose option "View/Edit BIE" from the drop-down list.

3. Locate the BIE you want.

4. Click on the three-dot ellipsis on the right of the BIE, then click
   option "Find Usage".

5. In the returned dialog, the reusing BIEs are listed. Click on the
   property term of a reusing BIE in order to view its details in a new
   tab. The reusing BIE is opened, and the BIE tree is expanded to
   reused BIE node.

To get the full reuse report:

1. On the top menu of the page, click "BIE".

2. Choose "Reuse Report" from the drop-down list.

Each entry of the returned page contains a reusing BIE and its corresponding reused BIE.
In particular, the top-level BIE (the reusing BIE) under which a BIE is reused is shown in the left side of the entry and the reused BIE is shown in the right side of the entry.
Click on the property term of a reusing BIE to view its details in a new tab.
In this new tab, the reusing BIE is opened, and the BIE tree is expanded to the reused BIE node.
You can also click on the property term of a reused BIE to view its details in a new tab.

Uplift a BIE
~~~~~~~~~~~~

BIE uplift allows for transferring BIEs that are based on a previous standard CC release to a newer release.
In the BIE uplifting process, the BIE based on the older CC release is called the *source BIE* while the resulting BIE is called the *uplifted BIE*.
The CC release, to which the source BIE is uplifted to is called the *target release*.
Correspondingly, the CC release associated with the source BIE is called the *source release*.
There is no change applied to the source BIE in the uplifting process.

The BIE uplifting process aims to assist the user in transferring information of the source BIE to the uplifted BIE taking into account the changes in CCs between the target and the source releases.
Business contexts associated with the source BIE are also transferred to the target BIE automatically.

BIEs in QA or Production state can be uplifted by anyone while BIEs in WIP state can be uplifted by their owners only.
Whoever uplifts the BIE becomes the owner of the uplifted BIE.
Therefore, multiple users can uplift the same BIE to different newer releases.

connectCenter requires limited user involvement for uplifting a BIE.
When a BIE is being uplifted, the information of the enabled nodes of the source BIE is transferred to the uplifted BIE.
That is, the uplifted BIE contains the information of the enabled nodes along with their details.
To this purpose, connectCenter creates the uplifted BIE based on the top-level ASCCP of the source BIE.
Afterward, connectCenter matches the nodes of the source BIE to the nodes of the uplifted BIE and transfer their details.

The current version of connectCenter performs that matching based on the GUID of the CCs that the nodes of the BIE derived from.
If a CC has been changed during a release, connectCenter presents the corresponding BIE node as unmatched and requires user to manually perform this matching.
Matching is allowed only between nodes of the same type, i.e., ASBIE/ASBIEP/ABIE node with ASBIE/ASBIEP/ABIE node, BBIE node with BBIE node, and BBIE_SC node with BBIE_SC node.
The user can also skip the manual matching procedure and leave some nodes unmatched.
In that case, the uplifted BIE does not include any information about the unmatched nodes.

In case that the source BIE contains a node that reuses a BIE, matching is performed at the same way as described above.
However, the user has to select the BIE to be reused in the uplifted BIE.
This BIE should belong to the same release as the uplifted BIE.
Therefore, it is recommended that the BIE which is reused under the source BIE should be uplifted before uplifting the source BIE.
The user can also create a completely new BIE in the newer release and reuse it under the uplifted BIE (see also `BIE reuse <#bie-reuse>`__).
When a reuse BIE is selected for a reuse node, the uplifted BIE keeps a reference to that reused BIE.
If a reuse node is left without a reuse BIE selected, its fields are copied directly into the uplifted BIE and no reference to a reused BIE is kept; connectCenter warns about this before the uplift proceeds (see Step 6 below).

To uplift a BIE:

1. Select "Uplift BIE" in the "BIE" menu at the top of the page.

2. On the returned "Uplift BIE – Select BIE" page:

   1. Select a source release in the *Source branch* dropdown. The
      latest release should not be selected. The BIEs belonging to the
      latest release cannot be uplifted.

   2. Choose the target release in the *Target branch* dropdown. The
      uplifted BIE will be associated with this release.

   3. Choose a source BIE from the listing table below. The list
      contains only BIEs in the source release selected in the first
      step. Use the pagination at the bottom or use the *Property Term*,
      *Business Context*, or other filters on the page to find the
      desired source BIE (see `How to use the Search field in
      general <#how-to-use-the-search-field-in-general>`__ and `How to
      use Search Filters <#how-to-use-search-filters>`__ for help with
      these filters). Optionally, click on the name of the BIE in the
      *Property Term* column to see details of the BIE.

3. Click the "Next" button.

4. The "Uplift BIE – Verification" page is returned displaying the
   source BIE tree on the left-side and the target BIE tree on the
   right-side. The target BIE represents how the source BIE will become
   the uplifted BIE. The source BIE tree shows only used nodes, while
   the target BIE tree shows all possible, use and unused, nodes. connectCenter
   automatically maps used nodes in the source BIE tree with nodes in
   the target BIE that share exactly the same underlying CCs based on CC
   GUIDs. Automatically mapped nodes in the source BIE are displayed
   without a checkbox. The user can click on the source BIE tree node to
   see how it is mapped in the target BIE tree. The following tasks are
   optional.

   1. Manually map\* the unmapped node in the source BIE tree to a node
      in the target BIE. There can be unmapped nodes in source BIE due
      to some changes in the CCs or extensions made in the source BIE.
      This step is optional, i.e., the user can choose to not carry
      those nodes to the new version. To map a node, click a node with a
      checkbox in the source BIE tree, then click the checkbox of a
      desired node in the target BIE*. The system then checks both
      checkboxes to indicate that the map has been performed. The user
      can review the map again by clicking on the source BIE node. The
      system will highlight the mapped node in the target BIE tree.

   2. Select a BIE for a BIE reuse node. If the source BIE has a BIE
      reuse node, which is marked with a recycling icon, and it has
      already been mapped, the system will display an exclamation icon
      next to the target BIE node. The user can select a BIE in the
      target release to make the target BIE node a BIE reuse node as
      well. To do so, make sure that a BIE reuse node is selected on the
      source BIE tree, the system will bring the mapped target BIE node
      into the focus. Click on the exclamation icon. The system will
      bring up a dialog that list compatible top-level BIEs to choose
      from. Search and select the desired BIE. See `BIE
      reuse <#bie-reuse>`__ for more information about BIE reuse. If the
      desired BIE is not present, the user may have to uplift the BIE
      first or create a new one. This step is optional. If no BIE is
      selected for the target node, the uplifted BIE node will contain
      only the association information if it was mapped in the target
      BIE.

5. Click the "Next" button.

   1. In this page (i.e., BIE Uplifting Report), the user can view or
      download the uplifting report. The report presents the details
      about mapped and unmapped node as well as reuse information. In
      addition, the report indicates some BIE nodes may use a developer
      code list or end-user code list that needs to be manually input
      again when the uplifted BIE is open**. The user can use the check
      box on the top to hide or view the issues of the BIE uplifting
      process that need more attention and manual work. Click the
      "Download" button on the report page to download a comma-separated
      file of the report. Click "Cancel" to go back to the previous
      page. The report table consists of:

      1. The *Type* column that presents the type of the node of the
         source BIE, e.g., BCCP, ASCCP.

      2. The *Source path* column presents the path of the node of the
         source BIE and the *Target path* column presenting the path of
         the matched node in the target BIE. Blank target path means the
         source node was not mapped. The name of these columns is in the
         form of *Source/Target "Release of the BIE" path*.

      3. The *Context Definition* column presents the *Context
         Definition* field of the BIE node.

      4. The *Matched* column conveys how the nodes have been mapped.
         The values can be "System" – mapped by connectCenter, or "Manual" –
         mapped by the user. Empty value in this column indicates that
         no map was performed for the *Source path*.

      5. The *Reused* column has three possible values including blank,
         "Selected", "Not Selected". Blank means the source path is not
         a BIE reuse node. "Selected" or "Not Selected" means the source
         path is a BIE reuse node. However, "Not Selected" means that
         the user did not assign a BIE to the mapped node in the target
         BIE indicated in the target path.

      6. The *Issue* column presents details about a specific issue.
         User should take care of the issue manually when editing the
         uplifted BIE.

6. Click "Uplift". If every BIE reuse node in the source BIE was
   assigned a reuse BIE in the target release (see Step 4.2 above), or
   the source BIE has no reuse nodes, the uplift proceeds directly.
   However, if one or more used reuse nodes were left without a reuse
   BIE selected for the target release, connectCenter displays a
   confirmation dialog titled "Proceed without selecting reuse BIEs?"
   before continuing. The dialog explains that the listed reuse nodes
   have no reuse BIE selected for the target release and that, if you
   continue, their fields will be copied into the uplifted BIE and the
   reference to the reused BIE will NOT be kept. It also lists the path
   of each unselected reuse node. To preserve the reference to the
   reused BIE instead, click "Cancel" to return to the "Uplift BIE –
   Verification" page, then click the reuse (exclamation) icon on each
   listed node to select a target BIE (see Step 4.2). To proceed and
   inline-copy the fields of the unselected reuse nodes, click
   "Continue".

7. The new uplifted BIE is created and opened. At this stage the user
   can make further changes to the BIE as described in `Restrict a
   BIE <#restrict-a-bie>`__ or to resolve the reported issues manually.

**Important**

\*The user’s manual map may have a cardinality and/or domain value restriction conflict.
For example, a cardinal conflict is present when a source node has 0..n but the target node has 1..n by default based on the corresponding CC.
If this happens, connectCenter copies over the invalid cardinality.
The domain value conflict can occur if the user maps BBIE (green or italic grey nodes) or BBIE_SC node (red nodes), to one with incompatible primitives.
For example, when the user maps Description to Creation Date Time.
In such a case, connectCenter may not properly copy the *Value Domain Type* and *Value Domain* *Restriction* details from the source to the target node.
The user should fix these conflicts in the uplifted BIE.
In any case, it is recommended that the user checks the domain value restriction of all manually mapped nodes in the uplifted BIE.
It is also prudent to express both source BIE and uplifted BIE and perform a diff to ensure that only expected differences are present as connectCenter 2.0 is an early version of the BIE uplifting functionality.

\**End-user code list assigned to a source BIE node can be carried into the uplifted BIE only if the end-user code list with the same name, list ID, and agency ID exists (or has been uplifted) in the target release and it is allowed by the target BIE node.
If this is not the case, an issue is reported in the uplifting report; and the default primitive will be assigned to that BIE node instead.
The user can use the report to make necessary adjustments to the uplifted BIE.
Therefore, it is recommended that the user download the report before uplifting the BIE.
Developer code lists used in source BIE nodes will be matched based on the internal ID and carried forward if they are allowed in the target node.
If it is not allowed in the target node, an issue is reported in the uplifting report and the default primitive will be used in the uplifted BIE.
For example, if the user/system maps a source node with System Environment Code to a target node with Action code, the issue will be reported because the two codes are not compatible.
The same logic applies to the agency ID list.
