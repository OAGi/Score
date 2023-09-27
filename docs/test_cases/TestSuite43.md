# Test Suite 43

**Open API Document**

> *Note*: In the next sections, we use the term "user" to refer to both developers and end-users, unless there is a need to clarify.

## Test Case 43.1
**View Open API Document**

> The "Open API Document" page allows users to:
>
> - Create new Open API documents
> - View a list of all Open API documents
> - View the details of a specific Open API document
> - Discard an Open API document

### Test Assertion:

#### Test Assertion #43.1.1
User can see any Open API documents created by any roles.

#### Test Steps:
1. Sign in as a user, User A.
2. Create a new Open API document.
3. Sign in as a different user, User B.
4. Navigate to the "Open API Document" page.
5. Verify that the new Open API document created in step 2 is listed on the page.

#### Expected Result:
The new Open API document is listed on the page.

#### Test Assertion #43.1.2
User can enter the "Create Open API document page".

#### Test Steps:
1. Sign in as any user.
2. Navigate to the "Open API Document" page.
3. Click on the "Create New Open API Document" button.

#### Expected Result:
"Create Open API document" page is opened successfully.

#### Test Assertion #43.1.3
User can discard Open API documents only if they were created by the signed-in user.

#### Test Steps:
1. Sign in as any user.
2. Create a new Open API document.
3. Navigate to the "Open API Document" page.
4. Click on the checkbox to select the Open API document created in step 2. 
5. Click on the "Discard" button.

#### Expected Result:
The Open API document is discarded successfully.

#### Test Assertion #43.1.4
User cannot discard Open API documents if they were not created by the signed-in user.

#### Test Steps:
1. Sign in as a user, User A.
2. Create a new Open API document.
3. Sign in as a different user, User B.
4. Navigate to the "Open API Document" page.
5. Click on the checkbox to select the Open API document created in step 2.
6. Click on the "Discard" button.

#### Expected Result:
Discarding the Open API document fails.