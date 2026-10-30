# Test Suite 49

**Multi-tenant Business Context Visibility**

This suite covers the multi-tenant mode: which business contexts connectCenter offers a user, how an Admin manages tenants, how what a user is offered follows what the Admin associates with a tenant, which BIEs a user sees, and to whom a BIE can be transferred. A user belongs to zero or more tenants, and a business context is connected to zero or more tenants. The `Create BIE` page lists the business contexts of the user's tenants, and only those; a business context that is connected to no tenant is offered to nobody. Managing tenants, and the associations of tenants with users and with business contexts, is for an Admin only (`Admin > Tenant`), and is available only while the multi-tenant mode is on. The tests turn the multi-tenant mode on for their run and restore the previous setting afterwards, so they must not overlap other tests. The tests create random users, tenants and business contexts with random names, and delete them when they are done.

## Test Case 49.1

**Business contexts of the tenants of a user**

Pre-condition: The multi-tenant mode can be turned on. The automated test creates a set of random users (developers and end users, one of them an Admin), random tenants and random business contexts, with random names, connects the users and the business contexts to the tenants as described in the steps, and deletes all of them when it is done.

### Test Assertion:

#### Test Assertion #49.1.1
A developer or an end user who belongs to exactly one tenant is offered the business contexts connected to that tenant on the `Create BIE` page, and no business context of another tenant or of no tenant.

#### Test Assertion #49.1.2
A user who belongs to several tenants is offered the business contexts of all of those tenants, and a user is not offered the business contexts of a tenant that the user does not belong to.

#### Test Assertion #49.1.3
A user who belongs to no tenant cannot start creating a BIE: the `BIE > Create BIE` menu item is disabled. This holds for a developer, an Admin developer and an end user.

#### Test Assertion #49.1.4
A business context that is connected to several tenants is listed once, not once per tenant.

### Test Step Pre-condition:
1. The multi-tenant mode is on.
2. Random users exist: a developer, an Admin developer and an end user who belong to no tenant; a developer who belongs to a first tenant; an end user who belongs to a second tenant; an end user who belongs to a third tenant; and an end user who belongs to the second and the third tenants.
3. Random business contexts exist, each with a random name that contains the same random prefix: one connected to the first tenant, one to the second, one to the third, and one to no tenant.
4. For step 6, an end user who belongs to a tenant, and a business context that is connected to that tenant and to two other tenants, exist.

### Test Step:
1. Sign in as the developer who belongs to the first tenant, open `BIE > Create BIE`, search by the random prefix and verify that only the business context of the first tenant is listed. (Assertion [#1](#test-assertion-4911))
2. Sign in as the end user who belongs to the second tenant, open `BIE > Create BIE`, search by the random prefix and verify that only the business context of the second tenant is listed. (Assertion [#1](#test-assertion-4911))
3. Sign in as the end user who belongs to the second and the third tenants and verify that the business contexts of both tenants are listed, and no other. (Assertion [#2](#test-assertion-4912))
4. Sign in as the end user who belongs to the third tenant only and verify that only the business context of the third tenant is listed, so the business context of the second tenant is not. (Assertion [#2](#test-assertion-4912))
5. Sign in as each user who belongs to no tenant and verify that `BIE > Create BIE` is disabled. (Assertion [#3](#test-assertion-4913))
6. Sign in as the end user of step pre-condition 4, open `BIE > Create BIE`, search by the random prefix and verify that the business context connected to several tenants is listed exactly once. (Assertion [#4](#test-assertion-4914))

## Test Case 49.2

**Tenant administration**

Pre-condition: The multi-tenant mode is on. An Admin account is available; the automated test creates a random Admin account, random users, random tenants and random business contexts.

### Test Assertion:

#### Test Assertion #49.2.1
An Admin can create a tenant with `Admin > Tenant > New Tenant`, and the new tenant is listed by the `Tenant` page.

#### Test Assertion #49.2.2
An Admin can associate a user with a tenant on the `Manage Users` page of the tenant: a user who does not belong to the tenant is offered by `Add User`; once added, the user is no longer offered, and is listed on the tenant's own list. `Remove` dissociates the user again, after which the user is offered by `Add User` again.

#### Test Assertion #49.2.3
An Admin can associate a business context with a tenant on the `Manage Business Context` page of the tenant in the same way: a business context that is not connected to the tenant is offered by `Add Business Context`; once added, it is listed on the tenant's own list and no longer offered; `Remove` dissociates it, after which it is offered again.

#### Test Assertion #49.2.4
A user who is not an Admin, whether a developer or an end user, has no `Admin` menu and cannot open the `Tenant` page by its address.

#### Test Assertion #49.2.5
The `Admin > Tenant` menu item exists while the multi-tenant mode is on. While the mode is off, an Admin has no `Tenant` menu item and cannot open the `Tenant` page by its address.

### Test Step Pre-condition:
1. The multi-tenant mode is on (except for the last part of step 5).
2. A random Admin account exists, and random end user, developer, tenant and business context records exist as the steps need.

### Test Step:
1. Sign in as the Admin, open `Admin > Tenant`, create a tenant with a random name, search the list by that name and verify that it is listed. (Assertion [#1](#test-assertion-4921))
2. Open `Manage Users` of a tenant that has no user. Verify that a user who is not in the tenant is not listed, switch to `Add User` and verify that the user is offered. Add the user and verify that the user is no longer offered, then switch back with `Done`, verify that the user is listed, remove the user, verify that the user is no longer listed and that `Add User` offers the user again. (Assertion [#2](#test-assertion-4922))
3. Open `Manage Business Context` of a tenant that has no business context. Repeat the checks of step 2 with a business context and `Add Business Context`. (Assertion [#3](#test-assertion-4923))
4. Sign in as an end user and as a developer who are not Admins. Verify that the `Admin` menu is missing and that opening the address of the `Tenant` page shows no tenant page. (Assertion [#4](#test-assertion-4924))
5. Sign in as the Admin and verify that `Admin` has the `Tenant` item. Turn the multi-tenant mode off, sign in as the Admin again, verify that the `Tenant` item is missing and that opening the address of the `Tenant` page shows no tenant page. Turn the mode on again. (Assertion [#5](#test-assertion-4925))

## Test Case 49.3

**Visibility follows the associations of a tenant**

Pre-condition: The multi-tenant mode is on. A random Admin account, a random end user, a random tenant and random business contexts exist. What is associated with the tenant is changed by the Admin on the `Admin > Tenant` pages, and what the user is offered is read by signing in as the user.

### Test Assertion:

#### Test Assertion #49.3.1
A user who belongs to no tenant cannot start creating a BIE. After an Admin adds the user to a tenant, the user is offered, on the `Create BIE` page, the business contexts of that tenant. After the Admin removes the user from the tenant, the user cannot create a BIE again.

#### Test Assertion #49.3.2
A business context that an Admin adds to a tenant is offered to the users of that tenant on the `Create BIE` page. A business context that the Admin removes from the tenant is no longer offered. When the last business context is removed, the users still belong to the tenant, can start creating a BIE, and are offered no business context.

### Test Step Pre-condition:
1. A random tenant, a random end user who does not belong to the tenant and a random business context connected to the tenant exist (assertion 1).
2. A random tenant, a random end user who belongs to the tenant, and two random business contexts, one of them connected to the tenant, exist (assertion 2).

### Test Step:
1. Sign in as the end user and verify that `BIE > Create BIE` is disabled. (Assertion [#1](#test-assertion-4931))
2. Sign in as the Admin, open `Manage Users` of the tenant, add the end user and sign out. Sign in as the end user, open `BIE > Create BIE`, search for the business contexts of the test and verify that the business context of the tenant is listed. (Assertion [#1](#test-assertion-4931))
3. Sign in as the Admin, remove the end user from the tenant and sign out. Sign in as the end user and verify that `BIE > Create BIE` is disabled again. (Assertion [#1](#test-assertion-4931))
4. For the second setup, sign in as the end user and verify that only the first business context is offered. Sign in as the Admin, open `Manage Business Context` of the tenant, add the second business context and sign out. Sign in as the end user and verify that both are offered. (Assertion [#2](#test-assertion-4932))
5. Sign in as the Admin and remove the first business context, then sign in as the end user and verify that only the second one is offered. Remove the second one as the Admin; sign in as the end user and verify that `Create BIE` is enabled and that no business context is offered. (Assertion [#2](#test-assertion-4932))

## Test Case 49.4

**BIE visibility by tenant**

Pre-condition: The multi-tenant mode is on. Random users (end users and a developer, one of them in no tenant, and two random Admins, one of them in the first tenant), random tenants, random business contexts and random BIEs exist. Each BIE is of its own random top-level concept and is assigned the business contexts named in the steps; the BIEs are in the `Production` state.

### Test Assertion:

#### Test Assertion #49.4.1
On `BIE > View/Edit BIE`, a user sees a BIE when at least one of the business contexts that the BIE is assigned to is connected to a tenant that the user belongs to, and does not see the BIEs of the other tenants.

#### Test Assertion #49.4.2
A user who belongs to several tenants sees the BIEs of all of them, and each BIE is listed once even when several of its business contexts, or several of the user's tenants, lead to it.

#### Test Assertion #49.4.3
A BIE that is assigned only business contexts of no tenant is seen by nobody, and a user who belongs to no tenant sees no BIE.

#### Test Assertion #49.4.4
An Admin sees the BIEs of every tenant, whether the Admin belongs to a tenant or not, including a BIE whose business contexts are of no tenant.

#### Test Assertion #49.4.5
An Admin who belongs to a tenant is also offered the business contexts of every tenant on `BIE > Create BIE`, not only those of the Admin's own tenant.

#### Test Assertion #49.4.6
What a user sees follows what an Admin does on `Admin > Tenant`: after the business context of a BIE is added to the tenant of the user, the user sees the BIE, and after it is removed, the user no longer does.

### Test Step Pre-condition:
1. Two tenants A and B. An end user of A, an end user of B, a developer of both, an end user of neither tenant, and an Admin.
2. Four business contexts: one connected to A, one connected to B, one connected to no tenant.
3. Four BIEs: one assigned the business context of A, one the business context of B, one both of these, and one the business context of no tenant.
4. For step 5, a user of a third tenant, a business context not yet connected to it and a BIE assigned that business context.

### Test Step:
1. Sign in as the end user of A, open `BIE > View/Edit BIE`, choose the branch of the test and search by the business contexts of the test. Verify that the BIE of A and the BIE of both tenants are listed, and no other. (Assertion [#1](#test-assertion-4941))
2. Do the same as the end user of B. Verify that the BIE of B and the BIE of both tenants are listed. (Assertion [#1](#test-assertion-4941))
3. Do the same as the developer of both tenants. Verify that the three BIEs of the tenants are listed, each once. (Assertion [#2](#test-assertion-4942))
4. Do the same as the user of no tenant. Verify that no BIE is listed; this includes the BIE of the business context of no tenant. (Assertion [#3](#test-assertion-4943))
5. Do the same as the Admin who belongs to no tenant. Verify that all four BIEs are listed. (Assertion [#4](#test-assertion-4944))
6. Do the same as the Admin of the first tenant. Verify that all four BIEs are listed, then open `BIE > Create BIE`, search for the business contexts of the test and verify that the business contexts of both tenants and the one of no tenant are offered. (Assertions [#4](#test-assertion-4944), [#5](#test-assertion-4945))
7. As the user of the third tenant, verify that the BIE is not listed. As the Admin, add the business context to the tenant on `Manage Business Context`; as the user, verify that the BIE is listed. As the Admin, remove the business context; as the user, verify that the BIE is not listed. (Assertion [#6](#test-assertion-4946))

## Test Case 49.5

**BIE ownership transfer by tenant**

Pre-condition: The multi-tenant mode is on. Random end users, random tenants, random business contexts and random BIEs in the `WIP` state exist. The owner of the BIEs belongs to the first tenant. The first business context is connected to the first tenant, the second to the second tenant.

### Test Assertion:

#### Test Assertion #49.5.1
The owner of a BIE that is assigned a business context of one tenant is offered, by the transfer dialog of `BIE > View/Edit BIE`, only the other users who belong to that tenant, and not users of other tenants.

#### Test Assertion #49.5.2
For a BIE that is assigned business contexts of several tenants, the owner is offered the users of all those tenants, and a user who belongs to several of them is offered once.

#### Test Assertion #49.5.3
The owner of the BIE and a user who belongs to no tenant are not offered.

#### Test Assertion #49.5.4
After the owner transfers the BIE to an offered user, the new owner has the BIE.

### Test Step Pre-condition:
1. Users: the owner and another user of the first tenant, a user of the second tenant, a user of both tenants and a user of no tenant.
2. BIEs: one assigned the business context of the first tenant, one assigned both business contexts.

### Test Step:
1. Sign in as the owner, open `BIE > View/Edit BIE`, open the transfer dialog of the first BIE and verify that the other user of the first tenant and the user of both tenants are offered. (Assertion [#1](#test-assertion-4951))
2. Open the transfer dialog of the second BIE and verify that the users of either tenant are offered, each once. (Assertion [#2](#test-assertion-4952))
3. Verify that the owner and the user of no tenant are in neither list. (Assertion [#3](#test-assertion-4953))
4. Transfer the first BIE to the other user of the first tenant, sign in as that user, search by owner and verify that the BIE is listed. (Assertion [#4](#test-assertion-4954))

## Test Case 49.6

**Tenant management**

Pre-condition: The multi-tenant mode is on. A random Admin account, random users, random tenants and random business contexts exist.

### Test Assertion:

#### Test Assertion #49.6.1
A tenant cannot be created with the name of another tenant: the Admin is told that a tenant with that name already exists, and the list still has one tenant of that name.

#### Test Assertion #49.6.2
An Admin can rename a tenant. The tenant is listed by its new name and no longer by the old one, and its users and business contexts are unchanged: a user of the tenant is still offered the business contexts of the tenant.

#### Test Assertion #49.6.3
A tenant cannot be renamed to the name of another tenant: the Admin is told that a tenant with that name already exists, and the tenant keeps its name.

#### Test Assertion #49.6.4
When an Admin discards a tenant, the confirmation says how many accounts and business contexts are associated with the tenant, and cancelling keeps the tenant. After it is confirmed, the tenant is no longer listed, the users of the tenant no longer have the tenant (a user of no other tenant cannot create a BIE), and the business contexts themselves are kept.

### Test Step Pre-condition:
1. A random Admin account, and random tenants, a random end user and a random business context as the steps need. For steps 2 and 4 the user and the business context are associated with the tenant.

### Test Step:
1. Sign in as the Admin and try to create a tenant with the name of an existing tenant. Verify the message and that the list has one tenant of that name. (Assertion [#1](#test-assertion-4961))
2. Open the tenant from the list, rename it and update. Verify the list by the new and the old name, then sign in as the user of the tenant and verify that the business context of the tenant is offered on `Create BIE`. (Assertion [#2](#test-assertion-4962))
3. Open a tenant and rename it to the name of another tenant. Verify the message and that the list still has the tenant by its own name. (Assertion [#3](#test-assertion-4963))
4. Open a tenant that has a user and a business context and choose `Discard`. Verify the warning, cancel, choose `Discard` again and confirm. Verify that the tenant is gone from the list, that the user cannot create a BIE, and that the business context still exists. (Assertion [#4](#test-assertion-4964))
