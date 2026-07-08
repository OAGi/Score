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

See [Create a new ACC](../developer/05-acc-management.md#create-a-new-acc).

## Edit detail of an ACC

To edit an ACC please see [Edit detail of an ACC](../developer/05-acc-management.md#edit-detail-of-an-acc).

**Important!** when an end user is editing the details of an ACC, only Non-standard Namespaces can be selected in the Namespace dropdown list.
See the [Non-standard namespace Management](./01-non-standard-namespace-management.md) section to create a Non-standard namespace if needed or the [Namespace Management](../developer/01-core-component-management-tips-and-tricks.md) section about how namespaces are used in connectCenter

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

An ACC in Production state can be amended where certain backwardly-compatible changes can be made.
Any end user can amend a production ACC.
He/she does not have to be its owner.
To do that:

1. [Find an ACC](#find-an-acc) in a published Release branch.

2. [Open the detail page of an ACC](#view-detail-of-an-acc) in Production state.

3. Click the "Amend" button at the top-right corner of the page. The ACC goes into the WIP state and its revision number increases by 1.

4. Only the following fields in the ACC detail pane on the right may be updated.

    1. *Deprecated*. This can only be updated from false (unchecked) to true (checked). In other words, if the ACC was deprecated in the previous revision, it cannot be un-deprecated.

    2. *Definition Source*. Specify the source of the definition. This is typically a URI, but the field accepts a free form text. *Definition Source* is optional.

    3. *Definition*. Specify the description of the BCCP. *Definition* is optional but a warning is given if none is specified.

5. Click the "Update" button at the top right to save changes.

6. The end user may want to perform these other actions on the ACC:

    1. [Set another ACC as a base of this ACC](../developer/05-acc-management.md#set-a-based-acc).

    2. [Remove the based ACC](../developer/05-acc-management.md#remove-the-based-acc).

    3. [Add a property to the ACC](../developer/05-acc-management.md#add-a-property-to-an-acc) and edit the detail of the resulting [BCC](../developer/05-acc-management.md#edit-details-of-a-new-bcc) or [ASCC](../developer/05-acc-management.md#edit-details-of-a-new-ascc).

    4. [Remove a property from the ACC](#remove-a-property-from-an-acc). Only the ASCC and BCC that are in revision 1 (i.e., added during the current revision) can be removed.

    5. [Order (i.e., change the sequence) the properties/associations](#order-the-propertiesassociations).

    6. [Change the state of the ACC](../developer/05-acc-management.md#change-acc-states).

    7. [Create an ASCCP from this ACC](../developer/04-asccp-management.md#create-a-new-asccp).

## Refactor a property to a based ACC

End users cannot refactor a property to a based ACC which is owned by developers.
They can only refactor properties to a based ACC belonging to end users only.
Everything else is the same as detailed in [Refactor a property to a based ACC](../developer/05-acc-management.md#refactor-a-property-in-an-acc).

## Cancel an ACC amendment

See [Cancel an EUCC amendment](./06-common-end-user-cc-management-functions.md#cancel-an-eucc-amendment).

## Change ACC states

See [Change EUCC states](./06-common-end-user-cc-management-functions.md#change-eucc-states).

## Transfer ownership of an ACC

See [Transfer ownership of a EUCC](./06-common-end-user-cc-management-functions.md#transfer-ownership-of-an-eucc).

## View history of changes to an ACC

See [View Change History of an EUCC](./06-common-end-user-cc-management-functions.md#view-change-history-of-an-eucc).
