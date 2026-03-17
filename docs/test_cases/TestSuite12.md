# Test Suite 12

**Working Branch Code List Management for End User**

> Basically, the end user cannot do any editing but only view the code list.

## Test Case 12.1

**Code list access**

Pre-condition: The Working branch is selected.


### Test Assertion:

#### Test Assertion #12.1.1
The end user can see in the list developer-owned code lists in the Working branch in WIP, Draft, and Candidate states.

#### Test Assertion #12.1.2
The end user can view the details and code values of developer-owned Working-branch code lists in WIP, Draft, and Candidate states, but cannot make any change except adding comments.

#### Test Assertion #12.1.3
End-user code lists created in release branches must not be listed when the Working branch is selected.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The Working branch is selected.
2. There is an end-user account to execute the scenario.
3. There is at least one developer account that owns Working-branch code lists in WIP, Draft, and Candidate states, and each code list has at least one code value.
4. There is at least one different end-user account that owns release-branch code lists in WIP, QA, and Production states, and each code list has at least one code value.


### Test Step:
1. The end user signs in to connectCenter and opens the View/Edit Code List page.
2. Search for each developer-owned Working-branch code list by name with branch `Working` and verify that it appears in the result list. (Assertion [#12.1.1](#test-assertion-1211))
3. Open each developer-owned Working-branch code list in WIP, Draft, and Candidate states and verify that the code-list detail fields are read-only, the add-code-value action is unavailable, existing code values are viewable, and comments can be added. (Assertion [#12.1.2](#test-assertion-1212))
4. Search for each end-user-owned release-branch code list while branch `Working` is selected and verify that no result is returned. (Assertion [#12.1.3](#test-assertion-1213))
