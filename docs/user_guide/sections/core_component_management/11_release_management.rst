Release Management
~~~~~~~~~~~~~~~~~~

Overview of release management process
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A release lifecycle consists of 3 stages including 1) Initialized, 2) Draft, 3) Published.
Only developers can manage releases.
Release does not have an owner; therefore, after a release has been created, any developer can manage the release.
Figure below shows the lifecycle state transitions.

.. image:: media/image8.png
   :alt: Diagram Description automatically generated
   :width: 5.79247in
   :height: 1.06265in

There may be multiple releases in the Initialized state.
This can help with release planning.
High-level scope of the release can be captured in the release note.

Only one release draft can be active at any point in time.
In other words, if there is a release draft in the system, another release draft cannot be created.
The release draft allows the standard development team/community to review the changes to the release and the release itself as a whole.
Once the review period is over, if some changes are needed the release draft has to be first canceled, and then all changes to CCs (recall that CCs also include developer code lists and agency identifier list) assigned to the draft can be updated again.
The release may move back and forth between the Initialized and Draft state until all comments are satisfactorily addressed.
Once that is the case, the release can be published, and it is moved to the Published state.
In the published state, the release information can no longer be changed.

A release in the Initialized state can be discarded.
In such case, the release is purged from the application and can never be restored back.

Find a release
^^^^^^^^^^^^^^

To find a release:

1. On the "Core Component" menu on the top, click the "View/Edit
   Release" menu item.

2. The "Release" page is open listing all the releases.

3. Use the pagination buttons on the bottom of the release table to find
   the desired release. Use the "Items per page" to display more or
   fewer releases per page. Alternatively, use the filters described
   below to search for the desired release. When multiple filters are
   specified, they are connected with the AND logical connector. When
   they are not specified, they are not considered.

   1. *Creator*. Select from the dropdown list the developer who created
      the release. The user can type in a few characters to narrow down
      the list.

   2. *Created start date* and *Created end date*. Specify the time
      period in which the release was created by using *Created end
      date* and/or *Created end date* fields.

   3. *Updated start date* and *Updated end date*. Specify the time
      period in which the release was updated by using the *Updated
      start date* and/or *Updated end date* fields.

   4. *Updater*. Select from the dropdown list the developer who last
      updated the release. The user can type in a few characters to
      narrow down the list.

   5. *Release Num*. Type in a free form text to do a string match with
      the release number. Note that despite its name, the release number
      is actually a free text field, not just a number.

   6. *State*. Select from the dropdown list the Initialized, Draft, or
      Published. Multiple states can be selected. If nothing is
      selected, all states are included, i.e., it is the same as
      selecting all of them.

Create a release
^^^^^^^^^^^^^^^^

To create a new release:

1. On the "Core Component" menu on the top, click the "View/Edit
   Release" menu item.

2. The "Release" page is open.

3. Click the "New Release" button at the top-right of the page.

4. The following fields can be filled in.

   1. *Release Number*. A freeform text representing the release number
      such as "10.0" or "10.0rc". Release number should be unique within
      the database, although the application does not enforce it.
      *Release Number* is required.

   2. *Release Namespace*. Select from the dropdown list a Standard
      Namespace. See also `Namespace
      Management <#core-component-management-tips-and-tricks>`__.
      Release Namespace is required.

   3. *Release Note*. A freeform text that describes or gives an
      overview of the release. The field is optional.

   4. *Release License*. A freeform text indicating the licensing of the
      CC release. The field is optional.

5. Click the "Create" button.

6. The "Release" page opens showing the created release in the
   Initialized state.

View detail of a release
^^^^^^^^^^^^^^^^^^^^^^^^

1. `Find a release <#find-a-release>`__.

2. Click on the release number to open the release detail page.

Edit detail of a release
^^^^^^^^^^^^^^^^^^^^^^^^

Detail of a release can be edited while it is in the Initialized or Draft state.
Any developer can edit the detail of a release.
He/she does not have to be the owner.
To change the detail of a release:

1. `Open the detail page of the release <#find-a-release>`__.

2. Fields of the release detail can be updated in the same way as when
   `the release was first created <#create-a-release>`__.

Create a release draft
^^^^^^^^^^^^^^^^^^^^^^

In the release draft creation process, CCs in the candidate state can be assigned to a release that is in the initialized state.
Any developer can create a release draft from a release in the Initialized state.
He/she does not have to be the owner.
To create a release draft:

1. `Open the detail page of a release <#find-a-release>`__ that is in
   the Initialized state and click the "Create Draft" button at the
   bottom of the page. Alternatively, open the "Release" page by
   clicking "View/Edit Release" under the "Core Component" menu. Then,
   click on ellipsis in the last column of the desired initialized
   release and select "Create Draft".

2. The "Release Assignment" page is open. The page has two panes left
   and right. The left, aka *Modified* pane, has all the CCs (recall
   that CCs also include code and agency identifier lists)
   created/changed in the *Working* branch. The right, aka *Assigned*
   pane, contains CCs assigned to the release and is initially empty.

3. Assign CCs to the release by selectively dragging CCs that are in the
   Candidate state from the left pane to the right pane. Alternatively,
   use the double-arrowhead icon (>>) on the top of the left pane to
   assign all CCs in the Candidate state. CCs on the right pane can also
   be dragged back to the left pane to unassign.

4. After all new and updated CCs planned for the release have been
   assigned,

   1. Click the "Validate" button at the bottom of the page. This step
      is optional. If there is any error such as when some CCs used by
      CCs assigned to the release are not included in the release, error
      messages are displayed. Warnings are also given for certain
      situations such as updated CCs that are not assigned to the
      release.

   2. Click the "Create" button at the bottom of the page. If the
      release assignment has not been validated, the system validates
      the assignment. If there is any error, draft is not created. If
      there is no error, the system starts the process to create the
      release draft.

5. The "Release" page is open showing that the release draft is being
   created – the State column is shown as Processing. If there are not
   hundreds of new or changed CCs, the release draft processing can
   finish in few seconds. The page is NOT automatically refreshed when
   the processing is done. The user has to refresh the page to see
   whether the processing is done. When that is the case, the State
   column will display *Draft*. At this stage, the user may go to the
   "Core Component" page and on the *Branch* filter at the top-left of
   the page select the release draft from the list to review all CCs in
   the release draft. The user may also want to notify other developers
   or end users that the release draft is ready for review.

Review a release draft
^^^^^^^^^^^^^^^^^^^^^^

Once a release draft has been created, users, both developers and end users, can review the release.
To do so:

1. Open the "Core Component" page by clicking the "Core Component" menu
   on the top and selecting "View/Edit Core Component".

2. In the *Branch* filter at the top-left of the page, select a branch
   with the *Release Draft* label and click the "Search" button under
   search filters.

3. CCs in the release draft are listed. There should be CCs in only two
   states, Release Draft and Published. The published ones are those CCs
   with no change from the previous release. The release draft ones are
   those CCs that are either new or revised in the release draft.

4. Use other filters as described in `Search and Browse CC
   Library <#search-and-browse-cc-library>`__.

5. Click on the DEN of a CC to open its detail page. See all `View
   Change History of a CC <#view-change-history-of-a-cc>`__.

6. If changes to any CCs in the release draft are needed, the release
   draft has to be cancelled first.

Discard a release
^^^^^^^^^^^^^^^^^

A release in the Initialized state can be discarded.
In such case, the release is purged from the application and can never be restored back.
To discard a release:

1. Open the "Release" page by clicking "View/Edit Release" under the
   Core Component menu on the top.

2. Find a release to be discarded.

3. Click on the ellipsis in the last column of the release to be
   discarded and select "Discard".

4. Confirm (or Cancel) the request on the confirmation dialog.

Cancel a release draft
^^^^^^^^^^^^^^^^^^^^^^

If changes to any CC in the release draft are needed, the release draft needs to be canceled first.
The cancellation frees CCs from the release and puts those ones with changes back to the Candidate state.
CCs that need modification can then be put back in the WIP state to make further changes.

1. Open the "Release" page by clicking "View/Edit Release" under the
   "Core Component" menu on the top.

2. `Find a release <#find-a-release>`__ that is the Release Draft state
   that is to be canceled.

3. Click on the ellipsis in the last column of the release draft and
   select "Move back to Initialized". Alternatively, click the release
   number to open the detail page and click the "Move back to
   Initialized" button.

4. A dialog appears to confirm or cancel the release draft cancellation.

Publish a release
^^^^^^^^^^^^^^^^^

After a release draft has been reviewed and accepted, it can be published.
Any developer can publish a release draft.
He/she does not have to be the owner.
No further changes can be made to the release after it is published.
To publish a release draft:

1. Open the "Release" page by clicking "View/Edit Release" under the
   "Core Component" menu at the top of the connectCenter page.

2. There are two ways to publish a release:

   1. If there is no need to update any release detail, the release can
      be published by clicking on the ellipsis in the last column of the
      release draft and selecting "Move to Published". Click "Update" in
      the confirmation dialog. It is important to note that release
      detail cannot be changed after the release is published.

   2. If there is a need to update some release detail:

      1. `Open the detail page of the release <#find-a-release>`__ that
         is in the Release Draft state.

      2. Release detail can be updated in the same way as when `the
         release was first created <#create-a-release>`__.

      3. Click the "Publish" button.

      4. Click "Update" in the confirmation dialog.

3. The "Release" page opens showing that the release is being published
   – the State column is shown as Processing. If there are not hundreds
   of new or changed CCs, the publish processing can finish in few
   seconds. The page is NOT automatically refreshed when the processing
   is done. The user has to refresh the page to see whether the
   processing is done. When that is the case the State column will
   display *Published*. At this stage, the user may go to the "Core
   Component" page and on the *Branch* filter at the top-left of the
   page select the release from the list to see all CCs in the published
   release.

Generate the migration script
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

After a new release has published, developers can download the migration script for the new release.
The script contains all developer records, including core components, code lists, agency ID lists, modules, etc. To download the script,

1. `Open the detail page of the new release <#find-a-release>`__.

2. Click the "Generate Migration Script" button.
