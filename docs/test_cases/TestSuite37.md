# Test Suite 37

**Release Branch Agency ID List Management for End User**

> Generally, the end user can add/edit an end user Agency ID list with base and without base.

## Test Case 37.1

**Agency ID access**

Pre-condition: A release branch is selected.


### Test Assertion:

#### Test Assertion #37.1.1
The end user can see in the View/Edit Agency ID List page all Agency ID lists owned by any end user in any state. There must also be all developer Agency Id lists in Published state. There must not be any Agency ID list in Draft, Candidate, or Release Draft state.

#### Test Assertion #37.1.2
The end user can view and edit the details and Agency ID List values of an end user Agency ID list that is in WIP state and owned by him.

#### Test Assertion #37.1.3
The end user CAN view but CANNOT edit the details of an Agency ID list that is in WIP state and owned by another end user. He can however add comments.

#### Test Assertion #37.1.4
The end user can view the details of an Agency ID list that is in QA or Production state and owned by another end user but he cannot make any change except adding comments.

#### Test Assertion #37.1.5
The end user can view details of any developer Agency ID list in the selected release branch. The Agency ID list must always be in the Published state. He cannot make any change. He can add comments.

#### Test Assertion #37.1.6
Developer users CAN view but CANNOT edit end user’s agency ID lists in any state.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A release branch is selected.
2. The users, branches, releases, and records needed to exercise "Agency ID access" are available in connectCenter.

### Test Step:
1. Sign in as an end user and open **Core Component > View/Edit Agency ID List** with the target release branch selected.
2. Verify that end user Agency ID Lists in the covered states are listed, that published developer Agency ID Lists are also listed, and that no Draft, Candidate, or Release Draft Agency ID Lists appear in the release branch. (Assertion [#37.1.1](#test-assertion-3711))
3. Open an Agency ID List owned by the current end user in `WIP` state and verify details and values are editable. (Assertion [#37.1.2](#test-assertion-3712))
4. Open Agency ID Lists owned by another end user in `WIP`, `QA`, and `Production` states and verify they are view-only while comments remain available. (Assertions [#37.1.3](#test-assertion-3713), [#37.1.4](#test-assertion-3714))
5. Open a published developer Agency ID List in the selected release and verify it is view-only while comments remain available. (Assertion [#37.1.5](#test-assertion-3715))
6. Sign in as a developer and verify end user Agency ID Lists remain view-only in this release-branch scenario. (Assertion [#37.1.6](#test-assertion-3716))
## Test Case 37.2

**Creating a brand-new end user Agency ID list**

Pre-condition: A release branch is selected.


### Test Assertion:

#### Test Assertion #37.2.1
On the Agency ID list View/Edit page where a release branch is selected, the end user can create a brand-new Agency ID list without base with only required information, See Create a Brand new Agency ID list in connectCenter User Guide for Mandatory/Optional fields. The following are default values – Based Code List = Null (this field can be hidden in case of an Agency ID list without base) and cannot be changed; Name = “AgencyIdentification”; List ID = Randomly Generated GUID; Agency ID =blank ; Version = blank; Definition = blank; Deprecated = false (and locked); Namespace = null; Comments = empty. It must not appear in any other branch. It has a revision number 1.

#### Test Assertion #37.2.2
The end user can create an Agency ID list without base with all information specified and multiple Agency ID list values. In addition, Agency ID list value cannot be duplicated.

#### Test Assertion #37.2.3
The end user can discard an Agency ID list value during the Agency ID list without base creation.

#### Test Assertion #37.2.4
The end user cannot create an Agency ID list without base, when it does not meet a uniqueness constraint (i.e., no other Agency ID list with same List ID, Agency ID and Version should exist in the selected branch). See Create a Brand New Agency ID List in connectCenter User Guide for the uniqueness constraint.

#### Test Assertion #37.2.5
The end user can create a brand-new Agency ID list based on another published developer Agency ID list. The developer Agency ID list must be in the same release as the selected release branch.

#### Test Assertion #37.2.6
The end user CANNOT create a brand-new Agency ID list based on another end-user code list.

#### Test Assertion #37.2.7
There is a developer Agency ID list that has different revisions in two releases, one of which is the release currently selected by the end user and is a later release. The end user must not be able to create a brand-new Agency ID list based on the earlier revision of the developer Agency ID list.

#### Test Assertion #37.2.8
The value in the Agency ID field must be one of the IDs in the list.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A release branch is selected.
2. The users, branches, releases, and records needed to exercise "Creating a brand-new end user Agency ID list" are available in connectCenter.

### Test Step:
1. Sign in as an end user and open **Core Component > View/Edit Agency ID List** with the target release branch selected.
2. Create a brand-new Agency ID List without a base using only required information and verify the documented defaults, branch scoping, and revision `1`. (Assertion [#37.2.1](#test-assertion-3721))
3. Create a brand-new Agency ID List without a base using full information and multiple values, verify duplicate values are rejected, and discard a temporary value during creation. (Assertions [#37.2.2](#test-assertion-3722), [#37.2.3](#test-assertion-3723))
4. Verify uniqueness-constraint rejection for duplicate List ID, Agency ID, and Version combinations, then create a new Agency ID List based on a published developer Agency ID List in the selected release. (Assertions [#37.2.4](#test-assertion-3724), [#37.2.5](#test-assertion-3725))
5. Verify creation is not allowed when the selected base is an end user Agency ID List or an earlier developer revision from another release. (Assertions [#37.2.6](#test-assertion-3726), [#37.2.7](#test-assertion-3727))
6. Verify the Agency ID field can only use IDs that exist in the current Agency ID List. (Assertion [#37.2.8](#test-assertion-3728))
## Test Case 37.3

**Editing a brand-new end user Agency ID list**

Pre-condition: The brand-new Agency ID list is created by the end user and is in the WIP state. The end user accesses these functionalities by opening the brand-new Agency ID list from the Agency ID list View/Edit page on a particular release branch or after creating a brand-new end user Agency ID list.


### Test Assertion:

#### Test Assertion #37.3.1
The end user can change the properties of the Agency ID list and save changes with the following business rules.

##### Test Assertion #37.3.1.a
The List ID, Agency ID, and Version have to be unique in the branch.
##### Test Assertion #37.3.1.b
Based Agency ID list may be null or has a value but must be locked (because a based Agency ID list, if there is, would already be selected at this Agency ID list creation).
##### Test Assertion #37.3.1.c
Name, List ID, Agency ID, Version and Namespace are mandatory. Deprecated is also mandatory but must be false (and locked). Namespace must be a non-standard namespace.
##### Test Assertion #37.3.1.d
A warning should be given when the Definition is empty.

#### Test Assertion #37.3.2
The end user can add an Agency ID list Value. The Agency ID list Value shall have the following field - Value, Meaning, Definition, Definition Source and Deprecated. The Value field has to be unique within the Agency ID list and its based Agency ID list. Deprecated field shall be false and locked.

#### Test Assertion #37.3.3
For an Agency ID list with base, the end user can discard derived Agency ID list values and save.

#### Test Assertion #37.3.4
For an Agency ID list with base, the end user can change existing Agency ID list values and their details.

#### Test Assertion #37.3.5
For new Agency ID list Value, only the Value and Meaning are required.

#### Test Assertion #37.3.6
A newly added Agency ID list Value can be discarded. Application shall check for any references before discarding it, e.g., when it is being referenced as a replacement of a deprecated value, or when it is referenced in code list (this likely shouldn’t happen b/c code list agency ID field also has a logic to prevent this).

#### Test Assertion #37.3.7
The end user can edit a newly added Agency ID list Value details except the Deprecated field with and without changing the Value field itself.

#### Test Assertion #37.3.8
In the Agency ID field, the end user can select a value from the Agency IDs in the current list.

##### Test Assertion #37.3.8.a
When a new Agency ID is added to the list, the end user must be able to use that ID in the Agency ID field.
##### Test Assertion #37.3.8.b
When the end user tries to discard an Agency ID in the list and it is used in the Agency ID field, the application must prevent that from happening. I.e., show a dialog box indicating that the Agency ID is used in the Agency ID field and cannot be deleted.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The brand-new Agency ID list is created by the end user and is in the WIP state. The end user accesses these functionalities by opening the brand-new Agency ID list from the Agency ID list View/Edit page on a particular release branch or after creating a brand-new end user Agency ID list.
2. The users, branches, releases, and records needed to exercise "Editing a brand-new end user Agency ID list" are available in connectCenter.

### Test Step:
1. Sign in as an end user and open the brand-new `WIP` Agency ID List created in the selected release branch.
2. Update Agency ID List header fields and verify uniqueness rules, based-list locking, required fields, namespace restrictions, and the empty-definition warning. (Assertions [#37.3.1.a](#test-assertion-3731a), [#37.3.1.b](#test-assertion-3731b), [#37.3.1.c](#test-assertion-3731c), [#37.3.1.d](#test-assertion-3731d))
3. Add Agency ID List values and verify required fields, uniqueness within the current and based Agency ID Lists, and locked `Deprecated` behavior for newly added values. (Assertions [#37.3.2](#test-assertion-3732), [#37.3.5](#test-assertion-3735))
4. For a based Agency ID List, discard derived values and edit inherited or existing values as allowed by the current implementation. (Assertions [#37.3.3](#test-assertion-3733), [#37.3.4](#test-assertion-3734))
5. Edit and discard newly added Agency ID List values and verify reference checks prevent removing an in-use Agency ID value. (Assertions [#37.3.6](#test-assertion-3736), [#37.3.7](#test-assertion-3737))
6. Verify the Agency ID field can select IDs from the current list, including a newly added ID, and blocks discarding an ID that is currently in use there. (Assertions [#37.3.8.a](#test-assertion-3738a), [#37.3.8.b](#test-assertion-3738b))
## Test Case 37.4

**Amend an end user Agency ID list**

Pre-condition: The end user has selected a particular release branch.


### Test Assertion:

#### Test Assertion #37.4.1
On the Agency ID list Detail page of an end user Agency ID list in Production state, the end user can amend the Agency ID list regardless of the current owner. The result is that the release branch has that Agency ID list is in the WIP state and the revision number incremented by 1.  Its detail attributes are initially the same as those of the previous revision. All the Agency ID list Values from the previous revisions shall be present.

#### Test Assertion #37.4.2
The end user cannot amend a developer Agency ID list in the release branch.

#### Test Assertion #37.4.3
The end user can change the properties of the Agency ID list and save changes with the following business rules.

##### Test Assertion #37.4.3.a
If the Deprecated was already True in the previous revision, the field along with the Replaced By field should be locked. If it was False before the amendment the checkbox shall be enabled. When Deprecated is changed to True, the end user must be able to select a replacement Agency ID list that is not already deprecated from a drop-down list in the Replaced By field – but the field is optional. There can be only one replacement Agency ID list. When the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.
##### Test Assertion #37.4.3.b
Based Agency ID list, Namespace, Name, List ID, and Agency ID cannot be changed. The Version field is initially set to pre-amendment + “New” (same as developer Agency ID list behavior), but the user can change it.
##### Test Assertion #37.4.3.c
Version, Definition, Definition Source and Remark can be changed.

#### Test Assertion #37.4.4
Existing Agency ID list Values from the previous revisions and that are not inherited from base cannot be removed and only their Meaning, and Definition field can be changed. If the Deprecated was already True in the previous revision, the field along with the Replaced By field should be locked. If Deprecated was false in the previous revision, the deprecated field can be changed from false to true. When it is changed to true, the end user can select ONE replacement Agency ID list value from the Agency ID list. When the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.

#### Test Assertion #37.4.5
Agency ID list Values from the previous revisions and that are inherited from base cannot be removed and only their Meaning, and Definition field can be changed. If the Deprecated was already True in the previous revision, the field along with the Replaced By field should be locked. If Deprecated was false in the previous revision, the deprecated field can be changed from false to true. When it is changed to true, the end user can select ONE replacement Agency ID list value from the Agency ID list. When the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.

#### Test Assertion #37.4.6
A new Agency ID list Value can be added and all of its details can be edited.

#### Test Assertion #37.4.7
A brand-new Agency ID list value added in this revision can be discarded, if it is not a replacement of a deprecated Agency ID list value.

#### Test Assertion #37.4.8
The end user can cancel the amendment. In this case, the system rollbacks the whole Agency ID list details and children Agency ID list Values to the previous revision.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The end user has selected a particular release branch.
2. The users, branches, releases, and records needed to exercise "Amend an end user Agency ID list" are available in connectCenter.

### Test Step:
1. Sign in as an end user, open a `Production` end user Agency ID List in the selected release branch, and amend it.
2. Verify amendment creates a new `WIP` revision with incremented revision number and copied details and values. (Assertion [#37.4.1](#test-assertion-3741))
3. Verify a developer Agency ID List in the release branch cannot be amended by the end user. (Assertion [#37.4.2](#test-assertion-3742))
4. Edit amended Agency ID List header fields and verify deprecation and replacement rules, locked fields, and editable Version, Definition, Definition Source, and Remark fields. (Assertions [#37.4.3.a](#test-assertion-3743a), [#37.4.3.b](#test-assertion-3743b), [#37.4.3.c](#test-assertion-3743c))
5. Edit existing inherited and non-inherited Agency ID List values and verify removal, deprecation, and replacement rules. (Assertions [#37.4.4](#test-assertion-3744), [#37.4.5](#test-assertion-3745))
6. Add and discard a brand-new Agency ID List value and verify discard is prevented when the value is referenced as a replacement. (Assertions [#37.4.6](#test-assertion-3746), [#37.4.7](#test-assertion-3747))
7. Cancel the amendment and verify the Agency ID List and its values roll back to the previous revision. (Assertion [#37.4.8](#test-assertion-3748))
## Test Case 37.5

**End user Agency ID list state management**

> All these state changes need a confirmation dialog box “Do you want to change state of the Agency ID list to XYZ?”.

Pre-condition: The end user is on the Agency ID list detail page, which he owns.


### Test Assertion:

#### Test Assertion #37.5.1
If there is no unsaved changes to the Agency ID list, the end user cannot change the state.

#### Test Assertion #37.5.2
Once changes to Agency ID list details or Agency ID list Value have been saved,

##### Test Assertion #37.5.2.a
The end user can change the Agency ID list state from WIP to QA.
##### Test Assertion #37.5.2.b
The end user can change the Agency ID list state from QA back to WIP.
##### Test Assertion #37.5.2.c
The end user can change the Agency ID list state from QA to Production.
##### Test Assertion #37.5.2.d
No state change can be in the Production state.
##### Test Assertion #37.5.2.e
The end user cannot change the Agency ID list state from WIP directly to Production.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The end user is on the Agency ID list detail page, which he owns.
2. The users, branches, releases, and records needed to exercise "End user Agency ID list state management" are available in connectCenter.

### Test Step:
1. Sign in as an end user and open an owned Agency ID List detail page in the selected release branch.
2. Verify state changes are unavailable until the Agency ID List has saved changes. (Assertion [#37.5.1](#test-assertion-3751))
3. Save changes to the Agency ID List or one of its values, then verify the allowed transitions `WIP` to `QA`, `QA` to `WIP`, and `QA` to `Production`. (Assertions [#37.5.2.a](#test-assertion-3752a), [#37.5.2.b](#test-assertion-3752b), [#37.5.2.c](#test-assertion-3752c))
4. Verify no further state change is available from `Production` and that `WIP` cannot move directly to `Production`. (Assertions [#37.5.2.d](#test-assertion-3752d), [#37.5.2.e](#test-assertion-3752e))
## Test Case 37.6

**Deleting an Agency ID list**

> Delete an Agency ID list means that it is marked as “Deleted” and it is still displayed in the Agency ID list page (Core Component -> View/Edit Agency ID list) when the release branch the Agency ID list belongs to is selected. If an Agency ID list is “Deleted” any other end user can restore it.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #37.6.1
If an end user Agency ID list revision number is 1, the end user owner can delete it when it is in WIP state and is owned by him. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the View/Edit Agency ID list page with the same release branch selected.

#### Test Assertion #37.6.2
End user Agency ID list whose revision number is more than 1 in any state cannot be deleted, check particularly the WIP state.

### Test Step Pre-condition:
1. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
2. Any additional data required by the assertions has been prepared before execution.

### Test Step:
1. Sign in as an end user and open an owned `WIP` Agency ID List whose revision number is `1`.
2. Delete the Agency ID List, confirm the dialog, and verify the user returns to the release-branch list page. (Assertion [#37.6.1](#test-assertion-3761))
3. Open an end user Agency ID List whose revision number is greater than `1` and verify deletion is unavailable. (Assertion [#37.6.2](#test-assertion-3762))
## Test Case 37.7

**Restoring end user Agency ID list**

Pre-condition: The end user is on the Agency ID list View/Edit page with a release branch selected. Deleted end user Agency ID lists are shown in the list (e.g., “Deleted” state is selected in the state filter box).


### Test Assertion:

#### Test Assertion #37.7.1
The end user can open a deleted end user Agency ID list and restore it or select one or more deleted Agency ID lists and restore them. All of its Agency ID list values shall be restored as well. The Agency ID list shall have the same data as before it was deleted.

#### Test Assertion #37.7.2
The end user can open a deleted end user Agency ID list and restore it without needing to have previously owned the deleted Agency ID list.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The end user is on the Agency ID list View/Edit page with a release branch selected. Deleted end user Agency ID lists are shown in the list (e.g., “Deleted” state is selected in the state filter box).
2. The users, branches, releases, and records needed to exercise "Restoring end user Agency ID list" are available in connectCenter.


### Test Step:
1. Sign in as an end user and open **Core Component > View/Edit Agency ID List** with the target release branch selected and deleted Agency ID Lists visible.
2. Restore a deleted end user Agency ID List from the detail page or from the list and verify the Agency ID List data and values are restored. (Assertion [#37.7.1](#test-assertion-3771))
3. Verify the same restore flow works even when the deleted Agency ID List was previously owned by another end user. (Assertion [#37.7.2](#test-assertion-3772))
