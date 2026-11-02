---
title: "ASCCP Management"
sidebar_position: 4
---

## Find an ASCCP

See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) to find the ASCCP needed.

:::tip
To find where an ASCCP is used, use the "Where Used" function instead of the list page.
Open a Core Component detail page, click the ellipsis next to the target ASCCP node, and choose "Where Used".
The returned dialog lists the ACCs that reference that ASCCP.
:::

## View detail of an ASCCP

[Find an ASCCP](#find-an-asccp).
Click on the ASCCP DEN after the desired ASCCP is found to open the ASCCP detail page.
To understand the detail of the ASCCP, see [Quick reference to different types of CCs](../03-search-and-browse-cc-library.md#quick-reference-to-different-types-of-ccs).

:::tip
You can open an ASCCP detail page from within another Core Component tree.
On any ASCCP node (bolded blue font node), click on the ellipsis next to the node and select "Open in new tab".
:::

## Create a new ASCCP

There are two ways to create a new ASCCP.

1. Create an ASCCP from scratch.

    1. If you are not already on, open the "Core Component" page by clicking the "View/Edit Core Component" menu item under the "Core Component" menu at the top of the page.
       Make sure the Working branch is selected.

    2. Click the plus (+) button near the top-right corner of the page.

    3. Select "ASCCP".

    4. The "Select ACC to create ASCCP" dialog opens.
       Check the checkbox in front of the desired ACC; the user can use the search filters to find it.
       Only ACCs whose component type is "Semantics" or "Semantic Group" are listed; all other component types (including "Base (Abstract)", "Extension", "User Extension Group", and "Embedded") are excluded.
       For explanation about these different types in connectCenter see [Component Types](../02-key-concepts.md#component-types).

       ![Select ACC to create ASCCP dialog listing published ACCs with checkboxes](/img/user-guide/asccp_select_acc_dialog.png)

    5. Click the "Create" button.
       If the selected ACC is deprecated, a confirmation dialog asks whether to proceed anyway.

    6. A new ASCCP is created with revision #1. Its detail page is open with default values populated; the property term is defaulted to the ACC's object class term.
       The new ASCCP is in the WIP state.
       The developer may [edit the detail of the ASCCP](#edit-detail-of-an-asccp).

2. Create an ASCCP from an ACC:

    1. [Open ACC detail page](./05-acc-management.md#view-detail-of-an-acc) where the current user is the owner of the ACC and the ACC is in the WIP state.

    2. Click the ellipsis next to the root node of the ACC tree in the left pane.

    3. Select the "Create ASCCP from this" menu item and confirm in the returned dialog.
       The menu item is disabled when the ACC is abstract or has unsaved changes.

    4. An ASCCP is created with default values; the property term is defaulted to the same as the ACC's object class term.
       The ASCCP is in WIP state.
       See also [Edit detail of an ASCCP](#edit-detail-of-an-asccp).

## Edit detail of an ASCCP

This section describes ASCCP editing when its revision number is 1.

1. Open the ASCCP detail page according to [View detail of an ASCCP](#view-detail-of-an-asccp).
   The ASCCP has to be in the WIP state and owned by the current user to be editable.
   The following fields can be updated.

    1. "Property Term". Property Term should be space-separated words, each with initial letter capitalized. Acronyms and plural words should be avoided. For connectSpec, it should be what one would expect to see in the expression. For example, a "Customer Party" ASCCP which uses the "Party" ACC should have a property term "Customer Party" (not just "Customer" as *CustomerParty* is expected in the expression), which would yield "Customer Party. Party" as DEN. In other words, the object class term "Party" is not used in the expression generation.

    2. "Nillable". Nillable specifies whether a null value can be assigned in the instance data. It is defaulted to false (unchecked).

    3. "Deprecated". Since this is a brand new ASCCP, the "Deprecated" field is locked.

    4. "Reusable". This flag supports the notion of local element expression in XML Schema. It is defaulted to true, which makes the ASCCP analogous to the global element in XML Schema. When an ASCCP is set to not reusable, the application will allow only one ASCC to use the ASCCP; and by convention, several non-reusable ASCCPs may share the same property term in a single release. In connectSpec, the Data Area component in a BOD is expressed as a local element. In such situation, the ASCCP corresponding to a Data Area would have this flag set to false.

    5. "Namespace". Select a standard namespace from the drop-down list. See the [Namespace Management](./02-namespace-management.md) section to create a standard namespace if needed or how namespaces may be used in connectCenter. "Namespace" is required; clicking "Update" without a namespace is rejected with a "Namespace is required" message.

    6. "Definition Source". Specify the source of the definition. This is typically a URI, but the field is free form text. "Definition Source" is optional.

    7. "Definition". Specify the description of the ASCCP. "Definition" is optional, but a warning is given if none is specified.

2. Click the "Update" button at the top right to save changes.

:::tip
To change the ACC associated with the ASCCP, click the ellipsis next to the root node of the ASCCP tree and select "Change ACC".
:::

## Delete a newly created ASCCP

See [Delete a newly created CC](./07-common-developer-cc-management-functions.md#delete-a-newly-created-cc).

## Restore a deleted ASCCP

See [Restore a deleted CC](./07-common-developer-cc-management-functions.md#restore-a-deleted-cc).

## Revise an ASCCP

An ASCCP in the Published state can be revised where certain changes can be made.
Any developer user can revise a published ASCCP.
He/she does not have to be its owner; upon revising, the ownership is transferred to the revising developer.
To do that:

1. [Find an ASCCP](#find-an-asccp) in the Working branch.

2. [Open detail page of the ASCCP](#view-detail-of-an-asccp).

3. Click the "Revise" button at the top-right corner of the page and confirm in the "Revise this ASCCP?" dialog.
   The ASCCP goes into the WIP state and its revision number increases by 1.

4. The following fields can be updated ("Property Term" and "Namespace" are locked during a revision).

    1. "Nillable". It can only be updated from false (unchecked) to true (checked).

    2. "Deprecated". It can only be updated from false (unchecked) to true (checked).

    3. "Reusable". It can only be updated from false (unchecked) to true (checked). If reusable is changed to true, it means that there can be multiple ASCCs using the ASCCP.

    4. "Definition Source". Specify the source of the definition. This is typically a URI, but the field is free form text. "Definition Source" is optional.

    5. "Definition". Specify the description of the ASCCP. "Definition" is optional but a warning is given if none is specified.

5. Click the "Update" button at the top right to save changes.

## Cancel an ASCCP revision

See [Cancel a CC revision](./07-common-developer-cc-management-functions.md#cancel-a-cc-revision).

## Change ASCCP states

See [Change CC states](./07-common-developer-cc-management-functions.md#change-a-cc-state).

## Transfer ownership of an ASCCP

See [Transfer ownership of a CC](./07-common-developer-cc-management-functions.md#transfer-ownership-of-a-cc).

## View history of changes to an ASCCP

See [View Change History of a CC](./07-common-developer-cc-management-functions.md#view-change-history-of-a-cc).
