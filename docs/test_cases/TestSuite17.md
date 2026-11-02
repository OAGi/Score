# Test Suite 17

**Release Branch Code List Management for End User**

> Generally, the end user can add/edit an end user code list with base and without base.

## Test Case 17.1

**Code list access**

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
1. The stated test-case pre-condition is satisfied: A release branch is selected.
2. The users, branches, releases, and records needed to exercise "Code list access" are available in connectCenter.

### Test Step:
1. The relevant user signs in to connectCenter.
2. The user opens the page, branch, release, or entity required for "Code list access".
3. The user performs the workflow described by the assertions.
4. The user verifies the expected result for each assertion in this test case.
## Test Case 17.2

**Creating a brand-new end user code list**

Pre-condition: A release branch is selected.


### Test Assertion:

#### Test Assertion #17.2.1
On the Code List View/Edit page where a release branch is selected, the end user can create a brand-new code list without base with only required information, See Create a Brand New Code List in connectCenter User Guide for Mandatory/Optional fields. The following are default values – Based Code List = Null and cannot be changed; Name = “a code list”; List ID = Randomly Generated GUID; Agency ID = default to the “Mutually Defined” one; Version = blank; Definition = blank; Definition Source= blank; Remark = blank; Deprecated = false (and locked); Namespace = null; Comments = empty. It must not appear in any other branch. It has a revision number 1. There must be no “Extensible” Checkbox.

#### Test Assertion #17.2.2
The end user can create a code list without base with all information specified and multiple code values. In addition, Code list value cannot be duplicated.

#### Test Assertion #17.2.3
The end user can remove a code value during the code list without base creation.

#### Test Assertion #17.2.4
The end user cannot create a code list without base, when it does not meet a uniqueness constraint. See Create a Brand New Code List in connectCenter User Guide for the uniqueness constraint.

#### Test Assertion #17.2.5
The end user can create a brand-new code list based on another published developer code list in the same branch.

#### Test Assertion #17.2.6
The end user CANNOT create a brand-new CL based on another end-user code list.

#### Test Assertion #17.2.7
There is a developer code list that has different revisions in two releases, one of which is the release currently selected by the end user and is a later release. The end user must not be able to create a brand-new code list based on the earlier revision of the developer code list.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A release branch is selected.
2. The users, branches, releases, and records needed to exercise "Creating a brand-new end user code list" are available in connectCenter.

### Test Step:
1. The relevant user signs in to connectCenter.
2. The user opens the page, branch, release, or entity required for "Creating a brand-new end user code list".
3. The user performs the workflow described by the assertions.
4. The user verifies the expected result for each assertion in this test case.
## Test Case 17.3

**Editing a brand-new end user code list**

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
1. The stated test-case pre-condition is satisfied: The brand-new CL is created by the end user and is in the WIP state. The end user accesses these functionalities by opening the brand-new CL from the CL View/Edit page on a particular release branch or after creating a brand-new end user CL.
2. The users, branches, releases, and records needed to exercise "Editing a brand-new end user code list" are available in connectCenter.

### Test Step:
1. The relevant user signs in to connectCenter.
2. The user opens the page, branch, release, or entity required for "Editing a brand-new end user code list".
3. The user performs the workflow described by the assertions.
4. The user verifies the expected result for each assertion in this test case.
## Test Case 17.4

**Amend an end user code list**

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
1. The stated test-case pre-condition is satisfied: The end user has selected a particular release branch.
2. The users, branches, releases, and records needed to exercise "Amend an end user code list" are available in connectCenter.

### Test Step:
1. The relevant user signs in to connectCenter.
2. The user opens the page, branch, release, or entity required for "Amend an end user code list".
3. The user performs the workflow described by the assertions.
4. The user verifies the expected result for each assertion in this test case.
## Test Case 17.5

**End user code list state management**

> All these state changes need a confirmation dialog box “Do you want to change state of the Code List to XYZ?”.

Pre-condition: The end user is on the Code List detail page, which he owns.


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
1. The stated test-case pre-condition is satisfied: The end user is on the Code List detail page, which he owns.
2. The users, branches, releases, and records needed to exercise "End user code list state management" are available in connectCenter.

### Test Step:
1. The relevant user signs in to connectCenter.
2. The user opens the page, branch, release, or entity required for "End user code list state management".
3. The user performs the workflow described by the assertions.
4. The user verifies the expected result for each assertion in this test case.
## Test Case 17.6

**Deleting a Code List**

> Delete a CL means that it is marked as “Deleted” and it is still displayed in the CC list when the release branch the code list belongs to is selected. If a CL is “Deleted” any other end user can restore it.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #17.6.1
If an end user CL revision number is 1, the end user owner can delete it when it is in WIP state and is owned by him. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the View/Edit Code List page with the same release branch selected.

#### Test Assertion #17.6.2
Upon opening an end user BDT that uses a deleted CL, the system shall be able to flag that the CL is in deleted state. The system shall provide an option for the end user to choose another CL for the BDT. The system shall also allow the end user to open the deleted CL in another tab where he can restore it even if the end user is not the owner of that CL. Then, the system shall be able to clear the flag (e.g., when the developer refreshes the BDT). [This is not implementable until we have BDT Management Functionality.]

#### Test Assertion #17.6.3
End user CL whose revision number is more than 1 in any state cannot be deleted, check particularly the WIP state.

### Test Step Pre-condition:
1. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
2. Any additional data required by the assertions has been prepared before execution.

### Test Step:
1. The relevant user signs in to connectCenter.
2. The user opens the page, branch, release, or entity required for "Deleting a Code List".
3. The user performs the workflow described by the assertions.
4. The user verifies the expected result for each assertion in this test case.
## Test Case 17.7

**Restoring end user code list**

Pre-condition: The end user is on the CL View/Edit page with a release branch selected. Deleted end user CLs are shown in the list (e.g., “Deleted” state is selected in the state filter box).


### Test Assertion:

#### Test Assertion #17.7.1
The end user can open a deleted end user CL and restore it or select one or more from deleted code list and restore them. All of its Code List Values shall be restored as well. The code list shall have the same data as before it was deleted.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The end user is on the CL View/Edit page with a release branch selected. Deleted end user CLs are shown in the list (e.g., “Deleted” state is selected in the state filter box).
2. The users, branches, releases, and records needed to exercise "Restoring end user code list" are available in connectCenter.


### Test Step:
1. The relevant user signs in to connectCenter.
2. The user opens the page, branch, release, or entity required for "Restoring end user code list".
3. The user performs the workflow described by the assertions.
4. The user verifies the expected result for each assertion in this test case.

## Test Case 17.8

**Using a custom end user code list in a BIE expression**

Pre-condition: A release branch is selected. An end user account can create and manage code lists and BIEs in that release. A BIE can be created with both a BBIE and a BBIE_SC whose underlying data types have a Token primitive, and both nodes permit the custom code lists used in this test.


### Test Assertion:

#### Test Assertion #17.8.1
The end user can create a custom code list without a base, add unique code values, move the code list to Production, select it as the code-list value domain of both a BBIE and a BBIE_SC in BIEs in the same release, save the BIEs, and reopen them with the selected code list and values intact.

#### Test Assertion #17.8.2
The end user can create a custom code list based on a compatible Published developer code list in the same release, add a unique code value, move the code list to Production, select it as the code-list value domain of both a BBIE and a BBIE_SC in BIEs in the same release, save the BIEs, and reopen them with the selected code list and inherited and added values intact.

#### Test Assertion #17.8.3
The end user can generate XML Schema, JSON Schema, OpenAPI, Open Document Spreadsheet (ODS), and Avro expressions for a BIE that uses a custom Code List. Each format produces a valid non-empty output. XML Schema and JSON Schema include the selected custom Code List values for both BBIE and BBIE_SC, including values inherited from a developer Code List. ODS and Avro include the selected BBIE and BBIE_SC fields but do not serialize Code List values as enumerations. OpenAPI Template generation is checked for successful output and its component schemas include the custom Code List values.

#### Test Assertion #17.8.4
The Agency ID List and Agency ID List Value fields on Code List details can both be left empty. If an Agency ID List is selected, an Agency ID List Value from that list is required; changing or clearing the list clears any value that is no longer valid. Code List Namespace is required to update the list or change its state, except that a list can still be deleted and a deleted list can be restored.

#### Test Assertion #17.8.5
A custom Code List can be managed by a custom Agency ID List when a value from that Agency ID List is selected on the Code List. When assigned to a BBIE and BBIE_SC, the BIE expression retains the custom Code List values and uses the selected Agency ID List Value in the Code List type name. A BIE inheriting from that BIE retains the same effective value domains.

#### Test Assertion #17.8.6
A custom Code List and its managing custom Agency ID List remain available to an expression when their assigned nodes are inherited from a base BIE. Repeated BBIE and BBIE_SC references to the same list manifest resolve to one compatible definition, and the definition uses the values belonging to that manifest.

### Test Step Pre-condition:
1. A release branch, an end user account, and the BIE permissions needed for this test are available in connectCenter.
2. A compatible Token-based BBIE and a compatible Token-based BBIE_SC are available in the selected release. A Published developer code list compatible with both node types is available for the based-list scenario.

### Test Step:
1. Sign in as an end user and select the target release branch.
2. Create a custom code list without a base and add multiple unique values. Change its state to `Production`. (Assertions [#17.2.1](#test-assertion-1721), [#17.2.2](#test-assertion-1722), [#17.5.2.c](#test-assertion-1752c))
3. Create a second custom code list based on a compatible Published developer code list in the same release, add a unique value, and change its state to `Production`. (Assertions [#17.2.5](#test-assertion-1725), [#17.3.2](#test-assertion-1732), [#17.5.2.c](#test-assertion-1752c))
4. Create end-user BIEs with a compatible Token-based BBIE and BBIE_SC, select the custom code list as the code-list value domain for each node, save the BIEs, and reopen them to verify that the selections and values are retained. Repeat for the based and no-base code lists. (Assertions [#17.8.1](#test-assertion-1781), [#17.8.2](#test-assertion-1782))
5. Generate XML Schema, JSON Schema, OpenAPI 3.1 YAML, ODS, and Avro expressions for each BIE using the single-BIE flow and default annotations. Verify a valid non-empty output for each format. In XSD and JSON Schema, verify that the BBIE and BBIE_SC resolve to the selected list and that custom and inherited values are present. Verify that ODS contains the `DefaultIndicator` BBIE and Language Code BBIE_SC fields and that Avro contains corresponding fields. Confirm that ODS and Avro do not serialize custom or inherited Code List values as enums. Verify the OpenAPI GET/POST templates and confirm component schemas include custom and inherited values. (Assertion [#17.8.3](#test-assertion-1783))
6. Open Code List details and verify Agency ID List and Agency ID List Value are not marked required while the pair is empty. Save with both empty and verify it succeeds; select a list and verify its value field becomes required and only its values are available, and that selecting a different list clears an incompatible value. Verify update and ordinary state changes, including returning to WIP, are blocked while Namespace is empty; deletion and restoration remain available. (Assertion [#17.8.4](#test-assertion-1784))
7. Create a custom Agency ID List and value, associate that pair with a custom Code List, and assign the Code List to the BBIE and BBIE_SC. Generate the BIE expression and verify the generated Code List type name uses the selected Agency ID List Value and the enum contains the custom Code List values. Make a second BIE inherit from the configured BIE and confirm the expression keeps those value domains. (Assertions [#17.8.5](#test-assertion-1785), [#17.8.6](#test-assertion-1786))
