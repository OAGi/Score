# Test Suite 34

**Working Branch Agency ID List Management for Developer**


## Test Case 34.1

**Agency ID access**

Pre-condition: The Working branch is selected.


### Test Assertion:

#### Test Assertion #34.1.1
The developer can see in the View/Edit Agency ID List page all Agency ID Lists owned by any developer in any state.

#### Test Assertion #34.1.2
The developer can view and edit the details (including Agency ID List values) of an Agency ID List that is in the WIP state and owned by him.

#### Test Assertion #34.1.3
The developer CAN view but CANNOT edit the details of an Agency ID List that is in WIP state and owned by another developer.

#### Test Assertion #34.1.4
The developer can view the details of an Agency ID List that is in Draft, Candidate, Deleted, or Release Draft state and owned by any developer but he cannot make any change except adding comments.

#### Test Assertion #34.1.5
The developer can view the details of a Published Agency ID List owned by any developer but he cannot make any change except adding comments or make a new revision of the Agency ID List.

#### Test Assertion #34.1.6
There must not be any end user Agency ID List listed in the Working branch.

#### Test Assertion #34.1.7
The developer can view details of a deleted Agency ID List owned by another developer (validated in automation through the deleted-state coverage in Test Assertion #34.1.4).

#### Test Assertion #34.1.8
A developer can add comments to any developer Agency ID List in any state.

#### Test Assertion #34.1.9
An end user can add comments to any developer Agency ID List in any state.

#### Test Assertion #34.1.10
The developer can filter Agency ID Lists based on the branch.

#### Test Assertion #34.1.11
The developer can filter Agency ID Lists based on deprecation status.

#### Test Assertion #34.1.12
The developer can filter Agency ID Lists based on their State.

#### Test Assertion #34.1.13
The developer can filter Agency ID Lists based on their Updated Date.

#### Test Assertion #34.1.14
The developer can search for Agency ID Lists based only on their Name.

#### Test Assertion #34.1.15
The developer can search for Agency ID Lists based only on their Definition.

#### Test Assertion #34.1.16
The developer can search for Agency ID Lists based only on their Module.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The Working branch is selected.
2. Developer and end user accounts plus Agency ID Lists in covered states are available in connectCenter.

### Test Step:
1. Sign in as a developer and open **Core Component > View/Edit Agency ID List** with branch set to Working.
2. Verify list visibility and detail-page editability for own and other developers' Agency ID Lists across WIP, Draft, Candidate, Release Draft, Published, and Deleted states.
3. Verify no end user-owned Agency ID Lists are listed in Working branch.
4. Add comments as developer and as end user to developer Agency ID Lists and verify comments are visible.
5. Verify branch, deprecated status, state, and updated-date filters, and verify search by name, definition, and module.
## Test Case 34.2

**Creating a brand-new developer Agency ID List**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #34.2.1
The developer cannot create a brand-new Agency ID List.

### Test Step Pre-condition:
1. A developer account and Working branch access are available.
2. The View/Edit Agency ID List page is accessible in connectCenter.

### Test Step:
1. Sign in as a developer and open **Core Component > View/Edit Agency ID List**.
2. Set branch to Working and execute search.
3. Attempt to open the create-new Agency ID List flow.
4. Verify creation is not available for developers.
## Test Case 34.3

**Creating a new revision of a developer Agency ID List**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #34.3.1
On the Agency ID List Detail page of the Working branch, the developer can create a new revision of a published Agency ID List regardless of the current owner. The result is that the Working branch has that Agency ID List with an incremental revision number that is in the WIP state.  Its detail attributes are initially the same as those of the previous revision. All the Agency ID List Values from the previous revisions shall be present.

### Test Step Pre-condition:
1. A published developer Agency ID List with values exists in Working branch.
2. A developer account with access to that Agency ID List is available.

### Test Step:
1. Sign in as a developer and open the published Agency ID List detail in Working branch.
2. Trigger **Revise** to create a new revision.
3. Verify revision increments by one, state becomes `WIP`, and owner/release stay as expected.
4. Verify Agency ID List details and existing values are carried over to the revised Agency ID List.
## Test Case 34.4

**Editing a revision of a developer Agency ID List**

Pre-condition: The Agency ID List under test has revision number greater than 1 and is in WIP state.


### Test Assertion:

#### Test Assertion #34.4.1
The developer can change the properties of the Agency ID List and save changes with the following business rules.

##### Test Assertion #34.4.1.a
The Name, List ID, Agency ID and Namespace cannot be updated.
##### Test Assertion #34.4.1.b
Only Version, Definition and Definition Source can be updated.
##### Test Assertion #34.4.1.c
Uniqueness of the List ID, Agency ID, and Version combination for a new revision is not separately automated in the current implementation.

#### Test Assertion #34.4.2
Existing Agency ID List Values from previous revisions cannot change their Value. Their Meaning, Definition, and Definition Source can be changed. Replaced By behavior is not automated in the current implementation.

#### Test Assertion #34.4.3
A new unique Agency ID List Value can be added and all of its details can be edited, namely the Name and Definition fields. The deprecated field should be disabled because it is a new value.

#### Test Assertion #34.4.4
The system shall not allow a new Agency ID List Value that is a duplicate value within the Agency ID List; its name should be unique.

#### Test Assertion #34.4.5
A brand-new Agency ID List Value added in this revision can be discarded.

#### Test Assertion #34.4.6
The developer can cancel the revision. In this case, the system rollbacks the Agency ID List to the previous revision and its children Agency ID List Values back to the previous revision; The system also discard Agency ID List Values added in this revision. The changes made before cancellation are in the history record.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The Agency ID List under test has revision number greater than 1 and is in WIP state.
2. A published developer Agency ID List with values is available so it can be revised before editing.

### Test Step:
1. Sign in as a developer, open a published Agency ID List in Working branch, and create a revision.
2. Verify locked and editable fields according to current implementation (Name/List ID/Agency ID/Namespace locked; Version/Definition/Definition Source editable).
3. Edit existing value details, add a new value, and verify duplicate value rejection.
4. Remove a newly added value and verify it is discarded after update.
5. Cancel the revision and verify rollback to the previous published revision with temporary changes removed.
## Test Case 34.5

**Developer Agency ID List state management**

> All these state changes need a confirmation dialog box “Do you want to change state of the Agency ID List to XYZ?”.

Pre-condition: The developer is on the Agency ID List detail page, which he owns.


### Test Assertion:

#### Test Assertion #34.5.1
The developer can change the Agency ID List state from WIP to Draft.

#### Test Assertion #34.5.2
The developer can change the Agency ID List state from Draft back to WIP.

#### Test Assertion #34.5.3
The developer can change the Agency ID List state from Draft to Candidate.

#### Test Assertion #34.5.4
The developer can change the Agency ID List state from Candidate back to WIP.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The developer is on the Agency ID List detail page, which he owns.
2. Developer-owned Agency ID Lists exist in WIP, Draft, and Candidate states in Working branch.

### Test Step:
1. Sign in as a developer and open the Agency ID List detail page.
2. Change state from `WIP` to `Draft` and verify.
3. Change state from `Draft` to `WIP` and verify.
4. Change state from `Draft` to `Candidate` and verify.
5. Change state from `Candidate` to `WIP` and verify.
## Test Case 34.6

**Deleting an Agency ID List**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #34.6.1
A developer Agency ID List cannot be deleted regardless of its state since its revision number is more than 1.

### Test Step Pre-condition:
1. A developer Agency ID List with revision greater than 1 is available in Working branch.
2. A developer account with access to the Agency ID List detail page is available.


### Test Step:
1. Sign in as a developer and open the Agency ID List detail page for a revision greater than 1.
2. Verify the delete action is unavailable.
