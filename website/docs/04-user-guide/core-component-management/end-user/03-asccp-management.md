---
title: "ASCCP Management"
sidebar_position: 3
---

## Find an ASCCP

See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) to find the ASCCP needed.
For more information about finding an ASCCP see [Find an ASCCP](../developer/04-asccp-management.md#find-an-asccp).
Make sure you are on a published release branch for EUCC.

## View detail of an ASCCP

See [View detail of an ASCCP](../developer/04-asccp-management.md#view-detail-of-an-asccp).

## Create a new ASCCP

There are two ways to create a new ASCCP.

1. Create an ASCCP from scratch.

    1. If you are not already on, open the "Core Component" page by clicking the "View/Edit Core Component" menu item under the "Core Component" menu at the top of the page. Make sure that a published release branch is selected on top-left *Branch* dropdown list.

    2. Click on the plus sign near the top-right corner of the page.

    3. Select ASCCP.

    4. The ACC selection page is open. Check the check box in front of the desired ACC. The user can use other search filters to find the desired ACC. Certain types of ACCs are excluded from the list including Extension, User Extension Group, Embedded, OAGIS10 Nouns, OAGIS10 BODs, For explanation about these different types in connectCenter see [Component Types](../02-key-concepts.md#component-types).

    5. A new ASCCP is created with revision #1. Its detail page is open with default values populated. The new ASCCP is in the WIP state. The end user may [edit the detail of the ASCCP](../developer/04-asccp-management.md#edit-detail-of-an-asccp).

2. Create an ASCCP from an ACC.

    1. [Open the detail page](../developer/05-acc-management.md#view-detail-of-an-acc) of an ACC where the current user is the owner of the ACC and the ACC is in the WIP state.

    2. Click the ellipsis next to the root node of the ACC tree in the left pane.

    3. Select "Create ASCCP from this" menu item.

    4. An ASCCP is created with default values. In this case, the property term is defaulted to the same as the ACC’s object class term. The end user may [edit the detail of the ASCCP](../developer/04-asccp-management.md#edit-detail-of-an-asccp).

## Edit detail of an ASCCP

To edit an ASCCP please see [Edit detail of an ASCCP](../developer/04-asccp-management.md#edit-detail-of-an-asccp).

**Important!** when an end user is editing the details of an ASCCP, only Non-standard Namespaces can be selected in the Namespace dropdown list.
See the [Non-standard namespace Management](./01-non-standard-namespace-management.md) section to create a Non-standard namespace if needed or the [Namespace Management](../developer/01-core-component-management-tips-and-tricks.md) section about how namespaces are used in connectCenter.

## Delete a newly created ASCCP

See [Delete a newly created EUCC](./06-common-end-user-cc-management-functions.md#delete-a-newly-created-eucc).

## Restore a deleted ASCCP

See [Restore a deleted EUCC](./06-common-end-user-cc-management-functions.md#restore-a-deleted-eucc).

## Amend an ASCCP

An ASCCP in the Production state can be amended in order to make certain backwardly-compatible changes.
Any end user can amend a production ASCCP.
He/she does not have to be its owner.
To do that:

1. [Find an ASCCP](#find-an-asccp) in a published Release branch.

2. [Open detail page of an ASCCP](#view-detail-of-an-asccp).

3. Click the "Amend" button at the top-right corner of the page. The ASCCP goes into the WIP state and its revision number increases by 1.

4. The following fields can be updated.

    1. *Nillable*. It can only be updated from false (unchecked) to true (checked).

    2. *Deprecated*. It can only be updated from false (unchecked) to true (checked).

    3. *Reusable*. It can only be updated from false (unchecked) to true (checked). If the reusable is changed to true, it means that there can be multiple ASCCs using the ASCCP. However, it may cause release invalidation if this results in multiple reusable ASCCP with the same property term.

    4. *Definition Source*. Specify the source of the definition. This is typically a URI but the field accepts free form text. *Definition Source* is optional.

    5. *Definition*. Specify the description of the ASCCP. *Definition* is optional but a warning is given if none is specified.

5. Click the "Update" button at the top right to save changes.

## Cancel an ASCCP amendment

See [Cancel an EUCC amendment](./06-common-end-user-cc-management-functions.md#cancel-an-eucc-amendment).

## Change ASCCP states

See [Change EUCC states](./06-common-end-user-cc-management-functions.md#change-eucc-states).

## Transfer ownership of an ASCCP

See [Transfer ownership of a EUCC](./06-common-end-user-cc-management-functions.md#transfer-ownership-of-an-eucc).

## View history of changes to an ASCCP

See [Transfer ownership of a EUCC](./06-common-end-user-cc-management-functions.md#transfer-ownership-of-an-eucc).
