---
title: "Agency ID List Management"
sidebar_position: 10
---

Agency ID List management has its own page which can be accessed under the "Core Component" menu.
In connectCenter, Agency ID Lists are considered a kind of CC because they can be included in the release of standard Core Components.
Only one active developer Agency ID List is allowed at a time: in a typical repository there is one Agency ID List created by the default account, and a new one can be created only when no active Agency ID List exists.
The idea is the agency ID is one of the lowest common denominators in a standard that facilitates interoperability, and having more than one Agency ID List does not help.
Developers can revise the existing Agency ID List to add new values and include it in the next release of Core Components.

## Find an Agency ID List

To find an Agency ID List needed to work on, select "View/Edit Agency ID List" under the "Core Component" menu on the top of connectCenter pages.
The "Agency ID List" page is open that contains a table listing the Agency ID List in a particular branch.
To manage the existing Agency ID List, ensure that "Working" is selected in the "Branch" selector.
There are also search filters on the top of the table to use, but there is usually no need since there is only one developer Agency ID List.
Developers can use these filters when a non-working release is selected in the "Branch" selector.
All filters on the Agency ID List page have the same meaning as those described in [How to Search and Filter for a Core Component](../03-search-and-browse-cc-library.md#how-to-search-and-filter-for-a-core-component), except that some filters do not exist on the Agency ID List page and the search bar is labeled "Search by Name" instead of "Search by DEN".
The [CC Specification](https://www.unece.org/cefact/codesfortrade/ccts_index.html) does not have Agency ID List as one of the Registry Classes and hence Agency ID List does not have a DEN specification.
The name of the Agency ID List is a free form text.

![Agency ID List page listing the single developer Agency ID List in the Published state](/img/user-guide/agency_id_list_page.png)

## View detail of an Agency ID List

To view Agency ID List detail:

1. [Find an Agency ID List](#find-an-agency-id-list).

2. Click on the Agency ID List name to open the "Edit Agency ID List" page.

   :::note
   Clicking somewhere else on an Agency ID List entry will display its textual definition.
   :::

![Edit Agency ID List page showing the Name, List ID, Version, Agency ID List Value, and Namespace fields and the Agency ID List Values table](/img/user-guide/agency_id_list_detail.png)

## Create a new Agency ID List

Developers can create a new Agency ID List only when no active (non-deleted) Agency ID List exists in the Working branch; in that case a plus (+) button (tooltip "New Agency ID List") appears at the top-right of the "Agency ID List" page.
Clicking the button creates the Agency ID List immediately and opens its "Edit Agency ID List" page, where the new revision 1 Agency ID List is in the WIP state and can be edited.
When an active Agency ID List already exists, developers should instead revise it (see [Revise an Agency ID List](#revise-an-agency-id-list)).

## Revise an Agency ID List

A developer Agency ID List in the Published state can be revised.
The current user does not have to be the owner of the Agency ID List; upon revising, the ownership is transferred to the revising developer.
To revise an Agency ID List:

1. Make sure you are on the Working branch. [Open the "Edit Agency ID List" page](#view-detail-of-an-agency-id-list) of an Agency ID List in the Published state.

2. Click the "Revise" button at the top-right corner of the page and confirm in the "Revise this agency ID list?" dialog.

3. The "Edit Agency ID List" page is refreshed with the Agency ID List whose revision number is incremented by 1 and the state changed to WIP.

4. [Detail of the Agency ID List can be updated including add/change Agency ID List values.](#edit-detail-of-a-revised-agency-id-list)

## Edit detail of a revised Agency ID List

This section describes Agency ID List editing when its revision number is 2 or more.

1. Open the "Edit Agency ID List" page according to [View detail of an Agency ID List](#view-detail-of-an-agency-id-list), if it is not already opened. The Agency ID List has to be in the WIP state, and the current user has to be the owner to be editable. The fields in the detail pane may be updated as follows:

    1. "Name". It is the name of the Agency ID List. The field is locked and not editable.

    2. "List ID". This is a free form text representing the external/global identifier of the Agency ID List. The field is locked, and change is not allowed.

    3. "Agency ID List Value". This field designates the value, among the list's own values, that identifies the organization owning and managing the Agency ID List. The field is locked during a revision.

    4. "Version". This field is a freeform text representing the version of the Agency ID List. The system will validate that the combination of "List ID", "Agency ID List Value", and "Version" is unique in the branch data. Although the version should generally be different from prior revisions, connectCenter does not validate this. "Version" is required.

    5. "Namespace". The field is locked and cannot be changed.

    6. "Definition". Specify the description of the Agency ID List. "Definition" is optional but a warning is given if none is specified.

    7. "Definition Source". Specify the source of the definition. This is typically a URI, but the field accepts a free form text. "Definition Source" is optional.

2. Click the "Update" button at the top-right of the page to save changes.

3. The developer may also want to perform these other actions on the Agency ID List:

    1. [Add a brand-new Agency ID List value to the Agency ID List](#add-a-brand-new-agency-id-list-value-to-the-agency-id-list)

    2. [Remove a brand-new Agency ID List value from the Agency ID List](#remove-a-brand-new-agency-id-list-value-from-the-agency-id-list)

    3. [Edit detail of a brand-new Agency ID List value](#edit-detail-of-a-brand-new-agency-id-list-value)

    4. [Revise detail of an Agency ID List value](#revise-detail-of-an-agency-id-list-value)

    5. [Change an Agency ID List state](#change-an-agency-id-list-state)

    6. [Transfer ownership of an Agency ID List](#transfer-ownership-of-an-agency-id-list)

## Add a brand-new Agency ID List value to the Agency ID List

An Agency ID List value can be added to an Agency ID List that is in the WIP state and is owned by the current user.

1. In the "Agency ID List Values" section of the ["Edit Agency ID List" page](#view-detail-of-an-agency-id-list), click the "Add" button.

2. The "Add Agency ID List Value" dialog pops up where the following fields can be edited.

    1. "Value". The value of the Agency ID List value. "Value" is required.

    2. "Meaning". Short name or short description of the Agency ID List value. "Meaning" is required.

    3. "Definition". Long description of the Agency ID List value. "Definition" is optional.

    4. "Definition Source". The source of the definition. This can be any text but usually a URI is specified. It is optional.

    5. "Deprecated". The checkbox is disabled when a value is newly added.

    6. "Developer Default" and "User Default". These developer-only checkboxes designate the value to be used as the default agency ID list value in code lists created by developers and by end users, respectively.

3. Click the "Add" button at the bottom of the dialog. To get out of the dialog without adding the Agency ID List value, hit the ESC button or click outside of the dialog.

4. Click the "Update" button at the top of the page.

## Remove a brand-new Agency ID List value from the Agency ID List

An Agency ID List value can be removed from an Agency ID List only when it is newly added in the current revision, it is not used in a Code List, and it is not selected as the list's own "Agency ID List Value".
The Agency ID List must be in the WIP state and owned by the current user.

1. In the "Agency ID List Values" table on the ["Edit Agency ID List" page](#view-detail-of-an-agency-id-list), click the checkboxes in front of one or more Agency ID List values intended to be removed. Only removable Agency ID List values have their checkboxes enabled.

2. Click the "Remove" button next to the "Add" button in the "Agency ID List Values" section.

3. A confirmation dialog is displayed. Confirm or cancel the removal request.

4. Click the "Update" button at the top of the page.

## Edit detail of a brand-new Agency ID List value

This section describes the case when the Agency ID List value is brand-new, i.e., an Agency ID List value added to a revision 1 Agency ID List (brand-new Agency ID List) or to a revised Agency ID List (revision number more than 1).
The Agency ID List must be in the WIP state and owned by the current user.
To edit the detail of an Agency ID List value:

1. In the "Agency ID List Values" table on the ["Edit Agency ID List" page](#view-detail-of-an-agency-id-list), click on the row of the Agency ID List value to be updated.

2. The "Edit Agency ID List Value" dialog is open. Detail of the Agency ID List value can be updated as described in [Add a brand-new Agency ID List value to the Agency ID List](#add-a-brand-new-agency-id-list-value-to-the-agency-id-list).

3. Click the "Save" button at the bottom of the dialog. To get out of the dialog without saving the changes, hit the ESC button or click outside of the dialog.

4. Click the "Update" button at the top of the page.

## Revise detail of an Agency ID List value

This section describes the case when the Agency ID List value has existed since the previous revision of the Agency ID List.
To revise the detail of an Agency ID List value:

1. In the "Agency ID List Values" table on the ["Edit Agency ID List" page](#view-detail-of-an-agency-id-list) where the revision number of the Agency ID List is 2 or more, click on the row of the Agency ID List value to be updated.

2. The "Edit Agency ID List Value" dialog is open. Detail of the Agency ID List value can be updated as follows.

    1. "Value". Change is not allowed.

    2. "Meaning". Change is allowed, and the field is mandatory.

    3. "Definition". Long description of the Agency ID List value. "Definition" is optional.

    4. "Definition Source". The source of the definition. This can be any text but usually a URI is specified. It is optional.

    5. "Deprecated". Check the checkbox to deprecate the Agency ID List value.

3. Click the "Save" button at the bottom of the dialog. To get out of the dialog without saving the changes, hit the ESC button or click outside of the dialog.

4. Click the "Update" button at the top of the page.

## Delete an Agency ID List

Developers cannot delete a published Agency ID List.
They cannot delete a revision of it either, since a revised CC cannot be deleted (the revision can only be [cancelled](#cancel-an-agency-id-list-revision)).
Only a brand-new (revision 1) Agency ID List in the WIP state can be deleted by its owner.

## Cancel an Agency ID List revision

The developer who is the owner of the Agency ID List being revised can cancel the revision.
In this case, all changes to the Agency ID List are discarded.
Agency ID List detail and its owner are rolled back to the pre-revised state.
To cancel an Agency ID List revision:

1. [Open the "Edit Agency ID List" page](#view-detail-of-an-agency-id-list) of an Agency ID List that is not in the Published state and with revision number greater than 1 (i.e., a revised Agency ID List). The current user must be the owner of the Agency ID List.

2. If it is not in the WIP state, [change the state back to WIP](#change-an-agency-id-list-state).

3. Click the "Cancel" button at the top-right of the page.

4. Confirm (or cancel) the revision cancellation.

## Change an Agency ID List state

The section covers the toggling between the WIP, Draft, and Candidate states of the Agency ID List.
For the detailed meaning of these and other states, see [Change a CC state](./07-common-developer-cc-management-functions.md#change-a-cc-state).
The current user has to be the owner of the Agency ID List to toggle between these three states.
To change the Agency ID List state:

1. [Open the "Edit Agency ID List" page](#view-detail-of-an-agency-id-list) of an Agency ID List that is owned by the current user. Also, make sure you are on the Working branch.

2. Depending on the current state of the Agency ID List, click either the "Move to Draft", "Move to Candidate", or "Back to WIP" button at the top-right corner of the page.

3. Confirm (or cancel) the state change.

## Transfer ownership of an Agency ID List

To let another developer make changes to an Agency ID List, the current owner has to transfer ownership of the Agency ID List to another developer.
Developer Agency ID Lists can be transferred only to another developer, and only while the Agency ID List is in the WIP state.
To transfer ownership:

1. Go to the "Agency ID List" page by clicking the "View/Edit Agency ID List" menu item under the "Core Component" menu. Make sure you are on the Working branch.

2. Click the transfer icon (icon with two opposite arrows) next to the owner of the Agency ID List.

3. A dialog is displayed to select a developer. Use the filter on the top to find the desired developer and check the checkbox in front of it.

4. Click the "Transfer" button.
