# Test Suite 4


## Test Case 4.1

> End user's profile management

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #4.1.1
An end user cannot manage another users’ profile.

#### Test Assertion #4.1.2
An end user cannot create a new user account.

#### Test Assertion #4.1.3
An end user cannot change his username (login id).

#### Test Assertion #4.1.4
An end user can change his password to a valid password.

#### Test Assertion #4.1.5
An end user cannot change his password to one that violates password rules.

#### Test Assertion #4.1.6
An end user cannot change his password in case he provides a wrong current password.

#### Test Assertion #4.1.7
An end user cannot change his password in case the New password and Confirm new password fields do not match.

#### Test Assertion #4.1.8
An end user cannot disable any other accounts.

#### Test Assertion #4.1.9
An end user cannot approve single sign account request.

### Test Step Pre-condition:

1. An end user account, euser1, exists.

### Test Step:

1. A user opens the Score homepage.
2. The user logs into the Score using the euser1 account.
3. Verify that the Admin menu does not exist (Assertion [#1](#test-assertion-411), [#2](#test-assertion-412))
4. The user clicks the drop-down menu labelled with his account’s username.
5. The user visits the Change password page.
6. Verify that the Login Id field does not exist. (Assertion [#3](#test-assertion-413))
7. He provides his current password and chooses a password that meets the password policy requirements.
8. Verify that the user can login with the new password (e.g., by verifying that the label of the top-right menu matches with the username). (Assertion [#4](#test-assertion-414))
9. The user visits the Change password page.
10. He provides his current password and chooses a password that does not meet the password policy requirements.
11. Verify that an error message has returned and that he can still login using his previous valid password. (Assertion [#5](#test-assertion-415))
12. The user provides a wrong current password and chooses a new password meeting the password policy requirements.
13. Verify that an error message has returned and that he can still login using his previous valid password. (Assertion [#6](#test-assertion-416))
14. The user provides his valid current password, provides a new one meeting the password policy requirements into the New password field, but a different one into the Confirm new password field.
15. Verify that the Update button is disabled. (Assertion [#7](#test-assertion-417))