# Test Suite 2

**OAGIS developer can manage users**


## Test Case 2.1

**OAGIS developer can manage OAGIS developer accounts**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #2.1.1
An OAGIS developer can successfully create a developer account with valid information.

#### Test Assertion #2.1.2
An OAGIS developer cannot create a developer account with a duplicate login ID.

#### Test Assertion #2.1.3
An OAGIS developer cannot create a developer account with a password shorter than the minimum length.

#### Test Assertion #2.1.4
The login ID field of another developer account is enabled on the edit account page.

#### Test Assertion #2.1.5
An OAGIS developer can update the password of another developer account with a new valid password.

#### Test Assertion #2.1.6
An OAGIS developer cannot update the password of another developer account with a password shorter than the minimum length.

#### Test Assertion #2.1.7
The login ID field of the current administrator account is enabled on the edit account page.

#### Test Assertion #2.1.8
A developer user can update the password of his developer account with a new valid password from the `Settings` page.

#### Test Assertion #2.1.9
A developer user cannot update his password from the `Settings` page if he enters an invalid old password.

#### Test Assertion #2.1.10
A developer user cannot update his password from the `Settings` page with a password shorter than the minimum length.

#### Test Assertion #2.1.11
A developer user cannot update his password from the `Settings` page if the `New password` and `Confirm new password` fields do not match.

#### Test Assertion #2.1.12
An OAGIS developer can disable another developer account. A disabled account cannot be used to log in to the application.

#### Test Assertion #2.1.13
An OAGIS developer can enable a disabled developer account. The enabled account can be used to log in to the application.

#### Test Assertion #2.1.14
The SSO approval or rejection scenario for another developer account is not automated yet.

### Test Step Pre-condition:

1. The developer accounts to be created do not exist.
2. The built-in `oagis` account is available and can access the `Admin > Account` page.

### Test Step:

1. Log in with the built-in `oagis` account.
2. Open `Admin > Account`.
3. Create a new developer account with valid information and verify that the account is created successfully and can log in. (Assertion [#1](#test-assertion-211))
4. Attempt to create a developer account with a duplicate login ID and verify that creation is rejected. (Assertion [#2](#test-assertion-212))
5. Attempt to create a developer account with a password shorter than the minimum length and verify that creation is rejected with the password-length validation message. (Assertion [#3](#test-assertion-213))
6. Open another developer account on the edit account page and verify that the login ID field is enabled. (Assertion [#4](#test-assertion-214))
7. Update another developer account password with a new valid password and verify that the account can log in with the new password. (Assertion [#5](#test-assertion-215))
8. Attempt to update another developer account password with a password shorter than the minimum length and verify that the update is rejected and the previous password still works. (Assertion [#6](#test-assertion-216))
9. Open the current administrator account on the edit account page and verify that the login ID field is enabled. (Assertion [#7](#test-assertion-217))
10. Log in with a developer account, open `Settings`, update the password with a new valid password, and verify that the new password works. (Assertion [#8](#test-assertion-218))
11. Attempt to update the developer password from `Settings` with an invalid old password and verify that the `Invalid old password` error is returned. (Assertion [#9](#test-assertion-219))
12. Attempt to update the developer password from `Settings` with a password shorter than the minimum length and verify that the password-length validation message is returned. (Assertion [#10](#test-assertion-2110))
13. Attempt to update the developer password from `Settings` with mismatched `New password` and `Confirm new password` values and verify that the update is rejected. (Assertion [#11](#test-assertion-2111))
14. Disable another developer account and verify that the disabled account cannot be used to log in to the application. (Assertion [#12](#test-assertion-2112))
15. Enable a disabled developer account and verify that the enabled account can be used to log in to the application again. (Assertion [#13](#test-assertion-2113))
16. Note that the SSO approval or rejection scenario for another developer account is present in the suite as a disabled placeholder test. (Assertion [#14](#test-assertion-2114))

## Test Case 2.2

**OAGIS developer can manage end user accounts**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #2.2.1
An OAGIS developer can successfully create an end user account with valid information.

#### Test Assertion #2.2.2
An OAGIS developer cannot create an end user account with a duplicate login ID.

#### Test Assertion #2.2.3
An OAGIS developer cannot create an end user account with a password shorter than the minimum length.

#### Test Assertion #2.2.4
The login ID field of another end user account is enabled on the edit account page.

#### Test Assertion #2.2.5
An OAGIS developer can update the password of another end user account with a new valid password.

#### Test Assertion #2.2.6
An OAGIS developer cannot update the password of another end user account with a password shorter than the minimum length.

#### Test Assertion #2.2.7
An OAGIS developer can disable an end user account. A disabled account cannot be used to log in to the application.

#### Test Assertion #2.2.8
An OAGIS developer can enable a disabled end user account. The enabled account can be used to log in to the application.

#### Test Assertion #2.2.9
The SSO approval or rejection scenario for an end user account is not automated yet.

### Test Step Pre-condition:

1. The end user accounts to be created do not exist.
2. The built-in `oagis` account is available and can access the `Admin > Account` page.

### Test Step:

1. Log in with the built-in `oagis` account.
2. Open `Admin > Account`.
3. Create a new end user account with valid information and verify that the account is created successfully and can log in. (Assertion [#1](#test-assertion-221))
4. Attempt to create an end user account with a duplicate login ID and verify that creation is rejected. (Assertion [#2](#test-assertion-222))
5. Attempt to create an end user account with a password shorter than the minimum length and verify that creation is rejected with the password-length validation message. (Assertion [#3](#test-assertion-223))
6. Open another end user account on the edit account page and verify that the login ID field is enabled. (Assertion [#4](#test-assertion-224))
7. Update another end user account password with a new valid password and verify that the account can log in with the new password. (Assertion [#5](#test-assertion-225))
8. Attempt to update another end user account password with a password shorter than the minimum length and verify that the update is rejected and the previous password still works. (Assertion [#6](#test-assertion-226))
9. Disable another end user account and verify that the disabled account cannot be used to log in to the application. (Assertion [#7](#test-assertion-227))
10. Enable a disabled end user account and verify that the enabled account can be used to log in to the application again. (Assertion [#8](#test-assertion-228))
11. Note that the SSO approval or rejection scenario for an end user account is present in the suite as a disabled placeholder test. (Assertion [#9](#test-assertion-229))
