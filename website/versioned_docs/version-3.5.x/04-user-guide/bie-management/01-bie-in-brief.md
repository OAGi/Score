---
title: "BIE in brief"
sidebar_position: 1
---

BIE (Business Information Entity) is a derivation (extension and restriction, also known as a **profile**) of a shared CC (Core Component).
CCs are the canonical, context-independent data structure and semantic definitions.
BIEs, on the other hand, are profiles of the CCs for a particular usage context and provide implementation details of the CCs for that particular context.

There are in fact a few types of BIEs corresponding to the few types of CCs per the UN/CEFACT Core Component Specification (CCS), also known as ISO 15000-5. connectCenter has somewhat simplified BIEs and made them more user-friendly than what you might experience in the CCS.
In this user guide we will not dive into the nitty-gritty detail of the various types of BIEs and how they are related to their CC counterparts.
So, in this user guide when talking about a BIE, it could mean the root node or any of its descendant nodes in a tree representation of a data structure definition.
The root BIE may be also referred to as **top-level BIE**.
A top-level BIE is created from an ASCCP.
Existing top-level BIEs can also be obtained via "Copy BIE", "Uplift BIE", the "Create Inherited BIE" action on the BIE list, or "Make BIE reusable" in the BIE tree — but each of these is still ultimately rooted in an ASCCP.
See [Manage BIE](./06-manage-bie.md) for all of these functions.

A BIE is always associated with a Business Context, which provides the metadata about the applicability of the BIE.
This is also referred to as the CC's usage context.
To create a BIE, a Business Context is needed; in fact, the first step of the "Create BIE" page is to select one or more Business Contexts, and its "Next" button stays disabled until at least one is selected.
See [Manage Context](./05-manage-context.md) which provides introductions and guidelines for the management of Business Context and its dependencies.

A BIE simplifies its corresponding CC in two ways so that the data structure definition (schema) becomes a simpler tree structure that is easier for business analysts and software developers to consume.
In other words, CCs are for the data modeler and data architect; and BIEs are for business analysts and integration developers.
You may have seen modelling constructs in XML Schema, UML or similar, where there are features, such as Subtyping, Restriction, Abstract, Group.
These constructs support formal semantic representations and the reuse of the vocabulary and data structure definition.
These constructs exist only in the CC realm and they are removed or flattened in the BIE realm, as a BIE is not about defining a data model but using it.

When the user views or edits the BIE tree, the tree has different font formats for each kind of tree node: aggregate nodes are blue, value nodes are dark green, attribute nodes are grey italic, and deprecated nodes are struck through.
These different formats are not very important for the user who is primarily interested in the use case associated with a BIE; [Manage BIE](./06-manage-bie.md) covers working with the tree in detail.
The detail pane on the right side of the tree shows the fields of whichever node is currently selected, whether or not the node is checked; for an unchecked node the fields are simply disabled.
To actually edit the fields, the BIE must be in the WIP state and owned by you, and the node must be enabled — its "Used" checkbox checked (required nodes cannot be unchecked once used).
The fields are based primarily on whether the node can have a value in an instance or not.
If so, it is possible to customize the value restriction (i.e., value domain).
The "Value Domain Restriction" selector chooses among "Primitive", "Code" (a code list), and "Agency" (an agency identification list — a code list that is specifically about identifying an organization), and the dependent "Value Domain" selector picks the concrete primitive, code list, or agency ID list.
In addition, a "Value Constraint" selector can impose a "Fixed Value" or a "Default Value" on the node, and a "Facets" card offers "Minimum Length", "Maximum Length", and "Pattern" restrictions, with a "Pattern Test" field to try the pattern out.
With that, we will first describe [BIE Content](./02-bie-content.md), [Manage End User Code Lists](./03-manage-end-user-code-lists.md), and then [Manage Context](./05-manage-context.md) before [Manage BIE](./06-manage-bie.md) where we will provide more details about the fields on the different kinds of BIE nodes.

A note about the names.
Names of BIEs (and their corresponding CCs) are stored and displayed on the UI in a space separated format, which is a canonical form.
This makes them easier to read and also allows for serializations into differing formats, such as upper or lower camel cases.

## Footnote about Qualifier

CCS has the notion of Qualifier, which is additional words that may be prefixed to parts of a CC DEN resulting in BIE DENs.
In other words, BIE DENs can be different from those of their CC counterparts according to CCS.
At this time, it is not clear how this concept can be practically applied in the standard development and usage environment considered by connectCenter.
This is because connectCenter treats CCs as the standard artifact and BIEs as profiles of CCs that shall still conform with the standard.
Therefore, for interoperability names of BIEs shall be the same as CCs because names are used in actual information exchanges and software implementation.
For these reasons, the Qualifier concept is not currently used in connectCenter.
However, this may change as practical usage of the concept is more understood in the course of industry adoption of connectCenter and these CCS concepts.
Note that the top-level node of a WIP BIE you own can still be given a "Display Name", via the node's context menu ("Change Display Name") or by double-clicking its label.
The tree then shows the node as "Display Name (Name)"; the node's actual name is preserved.
