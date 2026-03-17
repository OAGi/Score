# Test Suite 18

**Draft Release Branch Core Component and Code List Access by Developers and End Users**


## Test Case 18.1

**Core Component Access**

Pre-condition: The draft release branch is selected on the View/Edit Core Component page.


### Test Assertion:

#### Test Assertion #18.1.1
The developer can search and view CCs in the selected draft release branch, but cannot create a new CC from that branch.

#### Test Assertion #18.1.2
In the draft release branch created by this suite, only the candidate-derived CC content assigned into the draft release is listed. Searching with WIP, QA, Production, or Deleted state filters returns no rows.

#### Test Assertion #18.1.3
The end user can search and view CCs in the selected draft release branch, but cannot create a new CC from that branch.

#### Test Assertion #18.1.4
For the end user as well, searching the draft release branch with WIP, QA, Production, or Deleted state filters returns no rows.

#### Test Assertion #18.1.5
Verify that the draft release content differs from earlier released content for the scenarios currently covered by this suite:

##### Test Assertion #18.1.5.a
New property added to an ACC.
##### Test Assertion #18.1.5.b
A revised code list in the draft release exposes the revised version and definition.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The draft release branch is selected on the View/Edit Core Component page.
2. A developer-owned Working-branch candidate ACC is prepared and changed before creating the draft release.
3. The suite creates a draft release from an initialized release and assigns candidate Working-branch content into it.
4. The test users needed to verify developer and end-user access are available in connectCenter.

### Test Step:
1. The developer signs in, opens the View/Edit Core Component page, selects the generated draft release branch, and verifies that only search and view actions are available.
2. Search the draft release for the prepared candidate ACC and verify that non-candidate Working-branch CCs are not listed. Then apply WIP, QA, Production, and Deleted filters and verify that no rows are returned. (Assertions [#18.1.1](#test-assertion-1811), [#18.1.2](#test-assertion-1812))
3. The end user signs in, opens the same draft release branch, and verifies the same read-only access pattern with no CC creation available. (Assertions [#18.1.3](#test-assertion-1813), [#18.1.4](#test-assertion-1814))
4. Open the candidate-derived ACC in the draft release and verify that the added association is present. Then open the revised code list in the draft release and verify that the revised version and definition are visible. (Assertion [#18.1.5](#test-assertion-1815))
## Test Case 18.2

**Code List Access**

Pre-condition: The draft release branch is selected on the View/Edit Core List page.


### Test Assertion:

#### Test Assertion #18.2.1
The developer can search and view draft-release code lists, but cannot create a new code list, revise it, or edit its details from that branch.

#### Test Assertion #18.2.2
In the draft release branch created by this suite, only the candidate-derived code list assigned into the draft release is listed. Searching with WIP, QA, Production, or Deleted state filters returns no rows.

#### Test Assertion #18.2.3
The end user can search and view draft-release code lists, but cannot create a new code list or derive a new code list from that branch.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The draft release branch is selected on the View/Edit Core List page.
2. A developer-owned Working-branch candidate code list with at least one code list value is prepared and included in the generated draft release.
3. The developer and end-user accounts needed for the access checks are available in connectCenter.


### Test Step:
1. The developer signs in, opens the View/Edit Code List page, selects the generated draft release branch, and verifies that no new code list action is available.
2. Open the candidate-derived code list from the draft release and verify that the code list fields, code list value dialog fields, and revise action are read-only or unavailable. (Assertion [#18.2.1](#test-assertion-1821))
3. Apply WIP, QA, Production, and Deleted filters in the draft release branch and verify that no rows are returned. (Assertion [#18.2.2](#test-assertion-1822))
4. The end user signs in, opens the same draft release branch, opens the candidate-derived code list, and verifies that the code list is view-only and that derive/create actions are unavailable. (Assertion [#18.2.3](#test-assertion-1823))
