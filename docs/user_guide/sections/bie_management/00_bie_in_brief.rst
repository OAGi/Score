BIE in brief
------------

BIE (Business Information Entity) is a derivation (extension and restriction or also known as a **profile**) of a shared CC (Core Component).
CCs are the canonical, context-independent data structure and semantic definitions.
BIEs, on the other hand, are profile of the CCs for a particular usage context and provide implementation details of the CCs for that particular context.

There are in fact a few types of BIEs corresponding to the few types of CCs per the UN/CEFACT Core Component Specification (CCS), also known as ISO 15000-5. connectCenter has somewhat simplified the BIE and make it more user-friendlier than what you might experience in the CCS.
In this user guide we will not dive into the nitty-gritty detail of the various types of BIEs and how they are related to CC counterparts.
So, in this user guide when talking about BIE, it could mean the root node or any of its descendant nodes in a tree representation of a data structure definition.
The root BIE may be also referred to as **top-level BIE**.
A top-level BIE is created from an ASCCP.

A BIE is always associated with a Business Context, which provides the metadata about the applicability of the BIE.
This is also referred to as CC’s usage context.
To create a BIE, a Business Context is needed.
See `Manage Context <#manage-context>`__ which provides introductions and guidelines for the management of Business Context and its dependencies.

A BIE simplifies its corresponding CC in two ways so that the data structure definition (schema) becomes a simpler tree structure that is easier for business analyst and software developer to consume.
In other words, CCs are for the data modeler and data architect; and BIEs are for business analysts and integration developers.
You may have seen modelling constructs in XML Schema, UML or similar, where there are features, such as Subtyping, Restriction, Abstract, Group.
These constructs support formal semantic representations and the reuse of the vocabulary and data structure definition.
These constructs exist only in the CC realm and they are removed or flatten in the BIE realm, as BIE is not about defining a data model but using it.

When the user views or edits the BIE tree, the tree has different font formats for each tree node.
These different formats are not very important for the user who is primarily interested in the use case associated with a BIE.
The detail pane on the right side of the tree shows different fields where the user can customize the BIE, when the node is checked (enabled/used) and is currently selected.
The fields are based primarily on whether the node can have a value in an instance or not.
If so, it is possible to customize the value restriction (i.e., value domain).
The value restriction can be in the form of a primitive type, code list, or agency identification list (a code list that is specifically about identifying an organization).
With that, we will first describe `Manage Code Lists <#bie-content>`__ and then `Manage Context <#manage-context>`__ before `Manage BIE <#manage-bie>`__ where we will provide more details about fields on the different kinds of BIE nodes.

A note about the names.
Names of BIEs (and their corresponding CCs) are stored and displayed on the UI in a space separated format, which is a canonical form.
This makes them easier to read and also allows for serializations into differing formats, such as upper or lower camel cases.

Footnote about Qualifier
~~~~~~~~~~~~~~~~~~~~~~~~

CCS has the notion of Qualifier, which is addition words that may be prefixed to parts of CC DEN that results in BIE DENs.
In other words, BIE DENs can be different from those of their CC counterparts according to CCS.
At this time, it is not clear how this concept can be practically applied in the standard development and usage environment considered by connectCenter.
This is because connectCenter treats CCs as the standard artifact and BIEs as profiles of CCs that shall still conform with the standard.
Therefore, for interoperability names of BIEs shall be the same as CCs because names are used in actual information exchanges and software implementation.
For these reasons, the Qualifier concept is not currently used in connectCenter.
However, this may change as practical usage of concept is more understood in the course of industry adoption of connectCenter and these CCS concepts.
