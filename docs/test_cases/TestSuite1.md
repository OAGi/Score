# Test Suite 1


## Test Case 1.1

> Built-in OAGIS developer account exists

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #1.1.1
Built-in OAGIS developer account exists.

#### Test Assertion #1.1.2
Built-in OAGIS developer can successfully login with the valid password.

#### Test Assertion #1.1.3
Built-in OAGIS developer can log out.

#### Test Assertion #1.1.4
Built-in OAGIS developer account cannot login with an invalid password.

### Test Step Pre-condition:

1. There is no existing user session in the browser.

### Test Step:

1. A user opens the Score homepage.
2. The user logs in with the username "oagis" and valid password, namely "oagis".
3. Verify that the user successfully logged in and that it has the OAGIS developer role. (Assertion [#1](#test-assertion-111), [#2](#test-assertion-112))
4. The user logs out. (Assertion [#3](#test-assertion-113))
5. The user logs in with the username "oagis" and a random invalid password.
6. Verify that the user got notified with an invalid log in. (Assertion [#4](#test-assertion-114))

## Test Case 1.2

> OAGIS developer's authorized functionalities

Pre-condition: N/A
An OAGIS developer can access functionalities (menus) in the test assertion.


### Test Assertion:

#### Test Assertion #1.2.1
Create BIE

#### Test Assertion #1.2.2
BIE List

#### Test Assertion #1.2.3
Copy BIE

#### Test Assertion #1.2.4
Generate Expression

#### Test Assertion #1.2.5
Context Category

#### Test Assertion #1.2.6
Context Scheme

#### Test Assertion #1.2.7
Business Context

#### Test Assertion #1.2.8
Core Component

#### Test Assertion #1.2.9
View Code List (check both locations, i.e., via BIE menu and via Core Component menu)

#### Test Assertion #1.2.10
Create Code List w/o base

#### Test Assertion #1.2.11
View/Edit Module Set

#### Test Assertion #1.2.12
View/Edit Module Set Release

#### Test Assertion #1.2.13
View/Edit Release

#### Test Assertion #1.2.14
View/Edit Namespace

#### Test Assertion #1.2.15
Select a different UI terminology

#### Test Assertion #1.2.16
Manage Account

#### Test Assertion #1.2.17
Change Password

#### Test Assertion #1.2.18
Sign out

#### Test Assertion #1.2.19
Uplift BIE

#### Test Assertion #1.2.20
Agency ID list

### Test Step Pre-condition:

1. There is no existing user session in the browser.

### Test Step:

1. The user opens the system home page to log in.
2. The user logs in with an OAGIS developer account, preferably not the built-in account.
3. Verify that the menu items identified in the test assertions are accessible to the user. (Assertion [#1](#test-assertion-121) - [#20](#test-assertion-1220))