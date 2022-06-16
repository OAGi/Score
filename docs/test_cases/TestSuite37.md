# Test Suite 37

> Release Branch Agency ID List Management for End User

Generally, the end user can add/edit an end user Agency ID list with base and without base.

## Test Case 37.1

> Agency ID access

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



### Test Step:

## Test Case 37.2

> Creating a brand-new end user Agency ID list

Pre-condition: A release branch is selected.


### Test Assertion:

#### Test Assertion #37.2.1
On the Agency ID list View/Edit page where a release branch is selected, the end user can create a brand-new Agency ID list without base with only required information, See Create a Brand new Agency ID list in Score User Guide for Mandatory/Optional fields. The following are default values – Based Code List = Null (this field can be hidden in case of an Agency ID list without base) and cannot be changed; Name = “AgencyIdentification”; List ID = Randomly Generated GUID; Agency ID =blank ; Version = blank; Definition = blank; Deprecated = false (and locked); Namespace = null; Comments = empty. It must not appear in any other branch. It has a revision number 1.

#### Test Assertion #37.2.2
The end user can create an Agency ID list without base with all information specified and multiple Agency ID list values. In addition, Agency ID list value cannot be duplicated.

#### Test Assertion #37.2.3
The end user can discard an Agency ID list value during the Agency ID list without base creation.

#### Test Assertion #37.2.4
The end user cannot create an Agency ID list without base, when it does not meet a uniqueness constraint (i.e., no other Agency ID list with same List ID, Agency ID and Version should exist in the selected branch). See Create a Brand New Agency ID List in Score User Guide for the uniqueness constraint.

#### Test Assertion #37.2.5
The end user can create a brand-new Agency ID list based on another published developer code list. The developer Agency ID list must be in the same release as the selected release branch.

#### Test Assertion #37.2.6
The end user CANNOT create a brand-new Agency ID list based on another end-user code list.

#### Test Assertion #37.2.7
There is a developer Agency ID list that has different revisions in two releases, one of which is the release currently selected by the end user and is a later release. The end user must not be able to create a brand-new Agency ID list based on the earlier revision of the developer Agency ID list.

#### Test Assertion #37.2.8
The value in the Agency ID field must be one of the IDs in the list.

### Test Step Pre-condition:



### Test Step:

## Test Case 37.3

> Editing a brand-new end user Agency ID list

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



### Test Step:

## Test Case 37.4

> Amend an end user Agency ID list

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
Based Agency ID list,  Namespace, Name, List ID, and Agency ID cannot be changed. The Version field is initially set to pre-amendment + “New” (same as developer code list), but the user can change to anything
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



### Test Step:

## Test Case 37.5

> End user Agency ID list state management

Pre-condition: The end user is on the Code List detail page, which he owns.
All these state changes need a confirmation dialog box “Do you want to change state of the Agency ID list to XYZ?”.


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



### Test Step:

## Test Case 37.6

> Deleting an Agency ID list

Pre-condition: N/A
Delete an Agency ID list means that it is marked as “Deleted” and it is still displayed in the Agency ID list page (Core Component -> View/Edit Agency ID list) when the release branch the Agency ID list belongs to is selected. If an Agency ID list is “Deleted” any other end user can restore it.


### Test Assertion:

#### Test Assertion #37.6.1
If an end user Agency ID list revision number is 1, the end user owner can delete it when it is in WIP state and is owned by him. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the View/Edit Agency ID list page with the same release branch selected.

#### Test Assertion #37.6.2
End user Agency ID list whose revision number is more than 1 in any state cannot be deleted, check particularly the WIP state.

### Test Step Pre-condition:



### Test Step:

## Test Case 37.7

> Restoring end user Agency ID list

Pre-condition: The end user is on the Agency ID list View/Edit page with a release branch selected. Deleted end user Agency ID lists are shown in the list (e.g., “Deleted” state is selected in the state filter box).


### Test Assertion:

#### Test Assertion #37.7.1
The end user can open a deleted end user Agency ID list and restore it or select one or more from deleted code list and restore them. All of its Agency ID list Values shall be restored as well. The Agency ID list shall have the same data as before it was deleted.

#### Test Assertion #37.7.2
The end user can open a deleted end user Agency ID list and restore it without needed to pre-owned the deleted Agency ID list.

### Test Step Pre-condition:



### Test Step: