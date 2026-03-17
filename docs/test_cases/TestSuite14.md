# Test Suite 14

**Working Branch Core Component Management Behaviors for End User**


## Test Case 14.1

**Access to Core component viewing, editing, and commenting**

Pre-condition: A working release is selected.


### Test Assertion:

#### Test Assertion #14.1.1
The end user can see in the CC View/Edit page the list-supported developer-owned Working-branch CCs covered by this suite, including WIP, Draft, Candidate, Release Draft, Published, and Deleted states.

#### Test Assertion #14.1.2
There shall not be any CC listed that is owned by any end user. Particularly, there shall not be any UEGACC listed in the Working branch. In other words, all working release CCs shall be owned by developers.

#### Test Assertion #14.1.3
The end user CAN view but CANNOT edit the details of a CC that is in WIP state and owned by a developer. He can add comments.

#### Test Assertion #14.1.4
The end user can view the details of a CC that is in Draft state and owned by a developer but he cannot make any change except adding comments.

#### Test Assertion #14.1.5
The end user can view the details of a Candidate CC owned by a developer, but he cannot make any change. However, he can add comments.

#### Test Assertion #14.1.6
The end user can view the details of a Release Draft CC owned by a developer, but he cannot make any change. However, he can add comments.

#### Test Assertion #14.1.7
The end user can view the details of a Published CC owned by a developer, but he cannot make any change. However, he can add comments.

#### Test Assertion #14.1.8
The end user cannot create a brand-new ACC, ASCCP or a BCCP.

#### Test Assertion #14.1.9
The end user cannot revise an existing ACC, ASCCP or a BCCP.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A working release is selected.
2. There is an end-user account to execute the scenario.
3. There are developer-owned Working-branch ACCs, ASCCPs, and BCCPs in WIP, Draft, Candidate, Release Draft, Published, and Deleted states.
4. There are end-user-owned release-branch CCs, including at least one UEGACC, to verify that they do not appear in the Working branch.

### Test Step:
1. The end user signs in to connectCenter, opens the View/Edit Core Component page, and selects the Working branch.
2. Search for the prepared developer-owned Working-branch CCs and verify that the list-supported records in the suite’s states are visible. (Assertion [#14.1.1](#test-assertion-1411))
3. Verify that end-user-owned CCs, including UEGACC, are not listed when the Working branch is selected. (Assertion [#14.1.2](#test-assertion-1412))
4. Open developer-owned CCs in WIP, Draft, Candidate, Release Draft, and Published states and verify that they are view-only, comments are allowed, and revising is not available where applicable. (Assertions [#14.1.3](#test-assertion-1413), [#14.1.4](#test-assertion-1414), [#14.1.5](#test-assertion-1415), [#14.1.6](#test-assertion-1416), [#14.1.7](#test-assertion-1417))
5. Verify that creating a new ACC, ASCCP, or BCCP is not available to the end user in the Working branch. (Assertion [#14.1.8](#test-assertion-1418))
6. Verify that revising existing ACCs, ASCCPs, and BCCPs is not available to the end user. (Assertion [#14.1.9](#test-assertion-1419))
