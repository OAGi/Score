# Test Suite 1

**OAGIS developer Authentication and Authorized Functions**


## Test Case 1.1

**Built-in OAGIS developer account exists**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #1.1.1
Built-in OAGIS developer can successfully log in with the built-in account credentials.

#### Test Assertion #1.1.2
After successful login, the built-in account is identified as a developer account.

#### Test Assertion #1.1.3
Built-in OAGIS developer can log out and is redirected back to the home page.

#### Test Assertion #1.1.4
Built-in OAGIS developer account cannot log in with an invalid password and receives the message `Invalid username or password`.

#### Test Assertion #1.1.5
A random non-existent username cannot log in and receives the message `Invalid username or password`.

### Test Step Pre-condition:

1. There is no existing user session in the browser.

### Test Step:

1. A user opens the Score login page.
2. The user logs in with the built-in OAGIS developer account credentials.
3. Verify successful login. (Assertion [#1](#test-assertion-111))
4. Verify that the signed-in account is shown as a developer account. (Assertion [#2](#test-assertion-112))
5. The user logs out.
6. Verify that the browser is redirected back to the home page. (Assertion [#3](#test-assertion-113))
7. The user tries to log in again with username `oagis` and a random invalid password.
8. Verify that the user receives `Invalid username or password`. (Assertion [#4](#test-assertion-114))
9. The user tries to log in with a random username and a random password.
10. Verify that the user receives `Invalid username or password`. (Assertion [#5](#test-assertion-115))

## Test Case 1.2

**OAGIS developer's authorized functionalities**

> A developer account can access the menus and controls listed below. One assertion is conditional and is skipped when the test host is `localhost`.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #1.2.1
- `BIE > View/Edit BIE` menu is accessible.
- `BIE > Create BIE` menu is accessible.
- `BIE > Copy BIE` menu is accessible.
- `BIE > Uplift BIE` menu is accessible.
- `BIE > Express BIE` menu is accessible.
- `BIE > BIE Package` menu is accessible.
- `BIE > Reuse Report` menu is accessible.
- `BIE > View/Edit Code List` menu is accessible.
- `BIE > Uplift Code List` menu is present but disabled.

#### Test Assertion #1.2.2
- `Context > View/Edit Context Category` menu is accessible.
- `Context > View/Edit Context Scheme` menu is accessible.
- `Context > View/Edit Business Context` menu is accessible.

#### Test Assertion #1.2.3
- `Core Component > View/Edit Core Component` menu is accessible.
- `Core Component > View/Edit Data Type` menu is accessible.
- `Core Component > View/Edit Code List` menu is accessible.
- `Core Component > View/Edit Agency ID List` menu is accessible.
- `Core Component > View/Edit Release` menu is accessible.
- `Core Component > View/Edit Namespace` menu is accessible.

#### Test Assertion #1.2.4
- `Module > View/Edit Module Set` menu is accessible.
- `Module > View/Edit Module Set Release` menu is accessible.

#### Test Assertion #1.2.5
- `Library > View Library` menu is accessible for a developer account.

#### Test Assertion #1.2.6
- `Admin` menu is hidden from a developer account.

#### Test Assertion #1.2.7
- `Help > About` menu is accessible.
- `Help > User Guide` menu is accessible when the test host is not `localhost`.

#### Test Assertion #1.2.8
- The notification icon is accessible and can navigate to the `Message` page.

#### Test Assertion #1.2.9
The login ID menu shows `Signed in as <loginId>`.

#### Test Assertion #1.2.10
The `connectSpec Terminology` option is enabled and mutually exclusive with `CCTS Terminology`.

#### Test Assertion #1.2.11
The `CCTS Terminology` option is enabled and mutually exclusive with `connectSpec Terminology`.

#### Test Assertion #1.2.12
- `Login ID > Settings` menu is accessible.
- `Login ID > Logout` is accessible and returns the user to the login page.

### Test Step Pre-condition:

1. There is no existing user session in the browser.
2. A developer account is available for the test. The automated test creates a random developer account.

### Test Step:

1. Open the Score login page.
2. Log in with a developer account.
3. Verify the `BIE` menu items listed in Assertion [#1](#test-assertion-121), including that `Uplift Code List` is present but disabled.
4. Verify the `Context` menu items listed in Assertion [#2](#test-assertion-122).
5. Verify the `Core Component` menu items described in Assertion [#3](#test-assertion-123).
6. Verify the `Module` menu items in Assertion [#4](#test-assertion-124).
7. Verify the `Library` menu item in Assertion [#5](#test-assertion-125).
8. Verify that the `Admin` menu is hidden. (Assertion [#6](#test-assertion-126))
9. Verify the `Help` menu items in Assertion [#7](#test-assertion-127). If the test host is `localhost`, the `User Guide` check may be skipped.
10. Verify the notification icon behavior in Assertion [#8](#test-assertion-128), including navigation to the `Message` page.
11. Verify the login ID label in Assertion [#9](#test-assertion-129).
12. Verify the terminology toggle behavior in Assertions [#10](#test-assertion-1210) and [#11](#test-assertion-1211).
13. Verify the `Settings` and `Logout` items in Assertion [#12](#test-assertion-1212).

## Test Case 1.3

**OAGIS developer's authorized functionalities in tenant mode**

> A developer account without tenant roles sees tenant-mode restrictions in the navbar.

Pre-condition:

1. Tenant mode is enabled through the application settings.


### Test Assertion:

#### Test Assertion #1.3.1
- For a developer account without tenant roles, `BIE > Create BIE` menu is present but disabled.
- For a developer account without tenant roles, `BIE > View/Edit Code List` menu is present but disabled.
- For a developer account without tenant roles, `BIE > Uplift Code List` menu is present but disabled.

#### Test Assertion #1.3.2
- For a developer account without tenant roles, `Context` menu is hidden.
- For a developer account without tenant roles, `Module` menu is hidden.
- For a developer account without tenant roles, `Library` menu is hidden.

### Test Step Pre-condition:

1. There is no existing user session in the browser.
2. A developer account without tenant roles is available for the test. The automated test creates a random developer account.
3. Tenant mode is enabled through the application settings API before login.

### Test Step:

1. Enable tenant mode through the application settings API.
2. Open the Score login page.
3. Log in with a developer account without tenant roles.
4. Verify the disabled `BIE` menu items in Assertion [#1](#test-assertion-131).
5. Verify the hidden menus in Assertion [#2](#test-assertion-132).
