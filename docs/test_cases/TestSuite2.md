# Test Suite 2

**OAGIS developer can manage users**


## Test Case 2.1

**OAGIS developer can manage OAGIS developer accounts**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #2.1.1
An OAGIS developer can successfully create a developer account with all valid information.

#### Test Assertion #2.1.2
An OAGIS developer cannot create a developer account with a duplicate login ID.

#### Test Assertion #2.1.3
An OAGIS developer cannot create a developer account that does not meet password rules.

#### Test Assertion #2.1.4
An OAGIS developer can update the log in ID of another developer account.

#### Test Assertion #2.1.5
An OAGIS developer can update the password of another developer account with a new password that meets password rules.

#### Test Assertion #2.1.6
An OAGIS developer cannot update the password of another developer account with a new password that does not meet password rules.

#### Test Assertion #2.1.7
An OAGIS developer can update the log in ID of his developer account with a new one.

#### Test Assertion #2.1.8
An OAGIS developer can update the password of his developer account with a new password that meet password rules.

#### Test Assertion #2.1.9
An OAGIS developer cannot update his password via Change Password page if he enters an invalid old password.

#### Test Assertion #2.1.10
An OAGIS developer cannot update the password of his developer account with a new password that does not meet password rules.

#### Test Assertion #2.1.11
An OAGIS developer cannot update his password if the New password and the Confirm new password field do not match.

#### Test Assertion #2.1.12
An OAGIS developer can disable an OAGIS developer account. Disable account cannot be used for logging in to the application.

#### Test Assertion #2.1.13
An OAGIS developer can enable a disabled OAGIS developer account. Enabled account can be used for logging in to the application.

#### Test Assertion #2.1.14
An OAGIS developer can approve or reject a single sign on request by another OAGIS developer when the application is set up with SSO.

### Test Step Pre-condition:

1. Developer user accounts to be created do not exist.

### Test Step:

1. Logs into the Score with the built-in developer account.
2. The developer goes to the user management page.
3. The developer creates a new developer user account with all valid information.
4. The built-in developer user logs out.
5. Logs into the Score with the new developer account.
6. Verify that the log in was successful (e.g., by verifying that the label of the top-right menu matches with the username). (Assertion [#1](#test-assertion-211))
7. The built-in developer user tries to create a new developer user account with all valid information, however, with the same login id as the one of the developer user created in step #3.
8. Verify that the system reports an error and that the developer created in step [#3.can](#test-assertion-213can) log out and still log in with the same password. (Assertion [#2](#test-assertion-212))
9. The built-in developer user tries to create a new developer user account with all valid information except the password. The password does not meet the password rule.
10. Verify that, the Create button is disabled and that the new developer user account is not created (the username is not listed). Note that this and the previous steps may be repeated for various password rules. (Assertion [#3](#test-assertion-213))
11. The built-in developer opens the user profile of the OAGIS developer user created in step #3.
12. He tries to change the user id.
13. Verify that this field (login id) is disabled. (Assertion [#4](#test-assertion-214))
14. The built-in developer changes the password of the above developer with a new valid one.
15. Verify that the developer user’s password has been changed (Assertion [#5](#test-assertion-215))
16. The built-in developer changes the password of the user in step [#3.to](#test-assertion-213to) an invalid one.
17. Verify that an error box is displayed, the Update button is disabled and that the user can still log in with his previous valid credentials. (Assertion [#6](#test-assertion-216))
18. The developer user created in step [#3.open](#test-assertion-213open) his user profile.
19. He tries to change his user id.
20. Verify that the login id field is disabled (Assertion [#7](#test-assertion-217))
21. He changes his password to a new valid one.
22. Verify that he can login with the new password (Assertion [#8](#test-assertion-218))
23. He changes his password from the Change Password page by providing both the old and the new one
24. Verify that he can login with the new password (Assertion [#8](#test-assertion-218))
25. He goes to the Change Password page.
26. He tries to change his password by inserting an invalid old password to the corresponding field.
27. Verify that an error message is returned and that he can login with the old password. (Assertion [#9](#test-assertion-219))
28. He changes his password via Accounts page with a password that does not meet the password rule.
29. Verify that an error box is displayed, the Update button is disabled and that he can still log in with the old password. (Assertion [#10](#test-assertion-2110))
30. He changes his password via Change password page with a password that does not meet the password rule.
31. Verify that an error box is displayed, the Update button is disabled and that he can still log in with the old password. (Assertion [#10](#test-assertion-2110))
32. He changes his password via Accounts page by inserting different passwords in the New password and Confirm new password fields.
33. Verify that the Update button is disabled. (Assertion [#11](#test-assertion-2111))
34. He changes his password via Change password page by inserting different passwords in the New password and Confirm new password fields.
35. Verify that the Update button is disabled. (Assertion [#11](#test-assertion-2111))

## Test Case 2.2

**OAGIS developer can manage end user accounts**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #2.2.1
An OAGIS developer can successfully create an end user account with all valid information.

#### Test Assertion #2.2.2
An OAGIS developer cannot create an end user account with a duplicate login ID.

#### Test Assertion #2.2.3
An OAGIS developer cannot create an end user account that does not meet password rules.

#### Test Assertion #2.2.4
An OAGIS developer can update the log in ID of an end user account with a new unique log in ID.

#### Test Assertion #2.2.5
An OAGIS developer can update the password of an end user account with a new password that meet password rules.

#### Test Assertion #2.2.6
An OAGIS developer cannot update the password of an end user account with a new password that does not meet password rules.

#### Test Assertion #2.2.7
An OAGIS developer can disable an end user account. Disable account cannot be used for logging in to the application.

#### Test Assertion #2.2.8
An OAGIS developer can enable a disabled end user account. Enable account can be used for logging in to the application.

#### Test Assertion #2.2.9
An OAGIS developer can approve or reject a single sign on request by an end user when the application is set up with SSO.

### Test Step Pre-condition:

1. The end user accounts to be created do not exist.

### Test Step:

1. Logs into the Score with the built-in developer account.
2. The developer goes to the Accounts page.
3. The developer creates a new (say eusername1) end user account with all valid information.
4. The built-in developer user logs out.
5. Logs into the Score with the new end user account.
6. Verify that the log in was successful (e.g., by verifying that the label of the top-right menu matches with the username). (Assertion [#1](#test-assertion-221))
7. The end user logs out.
8. The user logs into the Score with the built-in developer account.
9. The developer user tries to create a new end user account with all valid information, however, with the same username as the previous end user account.
10. Verify that the system reports an error. (Assertion [#2](#test-assertion-222))
11. The developer user tries to create a new end user account with all valid information except the password. The password does not meet the password rule.
12. Verify that an error box is displayed and that the Create button is disabled. Note that this and the previous steps may be repeated for various password rules. (Assertion [#3](#test-assertion-223))
13. The OAGIS developer tries to update the end user account username of eusername1 to, say, eusername2 that is unique to the system.
14. Verify that the login ID field is disabled. (Assertion [#4](#test-assertion-224))
15. The OAGIS developer changes the password of the end user account to a new valid password.
16. The OAGIS developer log out.
17. Verify that the end user can log in with the new username and password. (Assertion [#5](#test-assertion-225))
18. The OAGIS developer updates the password of the end user account to a password that does not meet password rules.
19. Verify that an error box is displayed, the Update button is disabled and that the eusername1 can still login using the previous valid password. (Assertion [#6](#test-assertion-226))