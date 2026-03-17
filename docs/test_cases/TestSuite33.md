# Test Suite 33

**Code List Uplifting**


## Test Case 33.1

**Developer Code List Uplifting**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #33.1.1
The developer cannot use this functionality.

### Test Step Pre-condition:
1. A developer user account is available in connectCenter.
2. The Code List Uplifting menu is available in the application navigation.

### Test Step:
1. Sign in as a developer.
2. Try to open **BIE > Uplift Code List**.
3. Verify the developer cannot access this submenu or page.
## Test Case 33.2

**End User Code List Uplifting**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #33.2.1
The end user can visit the Uplift Code List page where he can choose a code list to uplift from a source release to a target release. The target release must be greater than the source release. The end user cannot select the Working branch.

#### Test Assertion #33.2.2
The end user can view in the Uplift Code List page end user code lists in the states and forms covered by automation (WIP, QA, Production, Deleted, Deprecated, Amended, and Derived code lists).

#### Test Assertion #33.2.3
The end user can uplift an end user code list in WIP state from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.4
The end user can uplift an end user code list in QA state from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.5
The end user can uplift an end user code list in Production state from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.6
The end user can uplift an end user code list in Deleted state from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.7
The end user can uplift an end user deprecated code list from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.8
The end user can uplift an end user amended code list from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.9
The end user can uplift an end user derived code list from a source release to a target release. All details of the code list including its code list values are transferred to the uplifted code list. The uplifted code list should be in the target release, in WIP state and its revision should be 1.

#### Test Assertion #33.2.10
The end user can uplift an end user derived code list from a source release to a target release, and overlapping values between target-release base values and uplifted end user values remain selectable after uplift.

### Test Step Pre-condition:
1. The users, releases, namespaces, and code lists needed to exercise this test case are available in connectCenter.
2. Source and target releases are prepared so that target release is newer than source release (e.g., `10.8.4` to `10.8.5`).
3. End user code lists needed for WIP, QA, Production, Deleted, Deprecated, Amended, and Derived uplift scenarios are prepared.


### Test Step:
1. Sign in as a developer and verify that **BIE > Uplift Code List** cannot be accessed. (Assertion [#33.1.1](#test-assertion-3311))
2. Sign in as an end user, open **BIE > Uplift Code List**, select source and target releases, and verify that uplift is allowed only to a newer release and that the Working branch cannot be selected as source or target. (Assertion [#33.2.1](#test-assertion-3321))
3. Verify that selectable source code lists include the covered end-user states and list forms: WIP, QA, Production, Deleted, Deprecated, Amended, and Derived. (Assertion [#33.2.2](#test-assertion-3322))
4. Uplift WIP, QA, Production, Deleted, Deprecated, and Amended end-user code lists, then verify copied details, copied values, target-release assignment, `WIP` state, and revision `1`. (Assertions [#33.2.3](#test-assertion-3323), [#33.2.4](#test-assertion-3324), [#33.2.5](#test-assertion-3325), [#33.2.6](#test-assertion-3326), [#33.2.7](#test-assertion-3327), [#33.2.8](#test-assertion-3328))
5. Uplift a derived end-user code list and verify that the expected base and derived values are present after uplift, including the overlapping-value case where values shared with the target-release base list remain selectable. (Assertions [#33.2.9](#test-assertion-3329), [#33.2.10](#test-assertion-33210))
