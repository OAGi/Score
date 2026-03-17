# Test Suite 4

**End user's profile management**


## Test Case 4.1

**End user's profile management**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #4.1.1
An end user cannot manage another user's profile.

#### Test Assertion #4.1.2
An end user cannot create a new user account.

#### Test Assertion #4.1.3
The `Login ID` field is not present on the end user `Settings` page.

#### Test Assertion #4.1.4
An end user can change his password to a new valid password.

#### Test Assertion #4.1.5
An end user cannot change his password to one shorter than the minimum length.

#### Test Assertion #4.1.6
An end user cannot change his password if he provides a wrong current password.

#### Test Assertion #4.1.7
An end user cannot change his password if the `New password` and `Confirm new password` fields do not match.

#### Test Assertion #4.1.8
After navigating to `Context > View/Edit Context Category`, an end user still cannot access the `Admin` menu.

#### Test Assertion #4.1.9
The SSO approval scenario for an end user is not automated yet.

### Test Step Pre-condition:

1. An end user account, euser1, exists.

### Test Step:

1. A user opens the connectCenter homepage.
2. The user logs into connectCenter using the euser1 account.
3. Verify that an end user cannot manage another user's profile because the `Admin` menu is not available. (Assertion [#1](#test-assertion-411))
4. Verify that an end user cannot create a new user account because the `Admin` menu is not available. (Assertion [#2](#test-assertion-412))
5. The user clicks the drop-down menu labelled with his account’s username.
6. The user visits the Change password page.
7. Verify that the `Login ID` field is not present on the `Settings` page. (Assertion [#3](#test-assertion-413))
8. The user provides his current password and chooses a new password that meets the password policy requirements.
9. Verify that the end user can change his password to a new valid password and log in with it. (Assertion [#4](#test-assertion-414))
10. The user visits the Change password page again.
11. The user provides his current password and chooses a password shorter than the minimum length.
12. Verify that the end user cannot change his password to one shorter than the minimum length, that the password-length validation message is returned, and that he can still log in using his previous valid password. (Assertion [#5](#test-assertion-415))
13. The user provides a wrong current password and chooses a new password meeting the password policy requirements.
14. Verify that the end user cannot change his password with a wrong current password, that the `Invalid old password` error is returned, and that he can still log in using his previous valid password. (Assertion [#6](#test-assertion-416))
15. The user provides his valid current password, provides a new password meeting the password policy requirements into the `New password` field, but a different one into the `Confirm new password` field.
16. Verify that the end user cannot change his password when the `New password` and `Confirm new password` fields do not match. (Assertion [#7](#test-assertion-417))
17. The user opens `Context > View/Edit Context Category` and verifies that the `Admin` menu is still not available after navigating there. (Assertion [#8](#test-assertion-418))
18. Note that the SSO approval scenario for an end user is present in the suite as a disabled placeholder test. (Assertion [#9](#test-assertion-419))
