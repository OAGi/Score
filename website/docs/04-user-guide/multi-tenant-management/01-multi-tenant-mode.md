---
title: "Multi-tenant mode"
sidebar_position: 1
---

Multi-tenant management functions are available only to users who have the Admin right when multi-tenant mode is enabled.

connectCenter multi-tenant feature enables organizations to offer connectCenter-based BIE development services to multiple members on a single instance with visibility limited by tenant.
The visibility is restricted by the business contexts associated with the BIE and the tenant.
In other words, if a BIE has a business context that is associated with a tenant, then end users in that tenant can see the BIE.
Users can also see BIEs whose business contexts do not belong to any tenant.

## Enable multi-tenant mode

By default, connectCenter does not use multi-tenant mode.
To enable multi-tenant mode:

1. Click your account name at the top-right of the page.

2. Choose "Settings" from the drop-down list.

3. Click "Application settings" on the left side of the page.

4. Click "Enable" for "Multi-tenant mode".

5. In the "Enable multi-tenant mode?" dialog, review the warning and click "Enable".
   Click "Cancel" if you do not want to change the mode.

6. Wait for the "Updated" message.

## Add a tenant

To add a tenant:

1. Choose "Admin" and then "Tenant".

2. On the "Tenant Roles" page, click the "New Tenant" button.

3. On the "Create Tenant" page, enter a value in the *Name* field.

4. Click the "Create" button.
