---
title: "Release Management"
sidebar_position: 12
---

## Overview of release management process

A release lifecycle consists of 3 stages including 1) Initialized, 2) Draft, 3) Published.
While a draft is being created or a release is being published, the release temporarily shows a transient "Processing" state in the "State" column.
Only developers can manage releases; publishing a release additionally requires a user with the Admin right.
A release does not have an owner; therefore, after a release has been created, any developer can manage the release.
The figure below shows the lifecycle state transitions.

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 570 125" width="570" height="125" class="uml-figure" role="img" aria-label="Release life-cycle state diagram: Initialized, Draft, and Published" font-size="12">
  <defs>
    <marker id="rel-arrow" viewBox="0 0 8 8" markerWidth="8" markerHeight="8" refX="7.5" refY="4" orient="auto">
      <path class="uml-arrowhead" d="M0.5,0.5 L7.5,4 L0.5,7.5 Z"/>
    </marker>
  </defs>

  <!-- initial state -->
  <circle class="uml-arrowhead" cx="22" cy="32" r="8"/>
  <path class="uml-edge" d="M30,32 H92" marker-end="url(#rel-arrow)"/>
  <text class="uml-label" x="61" y="27" text-anchor="middle" font-size="11">Create</text>

  <!-- Initialized -> Draft -->
  <path class="uml-edge" d="M185,32 H302" marker-end="url(#rel-arrow)"/>
  <text class="uml-label" x="243" y="27" text-anchor="middle" font-size="11">Create Release Draft</text>

  <!-- Draft -> Published -->
  <path class="uml-edge" d="M385,32 H475" marker-end="url(#rel-arrow)"/>
  <text class="uml-label" x="430" y="27" text-anchor="middle" font-size="11">Publish</text>

  <!-- Draft -> Initialized (cancel) -->
  <path class="uml-edge" d="M345,51 V95 H140 V54" marker-end="url(#rel-arrow)"/>
  <text class="uml-label" x="242" y="91" text-anchor="middle" font-size="11">Cancel Release Draft</text>

  <!-- Initialized -> final state (discard) -->
  <path class="uml-edge" d="M100,51 L31,103" marker-end="url(#rel-arrow)"/>
  <text class="uml-label" x="72" y="82" font-size="11">Discard</text>

  <!-- final state -->
  <circle class="uml-edge" cx="22" cy="110" r="8"/>
  <circle class="uml-arrowhead" cx="22" cy="110" r="4"/>

  <!-- states -->
  <rect x="95" y="14" width="90" height="37" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="140" y="36" text-anchor="middle" fill="#000">Initialized</text>
  <rect x="305" y="14" width="80" height="37" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="345" y="36" text-anchor="middle" fill="#000">Draft</text>
  <rect x="478" y="14" width="80" height="37" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="518" y="36" text-anchor="middle" fill="#000">Published</text>
</svg>

There may be multiple releases in the Initialized state.
This can help with release planning.
High-level scope of the release can be captured in the release note.

The intended workflow keeps at most one active release draft at any point in time.
The release draft allows the standard development team/community to review the changes to the release and the release itself as a whole.
Once the review period is over, if some changes are needed the release draft has to be first canceled, and then all changes to CCs (recall that CCs also include developer code lists and the agency ID list) assigned to the draft can be updated again.
The release may move back and forth between the Initialized and Draft state until all comments are satisfactorily addressed.
Once that is the case, the release can be published, and it is moved to the Published state.
In the published state, the release information can no longer be changed.

A release in the Initialized state can be discarded.
In such case, the release is purged from the application and can never be restored back.

## Find a release

To find a release:

1. On the "Core Component" menu on the top, click the "View/Edit Release" menu item.

2. The "Release" page is open listing the releases of the selected library (the internal Working branch is never shown here).

   ![Release page listing releases with the Release, State, Created on, and Updated on columns and the New Release button](/img/user-guide/release_page.png)

3. Use the pagination buttons on the bottom of the release table to find the desired release. Use the "Items per page" to display more or fewer releases per page. Alternatively, use the search bar and filters described below. When multiple filters are specified, they are connected with the AND logical connector. When they are not specified, they are not considered.

    1. The "Search by Release" bar matches the release number as a case-insensitive substring. Note that despite its name, the release number is actually a free text field, not just a number.

    2. "State". Select "Initialized", "Draft", or "Published" from the drop-down list. Multiple states can be selected. If nothing is selected, all states are included, i.e., it is the same as selecting all of them.

    3. "Creator". Select from the drop-down list the developer who created the release. The user can type in a few characters to narrow down the list.

    4. "Created start date" and "Created end date". Specify the time period in which the release was created.

    5. "Updater". Select from the drop-down list the developer who last updated the release. The user can type in a few characters to narrow down the list.

    6. "Updated start date" and "Updated end date". Specify the time period in which the release was updated.

    7. "Namespace". Filter the releases by their release namespace.

## Create a release

To create a new release:

1. On the "Core Component" menu on the top, click the "View/Edit Release" menu item.

2. The "Release" page is open.

3. Click the "New Release" button at the top-right of the page.
   The button is available to developers only.

4. The following fields can be filled in.

    1. "Release Number". A freeform text representing the release number such as "10.0" or "10.0rc". The release number must be unique within the library; a duplicate is rejected with a message such as "'10.0' already exists." "Release Number" is required.

    2. "Release Namespace". Select from the drop-down list a Standard namespace. See also [Namespace Management](./02-namespace-management.md). "Release Namespace" is required.

    3. "Release Note". A freeform text that describes or gives an overview of the release. The field is optional.

    4. "Release License". A freeform text indicating the licensing of the CC release. The field is optional.

   ![Create release page with the Release Number, Release Namespace, and Release Note fields filled in and an empty Release License field](/img/user-guide/release_create.png)

5. Click the "Create" button.

6. The "Release" page opens showing the created release in the Initialized state.

## View detail of a release

1. [Find a release](#find-a-release).

2. Click on the release number to open the release detail page.

## Edit detail of a release

Detail of a release can be edited while it is in the Initialized or Draft state.
Any developer can edit the detail of a release.
To change the detail of a release:

1. [Open the detail page of the release](#view-detail-of-a-release).

2. Fields of the release detail can be updated in the same way as when [the release was first created](#create-a-release).

3. Click the "Update" button.

## Create a release draft

In the release draft creation process, CCs in the Candidate state can be assigned to a release that is in the Initialized state.
Any developer can create a release draft from a release in the Initialized state.
To create a release draft:

1. [Open the detail page of a release](#view-detail-of-a-release) that is in the Initialized state and click the "Create Draft" button. Alternatively, open the "Release" page by clicking "View/Edit Release" under the "Core Component" menu; then, click on the ellipsis in the last column of the desired initialized release and select "Create Draft".

2. The "Release Assignment" page is open. The page has two panes left and right. The left, aka "Modified" pane, has all the CCs (recall that CCs also include code lists and the agency ID list) created/changed in the Working branch. The right, aka "Assigned" pane, contains CCs assigned to the release and is initially empty.

3. Assign CCs to the release by selectively dragging CCs that are in the Candidate state from the left pane to the right pane. Alternatively, use the double-arrowhead icon (>>) on the top of the left pane to assign all CCs in the Candidate state. CCs on the right pane can also be dragged back to the left pane to unassign.

4. After all new and updated CCs planned for the release have been assigned,

    1. Click the "Validate" button at the bottom of the page. This step is optional. If there is any error such as when some CCs used by CCs assigned to the release are not included in the release, error messages are displayed. Warnings are also given for certain situations such as updated CCs that are not assigned to the release.

    2. Click the "Create" button at the bottom of the page and confirm in the "Create Release Draft?" dialog. If the release assignment has not been validated, the system validates the assignment. If there is any error, the draft is not created. If there is no error, the system starts the process to create the release draft.

5. The "Release" page is open showing that the release draft is being created – the "State" column shows "Processing". If there are not hundreds of new or changed CCs, the release draft processing can finish in a few seconds. The page is NOT automatically refreshed when the processing is done. The user has to refresh the page to see whether the processing is done. When that is the case, the "State" column will display "Draft". At this stage, the user may go to the "Core Component" page and on the "Branch" selector at the top-left of the page select the release draft from the list to review all CCs in the release draft. The user may also want to notify other developers or end users that the release draft is ready for review.

## Review a release draft

Once a release draft has been created, users, both developers and end users, can review the release.
To do so:

1. Open the "Core Component" page by clicking the "Core Component" menu on the top and selecting "View/Edit Core Component".

2. In the "Branch" selector at the top-left of the page, select the branch of the release draft (the entries show the release numbers).

3. CCs in the release draft are listed. There should be CCs in only two states, Release Draft and Published. The published ones are those CCs with no change from the previous release. The release draft ones are those CCs that are either new or revised in the release draft.

4. Use other filters as described in [Search and Browse CC Library](../03-search-and-browse-cc-library.md).

5. Click on the DEN of a CC to open its detail page. See also [View Change History of a CC](./07-common-developer-cc-management-functions.md#view-change-history-of-a-cc).

6. If changes to any CCs in the release draft are needed, the release draft has to be cancelled first.

:::tip
The release detail page of a Draft or Published release also offers a "What's changed" button, which lists the new and revised components of the release.
:::

## Discard a release

A release in the Initialized state can be discarded.
In such case, the release is purged from the application and can never be restored back.
To discard a release:

1. Open the "Release" page by clicking "View/Edit Release" under the "Core Component" menu on the top.

2. Find a release to be discarded.

3. Click on the ellipsis in the last column of the release to be discarded and select "Discard".

4. Confirm (or cancel) the request on the confirmation dialog.

## Cancel a release draft

If changes to any CC in the release draft are needed, the release draft needs to be canceled first.
The cancellation frees CCs from the release and puts those ones with changes back to the Candidate state.
CCs that need modification can then be put back in the WIP state to make further changes.

1. Open the "Release" page by clicking "View/Edit Release" under the "Core Component" menu on the top.

2. [Find a release](#find-a-release) that is in the Draft state that is to be canceled.

3. Click on the ellipsis in the last column of the release draft and select "Move back to Initialized". Alternatively, click the release number to open the detail page and click the "Back to Initialized" button.

4. A dialog appears to confirm or cancel the release draft cancellation.

## Publish a release

After a release draft has been reviewed and accepted, it can be published.
Publishing requires a user with the Admin right; a non-admin user is rejected with the message "Only administrators can publish the release."
No further changes can be made to the release after it is published.
To publish a release draft:

1. Open the "Release" page by clicking "View/Edit Release" under the "Core Component" menu at the top of the connectCenter page.

2. There are two ways to publish a release:

    1. If there is no need to update any release detail, the release can be published by clicking on the ellipsis in the last column of the release draft and selecting "Move to Published". Confirm in the dialog. It is important to note that release detail cannot be changed after the release is published.

    2. If there is a need to update some release detail:

        1. [Open the detail page of the release](#view-detail-of-a-release) that is in the Draft state.

        2. Release detail can be updated in the same way as when [the release was first created](#create-a-release).

        3. Click the "Publish" button.

        4. Confirm in the dialog.

3. The "Release" page opens showing that the release is being published – the "State" column shows "Processing". If there are not hundreds of new or changed CCs, the publish processing can finish in a few seconds. The page is NOT automatically refreshed when the processing is done. The user has to refresh the page to see whether the processing is done. When that is the case the "State" column will display "Published". At this stage, the user may go to the "Core Component" page and on the "Branch" selector at the top-left of the page select the release from the list to see all CCs in the published release.

## Generate the migration script

After a new release has been published, developers can download the migration script for the new release.
The script contains all developer records, including core components, code lists, agency ID lists, modules, etc.
The button appears only on the latest published release.
To download the script:

1. [Open the detail page of the latest published release](#view-detail-of-a-release).

2. If you also want the developer-defined sibling ordering (see [Order sibling components in the tree](../03-search-and-browse-cc-library.md#order-sibling-components-in-the-tree)) to be reproduced when the script is applied, check the "Include sibling view order" checkbox next to the button.

3. Click the "Generate Migration Script" button.
   A ZIP file named after the release number is downloaded.
