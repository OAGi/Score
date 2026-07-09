---
title: "Manage tenant-business context associations"
sidebar_position: 3
---

The business contexts associated with a tenant determine what the users of the tenant can see and use: BIEs whose business contexts are associated with the tenant are visible to its users, and only the associated [business contexts](../../bie-management/05-manage-context.md#create-a-business-context) can be assigned when they [create a BIE](./04-multi-tenant-mode-feature-restrictions.md#features-with-restricted-behavior).

The associations of a tenant are managed on its "Business Context Management" page, titled with the tenant name.
The page lists the business contexts associated with the tenant with their name, GUID, and last update.

![Business Context Management page of the Acme Manufacturing tenant listing one associated business context with a Remove button, and the Add Business Context button in the toolbar](/img/user-guide/tenant_business_contexts.png)

## Associate a tenant and business context

To associate a tenant and business context:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click "Manage Business Context" for the desired tenant.

3. On the "Business Context Management" page, click the "Add Business
   Context" button.
   The page switches to the add view, listing the business contexts
   that are not yet associated with the tenant, and the toolbar button
   changes to "Done".

4. Search for the desired business context by Name, or expand the
   advanced search with the chevron-down button at the right end of
   the search bar to filter by "Updater", "Updated start date", or
   "Updated end date".
   See also [How to use Search Filters](../../bie-management/10-common-functions.md#how-to-use-search-filters).

5. Click the "Add" button for the desired business context.
   The business context disappears from the add view and is associated
   with the tenant immediately.

   ![Business Context Management page in the add view listing candidate business contexts with Add buttons, and the Done button in the toolbar](/img/user-guide/tenant_add_business_context.png)

6. Click "Done" to return to the list of associated business contexts.

## Dissociate a tenant and business context

To dissociate a tenant and business context:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click "Manage Business Context" for the desired tenant.

3. On the "Business Context Management" page, find the desired business context.

4. Click the "Remove" button for that business context.
   The association is removed immediately, without a confirmation
   dialog.
