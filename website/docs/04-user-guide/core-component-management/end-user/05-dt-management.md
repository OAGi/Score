---
title: "DT Management"
sidebar_position: 5
---

A non-working branch should be selected in order for the end users to manage end-user DTs.

## Find a DT

See [Find a DT](../developer/06-dt-management.md#find-a-dt).

## View detail of a DT

See [View detail of a DT](../developer/06-dt-management.md#view-detail-of-a-dt).

## Create a new DT

See [Create a new DT](../developer/06-dt-management.md#create-a-new-dt).

## Edit detail of a DT

See [Edit detail of a DT](../developer/06-dt-management.md#edit-detail-of-a-brand-new-dt). Note that for the Namespace field of an end-user DT, a non-standard namespace should be selected. See the [Non-standard Namespace Management](./01-non-standard-namespace-management.md) section to create a non-standard namespace if needed or how namespace may be used in connectCenter

## Edit Value Domain

See [Edit Value Domain](../developer/06-dt-management.md#edit-value-domain).

## Add an SC to a DT

See [Add an SC to a DT](../developer/06-dt-management.md#add-an-sc-to-a-dt).

## Edit details of a new SC

See [Edit details of a new SC](../developer/06-dt-management.md#edit-details-of-a-brand-new-sc-in-the-dt-where-it-was-added).

## Remove a newly added SC from a DT

See [Remove a newly added SC from a DT](../developer/06-dt-management.md#remove-a-newly-added-sc-from-a-dt).

## Edit details of an existing SC

See [Edit details of an existing SC](../developer/06-dt-management.md#edit-details-of-an-existing-sc).

## Delete a newly created DT

A DT whose revision number is 1 can be (marked) deleted. The DT has to be in the WIP state and owned by the current user. See [Delete a newly created EUCC](./06-common-end-user-cc-management-functions.md#delete-a-newly-created-eucc).

## Restore a deleted DT

See [Restore a deleted EUCC](./06-common-end-user-cc-management-functions.md#restore-a-deleted-eucc).

## Amend a DT

A DT in the Production state can be revised where certain changes can be made. Any end user can amend a DT that is in the Production state. He/she does not have to be its owner. To do that:

1. [Find a DT](#find-a-dt) in a non-working branch.

2. [Open detail page of a DT](#view-detail-of-a-dt) in the Production state.

3. Click the "Amend" button at the top-right corner of the page. The DT goes into the WIP state; and its revision number increases by 1.

4. Only the following fields in the DT detail pane on the right may be updated.

    1. *Definition Source*. Specify the source of the definition. This is typically a URI but the field accepts a free form text. *Definition Source* is optional.

    2. *Definition*. Specify the description of the BCCP. *Definition* is optional but a warning is given if none is specified.

    3. *Content Component Definition*. Specify the definition of the DT’s Content Component value. This is typically a free form text. *Content Component Definition* is optional.

<!-- list break: the RST source numbering jumps from 4 to 7, rendering as a second list starting at 7 -->

7. Click the "Update" button at the top right to save changes.

8. The end user may want to perform these other actions on the DT:

    1. [Edit Value Domain](../developer/06-dt-management.md#edit-value-domain).

    2. [Add an SC to the DT](../developer/06-dt-management.md#add-an-sc-to-a-dt).

    3. [Edit details of a new SC](../developer/06-dt-management.md#edit-details-of-a-brand-new-sc-in-the-dt-where-it-was-added).

    4. [Remove a newly added SC from the DT](../developer/06-dt-management.md#remove-a-newly-added-sc-from-a-dt).

    5. [Editing details of an existing SC](../developer/06-dt-management.md#edit-details-of-an-existing-sc).

## Cancel a DT amendment

See [Cancel an EUCC amendment](./06-common-end-user-cc-management-functions.md#cancel-an-eucc-amendment).

## Change DT states

See [Change EUCC states](./06-common-end-user-cc-management-functions.md#change-eucc-states).

## Transfer ownership of a DT

See [Transfer ownership of a EUCC](./06-common-end-user-cc-management-functions.md#transfer-ownership-of-an-eucc).

## View history of changes to a DT

See [View Change History of an EUCC](./06-common-end-user-cc-management-functions.md#view-change-history-of-an-eucc).
