# Test Suite 11

**Working Branch Code List Management for Developer**

> Note: A developer code list is also viewed as a kind of CC in connectCenter. Unless otherwise specifically indicated, a statement about a CC also applies to a code list.

## Test Case 11.1

**Code list access**

Pre-condition: The Working branch is selected.


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
1. The Working branch is selected.
2. There are developer-owned code lists in the Working branch in WIP, Draft, Candidate, Release Draft, Published, and Deleted states.
3. At least one WIP code list is owned by the logged-in developer, and at least one WIP code list is owned by another developer.
4. There are end-user code lists in release branches for the negative visibility check.
5. There are code lists with different definitions, modules, update dates, deprecation states, and branch assignments for search and filter verification.

### Test Step:
1. A developer logs into connectCenter and opens the View/Edit Code List page with the Working branch selected.
2. Verify that developer-owned code lists in all supported Working-branch states are listed and searchable by name. (Assertions [#11.1.1](#test-assertion-1111), [#11.1.14](#test-assertion-11114))
3. Open a WIP code list owned by the logged-in developer and verify that its details and code values can be edited. (Assertion [#11.1.2](#test-assertion-1112))
4. Open a WIP code list owned by another developer and verify that it is viewable but read-only. (Assertion [#11.1.3](#test-assertion-1113))
5. Open Draft, Candidate, Release Draft, and Deleted code lists owned by another developer and verify that they are read-only except for comments. (Assertion [#11.1.4](#test-assertion-1114))
6. Open a Published code list owned by another developer and verify that it is read-only except for comments and revising. (Assertion [#11.1.5](#test-assertion-1115))
7. Verify that end-user code lists are not listed when the Working branch is selected. (Assertion [#11.1.6](#test-assertion-1116))
8. Open a deleted code list owned by another developer and verify that its details are viewable. (Assertion [#11.1.7](#test-assertion-1117))
9. Add comments as a developer and as an end user across developer code lists in all supported states. (Assertions [#11.1.8](#test-assertion-1118), [#11.1.9](#test-assertion-1119))
10. Exercise the branch, deprecation, state, and updated-date filters and verify the filtered results. (Assertions [#11.1.10](#test-assertion-11110), [#11.1.11](#test-assertion-11111), [#11.1.12](#test-assertion-11112), [#11.1.13](#test-assertion-11113))
11. Search by definition and module and verify that only matching code lists are returned. (Assertions [#11.1.15](#test-assertion-11115), [#11.1.16](#test-assertion-11116))

## Test Case 11.2

**Creating a brand-new developer code list**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #11.2.1
On the Code List View/Edit page where the Working branch is selected, the developer can create a brand-new code list without a base using only the required information. See Create a Brand New Code List in the connectCenter User Guide for mandatory and optional fields. The following are default values: Name = “Code List”; List ID = randomly generated GUID; Agency ID = the default OAGI agency ID list value; Version = 1; Definition = blank; Definition Source = blank; Deprecated = false (and locked); Namespace = unselected; Comments = empty. The brand-new CL belongs only to the Working branch and has revision number 1.

#### Test Assertion #11.2.2
The developer can create a code list without a base with all information specified and multiple code values. In addition, Code List Values cannot be duplicated.

#### Test Assertion #11.2.3
The developer can remove a code value during code list creation without a base.

#### Test Assertion #11.2.4
The developer cannot create a code list without a base when it does not meet a uniqueness constraint. See Create a Brand New Code List in the connectCenter User Guide for the uniqueness constraint.

#### Test Assertion #11.2.5
The developer cannot create a CL based on another CL.

#### Test Assertion #11.2.6
The developer cannot create a brand-new developer CL when a release branch is selected.

#### Test Assertion #11.2.7
Brand-new Code List values should not be able to be deprecated; the Deprecated flag should be disabled or hidden.

### Test Step Pre-condition:
1. There is a developer account with access to the Working branch.
2. There is at least one release branch available for the negative branch-selection check.
3. There is at least one published developer code list in a release branch for the “based on another CL” negative scenario.

### Test Step:
1. A developer logs into connectCenter and opens the View/Edit Code List page with the Working branch selected.
2. Create a brand-new code list with only the required information and verify the default values, comments, and revision number. (Assertion [#11.2.1](#test-assertion-1121))
3. Add multiple code values with complete information and verify that duplicate values are rejected. (Assertion [#11.2.2](#test-assertion-1122))
4. Remove a newly added code value during creation. (Assertion [#11.2.3](#test-assertion-1123))
5. Attempt to create a code list that violates the uniqueness constraint and verify that creation is rejected. (Assertion [#11.2.4](#test-assertion-1124))
6. Open an existing published developer code list and verify that creating a brand-new developer code list based on it is not allowed. (Assertion [#11.2.5](#test-assertion-1125))
7. Switch to a release branch and verify that creating a brand-new developer code list is not available. (Assertion [#11.2.6](#test-assertion-1126))
8. Open the add-code-value dialog during creation and verify that the Deprecated flag is disabled. (Assertion [#11.2.7](#test-assertion-1127))

## Test Case 11.3

**Editing a brand-new developer code list**

Pre-condition: The brand-new CL is created by the developer and is in the WIP state. The developer accesses these functionalities by opening the brand-new CL from the CL View/Edit page or after creating a brand-new CL according to Test Assertion #11.2.1.


### Test Assertion:

#### Test Assertion #11.3.1
The developer can change the properties of the CL and save changes with the following business rules.

##### Test Assertion #11.3.1.a
The combination of List ID, Agency ID, and Version have to be unique in the Working branch.
##### Test Assertion #11.3.1.b
Based Code List must be null and locked.
##### Test Assertion #11.3.1.c
Name, List ID, Agency ID, Version, and Namespace are mandatory. Deprecated is also mandatory but must be false and locked.
##### Test Assertion #11.3.1.d
A warning should be given when the Definition is empty.
##### Test Assertion #11.3.1.e
Only standard namespace shall be allowed for the “Namespace”.
##### Test Assertion #11.3.1.f
The developer can add a Code List Value. The Code List Value can have the following fields: Code, Meaning, Definition, Definition Source, and Deprecated. The Code field has to be unique within the CL. The Deprecated field shall be false and locked.

#### Test Assertion #11.3.2
For a Code List Value, only the Code and Meaning are required.

#### Test Assertion #11.3.3
A Code List Value can be removed.

#### Test Assertion #11.3.4
The developer can edit Code List Value details except the Deprecated field, with and without changing the Code field itself, during brand-new code list editing. Specifically verify that the Deprecated flag is still locked when a code value is opened for editing.

#### Test Assertion #11.3.5
Code List Values shall be unique within the code list.

### Test Step Pre-condition:
1. There is a brand-new developer code list in revision 1 and WIP state in the Working branch.
2. The code list is owned by the logged-in developer.
3. The code list has enough code values to verify add, edit, remove, and duplicate scenarios.
4. Standard and non-standard namespaces are available for namespace validation.

### Test Step:
1. The developer logs into connectCenter, opens the brand-new developer code list, and edits its details.
2. Verify uniqueness, required fields, definition warning, namespace restrictions, and locked Deprecated behavior at the code-list level. (Assertion [#11.3.1](#test-assertion-1131))
3. Add a code value and verify its supported fields, uniqueness, and locked Deprecated flag. (Assertion [#11.3.1.f](#test-assertion-1131f))
4. Open the add-code-value dialog and verify that only Code and Meaning are required. (Assertion [#11.3.2](#test-assertion-1132))
5. Remove an existing code value from the brand-new code list. (Assertion [#11.3.3](#test-assertion-1133))
6. Edit existing code values with and without changing the Code field and verify that the Deprecated flag remains locked. (Assertion [#11.3.4](#test-assertion-1134))
7. Attempt to create duplicate code values and verify that duplicates are rejected. (Assertion [#11.3.5](#test-assertion-1135))

## Test Case 11.4

**Creating a new revision of a developer code list**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #11.4.1
On the CL Detail page of the Working branch, the developer can create a new revision of a published CL regardless of the current owner. The result is that the Working branch has that CL and its code list values with an incremental revision number that is in the WIP state. Its detail attributes are initially the same as those of the previous revision except the Version field. The Version field shall default to the previous value suffixed with “_New”. All the Code List Values from the previous revisions shall be present.

### Test Step Pre-condition:
1. There are published developer code lists in the Working branch.
2. At least one published code list is owned by the logged-in developer and at least one is owned by another developer.
3. Each published code list has one or more existing code values.

### Test Step:
1. The developer logs into connectCenter and opens a published developer code list in the Working branch.
2. Create a new revision from a published code list owned by another developer and verify the Working-branch revision is created in WIP state. (Assertion [#11.4.1](#test-assertion-1141))
3. Repeat the same action for a published code list owned by the logged-in developer. (Assertion [#11.4.1](#test-assertion-1141))
4. Verify that the revision number is incremented, the Version field defaults to the previous value suffixed with `_New`, and the prior code values are copied forward. (Assertion [#11.4.1](#test-assertion-1141))

## Test Case 11.5

**Editing a revision of a developer code list**

Pre-condition: The CL under test has revision number greater than 1 and is in WIP state.


### Test Assertion:

#### Test Assertion #11.5.1
The developer can change the properties of the CL and save changes with the following business rules.

##### Test Assertion #11.5.1.a
There should not exist any Extensible checkbox.
##### Test Assertion #11.5.1.b
If Deprecated was already true in the previous revision, the field along with the Replaced By field should be locked. If it was false in the previous revision, the checkbox shall be enabled. When Deprecated is changed to true, the developer must be able to select a replacement CL that is not already deprecated from a drop-down list in the Replaced By field, but the field is optional. There can be only one replacement code list. If Deprecated is changed to false, the Replaced By field shall be null and may disappear from the UI.
##### Test Assertion #11.5.1.c
The Based Code List, Name, List ID, Agency ID and Namespace cannot be updated.
##### Test Assertion #11.5.1.d
Only Version, Definition, and Definition Source can be updated.
##### Test Assertion #11.5.1.e
The combination of List ID, Agency ID, and Version have to be unique in the Working branch.

> Automation note: Current TS_11 automation verifies deprecated-field locking and editable metadata. Replacement selection in the Replaced By field still needs dedicated page-object support.

#### Test Assertion #11.5.2
Existing Code List Value from the previous revisions cannot be removed. Only its Meaning, Definition, and Definition Source field can be changed. It can also be deprecated, if deprecated was false. If deprecated was true, the deprecated flag shall be locked. When the deprecated is changed from false to true, the developer can select one of its CL values as a replacement.

> Automation note: Current TS_11 automation verifies remove restrictions, allowed field edits, and deprecated-field locking. Replacement-value selection still needs dedicated page-object support.

#### Test Assertion #11.5.3
A new unique Code List Value can be added and all of its details can be edited.

#### Test Assertion #11.5.4
The system shall not allow a new code value to be edited/updated such that it results in a duplicate value within the CL.

#### Test Assertion #11.5.5
A brand-new code list value added in this revision can be removed if it is not a replacement of a deprecated value. A dialog should tell the developer when the value cannot be removed because it is a replacement of a deprecated code list value.

#### Test Assertion #11.5.6
The developer can cancel the revision. In this case, the system rolls the CL back to the previous revision and restores its child Code List Values to the previous revision. The system also discards Code List Values added in this revision. The changes made before cancellation are also removed from the history record.

### Test Step Pre-condition:
1. There is a developer code list in the Working branch with revision number greater than 1 and state WIP.
2. One scenario has the code list already deprecated in the previous revision, and another has it not deprecated.
3. There are inherited code values from the previous revision, including at least one that was already deprecated.
4. There is at least one new code value created in the current revision for remove and cancel scenarios.

### Test Step:
1. The developer logs into connectCenter and opens a revised developer code list in WIP state.
2. Verify that identity fields remain read-only, supported metadata fields remain editable, uniqueness is enforced, and prior Deprecated states are locked. (Assertion [#11.5.1](#test-assertion-1151))
3. Open inherited code values from previous revisions and verify that they cannot be removed, only supported fields can change, and prior Deprecated states remain locked. (Assertion [#11.5.2](#test-assertion-1152))
4. Add a new unique code value and verify that it can be edited. (Assertion [#11.5.3](#test-assertion-1153))
5. Attempt to create or edit a new code value so that it duplicates an existing value and verify that it is rejected. (Assertion [#11.5.4](#test-assertion-1154))
6. Remove a brand-new code value added in the current revision when it is not used as a replacement. (Assertion [#11.5.5](#test-assertion-1155))
7. Cancel the revision and verify that the code list and its code values roll back to the previous revision state. (Assertion [#11.5.6](#test-assertion-1156))

## Test Case 11.6

**Developer code list state management**

> All these state changes need a confirmation dialog box “Do you want to change state of the Code List to XYZ?”.

Pre-condition: The developer is on the Code List detail page, which he owns.


### Test Assertion:

#### Test Assertion #11.6.1
The developer can change the CL state from WIP to Draft.

#### Test Assertion #11.6.2
The developer can change the CL state from Draft back to WIP.

#### Test Assertion #11.6.3
The developer can change the CL state from Draft to Candidate.

#### Test Assertion #11.6.4
The developer can change the CL state from Candidate back to WIP.

> Automation note: Current TS_11 automation verifies the state transitions themselves. The confirmation dialog text is handled by page-object actions and is not asserted separately.

### Test Step Pre-condition:
1. There is a developer-owned code list in the Working branch.
2. The code list starts in WIP state and has no unsaved changes before each state transition.

### Test Step:
1. The developer logs into connectCenter and opens the code list detail page in the Working branch.
2. Move the code list from WIP to Draft and verify the new state. (Assertion [#11.6.1](#test-assertion-1161))
3. Move the code list from Draft back to WIP and verify the new state. (Assertion [#11.6.2](#test-assertion-1162))
4. Move the code list from Draft to Candidate and verify the new state. (Assertion [#11.6.3](#test-assertion-1163))
5. Move the code list from Candidate back to WIP and verify the new state. (Assertion [#11.6.4](#test-assertion-1164))

## Test Case 11.7

**Deleting a code list**

> Deleting a CL means that it is marked as “Deleted” and is still displayed in the CC list when the Working branch is selected. If a CL is “Deleted”, any other developer can restore it. Generally, when the developer opens an entity containing a deleted entity at any level of the tree, the system shall flag the opened entity as being in an invalid state and the deleted entity as being in the deleted state.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #11.7.1
If a CL revision number is 1, the developer owner can delete it when it is in WIP state. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the View/Edit Code List page.

#### Test Assertion #11.7.2
Upon opening a BDT that uses a deleted CL, the system shall be able to flag that the CL is in a deleted state. The system shall provide an option for the developer to choose another CL for the BDT. The system shall also allow the developer to restore the deleted CL and then clear the flag (for example, when the developer refreshes the BDT). Note that on the CC side, by the OAGIS import pattern, a code list can be used only through a BDT; i.e., a CL cannot be assigned directly to a BCCP.

> Automation note: Current TS_11 automation verifies the deleted marker, alternate CL selection, and clear-after-restore flow on the BDT page. Opening the deleted CL directly from the BDT path is not asserted separately.

#### Test Assertion #11.7.3
Upon opening an ACC (or ASCCP) that has a descendant (not direct child) ACC using a deleted CL, the ACC shall be flagged as being in an invalid state. As the developer expands the tree down to the BCCP that uses the BDT with the deleted CL, the CL shall be flagged as deleted. The system shall also allow the developer to restore the deleted CL and then clear the flag (for example, when the developer refreshes the BDT).

> Automation note: Current TS_11 automation verifies the descendant BCCP/BDT trace, the deleted CL marker, and the clear-after-restore behavior. The ancestor ACC invalid-state banner and opening the deleted CL directly from that path are not asserted separately.

#### Test Assertion #11.7.4
CL whose revision number is more than 1 in any state cannot be deleted.

### Test Step Pre-condition:
1. There is a revision-1 developer code list in WIP state in the Working branch.
2. There is a BDT that uses that code list.
3. There is an ACC descendant path that reaches a BCCP using the BDT that uses that code list.
4. There is a developer code list with revision number greater than 1 for the negative deletion scenario.

### Test Step:
1. The developer logs into connectCenter and opens a revision-1 WIP developer code list.
2. Delete the code list and verify that it moves to Deleted state and remains visible from the View/Edit Code List page. (Assertion [#11.7.1](#test-assertion-1171))
3. Open a BDT that uses the deleted code list, verify the deleted marker, select another code list if needed, restore the deleted code list, and verify that the deleted marker clears. (Assertion [#11.7.2](#test-assertion-1172))
4. Open an ACC that reaches the deleted code list through a descendant BCCP/BDT path, verify the deleted marker through that path, restore the deleted code list, and verify that the marker clears. (Assertion [#11.7.3](#test-assertion-1173))
5. Open a code list whose revision number is greater than 1 and verify that deletion is not allowed in any state under test. (Assertion [#11.7.4](#test-assertion-1174))

## Test Case 11.8

**Restoring a developer code list**

Pre-condition: The developer is on the CL View/Edit page with the Working branch selected. Deleted CLs are shown in the list (e.g., “Deleted” state is selected in the state filter box).


### Test Assertion:

#### Test Assertion #11.8.1
The developer user who is the current owner can open a CL and restore it (or optionally select one or more from the list and restore them). All of its Code List Values shall be restored as well.

#### Test Assertion #11.8.2
The developer user who is not the current owner can also restore. He becomes a new owner.

### Test Step Pre-condition:
1. The Working branch is selected on the View/Edit Code List page.
2. Deleted code lists are visible in the list.
3. There is one deleted code list owned by the logged-in developer.
4. There is one deleted code list owned by another developer.

### Test Step:
1. The developer logs into connectCenter and opens a deleted code list that he currently owns.
2. Restore the code list and verify that all of its code values are restored as well. (Assertion [#11.8.1](#test-assertion-1181))
3. Open a deleted code list owned by another developer.
4. Restore the code list and verify that ownership is transferred to the restoring developer. (Assertion [#11.8.2](#test-assertion-1182))
