BIE Content
-----------

While BIEs are mainly created by connectCenter end users, they may also be created by connectCenter developers. connectCenter considers these entities as BIE related content (or BIE content for short) – the BIE itself, Context Category, Context Scheme, Business Context, End User Code List, and End User Core Component.

As the name implied, End User Code List and End User Core Component are those created and owned by the end user.
There are also Developer Code List and Developer Core Component – see `Developer vs. End User Core Components <#developer-vs-end-user-core-components>`__.
Developer Core Components (that includes Developer Code Lists) have a different set of states than End User Core Components and End User Code List (parallel to the Developer Core Components (DCC), let us also include End User Code Lists when referring to the End User Core Components (EUCC)).
Please review the `End user CC states <#end-user-cc-states>`__ section.
Since BIEs are similar to EUCC in that they are already tied to a specific DCC release and BIEs and EUCCs are used together in `BIE Extension <#extend-a-bie>`__, BIEs have a similar set of states as EUCCs.
Details of BIE states and user access right are described in `BIE States <#bie-states>`__.

Since DCCs are standard CCs, EUCCs can use, i.e., can make up of not only EUCCs but also DCCs.
On the contrary, DCCs cannot use any EUCC.
Similarly, as a BIE may belong to an end user or belong to a developer.
An end user BIE may reuse a developer BIE but not vice versa.

On the other hand, Context Category, Context Scheme, and Business Context do not have boundary between end user ones and developer ones (although this may change in the future).

Manage End User Code Lists
--------------------------

End users can access code list management to create code lists to be used for restricting fields in an End User Core Component and BIE.
Code list management has its own page and it can be accessed under the Core Component menu.
**End-user code lists can be managed when a published release branch is selected in the Branch filter.**

There are two ways to create a code list.
The first way is to base it on (i.e., derive it from) another developer code list, where both restrictions and extensions are allowed (an end-user code list cannot be based on another end-user code list).
This is important because if a Core Component (connectSpec Model) already uses a specific code list, such as a Language Code, **only a code list which is derived from the specified code list, in this case, the Language Code, can be used in the BIE restriction**.

The other way is to create a brand-new code list.
Such a code list can be used in a BIE whose based CC is typed to the generic code type.
Most connectSpec CC fields use the generic code type.

.. _find-a-code-list-1:

Find a code list
~~~~~~~~~~~~~~~~

Open the "Code List" page by choosing "View/Edit Code List" from either the "Core Component" menu or the "BIE" menu.
Make sure that the desired published release branch is selected in the *Branch* filter.
Then use the search field and advanced search filters to locate the desired code list.

.. _view-detail-of-a-code-list-1:

View detail of a code list
~~~~~~~~~~~~~~~~~~~~~~~~~~

`Find a code list <#find-a-code-list-1>`__.
Click the code list name in the table to open its detail page.

Create a brand-new code list
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Open the "Code List" page from either the "Core Component" menu or the "BIE" menu.
Make sure that the desired published release branch is selected.
Click the "New Code List" button near the top-right corner of the page.

.. _edit-detail-of-a-brand-new-code-list-1:

Edit detail of a brand-new code list
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

`Open the code list detail <#view-detail-of-a-code-list-1>`__ of a code list in the WIP state and that is owned by the current end user.
The fields in the detail pane may be updated as follows.

1.  The following fields accept free-form texts: *Name*, *List ID*,
    *Version*, *Definition*, *Remark,* and *Definition Source*. *Remark*, in particular,
    exists only in the end-user code list. It may be used to capture
    notes during development that is not wanted (to be published) in the
    *Definition*, e.g., "Need to go over this with the team". It is
    important to note that the system will validate the combination of
    *List ID*, *Agency ID*, and *Version* to be unique in the release
    branch, to which this code list belongs.

2.  *Namespace*. Select a non-standard namespace from the dropdown list.
    If the dropdown is empty, a `non-standard namespace needs to be
    created <#create-a-namespace>`__ first.

3.  *Agency ID List* and *Agency ID*. These two fields represent an
    organization that owns and manages the code list. Select from the
    dropdown an agency ID list that can be one created by an end user or
    by a developer (an end user agency ID list or a developer agency ID
    list) and then select the agency ID list value. The end user agency
    ID lists can be in any state while developer agency ID list are in
    Published state. All of them must belong to the same release with
    the code list being edited.

4.  *Version*. This field is a freeform text representing the version of
    the code list. The system will validate the combination of List ID,
    Agency ID, and Version is unique in the (working) branch data.
    Version is required.

5.  *Definition*. Specify the description of the code list. Definition is
    optional, but a warning is given if none is specified.

6.  *Definition Source*. Specify the source of the definition. This is
    typically a URI, but the field is free form text. Definition Source
    is optional.

7.  *Deprecated*. The Deprecated checkbox is only applicable when the code
    list revision is higher than 1. Therefore, the field is locked.

8.  Click the "Update" button at the top right to save changes.

9.  The end user may also want to perform these other actions on the
    code list:

    1. `Add a brand-new code list value to the code
       list <#add-a-brand-new-code-list-value-to-the-code-list-1>`__

    2. `Remove a brand-new code list value from the code
       list <#remove-a-brand-new-code-list-value-from-the-code-list-1>`__

    3. `Edit detail of a brand-new code list
       value <#edit-detail-of-a-brand-new-code-list-value-1>`__

    4. `Change a code list state <#change-a-code-list-state-1>`__

    5. `Transfer ownership of a code
       list <#transfer-ownership-of-a-code-list-1>`__

Create a Code List Based on Another
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This function allows a code list to be extended and restricted based on a published developer code list.
Only end users are allowed to create a code list based on another.
Creating such a code list makes the code list available for value domain restriction in the BIE, when the CC from which the BIE is derived uses the based code list.
For example, if a "Language Code" CC field has been assigned the "oacl_LanguageCode" code list, the user can only restrict the "Language Code" BIE to one of the code lists derived from the "oacl_LanguageCode "code list.
See the third table in `Restrict a BIE <#restrict-a-bie>`__.
To create a code list based on another developer code list:

1. `Open the detail page of <#view-detail-of-a-code-list-1>`__ the
   desired developer Code list to be used as a base.

2. Click the "Derive Code List based on this" button at the top-right of
   the page.

3. A new, derived code list is created. All the code list values from
   the base code list are copied to the derived code list. These values
   are considered as brand-new values added by the user and can be
   changed according to `Edit detail of a brand-new code list
   value <#edit-detail-of-a-brand-new-code-list-value-1>`__. They can be
   also removed according to `Remove a brand-new code list value from
   the code
   list <#remove-a-brand-new-code-list-value-from-the-code-list-1>`__.

Edit detail of a Code List derived from another
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This section describes code list editing when the code list is derived from another code list.
See `View detail of a code list <#view-detail-of-a-code-list-1>`__ to open the "Edit Code list" page.
The code list has to be in the WIP state and owned by the current user to be editable.
The fields in the detail pane may be updated as follows.

1. *Based Code List*. The name of the code list that the current code
   list is derived from. This field is locked and cannot be changed.

2. *Name*. Name of the code list. By default, the name is the same as
   the previous field and it is recommended to be changed. The value
   should be space separated set of words. Acronyms and plural words
   should be avoided. *Name* is required.

3. Change the textual content of any of the following fields: List ID,
   Version, Namespace, Definition, Definition Source and Remark. See
   `Edit detail of a brand-new code list <#edit-detail-of-a-brand-new-code-list-1>`__.

4. Select an Agency ID and an Agency ID List value in the corresponding
   fields. See `Edit detail of a brand-new code
   list <#edit-detail-of-a-brand-new-code-list-1>`__ for more information about these fields.

5. The Deprecated checkboxes are unchecked and locked. See `Edit detail
   of a brand-new code list <#edit-detail-of-a-brand-new-code-list-1>`__.

6. Click the "Update" button at the top right to save changes.

7. It should be noted that a code list value derived from another code
   list cannot be edited. Apart from that, the end user may perform
   these other actions on the code list:

   1. `Add a brand-new code list value to the code
      list <#add-a-brand-new-code-list-value-to-the-code-list-1>`__.

   2. `Remove a brand-new code list value from the code
      list <#remove-a-brand-new-code-list-value-from-the-code-list-1>`__.

   3. `Edit detail of a brand-new code list
      value <#edit-detail-of-a-brand-new-code-list-value-1>`__.

   4. `Change a code list state <#change-a-code-list-state-1>`__.

   5. `Transfer ownership of a code
      list <#transfer-ownership-of-a-code-list-1>`__.

.. _add-a-brand-new-code-list-value-to-the-code-list-1:

Add a brand-new code list value to the code list
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Add a brand-new Agency ID List value to the Agency ID List <#add-a-brand-new-agency-id-list-value-to-the-agency-id-list-1>`__.

.. _remove-a-brand-new-code-list-value-from-the-code-list-1:

Remove a brand-new code list value from the code list
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Remove a brand-new Agency ID List value from the Agency ID List <#remove-a-brand-new-agency-id-list-value-from-the-agency-id-list-1>`__.

.. _edit-detail-of-a-brand-new-code-list-value-1:

Edit detail of a brand-new code list value
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

See `Edit detail of a brand-new Agency ID List value <#edit-detail-of-a-brand-new-agency-id-list-value-1>`__.

Amend a code list
~~~~~~~~~~~~~~~~~

An end user code list in the Production state can be amended.
The current user does not have to be the owner of the code list.
To amend a code list:

1. Make sure you are on a published release branch. `Open the Edit Code
   List page <#view-detail-of-a-code-list-1>`__ of a code list in the
   Production state.

2. Click the "Amend" button at the top-right corner of the page.

3. The "Edit Code List" page is refreshed with the code list whose
   revision number is incremented by 1.

4. `Detail of the code list can be updated including add/change code
   list values. <#edit-detail-of-a-code-list-during-its-amendment>`__

**Amend Vs. Revise**: ‘Revise’ is the term used with developer CCs. ‘Amend’ is the term used with end-user CC.
The difference is that there is a snapshot of every revision of a CC such that its whole hierarchical structure can be retrieved.
That is not the case for an amendment.
Even though there are history records for both cases, history records only capture changes local to the CC and cannot be used to reconstruct the hierarchical snapshot of a CC.

Edit detail of a code list during its amendment
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This section describes code list editing when its revision number is 2 or more.

1. Open the "Edit Code List" detail page according to `View detail of a
   code list <#view-detail-of-a-code-list-1>`__. The code list has to be
   in the WIP state, and the current user has to be the owner to be
   editable. The fields in the detail pane may be updated as follows:

   1. Only some fields can be edited according to `Edit detail of a
      revised Agency ID List <#edit-detail-of-an-agency-id-list-during-its-amendment>`__.

   2. *Remark* can also be edited. It is an optional free text form
      field used for providing comments about the code list.

2. Click the "Update" button at the top-right of the page to save
   changes.

3. The end user may also want to perform these other actions on the code
   list:

   1. `Add a brand-new code list value to the code
      list <#add-a-brand-new-code-list-value-to-the-code-list-1>`__.

   2. `Remove a brand-new code list value from the code
      list <#remove-a-brand-new-code-list-value-from-the-code-list-1>`__.

   3. `Edit detail of a brand-new code list
      value <#edit-detail-of-a-brand-new-code-list-value-1>`__.

   4. `Edit detail of code list value that existed before the
      amendment <#edit-detail-of-a-code-list-value-that-existed-before-the-amendment>`__.

   5. `Change a code list state <#change-a-code-list-state-1>`__.

   6. `Transfer ownership of a code
      list <#transfer-ownership-of-a-code-list-1>`__.

Edit detail of a code list value that existed before the amendment
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This section is about editing detail of a code list value that existed before an amendment.
The detail can be edited in the same way as `Revise detail of an Agency ID List value <#revise-detail-of-an-agency-id-list-value-1>`__.

.. _delete-a-brand-new-code-list-1:

Delete a brand-new code list
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A code list with revision #1 can be deleted.
Doing so will put the code list into the deleted state.
This signifies that the owner of the deleted code list no longer wants to use it.
This suggests that if the code list is used by another end user in another end user CC or BIE, he/she should consider using another code list.
It is recommended that the owner documents the reason for deletion in the Definition field before deleting.
Other end users (or the owner himself) can however restore the code list – see `Restore a deleted code list <#restore-a-deleted-code-list-1>`__.
To delete a code list:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find one or more Code Lists to
   delete. Make sure you are on a published release branch. See `Find a
   code list <#find-a-code-list-1>`__ for help in locating a code list.

2. There are two ways to delete a code list.

   1. Delete one or more code lists simultaneously.

      1. Check the checkbox in front of the code lists that are in the
         WIP state, have revision 1, and are owned by the current end
         user.

      2. The "Delete" button at top-right corner of the page is
         activated.

      3. Click the "Delete" button.

   2. Delete a code list individually.

      1. Click on the ellipsis in the last column of the code list
         entry. The code list must be in the WIP state, has revision 1,
         and is owned by the current end user. Click the "Delete" menu
         item in the pop-up menu.

      2. Alternatively, click the code list name to open its detail page
         and click the Delete button at the top-right corner of the
         page.

3. Confirm (or cancel) deletion on the pop-up dialog.

.. _restore-a-deleted-code-list-1:

Restore a deleted code list
~~~~~~~~~~~~~~~~~~~~~~~~~~~

To restore a deleted code list:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu or the "BIE" menu.

2. Make sure you are on a published release branch and `find a code
   list <#find-a-code-list-1>`__ that is in the Deleted state.

3. Click the code list name to open its detail page, or use the
   ellipsis menu in the list.

4. Click the "Restore" button or choose the "Restore" menu item, and
   then confirm the restoration.

Cancel a code list amendment
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The end user who is the owner of a code list being amended can cancel the amendment.
In this case, all changes to the code list are discarded.
Code list detail and its owner are rollbacked to the pre-revised state.
To cancel code list revision:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find a code list to restore
   from deletion. Make sure you are on a published release branch. See
   `Find a code list <#find-a-code-list-1>`__ for help in locating a code
   list.

2. Click on the name of the code list to open its detail page. The
   current user has to be the owner of the code list and the code list
   has to be the WIP state with revision number greater than 1.

3. Click the "Cancel" button at the top-right of the page.

4. Confirm (or cancel) the amendment cancellation.

.. _change-a-code-list-state-1:

Change a code list state
~~~~~~~~~~~~~~~~~~~~~~~~~

The section covers the toggling between WIP, QA, and Production states of the code list.
For detailed meaning of these and other states, see `End user CC states <#end-user-cc-states>`__.
The current user has to be the owner of the code list to toggle between these three states.
To change the code list state:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find a code list to work on.
   Make sure you are on a published release branch. See `Find a code
   list <#find-a-code-list-1>`__ for help in locating a code list.

2. Click on the name of the code list to work on.

3. Depending on the current state of the code list, click either the
   "Move to QA", "Move to Production", or "Back to WIP" button at the
   top-right corner of the page.

4. Confirm (or cancel) the state change.

.. _transfer-ownership-of-a-code-list-1:

Transfer ownership of a code list
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To let another end user make changes to a code list, the current owner has to transfer ownership of the code list to another end user.
End-user code lists can be transferred only to another end user.
To transfer ownership:

1. Go to the "Code List" page by clicking the "View/Edit Code List" menu
   item under the "Core Component" menu. Find a code list to transfer
   ownership. Make sure you are on a published release branch. See `Find
   a code list <#find-a-code-list-1>`__ for help in locating a code list.

2. There are two ways to transfer ownership of a code list.

   1. Click the transfer icon (icon with two opposite arrows) next to
      the owner of the code list, or

   2. Click on the ellipsis in the last column of the code list to
      transfer the ownership and select the "Transfer Ownership" menu
      item in the pop-up menu.

3. A dialog is displayed to select an end user. Use the filter on the
   top to find the desired end user and check the checkbox in front of
   it.

4. Click the "Transfer" button.

Uplift an end-user code list
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Like BIE, each end-user code list is assigned to a particular release.
This function allows an end-user code list to be transferred from an older release to a newer release.
Only end users have access to the uplift end-user code list function, developers do not.
To uplift an end-user code list:

1. Select "Uplift Code List" in the "BIE" menu at the top of the page.

2. On the returned "Uplift Code List" page:

   1. Select a source release in the *Source branch* dropdown. The
      latest release should not be selected. The code list belonging to
      the latest release cannot be uplifted.

   2. Choose the target release in the *Target branch* dropdown. The
      uplifted code list will be associated with this release.

   3. Choose a source code list from the listing table below. The list
      contains only end-user code lists in the source release selected
      in the first step. Use the pagination at the bottom or use the
      *Name* or other filters on the page to find the desired source
      code list (all filters on the Code List have the same meaning as
      those described in `How to Search and Filter for a Core
      Component <#how-to-search-and-filter-for-a-core-component>`__,
      except that some filters do not exist on the Code List page and
      the Code List page has Name instead of DEN). Optionally, click on
      the name of the code list in the *Name* column to see all details
      of the code list.

3. Click "Next".

4. The "Edit Code List" page of the uplifted code list is displayed
   where the user can make further changes. All information is carried
   from the source code list including the List ID. Their GUIDs are
   however different.

Note that an end user Code List may be an extension of a developer Code List with additional code list values.
The developer Code List, however, might have been revised and include these values in the new release.
In this case, the duplicate code list values will not be carried forward in the uplifted end user Code List A report is given for the duplicated values.
The user will want to verify whether the code list value added to the developer Code List is semantically the same as those exist a priori in the end user Code List.
