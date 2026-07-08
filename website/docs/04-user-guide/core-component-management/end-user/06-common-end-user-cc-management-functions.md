---
title: "Common End User CC Management Functions"
sidebar_position: 6
---

The user guide in this section commonly applies to end user BCCP, ASCCP, and ACC.

## Delete a newly created EUCC

Similar to [Delete a newly created CC](../developer/07-common-developer-cc-management-functions.md#delete-a-newly-created-cc), an end user can put an EUCC into the deleted state. The EUCC has to have revision #1. This signifies that the owner of the deleted CC does not want to use it anymore. It suggests that if the EUCC is used by another end user, he/she should consider using another component. It is recommended that the owner documents the reason for deletion in the *Definition* field before deleting. Other end users (or the owner himself) can however restore the EUCC – See [Restore a deleted EUCC](#restore-a-deleted-eucc). To delete a EUCC:

1. Go to the "Core Component" page by clicking the "View/Edit Core Component" menu item under the "Core Component" menu. Find one or more EUCCs to delete. See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) for help in locating an EUCC. Individual EUCC management sections also contain additional tips to search for EUCCs.

2. There are two ways to delete a EUCC.

    1. Delete one or more EUCCs simultaneously.

        1. Check the checkbox in front of one or more EUCCs that are in the WIP state, have revision 1, and are owned by the current end user.

        2. A trash icon is displayed at the top-right corner of the page.

        3. Click the trash icon.

    2. Delete an EUCC individually.

        1. Click on the DEN of the EUCC to open its detail page. The EUCC must be in the WIP state, has revision 1, and is owned by the current end user.

        2. Click the "Delete" button at the top-right corner.

3. Confirm (or cancel) the deletion on the pop-up dialog.

## Restore a deleted EUCC

Once an EUCC is deleted, the ownership is delinquent from the current owner. Any other end users can restore the EUCC to the WIP state and take the ownership. To restore an EUCC:

1. Go to the "Core Component" page by clicking the "View/Edit Core Component" menu item under the "Core Component" menu. Find one or more EUCCs to restore from deletion. See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) for help in locating a EUCC. There are two ways to restore an EUCC.

    1. Restore one or more EUCCs simultaneously.

        1. Check the checkbox in front of the EUCCs that are in the Deleted state.

        2. A trash can with an arrow icon inside is displayed at the top-right corner of the page.

        3. Click the icon.

    2. Restore an EUCC individually.

        1. Click on the DEN of a deleted EUCC to open its detail page.

        2. Click the "Restore" button at the top-right corner of the page.

2. Confirm (or cancel) the restoration on the pop-up dialog.

## Cancel an EUCC amendment

The end user who is the owner of a EUCC being amended can cancel the amendment. In this case, all changes to the EUCC are discarded. EUCC detail and its owner are rollbacked to the pre-amendment state. To cancel an EUCC amendment:

1. Go to the "Core Component" page by clicking the "View/Edit Core Component" menu item under the "Core Component" menu. Find the EUCC to cancel its amendment. See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) for help in locating the desired EUCC. Individual EUCC management sections also contain additional tips to search for EUCCs.

2. Click on the DEN of the EUCC to open its detail page. The current user has to be the owner of the EUCC and the EUCC has to be the WIP state with revision number greater than 1.

3. Click the "Cancel" button at the top-right of the page.

4. Confirm (or cancel) the amendment cancellation on the pop-up dialog.

## Change EUCC states

The section covers the toggling between WIP, QA, and Production EUCC states. For detailed meaning of these and other states, see [EUCC States](../02-key-concepts.md#end-user-cc-states). The current user has to be the owner of the EUCC to toggle between these three states. To change the EUCC state:

1. Go to the "Core Component" page by clicking the "View/Edit Core Component" menu item under the "Core Component" menu. Find one or more EUCCs to change the state. See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) for help in locating a EUCC. Individual EUCC management sections contain additional tips to search for EUCCs.

2. There are two ways to change state of a EUCC:

    1. Change the state of one or more EUCCs simultaneously.

        1. Check the checkbox in front of one or more CCs that are in the same state and owned by the current end user.

        2. Arrow icons are displayed at the top-right corner of the page. Depending on the current state of the selected EUCC; left, right, or both arrow icons are displayed.

        3. Click the left arrow icon to retract the state or click the right arrow icon to advance the state.

    2. Change the state of EUCC individually:

        1. Open the "Core Component detail" page by clicking the DEN of the EUCC in WIP or QA, that is owned the current end user.

        2. Depending on the current state of the EUCC, click either the "Move to QA", "Move to Production", or "Back to WIP" (from QA) button at the top-right corner of the page.

    3. Confirm (or cancel) the state change in the pop-up dialog.

## Transfer ownership of an EUCC

To let another end user, make changes to an EUCC, the current owner has to transfer the ownership of the EUCC to another end user. EUCC can be transferred only to another end user. To transfer ownership:

1. Go to the "Core Component" page by clicking the "View/Edit Core Component" menu item under the "Core Component" menu. Find one or more EUCCs to transfer ownership. See [Search and Browse CC Library](../03-search-and-browse-cc-library.md) for help in locating a EUCC. Individual EUCC management sections contain additional tips to search for EUCCs.

2. There are two ways to transfer ownership of an EUCC:

    1. Transfer ownership of one or more EUCCs simultaneously.

        1. Check the checkbox in front of one or more EUCCs that are owned by the current end user and are in the WIP state.

        2. The double-arrow icon is displayed at the top-right corner of the page.

        3. Click the double-arrow icon.

        4. A dialog is displayed to select an end user to transfer the ownership to. Use the filter on the top to find the desired end user and check the checkbox in front of it.

        5. Click the "Transfer" button.

    2. Transfer ownership of a EUCC individually.

        1. On the "Core Component" page, when a EUCC is in the WIP state and is owned by the current end user, there is a double-arrow icon next to the username of the owner.

        2. Click the double-arrow icon.

        3. A dialog is displayed to select an end user. Use the filter on the top to find the desired end user and check the checkbox in front of it.

        4. Click the "Transfer" button.

## View Change History of an EUCC

See [View Change History of a CC](../developer/07-common-developer-cc-management-functions.md#view-change-history-of-a-cc).
