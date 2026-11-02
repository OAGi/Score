---
title: "ACC Management"
sidebar_position: 4
---

## Find an ACC

See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) to find the ACC needed.
For more information about finding an ACC see [Find an ACC](../developer/05-acc-management.md#find-an-acc).
Make sure you are on a published release branch for EUCC.

## View detail of an ACC

See [View detail of an ACC](../developer/05-acc-management.md#view-detail-of-an-acc).

## Create a new ACC

See [Create a new ACC](../developer/05-acc-management.md#create-a-new-acc); for end users, the create button is available on a published release branch instead of the Working branch.

## Edit detail of an ACC

To edit an ACC please see [Edit detail of an ACC](../developer/05-acc-management.md#edit-detail-of-an-acc).

:::info
When an end user is editing the details of an ACC, only Non-standard namespaces can be selected in the "Namespace" drop-down list.
See the [Non-standard Namespace Management](./01-non-standard-namespace-management.md) section to create a Non-standard namespace if needed or the [Namespace Management](../developer/02-namespace-management.md) section about how namespaces are used in connectCenter.
:::

## Set a based ACC

See [Set a based ACC](../developer/05-acc-management.md#set-a-based-acc).

## Remove the based ACC

See [Remove the based ACC](../developer/05-acc-management.md#remove-the-based-acc).

## Add a property to an ACC

See [Add a property to an ACC](../developer/05-acc-management.md#add-a-property-to-an-acc).

## Remove a property from an ACC

See [Remove a property from an ACC](../developer/05-acc-management.md#remove-a-property-from-an-acc).

## Edit details of a new ASCC

See [Edit details of a new ASCC](../developer/05-acc-management.md#edit-details-of-a-new-ascc).

## Edit details of a new BCC

See [Edit details of a new BCC](../developer/05-acc-management.md#edit-details-of-a-new-bcc).

## Order the properties/associations

See [Order the properties/associations](../developer/05-acc-management.md#order-the-propertiesassociations).

## Delete a newly created ACC

An ACC whose revision number is 1 can be (marked) deleted.
The ACC has to be in the WIP state and owned by the current user.
See [Delete a newly created EUCC](./06-common-end-user-cc-management-functions.md#delete-a-newly-created-eucc).

## Restore a deleted ACC

See [Restore a deleted EUCC](./06-common-end-user-cc-management-functions.md#restore-a-deleted-eucc).

## Amend an ACC

An ACC in the Production state can be amended where certain backwardly-compatible changes can be made.
Any end user can amend a production ACC.
He/she does not have to be its owner; upon amending, the ownership is transferred to the amending end user.
To do that:

1. [Find an ACC](#find-an-acc) in a published release branch.

2. [Open the detail page of an ACC](#view-detail-of-an-acc) in the Production state.

3. Click the "Amend" button at the top-right corner of the page and confirm in the "Amend this ACC?" dialog.
   The ACC goes into the WIP state and its revision number increases by 1.

4. The following fields in the ACC detail pane on the right may be updated ("Component Type" and "Namespace" are locked during an amendment).

    1. "Object Class Term". The name of the ACC can still be changed.

    2. "Abstract". It can be unchecked when the previous revision was abstract, unless the "Component Type" is "Base (Abstract)" (Base ACCs are always abstract).

    3. "Deprecated". This can only be updated from false (unchecked) to true (checked). In other words, if the ACC was deprecated in the previous revision, it cannot be un-deprecated.

    4. "Definition Source". Specify the source of the definition. This is typically a URI, but the field accepts a free form text. "Definition Source" is optional.

    5. "Definition". Specify the description of the ACC. "Definition" is optional but a warning is given if none is specified.

5. Click the "Update" button at the top right to save changes.

6. The end user may want to perform these other actions on the ACC:

    1. [Set another ACC as a base of this ACC](../developer/05-acc-management.md#set-a-based-acc).

    2. [Remove the based ACC](../developer/05-acc-management.md#remove-the-based-acc).

    3. [Add a property to the ACC](../developer/05-acc-management.md#add-a-property-to-an-acc) and edit the detail of the resulting [BCC](../developer/05-acc-management.md#edit-details-of-a-new-bcc) or [ASCC](../developer/05-acc-management.md#edit-details-of-a-new-ascc).

    4. [Remove a property from the ACC](#remove-a-property-from-an-acc). Only the ASCCs and BCCs added during the current amendment can be removed.

    5. [Order (i.e., change the sequence of) the properties/associations](#order-the-propertiesassociations).

    6. [Change the state of the ACC](./06-common-end-user-cc-management-functions.md#change-eucc-states).

    7. [Create an ASCCP from this ACC](./03-asccp-management.md#create-a-new-asccp).

## Refactor a property to a based ACC

An end user can refactor a property up to a based ACC only when the destination based ACC is in the WIP state and owned by the end user.
In practice this means properties cannot be refactored into developer-owned (standard) based ACCs; the destination has to be an end-user ACC being worked on.
In addition, during an amendment only properties added in the current amendment can be refactored, and a property (association) that is used by any BIE cannot be refactored.
Everything else is the same as detailed in [Refactor a property in an ACC](../developer/05-acc-management.md#refactor-a-property-in-an-acc).

## Cancel an ACC amendment

See [Cancel an EUCC amendment](./06-common-end-user-cc-management-functions.md#cancel-an-eucc-amendment).

## Change ACC states

See [Change EUCC states](./06-common-end-user-cc-management-functions.md#change-eucc-states).

## Transfer ownership of an ACC

See [Transfer ownership of an EUCC](./06-common-end-user-cc-management-functions.md#transfer-ownership-of-an-eucc).

## View history of changes to an ACC

See [View Change History of an EUCC](./06-common-end-user-cc-management-functions.md#view-change-history-of-an-eucc).
