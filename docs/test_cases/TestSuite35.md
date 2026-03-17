# Test Suite 35

**Working Branch Agency ID List Management for End User**

> Basically, the end user cannot do any editing but only view the Agency ID lists.

## Test Case 35.1

**Agency ID list access**

Pre-condition: Working branch is selected.


### Test Assertion:

#### Test Assertion #35.1.1
The end user can see in the Agency ID list page, Agency ID lists, owned by developers in any state.

#### Test Assertion #35.1.2
The end user can view details of Agency ID lists and values in any state, but he cannot make any change except adding comments.

#### Test Assertion #35.1.3
Working branch does not have any end user Agency ID list; therefore, it must not have any end user Agency ID list listed.

#### Test Assertion #35.1.4
The end user cannot create an Agency ID list when the Working branch is selected.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: Working branch is selected.
2. End user and developer accounts plus working-branch developer Agency ID Lists are available in connectCenter.


### Test Step:
1. Sign in as an end user and open **Core Component > View/Edit Agency ID List** with branch set to Working.
2. Verify developer-owned Agency ID Lists in covered states are listed in the Working branch.
3. Open a developer-owned Agency ID List and verify Agency ID List details and value details are view-only while comment is available.
4. Search by end user owner in Working branch and verify no end user Agency ID Lists are returned.
5. Attempt to create a new Agency ID List in Working branch and verify creation is not allowed.
