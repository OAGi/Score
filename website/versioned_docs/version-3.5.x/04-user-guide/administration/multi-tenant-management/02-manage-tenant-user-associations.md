---
title: "Manage tenant-user associations"
sidebar_position: 2
---

Users must be associated with a tenant to work with BIEs in multi-tenant mode; among others, [a user who is not associated with any tenant cannot create a BIE](./04-multi-tenant-mode-feature-restrictions.md#features-with-restricted-behavior).

The associations of a tenant are managed on its "Users Management" page, titled with the tenant name.
The page lists the users associated with the tenant with the "Login ID", "Role", "Name", "Organization", and "Status" columns.

![Users Management page of the Acme Manufacturing tenant listing one associated end user with a Remove button, and the Add User button in the toolbar](/img/user-guide/tenant_users_management.png)

## Associate a tenant and user

To associate a tenant and user:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click "Manage Users" for the desired tenant.

3. On the "Users Management" page, click the "Add User" button.
   The page switches to the add view, listing the users that are not
   yet associated with the tenant, and the toolbar button changes to
   "Done".

4. Search for the desired user by Login ID, or expand the advanced
   search with the chevron-down button at the right end of the search
   bar to filter by "Name", "Organization", or "Status"
   (Enable/Disable).
   See also [How to use Search Filters](../../bie-management/10-common-functions.md#how-to-use-search-filters).

5. Click the "Add" button for the desired user.
   The user disappears from the add view and is associated with the
   tenant immediately.
   Both end users and developers can be associated.

   ![Users Management page in the add view with the search bar filtered and Add buttons for the test_dev and test_eu users, and the Done button in the toolbar](/img/user-guide/tenant_add_user.png)

6. Click "Done" to return to the list of associated users.

## Dissociate a tenant and user

To dissociate a tenant and user:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click "Manage Users" for the desired tenant.

3. On the "Users Management" page, find the desired user.

4. Click the "Remove" button for that user.
   The association is removed immediately, without a confirmation
   dialog.
