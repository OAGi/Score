# Test Suite 20

**Namespace Management**

> Namespaces have no state. It has an owner which is the creator. Ownership transfer functionality should be available to transfer to the same kind of user. Namespace details are visible to anybody. A namespace cannot be discarded if it is used. If the namespace is created by a developer, it is considered a standard namespace; otherwise, it gets the is_std_nmsp value false. Details of namespaces should be visible within one table and expose the following columns GUID, Owner, URI, Prefix, Description, Standard (flag), Updated (date time). The table should allow the user to sort by those columns. The user should be able to filter namespaces using GUID, URI, Prefix, Owner, and Standard flag. Namespace Management menu item should be under the Core Component menu.

## Test Case 20.1

**Developer management of namespaces**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #20.1.1
The developer can create a new namespace. He becomes the owner of the namespace. The following business rules must be satisfied.

##### Test Assertion #20.1.1.a
URI is required and unique. The system should not validate the URI syntax to be a valid URI, i.e., this is actually a text field.
##### Test Assertion #20.1.1.b
Prefix is required and unique.
##### Test Assertion #20.1.1.c
Description is optional.
##### Test Assertion #20.1.1.d
Standard flag (is_std_nmsp) is True and locked.

#### Test Assertion #20.1.2
The developer who is an owner of the namespace can change details of an existing namespace. The modified details must satisfy the business rules same as those indicated in case of a new namespace.

#### Test Assertion #20.1.3
The developer and end user who does not own the namespace cannot update it.

#### Test Assertion #20.1.4
The developer who is the owner of the namespace can discard it if the namespace has no reference.

#### Test Assertion #20.1.5
Any user can see the detail of developer namespace.

#### Test Assertion #20.1.6
The owner developer of the namespace can transfer ownership only to another developer.

### Test Step Pre-condition:
1. Developer and end-user accounts needed for namespace scenarios are available in connectCenter.
2. Existing standard and end-user namespaces are available so that uniqueness, visibility, and ownership-transfer checks can be exercised.

### Test Step:
1. The developer signs in to connectCenter and opens the View/Edit Namespace page.
2. Create a developer namespace and verify the required URI and prefix rules, the locked standard flag, and duplicate-URI validation. (Assertion [#20.1.1](#test-assertion-2011))
3. Open the created namespace as the owner, update its details, and verify that uniqueness rules are still enforced. (Assertion [#20.1.2](#test-assertion-2012))
4. Open namespaces owned by other users and verify that developer and end-user non-owners have read-only access. (Assertions [#20.1.3](#test-assertion-2013), [#20.1.5](#test-assertion-2015))
5. Discard an unused developer-owned namespace and verify it is removed from the list. Then transfer namespace ownership to another developer and verify the new owner. (Assertions [#20.1.4](#test-assertion-2014), [#20.1.6](#test-assertion-2016))
## Test Case 20.2

**End user management of namespaces**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #20.2.1
The end user can create a new namespace. He becomes the owner of the namespace. The following business rules must be satisfied.

##### Test Assertion #20.2.1.a
URI is required and unique. This is a text field. Do not validate the URI format.
##### Test Assertion #20.2.1.b
Prefix is required and unique.
##### Test Assertion #20.2.1.c
Description is optional.
##### Test Assertion #20.2.1.d
Standard flag (is_std_nmsp) is False and locked.

#### Test Assertion #20.2.2
The end user who is an owner of the namespace can change details of an existing namespace. The modified details must satisfy the business rules same as those indicated in case of a new namespace.

#### Test Assertion #20.2.3
The developer and end user who does not own the namespace cannot update it.

#### Test Assertion #20.2.4
The end user who is the owner of the namespace can discard it if the namespace has no reference.

#### Test Assertion #20.2.5
Any user can see the detail of end user namespace.

#### Test Assertion #20.2.6
The owner end user of the namespace can transfer ownership only to another end user.

### Test Step Pre-condition:
1. Developer and end-user accounts needed for namespace scenarios are available in connectCenter.
2. Existing end-user namespaces are available so that uniqueness, visibility, and ownership-transfer checks can be exercised.


### Test Step:
1. The end user signs in to connectCenter and opens the View/Edit Namespace page.
2. Create an end-user namespace and verify the required URI and prefix rules, the locked non-standard flag, and duplicate-URI validation. (Assertion [#20.2.1](#test-assertion-2021))
3. Open the created namespace as the owner, update its details, and verify that uniqueness rules are still enforced. (Assertion [#20.2.2](#test-assertion-2022))
4. Open an end-user namespace as a developer and as another end user, and verify that non-owners have read-only access. (Assertions [#20.2.3](#test-assertion-2023), [#20.2.5](#test-assertion-2025))
5. Discard an unused end-user-owned namespace and verify it is removed from the list. Then transfer namespace ownership to another end user and verify the new owner. (Assertions [#20.2.4](#test-assertion-2024), [#20.2.6](#test-assertion-2026))
