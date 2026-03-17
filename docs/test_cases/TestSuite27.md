# Test Suite 27

**About Page**


## Test Case 27.1

**About Page Contains Following Information**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #27.1.1
The about page contains the following information

##### Test Assertion #27.1.1.a
The version of the application in the “score-web” field.
##### Test Assertion #27.1.1.b
The version of the API service in the “score-http” field.
##### Test Assertion #27.1.1.c
The version of the database/server in the “MariaDB” field.
##### Test Assertion #27.1.1.d
The version of the Redis application (cache) in the “Redis” field.
##### Test Assertion #27.1.1.e
Link to the contributors of the application.
##### Test Assertion #27.1.1.f
The License of the application.

### Test Step Pre-condition:
1. A developer account is available for sign-in.
2. The About page is accessible from the Help menu.

### Test Step:
1. Sign in as a developer and open `Help > About`.
2. Verify that the About page shows version rows for `score-web`, `score-http`, `MariaDB`, and `Redis`. (Assertions [#27.1.1.a](#test-assertion-2711a), [#27.1.1.b](#test-assertion-2711b), [#27.1.1.c](#test-assertion-2711c), [#27.1.1.d](#test-assertion-2711d))
3. Verify that the contributors link is displayed and that the license section is displayed. (Assertions [#27.1.1.e](#test-assertion-2711e), [#27.1.1.f](#test-assertion-2711f))
