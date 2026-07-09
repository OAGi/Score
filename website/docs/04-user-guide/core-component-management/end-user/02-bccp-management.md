---
title: "BCCP Management"
sidebar_position: 2
---

## Find a BCCP

See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) to find the BCCP needed.
For more information about finding a BCCP see also [Find a BCCP](../developer/03-bccp-management.md#find-a-bccp).
Make sure you are on a published release branch for EUCC.

## View detail of a BCCP

See [View detail of a BCCP](../developer/03-bccp-management.md#view-detail-of-a-bccp).

## Create a new BCCP

To create a new end user BCCP see [Create a new BCCP](../developer/03-bccp-management.md#create-a-new-bccp); for end users, the create button is available on a published release branch instead of the Working branch.

## Edit detail of a BCCP

To edit an end user BCCP please see [Edit detail of a BCCP](../developer/03-bccp-management.md#edit-detail-of-a-bccp).

:::info
When an end user is editing the details of a BCCP, only Non-standard namespaces can be selected in the "Namespace" drop-down list.
See the [Non-standard Namespace Management](./01-non-standard-namespace-management.md) section to create a Non-standard namespace if needed or the [Namespace Management](../developer/02-namespace-management.md) section about how namespaces are used in connectCenter.
:::

The screenshot below shows an end user BCCP in the WIP state on a published release branch.
Instead of the developer's "Move to Draft" button, the page offers "Move to QA", and the "Namespace" drop-down lists only Non-standard namespaces.

![BCCP detail page of a WIP end-user BCCP on the 10.13 release branch, with the Move to QA button and the Namespace drop-down opened showing only non-standard namespaces](/img/user-guide/eu_bccp_detail_wip.png)

## Delete a newly created BCCP

See [Delete a newly created EUCC](./06-common-end-user-cc-management-functions.md#delete-a-newly-created-eucc).

## Restore a deleted BCCP

See [Restore a deleted EUCC](./06-common-end-user-cc-management-functions.md#restore-a-deleted-eucc).

## Amend a BCCP

An end user BCCP in the Production state can be amended where certain backwardly-compatible changes can be made.
Any end user can amend a production BCCP.
He/she does not have to be its owner; upon amending, the ownership is transferred to the amending end user.
To do that:

1. [Find a BCCP](#find-a-bccp) in the desired published release branch.

2. [Open detail page of the BCCP](#view-detail-of-a-bccp).

3. Click the "Amend" button at the top-right corner of the page and confirm in the "Amend this BCCP?" dialog.
   The BCCP goes into the WIP state and its revision number increases by 1.

4. The following fields can be updated ("Property Term" and "Namespace" are locked during an amendment).

    1. "Nillable". It can only be updated from false (unchecked) to true (checked), and it is locked when the previous revision has a fixed value.

    2. "Deprecated". It can only be updated from false (unchecked) to true (checked).

    3. "Value Constraint". Select "Fixed Value" or "Default Value" in the drop-down (or "None") and specify the value in the adjacent field. The "Fixed Value" option is not selectable when the previous revision is nillable, and a fixed value set in the previous revision cannot be changed. "Value Constraint" is optional.

    4. "Definition Source". Specify the source of the definition. This is typically a URI, but the field accepts a free form text. "Definition Source" is optional.

    5. "Definition". Specify the description of the BCCP. "Definition" is optional but a warning is given if none is specified.

    6. The associated BDT can be changed with the "Change DT" context-menu item on the root node.

5. Click the "Update" button at the top right to save changes.

## Cancel a BCCP amendment

See [Cancel an EUCC amendment](./06-common-end-user-cc-management-functions.md#cancel-an-eucc-amendment).

## Change BCCP states

See [Change EUCC states](./06-common-end-user-cc-management-functions.md#change-eucc-states).

## Transfer ownership of a BCCP

See [Transfer ownership of an EUCC](./06-common-end-user-cc-management-functions.md#transfer-ownership-of-an-eucc).

## View history of changes to a BCCP

See [View Change History of an EUCC](./06-common-end-user-cc-management-functions.md#view-change-history-of-an-eucc).
