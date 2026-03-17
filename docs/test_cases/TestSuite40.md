# Test Suite 40

**Working Branch Data Type Management for End User**


## Test Case 40.1

**Access to DT viewing, editing, and commenting**

Pre-condition: A working release is selected.


### Test Assertion:

#### Test Assertion #40.1.1
The end user can see in the CC View/Edit page all DTs owned by any user in any state in the Working branch. There should not be any end user DT listed in the Working branch (this is not a query condition, i.e., such situation shouldn’t exist in the database).

#### Test Assertion #40.1.2
The end user CAN view but CANNOT edit the details of a DT in the Working release that is in WIP state and owned by another user. He can also add comments.

#### Test Assertion #40.1.3
The end user can view the details of an DT that is in Draft and owned by any user, but he cannot make any change except adding comments.

#### Test Assertion #40.1.4
The end user can view the details of an DT which is in Candidate state owned by any user but he cannot make any change. He also cannot make a revision on the DT either, but he can add comments.

#### Test Assertion #40.1.5
The end user can view the details of an DT that is in Deleted, but he cannot make any change except adding comments. He cannot either restore the Deleted DT.

#### Test Assertion #40.1.6
The end user can view details of any Published DT but cannot make any change except adding comments.

#### Test Assertion #40.1.7
The end user cannot make a new revision on any DT.

#### Test Assertion #40.1.8
The end user shall not be able to create any new DT.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A working release is selected.
2. The users, branches, releases, and records needed to exercise "Access to DT viewing, editing, and commenting" are available in connectCenter.


### Test Step:
1. The end user signs in to connectCenter, opens `Core Component > View/Edit Core Component`, and selects the Working branch.
2. Verify DT visibility for the Working branch and confirm state-specific permissions across `WIP`, `Draft`, `Candidate`, `Deleted`, and `Published` records by ownership. (Assertions [#40.1.1](#test-assertion-4011), [#40.1.2](#test-assertion-4012), [#40.1.3](#test-assertion-4013), [#40.1.4](#test-assertion-4014), [#40.1.5](#test-assertion-4015), [#40.1.6](#test-assertion-4016))
3. Verify that creating a new revision and creating a new DT are unavailable in this working-branch access scenario. (Assertions [#40.1.7](#test-assertion-4017), [#40.1.8](#test-assertion-4018))
