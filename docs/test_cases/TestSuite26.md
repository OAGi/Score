# Test Suite 26

**User Guide**


## Test Case 26.1

**The User Guide is accessible**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #26.1.1
Link to the User Guide is available to the OAGIS developer and is resolved.

#### Test Assertion #26.1.2
Link to the User Guide is available to the end user and is resolved.

### Test Step Pre-condition:
1. Developer and end-user accounts are available for sign-in.
2. `TC_26_1_UserGuideIsAccessible` is annotated with `@DisabledIfLocalhost`, so the automated checks are skipped when the app is running in localhost mode.

### Test Step:
1. Sign in as a developer, open the `Help > User Guide` menu, and verify that the User Guide page title is displayed. (Assertion [#26.1.1](#test-assertion-2611))
2. Sign in as an end user, open the `Help > User Guide` menu, and verify that the User Guide page title is displayed. (Assertion [#26.1.2](#test-assertion-2612))
