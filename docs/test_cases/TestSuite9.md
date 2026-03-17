# Test Suite 9

**Data retention**


## Test Case 9.1

**No user account can be deleted**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #9.1.1
End user accounts can never be deleted.

#### Test Assertion #9.1.2
OAGi developer accounts can never be deleted.

### Test Step Pre-condition:
1. An administrator (for example, `oagis`) can sign in and open `Admin > Account`.
2. The suite can create temporary end-user and developer accounts for the verification flow.
3. Account cleanup after execution is available so test-created users can be removed.

### Test Step:

1. Sign in as the administrator, open `Admin > Account`, create a temporary end-user account, open its edit page, and verify the delete action is not available. (Assertion [#1](#test-assertion-911))
2. Create a temporary developer account, open its edit page, and verify the delete action is not available there as well. (Assertion [#2](#test-assertion-912))
