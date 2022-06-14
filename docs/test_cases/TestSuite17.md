# Test Suite 17

Generally, the end user can add/edit an end user code list with base and without base.

## Test Case 17.1

> Code list access

Pre-condition: A release branch is selected.


### Test Assertion:

#### Test Assertion #17.1.1
The end user can see in the View/Edit Code List page all code lists (CLs) owned by any end user in any state. Included in the list must also be all developer code lists in Published state. There must not be any code list in Draft, Candidate, or Release Draft state.

#### Test Assertion #17.1.2
The end user can view and edit the details and code values of an end user CL that is in WIP state and owned by him.

#### Test Assertion #17.1.3
The end user CAN view but CANNOT edit the details of a CL that is in WIP state and owned by another end user. He can however add comments.

#### Test Assertion #17.1.4
The end user can view the details of a CL that is in QA or Production state and owned by another end user but he cannot make any change except adding comments.

#### Test Assertion #17.1.5
The end user can view details of any developer code list in the selected release branch. The CL must always be in the Published state. He cannot make any change. He can add comments.

### Test Step Pre-condition:



### Test Step:

## Test Case 17.2

> Creating a brand-new end user code list

Pre-condition: A release branch is selected.


### Test Assertion:

#### Test Assertion #17.2.1
On the Code List View/Edit page where a release branch is selected, the end user can create a brand-new code list without base with only required information, See Create a Brand New Code List in Score User Guide for Mandatory/Optional fields. The following are default values – Based Code List = Null and cannot be changed; Name = “a code list”; List ID = Randomly Generated GUID; Agency ID = default to the “Mutually Defined” one; Version = blank; Definition = blank; Definition Source= blank; Remark = blank; Deprecated = false (and locked); Namespace = null; Comments = empty. It must not appear in any other branch. It has a revision number 1. There must be no “Extensible” Checkbox.

#### Test Assertion #17.2.2
The end user can create a code list without base with all information specified and multiple code values. In addition, Code list value cannot be duplicated.

#### Test Assertion #17.2.3
The end user can remove a code value during the code list without base creation.

#### Test Assertion #17.2.4
The end user cannot create a code list without base, when it does not meet a uniqueness constraint. See Create a Brand New Code List in Score User Guide for the uniqueness constraint.

#### Test Assertion #17.2.5
The end user can create a brand-new code list based on another published developer code list in the same branch.

#### Test Assertion #17.2.6
The end user CANNOT create a brand-new CL based on another end-user code list.

#### Test Assertion #17.2.7
There is a developer code list that has different revisions in two releases, one of which is the release currently selected by the end user and is a later release. The end user must not be able to create a brand-new code list based on the earlier revision of the developer code list.

### Test Step Pre-condition:



### Test Step:

## Test Case 17.3

> Editing a brand-new end user code list

Pre-condition: The brand-new CL is created by the end user and is in the WIP state. The end user accesses these functionalities by opening the brand-new CL from the CL View/Edit page on a particular release branch or after creating a brand-new end user CL.


### Test Assertion:

#### Test Assertion #17.3.1
The end user can change the properties of the CL and save changes with the following business rules.

##### Test Assertion #17.3.1.a
The List ID, Agency ID, and Version have to be unique in the branch.
##### Test Assertion #17.3.1.b
Based Code List may be null or has a value but must be locked (because a based CL, if there is, would already be selected at this CL creation). Based code list is locked because code list is derived by copy. Changing this would mean recopying again, so it is better to deleted and create a new one.
##### Test Assertion #17.3.1.c
Name, List ID, Agency ID, Version, and Namespace. Deprecated is also mandatory but must be false (and locked). Namespace must be a non-standard namespace.
##### Test Assertion #17.3.1.d
A warning should be given when the Definition is empty.

#### Test Assertion #17.3.2
The end user can add a Code List Value. The Code List Value shall have the following field - Code, Short Name, Definition, Definition Source, and Deprecated. The Code field has to be unique within the CL and its based CL. Deprecated field shall be false and locked.

#### Test Assertion #17.3.3
For a CL with base, the end user can remove derived CL values and save.

#### Test Assertion #17.3.4
For a CL with base, the end user can change existing CL values and their details.

#### Test Assertion #17.3.5
For new Code List Value, only the Code and Short Name are required.

#### Test Assertion #17.3.6
An added Code List Value can be removed.

#### Test Assertion #17.3.7
The end user can edit a newly added Code List Value details except the Deprecated field with and without changing the Code field itself.

#### Test Assertion #17.3.8
The end user can select an end user Agency ID list in Production state under the Code List.

### Test Step Pre-condition:



### Test Step:

## Test Case 17.4

> Amend an end user code list

Pre-condition: The end user has selected a particular release branch.


### Test Assertion:

#### Test Assertion #17.4.1
On the CL Detail page of an end user CL in Production state, the end user can amend the CL regardless of the current owner. The result is that the release branch has that CL and its code list values with an incremental revision number that is in the WIP state.  Its detail attributes are initially the same as those of the previous revision. All the Code List Values from the previous revisions shall be present.

#### Test Assertion #17.4.2
The end user cannot amend a developer code list in the release branch.

#### Test Assertion #17.4.3
The end user can change the properties of the CL and save changes with the following business rules.

##### Test Assertion #17.4.3.a
If the Deprecated was already True in the previous revision, the field along with the Replaced By field should be locked. If it was False before the amendment the checkbox shall be enabled. When Deprecated is changed to True, the end user must be able to select a replacement CL that is not already deprecated from a drop-down list in the Replaced By field – but the field is optional. There can be only one replacement code list. When the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.
##### Test Assertion #17.4.3.b
Based Code List, Namespace, Name, List ID, and Agency ID cannot be changed. The Version field is initially set to pre-amendment + “New” (same as developer code list), but the user can change to anything
##### Test Assertion #17.4.3.c
Definition, Definition Source, and Remark can be changed.

#### Test Assertion #17.4.4
Existing locally defined (i.e., not inherited) Code List Value from the previous revisions cannot be discarded. Only its Short Name, Definition, and Definition Source field can be changed. If the Deprecated field was true in the previous revision, the field along with the Replaced By field shall be locked. If it was false before the amendment, the checkbox shall be enabled. When it is changed to true, the end user can select ONE replacement CL value from the CL.

#### Test Assertion #17.4.5
For the Code List Value inherited from the based Code List, the values cannot be removed since it is an amended code list (i.e., revision > #1). Only its Short Name, Definition, and Definition Source field can be changed. If the Deprecated field was true in the previous revision, the field along with the Replaced By field shall be locked. If it was false before the amendment, the checkbox shall be enabled. When it is changed to true, the end user can select ONE replacement CL value from the CL.

#### Test Assertion #17.4.6
A new Code List Value can be added and all of its details can be edited.

#### Test Assertion #17.4.7
A brand-new code list value added in this revision can be discarded, if it is not a replacement of a deprecated code list value.

#### Test Assertion #17.4.8
The end user can cancel the amendment. In this case, the system rollbacks the whole CL details and children Code List Values to the previous revision.

#### Test Assertion #17.4.9
Test expressing BIE that uses an amended end user code list and make sure that it is generated with the expected differences. Test with end user code list that is based on a developer code list and one that has no based.

### Test Step Pre-condition:



### Test Step:

## Test Case 17.5

> End user code list state management

Pre-condition: The end user is on the Code List detail page, which he owns.
All these state changes need a confirmation dialog box “Do you want to change state of the Code List to XYZ?”.


### Test Assertion:

#### Test Assertion #17.5.1
If there is no unsaved changes to the CL, the end user cannot change the state.

#### Test Assertion #17.5.2
Once changes to code list details or Code List Value have been saved,

##### Test Assertion #17.5.2.a
The end user can change the CL state from WIP to QA.
##### Test Assertion #17.5.2.b
The end user can change the CL state from QA back to WIP.
##### Test Assertion #17.5.2.c
The end user can change the CL state from QA to Production.
##### Test Assertion #17.5.2.d
No state change can be in the Production state.
##### Test Assertion #17.5.2.e
The end user cannot change the CL state from WIP directly to Production.

### Test Step Pre-condition:



### Test Step:

## Test Case 17.6

> Deleting a Code List

Pre-condition: N/A
Delete a CL means that it is marked as “Deleted” and it is still displayed in the CC list when the release branch the code list belongs to is selected. If a CL is “Deleted” any other end user can restore it.


### Test Assertion:

#### Test Assertion #17.6.1
If an end user CL revision number is 1, the end user owner can delete it when it is in WIP state and is owned by him. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the View/Edit Code List page with the same release branch selected.

#### Test Assertion #17.6.2
Upon opening an end user BDT that uses a deleted CL, the system shall be able to flag that the CL is in deleted state. The system shall provide an option for the end user to choose another CL for the BDT. The system shall also allow the end user to open the deleted CL in another tab where he can restore it even if the end user is not the owner of that CL. Then, the system shall be able to clear the flag (e.g., when the developer refreshes the BDT). [This is not implementable until we have BDT Management Functionality.]

#### Test Assertion #17.6.3
End user CL whose revision number is more than 1 in any state cannot be deleted, check particularly the WIP state.

### Test Step Pre-condition:



### Test Step:

## Test Case 17.7

> Restoring end user code list

Pre-condition: The end user is on the CL View/Edit page with a release branch selected. Deleted end user CLs are shown in the list (e.g., “Deleted” state is selected in the state filter box).


### Test Assertion:

#### Test Assertion #17.7.1
The end user can open a deleted end user CL and restore it or select one or more from deleted code list and restore them. All of its Code List Values shall be restored as well. The code list shall have the same data as before it was deleted.

### Test Step Pre-condition:



### Test Step: