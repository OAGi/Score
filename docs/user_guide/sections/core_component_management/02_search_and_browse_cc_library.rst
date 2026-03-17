Search and Browse CC Library
----------------------------

This section describes how to search and view core components.
Core Components can be partitioned or grouped in many ways using filters described in this section, in addition to the different CC types according to CCS described in the `Core Component in Brief <#core-component-in-brief>`__ section.

The very first version of connectCenter was populated with connectSpec 10.4 standard.
For details about how connectSpec 10.4 was imported into connectCenter, consult `Overview of the connectSpec Repository <https://drive.google.com/open?id=0B--IONsLNMMRTmhzdklOOFRmN1U&tabid=134&portalid=0&mid=494>`__, a Component of the connectCenter.

Drop-down List
~~~~~~~~~~~~~~

Most drop-down list has a built-in filter.
This is particularly useful when a list is big.
The filter is displayed after a drop-down list is clicked.
The user can narrow down the values in the list by typing in a few characters.
For example, in the owner drop-down, typing in "oa" will narrow down the list to usernames containing "oa".

.. _how-to-search-and-filter-for-a-core-component:

How to Search and Filter for a Core Component
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Searching for a Core Component is performed in the "Core Component" page.
To visit this page:

1. On the top menu of the page, click "Core Component".

2. Choose "View/Edit Core Component" from the drop-down list.

The "Core Component" page is returned.
This list page is separate from the "Data Type" page.
The Core Component page lists ACC, ASCCP, and BCCP entries.
Data Types are managed on the separate "Data Type" page instead.
When the application-level Browse Standard mode is enabled for an end user, the top menu shows "Browse Standard" instead of "Core Component", and that page opens directly in standard-browsing mode.

The page title bar also includes a *Library* selector.
If the user has not chosen a library preference yet, connectCenter preselects the default library and shows it first in the selector.

The page includes a search bar with a Branch selector on the left.
On the regular Core Component page, the search field searches by *DEN* and the chevron-down button opens Advanced Search filters.
On the Browse Standard page, the search field searches by *Name* instead, and Advanced Search is disabled.
Unless otherwise stated, the filters below describe the regular Core Component page.

-  *Branch* allows for filtering the Core Components based on their
   release. To do this:

   -  | Choose the release you would like to view from the "Branch"
        drop-down list on the top-left of the search bar. Specifically,
        "Working" means the release being worked on, based on the latest
        release. In other words, if the current latest release is
        "10.6", "Working" means 10.6 plus changes; and if "10.6" or
        "10.5" is selected then only core components **and their
        details** as they were at the release will be displayed. All
        releases are generally incremental. See also
        `Branch <#branch>`__.
      | **Note:** Any change in the *Branch* filter of a CC-related page
        (i.e., the "Code List" and "Core Component" page) is stored and
        used across all CC-related pages. The "Code List" page is
        considered CC-related when it is visited via "View/Edit Code
        List" under the "Core Component" menu. However, it is considered
        BIE-related when it is visited via "View/Edit Code List" under
        the "BIE" menu.

-  *Type* allows for filtering the results based on the `CC
   type <#core-component-in-brief>`__. To use this filter:

   -  | On the regular Core Component page in Dev View, click on it and
        check or uncheck the "ACC", "ASCCP", or "BCCP" checkboxes to
        filter the list.
        If no type is selected, the filter is not used.
        Data Types are not part of this page.
        They are managed separately on the "Data Type" page.
        In Browser View, the available type filters are reduced to
        browseable property components.
        Developer and admin users can filter by "ASCCP" and "BCCP".
        End users can browse only "ASCCP".
        In Browse Standard mode, the page is fixed to ASCCPs only and
        excludes Extension and DataArea ASCCPs.

-  *State* allows for filtering the results based on Core Components’
   state. To use this filter:

   -  | Click on it and check the checkboxes to list core components in
        those states. If no State is selected, the filter is not used. For
        definitions of states, see the `CC States <#cc-states>`__ section.
        See also the `CC unit of control <#cc-unit-of-control>`__ section.

-  | *Deprecated* allows for filtering in or out deprecated CCs. Select
     "True" to show only deprecated CCs or "False" to show only CCs that
     are not deprecated. Both are included when neither option is
     selected.

-  *Component Type* enables filtering ACC based on the `Component
   Type <#component-types>`__ that supports connectSpec architecture. To use
   this filter:

   -  | Check the desired checkboxes next to the `Component
        Type <#component-types>`__. If no selection is performed, the
        filter is not used. Note that Component Type only applies to ACC.
        If no (CC) Type filtering is selected, all types of CCs are still
        listed. In other words, if the Component Type filter is used, it
        might be better to set the Type filter to only ACC.

-  *Tag* enables filtering components based on the tag. Note that this only
   applies to the CC associated with the tag(s). See `Tagging CCs <#tagging-ccs>`__
   for more details. To use this filter:

   -  | Check the checkboxes to list core components that associated with
        the tag(s).

-  *Namespace* enables filtering components based on the `namespace <#namespace-management>`__
   assigned to components. To use this filter:

   -  | Check the checkboxes to list core components that related to the namespace(s).

-  Free form text filtering based on CCs’ *DEN* (dictionary entry name),
   *Definition*, *Module* or a combination of them. The matching is case
   insensitive. To use these filters:

   -  | Enter a search string in the *DEN* (the name of the core
        component), *Definition* or *Module* field and click "Search"
        button. Note that search strings entered in three fields are
        treated as having an AND logical relationship.

   -  | It is important to note that the DEN is stored in space-separated
        format (while the XML schema or other expressions of the standard
        may have the name formatted in camel case). For example, type in
        "Employee Count" instead of "EmployeeCount". In addition, "ID" is
        stored as "Identifier" in DEN.

   -  | The *Definition* field allows you to find a core component whose
        definition matches the input string. The content in the Definition
        is generally written in normal language grammar. Keep in mind
        though that if you try to match a data element name in the
        Definition, it may still be in the camel case format. The tool
        does not parse the Definition when it is imported. Unfortunately,
        when standard developers refer to data elements in the definition
        there is no consistent convention. For example, one definition may
        be "Address of the Customer Party" while another may be "Business
        Unit of the CustomerParty". Notice that the format of the
        "Customer Party" data element is inconsistent in the two
        definitions.

   -  | A good technique is to search with longer input string first, if
        nothing found try changing to different synonyms, and also try
        shortening the input string where more results, yet less accurate,
        will be returned.

   -  | Use double quotes around the search terms in *DEN* and
        *Definition*, to match the exact substring as in the double
        quotes. For example, if search input in the *DEN* field is ""Name
        Identification"", part of the DEN has to match the whole search
        input. In other words, a component with DEN "Named Identification.
        Details" won’t be returned. However, if the search input in the
        *DEN* field is "Name Identification" without double quotes, DENs
        that partially match both tokens will be returned. In other words,
        the CC with DEN "Named Identification. Details" will be returned.

   -  | The *Module* field allows for filtering based on the physical file
        path the core component resides based on the Module Assignment in
        connectCenter. The path is stored with a backslash and without the file
        extension, such as ".xsd". In the case of connectSpec 10.4 standard, for
        example, all shared components reside in subfolders of the
        "Model\\Platform\\2_4\\Common" folder, nouns are serialized to the
        respective noun module (e.g. "Model\\Nouns\\PurchaseOrder"). To
        search only shared components, the user may enter "common" in the
        *Module* field. Or, to search only about nouns, the user may enter
        "nouns" in the *Module* field.

After searching for a CC, clicking anywhere in the row will display its definition.
Click on its DEN to open its detail page.

Browser View Mode
~~~~~~~~~~~~~~~~~

The Top-Level BIE is generated from an ASCCP (see `BIE in brief <#bie-in-brief>`__), and you might want to see how it will appear before creating the BIE.
The *Browser View* mode is used to meet this need.
When in *Dev View* mode, the regular Core Component page supports creation, bulk actions, and the full Core Component filter set.
However, in Browser View mode, the page is focused on browsing and opens ASCCP and BCCP entries in browser-style detail pages.
To switch between Dev View and Browser View, use the toggle at the top of the regular Core Component page.
In Browser View on the regular Core Component page, developer and admin users can browse ASCCP and BCCP entries, while end users can browse ASCCP entries only.
When Browse Standard mode is enabled in application settings for end users, end users do not use the regular Core Component menu.
Instead, they see the separate "Browse Standard" menu, which forces browser-style behavior, uses Name search, hides Advanced Search, and lists ASCCPs only.

.. _how-to-read-a-core-component:

How to Read a Core Component
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

After searching for a Core Component (see `How to Search and Filter for a Core Component <#how-to-search-and-filter-for-a-core-component>`__), you can view its details by clicking its DEN (DEN column).
Clicking anywhere else in the row and the row will be expanded to show the definition of the CC.
On the Browse Standard page, click the *Name* link instead of the DEN link.

Doing so, a page is returned having the name of the CC you have chosen.
This page is divided into the two panes; the left one depicting the structure of the CC in the form of a tree and the right one showing the fields of a selected CC in the tree.

The CC tree is a data structure in which different types of components are organized through the tree hierarchy.
The table and figure below provide a quick reference to what these types of CCs are.
The following five (5) formats are used to distinguish different types of components in the tree:

1. **Bold Red font** is for an ACC. Another way to recognize an ACC in
   the tree is it is displayed with its DEN, which ends with ".
   Details". When there is an ACC node directly under another ACC node,
   the node on the top is based on (i.e., extends) the bottom ACC and
   inherits all properties from the bottom (based) ACC.

2. **Bold Blue font** is for ASCCs along with ASCCPs they use. This node
   displays the property term of the ASCCP.

3. *Italic Grey font* is for BCCs along with BCCPs and BDTs they use.
   These BCCs have been designated as attributes (as in XML Schema, it
   should be noted that this attribute designation is needed so that
   connectCenter can maintain backward compatibility with some standards that
   have their normative form in XML Schema). The node displays the
   property term of the BCCP.

4. Regular green font is for BCCs along with BCCPs and BDTs they use.
   This node displays the property term of the BCCP.

5. Regular red font is for Supplementary Components (SC).

Search within a Core Component Tree
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

On an opened "Core Component" detail page, the user can search for any descendant nodes, which can be various types of CC entities.
To do this:

1. Click in the tree, on the node to be used as search scope. Only nodes
   under the selected node will be used as the search space.

2. Enter the search term above the tree pane.

3. Click the search (magnifying glass) button. Some CCs are very big
   containing tens of thousands of nodes and search can take a few
   seconds.

4. Use the "Exclude SCs" checkbox in order to skip (i.e., exclude) the
   SC nodes from the searching function. When it is checked (i.e.,
   enabled) the searching function does not consider the SC nodes.

**Tip**: There is a search option next to the search button.
For faster search, supplementary components of the CCs, which repeat a lot, are excluded by default.
The user can switch off this exclusion, but the search will be slower.

Find the usages (a.k.a. Where Used) of a Core Component
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This function allows the user to research about how a CC has been used or referenced in other CCs.
The function can be invoked on an ACC, ASCCP, or a BCCP.
Invoked on an ACC, the application will display ACCs that are based on (extensions/subtype of) the ACC, and ASCCPs created from the ACC.
Invoked on the ASCCP or BCCP, the application will show ACCs that have associations to it.
To use this function:

1. Open a detail page of a core component.

2. Expand the tree.

3. Click the ellipsis icon located next to any ACC (red), ASCCP (blue),
   or BCCP (green or grey) node.

4. In the returned context menu, click the option "Where Used"

5. The returned dialog lists the CCs wherein the specific node is used
   as described above.

6. Click on a listed CC to open its detail page in a new tab.

Quick reference to different types of CCs
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

+-------------+--------------------+-------------------------------+-------------------------------------+
| **CC Type** | **Full Name**      | **Mapping to XML Schema**     | **Examples**                        |
+-------------+--------------------+-------------------------------+-------------------------------------+
| ACC         | Aggregate Core     | Type definition with          | Invoice Type,                       |
|             | Component          | complex content               | Address Type                        |
+-------------+--------------------+-------------------------------+-------------------------------------+
| BDT         | Business Data Type | A simple content definition   | Amount Type,                        |
|             |                    | with or without attributes    | Quantity Type                       |
+-------------+--------------------+-------------------------------+-------------------------------------+
| SC          | Supplementary      | Attribute of a BDT            | Currency Code of the Amount Type,   |
|             | Component          |                               | Unit Code of the Quantity Type      |
+-------------+--------------------+-------------------------------+-------------------------------------+
| BCCP        | Basic Core         | Element definition            | Tax Amount or Total Amount          |
|             | Component          | with simple content           | using the Amount Type BDT           |
|             | Property           | (using a BDT)                 |                                     |
+-------------+--------------------+-------------------------------+-------------------------------------+
| BCC         | Basic Core         | Element reference             | The relationship from Invoice Type  |
|             | Component          | (using a BCCP)                | to Tax Amount BCCP,                 |
|             |                    |                               | the relationship from Invoice Type  |
|             |                    |                               | to Total Amount BCCP                |
+-------------+--------------------+-------------------------------+-------------------------------------+
| ASCCP       | Association Core   | Element definition            | Billing Address or Shipping Address |
|             | Component          | with complex content          | using Address Type                  |
|             | Property           | reusing an ACC                |                                     |
+-------------+--------------------+-------------------------------+-------------------------------------+
| ASCC        | Association Core   | Element reference             | The relationship from Invoice Type  |
|             | Component          | using an ASCCP                | to Billing Address ASCCP,           |
|             |                    |                               | the relationship from Invoice Type  |
|             |                    |                               | to Shipping Address                 |
+-------------+--------------------+-------------------------------+-------------------------------------+

In the example figure below, **Work Order.
Details** is an ACC.
The *Type Code* directly underneath it is a BCC as well as BCCP bundled into one node.
**Work Order Header** is an ASCC and ASCCP bundled into one node.
Under the **Work Order Header** of the **Work Order.
Details**, the tree shows that its ASCCP was created from the **Work Order Header.
Details** ACC.
The hierarchy of ACCs directly under the **Work Order Header.
Details** ACC shows the series of extension/inheritance that **Work Order Header.
Details** extends **Work Order Header Base.
Details**, which in turn extends another hierarchy of extensions from **Request Header Base.
Details,** **Status Header Base.
Details,** to **Header Base.
Details**.
Notice however that all the extensions did not add any ASCC or BCC to a property except for the **Status Header Base.
Details** ACC, which has the **Status** ASCCP added.
The tree goes on to show how the **Status.
Details** ACC was modelled.
Finally, it shows that the Status.
Details ACC has Code and Description BCCs (inherited from **Status ABIE.
Details**).
And Code BCCP has four SCs including Type Code, List Identifier, List Agency Identifier, and List Version Identifier.

.. image:: media/image5.png
   :alt: Chart Description automatically generated
   :width: 2.63356in
   :height: 5.65049in

Whenever a node in the tree is selected, its details are depicted on the right pane.
These fields are different for each component type and they are described below.

**An ACC has the following fields**:

+------------------+---------------------------------------------------+
| GUID             | A globally unique number of the component.        |
+------------------+---------------------------------------------------+
| Object Class     | The name of the ACC (this is typically used in    |
| Term             | expression generation).                           |
+------------------+---------------------------------------------------+
| DEN              | The full official name of the ACC.                |
+------------------+---------------------------------------------------+
| Component Type   | -  See `Component types <#component-types>`__     |
+------------------+---------------------------------------------------+
| Abstract         | An indicator the ACC is not instantiable.         |
+------------------+---------------------------------------------------+
| Deprecated       | A status to indicate that the ACC should no       |
|                  | longer be reused and that it may be replaced by   |
|                  | something else. There are some business rules     |
|                  | applied to deprecated ACC. For example, when the  |
|                  | user wants to create a new ASCCP, the application |
|                  | will give a warning when a deprecated ACC is      |
|                  | selected.                                         |
+------------------+---------------------------------------------------+
| Definition       | The unique semantic meaning of the ACC.           |
+------------------+---------------------------------------------------+

**An ASCC has the following fields**:

+------------------+---------------------------------------------------+
| GUID             | A globally unique number of the component.        |
+------------------+---------------------------------------------------+
| DEN              | The unique official name of the ASCC (DEN of an   |
|                  | ASCC is constructed from Object Class Term of the |
|                  | ACC and DEN of the ASCCP it uses).                |
+------------------+---------------------------------------------------+
| Min              | Minimum cardinality/occurrences of the            |
|                  | association in an instance.                       |
+------------------+---------------------------------------------------+
| Max              | Maximum cardinality/occurrences of the            |
|                  | association in an instance.                       |
+------------------+---------------------------------------------------+
| Deprecated       | A status applied to indicate that the ASCC should |
|                  | no longer be used in an instance document (and    |
|                  | there may be a replacement).                      |
+------------------+---------------------------------------------------+
| Definition       | The unique semantic meaning of the ASCC. It       |
|                  | usually indicates context specific semantics of   |
|                  | the ASCCP used by the ASCC. The context is the    |
|                  | ACC owning the ASCC.                              |
+------------------+---------------------------------------------------+

**An ASCCP has the following fields**:

.. list-table::
   :widths: 30 70

   * - GUID
     - A globally unique identifier of the component.
   * - Property Term
     - Name of the ASCCP expressing a qualification of the Associated ACC (this is typically used in expression generation). When there is no qualification, the Property Term should be the same as the Object Class Term of the ACC.
   * - DEN
     - The full official name of the ASCCP (DEN of an ASCCP is constructed from its Property Term and Object Class Term of the ACC it uses).
   * - Nillable
     - Indicating if a NULL value can be assigned in an instance.
   * - Reusable
     - Indicating whether the ASCCP can be reused (this is primarily to support the notion of the local element in XML Schema expression).
   * - Deprecated
     - A status indicating that the ASCCP should no longer be reused and that it may have been replaced by something else. There are business rules associated with deprecated ASCCPs. For example, when the user wants to create a new ASCC, the application will give a warning when a deprecated ASCCP is selected.
   * - Definition
     - The unique semantic meaning of the ASCCP.

**A BCCP has the following fields**:

+------------------+---------------------------------------------------+
| GUID             | A globally unique identifier of the component.    |
+------------------+---------------------------------------------------+
| Property Term    | Name of the BCCP (this is typically used in       |
|                  | expression generation).                           |
+------------------+---------------------------------------------------+
| DEN              | The full official name of the BCCP (DEN of a BCCP |
|                  | is derived from its property term and data type   |
|                  | term of the BDT it uses).                         |
+------------------+---------------------------------------------------+
| Nillable         | Indicating if a NULL value can be assigned in an  |
|                  | instance. If a BCCP is nillable, a BCC using it   |
|                  | is automatically nillable regardless of the       |
|                  | setting in the BCC.                               |
+------------------+---------------------------------------------------+
| Deprecated       | A status to indicate that the BCCP should no      |
|                  | longer be reused and that existing uses have been |
|                  | replaced by something else. For example, when the |
|                  | user wants to create a new BCC, the application   |
|                  | will not allow it to associate to a deprecated    |
|                  | BCCP.                                             |
+------------------+---------------------------------------------------+
| Value            | Value that a data processing system should assume |
| Co               | if no value is assigned in an instance.           |
| nstraint/Default |                                                   |
| Value            |                                                   |
+------------------+---------------------------------------------------+
| Value            | Value that all instance data must have.           |
| Constraint/Fixed |                                                   |
| Value            |                                                   |
+------------------+---------------------------------------------------+
| Definition       | The unique semantic meaning of the BCCP.          |
+------------------+---------------------------------------------------+

**A BCC has the following fields**:

+------------------+---------------------------------------------------+
| GUID             | A globally unique identifier of the component.    |
+------------------+---------------------------------------------------+
| DEN              | The full official name of the BCC (DEN of a BCC   |
|                  | is constructed from Object Class Term of the ACC  |
|                  | and DEN of the BCCP it uses).                     |
+------------------+---------------------------------------------------+
| Entity Type      | Possible values are Element or Attribute. The     |
|                  | primary purpose of this is to support legacy XML  |
|                  | Schema. Attribute indicates that this BCC should  |
|                  | be serialized as an xsd:attribute.                |
+------------------+---------------------------------------------------+
| Min              | Minimum cardinality/occurrences of the            |
|                  | association in an instance.                       |
+------------------+---------------------------------------------------+
| Max              | Maximum cardinality/occurrences of the            |
|                  | association in an instance.                       |
+------------------+---------------------------------------------------+
| Deprecated       | A status indicating that the BCC should no longer |
|                  | be used in an instance document (and there may be |
|                  | a replacement).                                   |
+------------------+---------------------------------------------------+
| Default Value    | Value that a data processing system should assume |
|                  | if no value is assigned in an instance.           |
+------------------+---------------------------------------------------+
| Definition       | The unique semantic meaning of the BCC. It        |
|                  | usually indicates context specific semantics of   |
|                  | the BCCP used by the BCC. The context is the ACC  |
|                  | owning the BCC.                                   |
+------------------+---------------------------------------------------+

**A (Business) Data Type has the following fields:**

+------------------+---------------------------------------------------+
| GUID             | A globally unique identifier of the Data Type.    |
+------------------+---------------------------------------------------+
| Data Type Term   | The basic semantics of the Data Type. It also     |
|                  | tells the general value domain of the data type.  |
|                  | There is a finite set of allowed Representation   |
|                  | Terms defined in the CC specification that can be |
|                  | used as a Data Type Term, e.g., Amount, Code,     |
|                  | Date, Date Time.                                  |
+------------------+---------------------------------------------------+
| Qualifier        | A term that indicates a refined semantics and     |
|                  | possibly value domain of the Data Type.           |
+------------------+---------------------------------------------------+
| DEN              | The full official name of the Data Type (DEN of   |
|                  | the data type is derived from the Qualifier and   |
|                  | the Data Type Term).                              |
+------------------+---------------------------------------------------+
| Definition       | The unique semantic meaning of the Data Type.     |
+------------------+---------------------------------------------------+

**A Supplementary Component has the following fields:**

+------------------+---------------------------------------------------+
| GUID             | A globally unique number identifier of the        |
|                  | component.                                        |
+------------------+---------------------------------------------------+
| DEN              | The full official name of the Supplementary       |
|                  | Component.                                        |
+------------------+---------------------------------------------------+
| Min              | Minimum cardinality/occurrences of the            |
|                  | Supplementary Component in an instance.           |
+------------------+---------------------------------------------------+
| Max              | Maximum cardinality/occurrences of the            |
|                  | Supplementary Component in an instance.           |
+------------------+---------------------------------------------------+
| Definition       | The unique semantic meaning of the Supplementary  |
|                  | Component for the Data Type.                      |
+------------------+---------------------------------------------------+
