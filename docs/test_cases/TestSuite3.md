# Test Suite 3

**End user authorized functions**


## Test Case 3.1

**End user's authorized functionalities**

> A Score end user can access the functionalities listed in the test assertions.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #3.1.1
- `BIE > View/Edit BIE` menu is accessible.
- `BIE > Create BIE` menu is accessible.
- `BIE > Copy BIE` menu is accessible.
- `BIE > Uplift BIE` menu is accessible.
- `BIE > Express BIE` menu is accessible.
- `BIE > BIE Package` menu is accessible.
- `BIE > OpenAPI Document` menu is accessible.
- `BIE > Reuse Report` menu is accessible.
- `BIE > View/Edit Code List` menu is accessible.
- `BIE > Uplift Code List` menu is accessible.

#### Test Assertion #3.1.2
- `Context > View/Edit Context Category` menu is accessible.
- `Context > View/Edit Context Scheme` menu is accessible.
- `Context > View/Edit Business Context` menu is accessible.

#### Test Assertion #3.1.3
- `Core Component > View/Edit Core Component` menu is accessible when the `Core Component` menu is shown.
- `Core Component > View/Edit Data Type` menu is accessible when the `Core Component` menu is shown.
- `Core Component > View/Edit Code List` menu is accessible when the `Core Component` menu is shown.
- `Core Component > View/Edit Agency ID List` menu is accessible when the `Core Component` menu is shown.
- `Core Component > View/Edit Release` menu is accessible when the `Core Component` menu is shown.
- `Core Component > View/Edit Namespace` menu is accessible when the `Core Component` menu is shown.

#### Test Assertion #3.1.4
- `Module > View Module Set` menu is accessible.
- `Module > View Module Set Release` menu is accessible.

#### Test Assertion #3.1.5
- `Library > View Library` menu is accessible for an end user account.

#### Test Assertion #3.1.6
- `Admin` menu is hidden from an end user account.

#### Test Assertion #3.1.7
- `Help > About` menu is accessible.
- `Help > User Guide` menu is accessible when the test host is not `localhost`.

#### Test Assertion #3.1.8
- The notification icon is accessible and can navigate to the `Message` page.

#### Test Assertion #3.1.9
The login ID menu shows `Signed in as <loginId>`.

#### Test Assertion #3.1.10
The `connectSpec Terminology` option is enabled and mutually exclusive with `CCTS Terminology`.

#### Test Assertion #3.1.11
The `CCTS Terminology` option is enabled and mutually exclusive with `connectSpec Terminology`.

#### Test Assertion #3.1.12
- `Login ID > Settings` menu is accessible.
- `Login ID > Logout` is accessible and returns the user to the login page.

### Test Step Pre-condition:

1. There is no existing user session in the browser.
2. An end user account is available for the test. The automated test creates a random end user account.

### Test Step:

1. Open the Score login page.
2. Log in with an end user account.
3. Verify the `BIE` menu items listed in Assertion [#1](#test-assertion-311).
4. Verify the `Context` menu items listed in Assertion [#2](#test-assertion-312).
5. Verify the `Core Component` menu items in Assertion [#3](#test-assertion-313) when the `Core Component` menu is shown.
6. Verify the `Module` menu items in Assertion [#4](#test-assertion-314).
7. Verify the `Library` menu item in Assertion [#5](#test-assertion-315).
8. Verify that the `Admin` menu is hidden. (Assertion [#6](#test-assertion-316))
9. Verify the `Help` menu items in Assertion [#7](#test-assertion-317). If the test host is `localhost`, the `User Guide` check may be skipped.
10. Verify the notification icon behavior in Assertion [#8](#test-assertion-318), including navigation to the `Message` page.
11. Verify the login ID label in Assertion [#9](#test-assertion-319).
12. Verify the terminology toggle behavior in Assertions [#10](#test-assertion-3110) and [#11](#test-assertion-3111).
13. Verify the `Settings` and `Logout` items in Assertion [#12](#test-assertion-3112).


## Test Case 3.2

**End user's authorized functionalities in standard browsing mode**

> A Score end user in standard browsing mode sees the `Browse Standard` menu instead of the `Core Component` menu.

Pre-condition:

1. Standard browsing mode is enabled through the application settings.


### Test Assertion:

#### Test Assertion #3.2.1
- The `Browse Standard` menu is shown to an end user account.
- The `Core Component` menu is hidden from an end user account.
- The `Browse Standard` menu navigates to the core component browsing page.

### Test Step Pre-condition:

1. There is no existing user session in the browser.
2. An end user account is available for the test. The automated test creates a random end user account.
3. Standard browsing mode is enabled through the application settings API before login.

### Test Step:

1. Enable standard browsing mode through the application settings API.
2. Open the Score login page.
3. Log in with an end user account.
4. Verify the `Browse Standard` menu is shown and the `Core Component` menu is hidden. (Assertion [#1](#test-assertion-321))
5. Open the `Browse Standard` menu and verify navigation to the core component browsing page. (Assertion [#1](#test-assertion-321))


## Test Case 3.3

**End user's authorized functionalities in tenant mode**

> An end user account without tenant roles sees tenant-mode restrictions in the navbar.

Pre-condition:

1. Tenant mode is enabled through the application settings.


### Test Assertion:

#### Test Assertion #3.3.1
- For an end user account without tenant roles, `BIE > Create BIE` menu is present but disabled.
- For an end user account without tenant roles, `BIE > View/Edit Code List` menu is present but disabled.
- For an end user account without tenant roles, `BIE > Uplift Code List` menu is present but disabled.

#### Test Assertion #3.3.2
- For an end user account without tenant roles, `Context` menu is hidden.
- For an end user account without tenant roles, `Module` menu is hidden.
- For an end user account without tenant roles, `Library` menu is hidden.

### Test Step Pre-condition:

1. There is no existing user session in the browser.
2. An end user account without tenant roles is available for the test. The automated test creates a random end user account.
3. Tenant mode is enabled through the application settings API before login.

### Test Step:

1. Enable tenant mode through the application settings API.
2. Open the Score login page.
3. Log in with an end user account without tenant roles.
4. Verify the disabled `BIE` menu items in Assertion [#1](#test-assertion-331).
5. Verify the hidden menus in Assertion [#2](#test-assertion-332).
