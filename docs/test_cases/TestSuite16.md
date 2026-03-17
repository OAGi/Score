# Test Suite 16

**Release Branch Code List Management for Developer**

> Generally, the developer cannot do anything but viewing code lists.

## Test Case 16.1

**Code list access**

Pre-condition: The developer is on the View/Edit Code List page with a particular release branch selected.


### Test Assertion:

#### Test Assertion #16.1.1
The developer can see in the selected release branch the published developer-owned code lists for that release and the end-user-owned code lists in that release. Developer code lists that are not in the Published state are not listed in the selected release branch.

#### Test Assertion #16.1.2
The developer can view the detail of a published developer code list in the selected release branch even when it is not owned by himself.

#### Test Assertion #16.1.3
The developer can view the detail of an end-user code list in the selected release branch in WIP, Draft, and Production states; he cannot make any change, but he can add comments.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The developer is on the View/Edit Code List page with a particular release branch selected.
2. There is a developer account to execute the scenario.
3. There is at least one published developer-owned code list in the selected release branch.
4. There are end-user-owned code lists in the selected release branch in WIP, Draft, and Production states.
5. There are developer-owned Working-branch code lists in non-published states to verify that they do not appear in the selected release branch.


### Test Step:
1. The developer signs in to connectCenter, opens the View/Edit Code List page, and selects the target release branch.
2. Search for the prepared code lists and verify that the published developer code lists and release-branch end-user code lists are listed, while non-published developer code lists are not listed in the selected release branch. (Assertion [#16.1.1](#test-assertion-1611))
3. Open a published developer code list owned by another developer and verify that its detail page is accessible. (Assertion [#16.1.2](#test-assertion-1612))
4. Open end-user code lists in WIP, Draft, and Production states and verify that their details are read-only, code values are viewable, and comments can be added. (Assertion [#16.1.3](#test-assertion-1613))
