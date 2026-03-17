# Test Suite 39

**Release Branch Data Type Management for Developer**


## Test Case 39.1

**Access to DT viewing, editing and commenting**

Pre-condition: A release branch, i.e., one of the release branches, is selected.


### Test Assertion:

#### Test Assertion #39.1.1
The developer can see in the CC View/Edit page all DTs owned by any user in any state in the selected release. There should not be any developer DT listed in a release branch that is not in the Published state (this is not a query condition, i.e., such situation shouldn’t exist in the database).

#### Test Assertion #39.1.2
The developer CAN view but CANNOT edit the details of a DT in the selected release that is in WIP state and owned by another user. He can also add comments.

#### Test Assertion #39.1.3
The developer can view the details of an DT that is in QA and owned by any user, but he cannot make any change except adding comments.

#### Test Assertion #39.1.4
The developer can view the details of an DT which is in Production state owned by any user but he cannot make any change. He also cannot make an amendment on the DT either, but he can add comments.

#### Test Assertion #39.1.5
The developer can view the details of an DT that is in Deleted, but he cannot make any change except adding comments. He cannot either restore the Deleted DT.

#### Test Assertion #39.1.6
The developer can view details of any Published DT but cannot make any change except adding comments.

#### Test Assertion #39.1.7
The developer cannot make a new revision on any DT.

#### Test Assertion #39.1.8
The developer shall not be able to create any new DT.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A release branch, i.e., one of the release branches, is selected.
2. The users, branches, releases, and records needed to exercise "Access to DT viewing, editing and commenting" are available in connectCenter.


### Test Step:
1. The developer signs in to connectCenter, opens `Core Component > View/Edit Core Component`, and selects the target release branch.
2. Verify DT visibility for the selected release and confirm that DT records in non-published release branches are not listed. (Assertion [#39.1.1](#test-assertion-3911))
3. Open DT detail pages in `WIP`, `QA`, `Production`, `Deleted`, and `Published` states and verify view, edit, comment, amend, and restore permissions by state and ownership. (Assertions [#39.1.2](#test-assertion-3912), [#39.1.3](#test-assertion-3913), [#39.1.4](#test-assertion-3914), [#39.1.5](#test-assertion-3915), [#39.1.6](#test-assertion-3916))
4. Verify that creating a new revision and creating a new DT are unavailable in this release-branch access scenario. (Assertions [#39.1.7](#test-assertion-3917), [#39.1.8](#test-assertion-3918))
