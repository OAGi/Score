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

To create a new end user BCCP see [Create a new BCCP](../developer/03-bccp-management.md#create-a-new-bccp).

## Edit detail of a BCCP

To edit an end user BCCP please see [Edit detail of a BCCP](../developer/03-bccp-management.md#edit-detail-of-a-bccp).

**Important!** when an end user is editing the details of a BCCP, only Non-standard Namespaces can be selected in the Namespace dropdown list.
See the [Non-standard namespace Management](./01-non-standard-namespace-management.md) section to create a Non-standard namespace if needed or the [Namespace Management](../developer/01-core-component-management-tips-and-tricks.md) section about how namespaces are used in connectCenter.

## Delete a newly created BCCP

See [Delete a newly created EUCC](./06-common-end-user-cc-management-functions.md#delete-a-newly-created-eucc).

## Restore a deleted BCCP

See [Restore a deleted EUCC](./06-common-end-user-cc-management-functions.md#restore-a-deleted-eucc).

## Amend a BCCP

An end user BCCP in Production state can be amended where certain backwardly-compatible changes be made.
Any end user can amend a production BCCP.
He/she does not have to be its owner.
To do that:

1. [Find a BCCP](#find-a-bccp) in the desired published Release
   branch.

2. [Open detail page of the BCCP](#view-detail-of-a-bccp).

3. Click the Amend button at the top-right corner of the page. The BCCP
   goes into the WIP state and its revision number increases by 1.

4. The following fields can be updated.

    1. *Nillable*. It can only be updated from false (unchecked) to true
       (checked).

    2. *Deprecated*. It can only be updated from false (unchecked) to
       true (checked).

    3. *Value Constraint*. Select *default* or *fixed value* constraint
       in the dropdown list and specify the value in the adjacent field.
       Note that *fixed value* constraint and *nillable* are mutually
       exclusive, i.e., nillable cannot be true if there is a fixed value
       constraint and vice versa. Value constraint is optional.

    4. *Definition Source*. Specify the source of the definition. This is
       typically a URI, but the field accepts a free form text.
       *Definition Source* is optional.

    5. *Definition*. Specify the description of the BCCP. *Definition* is
       optional but a warning is given if none is specified.

5. Click the "Update" button at the top right to save changes.

## Cancel a BCCP amendment

See [Cancel an EUCC amendment](./06-common-end-user-cc-management-functions.md#cancel-an-eucc-amendment).

## Change BCCP states

See [Change CC states](../developer/07-common-developer-cc-management-functions.md#change-a-cc-state).

## Transfer ownership of a BCCP

See [Transfer ownership of a EUCC](./06-common-end-user-cc-management-functions.md#transfer-ownership-of-an-eucc).

## View history of changes to a BCCP

See [View Change History of an EUCC](./06-common-end-user-cc-management-functions.md#view-change-history-of-an-eucc).
