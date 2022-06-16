# Test Suite 11

> Working Branch Code List Management for Developer

Note: A developer code list is also viewed as a kind of CC in Score. Unless otherwise, specifically indicated, a statement about CC is also applied to a code list.

## Test Case 11.1

> Code list access

Pre-condition: A working branch is selected.


### Test Assertion:

#### Test Assertion #11.1.1
The developer can see in the View/Edit Code List page all code lists (CLs) owned by any developer in any state.

#### Test Assertion #11.1.2
The developer can view and edit the details (including code values) of a CL that is in the WIP state and owned by him.

#### Test Assertion #11.1.3
The developer CAN view but CANNOT edit the details of a CL that is in WIP state and owned by another developer.

#### Test Assertion #11.1.4
The developer can view the details of a CL that is in Draft, Candidate, Deleted, or Release Draft state and owned by any developer but he cannot make any change except adding comments.

#### Test Assertion #11.1.5
The developer can view the details of a Published CL owned by any developer but he cannot make any change except adding comments or make a new revision of the CL.

#### Test Assertion #11.1.6
There must not be any end user CL listed in the Working branch.

#### Test Assertion #11.1.7
The developer can view details of a deleted code list owned by another developer.

#### Test Assertion #11.1.8
A developer can add comments to any developer CL in any state.

#### Test Assertion #11.1.9
An end user can add comments to any developer CL in any state.

#### Test Assertion #11.1.10
The developer can filter CLs based on the branch.

#### Test Assertion #11.1.11
The developer can filter CLs based on deprecation status.

#### Test Assertion #11.1.12
The developer can filter CLs based on their State.

#### Test Assertion #11.1.13
The developer can filter CLs based on their Updated Date.

#### Test Assertion #11.1.14
The developer can search for CLs based only on their Name.

#### Test Assertion #11.1.15
The developer can search for CLs based only on their Definition.

#### Test Assertion #11.1.16
The developer can search for CLs based only on their Module.

### Test Step Pre-condition:



### Test Step:

## Test Case 11.2

> Creating a brand-new developer code list

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #11.2.1
On the Code List View/Edit page where a working branch is selected, the developer can create a brand-new code list without base with only required information, See Create a Brand New Code List in Score User Guide for Mandatory/Optional fields. The following are default values – Based Code List = Null and cannot be changed; Name = “a code list”; List ID = Randomly Generated GUID; Agency ID = the OAGI one; Version = 1; Definition = blank; Definition Source= blank; Remark = blank; Deprecated = false (and locked); Namespace = null; Comments = empty. The brand-new CL must not have any release assigned yet, i.e., it must not appear in any release branch except the working branch. It has a revision number 1.

#### Test Assertion #11.2.2
The developer can create a code list without base with all information specified and multiple code values. In addition, Code list value cannot be duplicated.

#### Test Assertion #11.2.3
The developer can remove a code value during the code list without base creation.

#### Test Assertion #11.2.4
The developer cannot create a code list without base, when it does not meet a uniqueness constraint. See Create a Brand New Code List in Score User Guide for the uniqueness constraint.

#### Test Assertion #11.2.5
The developer cannot create a CL based on another CL.

#### Test Assertion #11.2.6
The developer cannot create a brand-new developer CL when a release branch is selected.

#### Test Assertion #11.2.7
Brand new Code List values should not be able to be Unused – the flag should be disabled or hidden.

### Test Step Pre-condition:



### Test Step:

## Test Case 11.3

> Editing a brand-new developer code list

Pre-condition: The brand-new CL is created by the developer and is in the WIP state. The developer accesses these functionalities by opening the brand-new CL from the CL View/Edit page or after creating a brand-new CL according to Test Assertion #1 of the Test Case 11.2.


### Test Assertion:

#### Test Assertion #11.3.1
The developer can change the properties of the CL and save changes with the following business rules.

##### Test Assertion #11.3.1.a
The combination of List ID, Agency ID, and Version have to be unique in the working branch.
##### Test Assertion #11.3.1.b
Based Code List must be Null and locked.
##### Test Assertion #11.3.1.c
Name, List ID, Agency ID, Version, and Namespace are mandatory. Deprecated is also mandatory but must be false and locked.
##### Test Assertion #11.3.1.d
A warning should be given when the Definition is empty.
##### Test Assertion #11.3.1.e
Only standard namespace shall be allowed for the “Namespace”.
##### Test Assertion #11.3.1.f
The developer can add a Code List Value. The Code List Value can have the following field - Code, Short Name, Definition, Definition Source, and Deprecated. The Code field has to be unique within the CL. Deprecated field shall be false and locked.

#### Test Assertion #11.3.2
For Code List Value, only the Code and Short Name are required.

#### Test Assertion #11.3.3
A Code List Value can be removed.

#### Test Assertion #11.3.4
The developer can edit a Code List Value details except the Deprecated field with and without changing the Code field itself during the code list without base creation. Specifically verify that the deprecated flag is still locked when open a code value to edit.

#### Test Assertion #11.3.5
Code List Values shall be unique within the code list.

### Test Step Pre-condition:



### Test Step:

## Test Case 11.4

> Creating a new revision of a developer code list

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #11.4.1
On the CL Detail page of the working branch, the developer can create a new revision of a published CL regardless of the current owner. The result is that the working branch has that CL and its code list values with an incremental revision number that is in the WIP state.  Its detail attributes are initially the same as those of the previous revision except the Version field. The Version field shall be default to the previous value suffixed with “_New”. All the Code List Values from the previous revisions shall be present.

### Test Step Pre-condition:



### Test Step:

## Test Case 11.5

> Editing a revision of a developer code list

Pre-condition: The CL under test has revision number greater than 1 and is in WIP state.


### Test Assertion:

#### Test Assertion #11.5.1
The developer can change the properties of the CL and save changes with the following business rules.

##### Test Assertion #11.5.1.a
There should not exist any Extensible checkbox.
##### Test Assertion #11.5.1.b
If the Deprecated was already True in the previous revision, the field along with the Replaced By field should be lock. If it was False in the previous revision the checkbox shall be enabled. When Deprecated is changed to True, the developer must be able to select a replacement CL that is not already deprecated from a drop-down list in the Replaced By field – but the field is optional. There can be only one replacement code list. If the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.
##### Test Assertion #11.5.1.c
The Based Code List, Name, List ID, Agency ID and Namespace cannot be updated.
##### Test Assertion #11.5.1.d
Only Version, Definition, and Definition Source can be updated.
##### Test Assertion #11.5.1.e
The combination of List ID, Agency ID, and Version have to be unique in the working branch.

#### Test Assertion #11.5.2
Existing Code List Value from the previous revisions cannot be removed. Only its Meaning, Definition, and Definition Source field can be changed. It can also be deprecated, if deprecated was false. If deprecated was true, the deprecated flag shall be locked. When the deprecated is changed from false to true, the developer can select one of its CL values as a replacement.

#### Test Assertion #11.5.3
A new unique Code List Value can be added and all of its details can be edited.

#### Test Assertion #11.5.4
The system shall not allow a new code value to be edited/updated such that it results in a duplicate value within the CL.

#### Test Assertion #11.5.5
A brand-new code list value added in this revision can be removed if it is not a replacement of a deprecated value. A dialog should tell the developer when the value cannot be removed because it is a replacement of a deprecated code list value.

#### Test Assertion #11.5.6
The developer can cancel the revision. In this case, the system rollbacks the CL to the previous revision and its children Code List Value back to the previous revision; The system also discard Code List Values added in this revision. The changes made before cancellation are also removed from the history record.

### Test Step Pre-condition:



### Test Step:

## Test Case 11.6

> Developer code list state management

Pre-condition: The developer is on the Code List detail page, which he owns.
All these state changes need a confirmation dialog box “Do you want to change state of the Code List to XYZ?”.


### Test Assertion:

#### Test Assertion #11.6.1
The developer can change the CL state from WIP to Draft.

#### Test Assertion #11.6.2
The developer can change the CL state from Draft back to WIP.

#### Test Assertion #11.6.3
The developer can change the CL state from Draft to Candidate.

#### Test Assertion #11.6.4
The developer can change the CL state from Candidate back to WIP.

### Test Step Pre-condition:



### Test Step:

## Test Case 11.7

> Deleting a Code List

Pre-condition: N/A
Delete a CL means that it is marked as “Deleted” and it is still displayed in the CC list when the working branch is selected. If a CL is “Deleted” any other developer can restore it. Generally, when the developer opens an entity containing a deleted entity at any level of the tree, the system shall flag the opened entity as in an invalid state and the deleted entity as in the deleted state.


### Test Assertion:

#### Test Assertion #11.7.1
If a CL revision number is 1, the developer owner can delete it when it is in WIP state. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the View/Edit Code List page.

#### Test Assertion #11.7.2
Upon opening a BDT that uses a deleted CL, the system shall be able to flag that the CL is in deleted state. The system shall provide an option for the developer to choose another CL for the BDT. The system shall also allow the developer to open the deleted CL in another tab where he can restore it. Then, the system shall be able to clear the flag (e.g., when the developer refreshes the BDT). [This is not implementable until we have BDT Management Functionality. Also note that on the CC side, by OAGIS import pattern code list can be used only thru a BDT. I.e., CL cannot be assigned directly to a BCCP.]

#### Test Assertion #11.7.3
Upon opening an ACC (or ASCCP) which has a descendant (not direct child) ACC using a deleted CL, the ACC shall be flagged as in an invalid state. As the developer expands the tree down to the BCCP using the BDT that the deleted CL, the CL shall be flagged as deleted. The system shall also allow the developer to open the deleted CL in another tab where he can restore it. Then, the system shall be able to clear the flag (e.g., when the developer refreshes the BDT). [This is not implementable until we have BDT Management Functionality.]

#### Test Assertion #11.7.4
CL whose revision number is more than 1 in any state cannot be deleted.

### Test Step Pre-condition:



### Test Step:

## Test Case 11.8

> Restoring developer code list

Pre-condition: The developer is on the CL View/Edit page with the Working branch selected. Deleted CLs are shown in the list (e.g., “Deleted” state is selected in the state filter box).


### Test Assertion:

#### Test Assertion #11.8.1
The developer user who is the current owner can open a CL and restore it (or optionally select one or more from the list and restore them. All of its Code List Values shall be restored as well).

#### Test Assertion #11.8.2
The developer user who is not the current owner can also restore. He becomes a new owner.

### Test Step Pre-condition:



### Test Step: