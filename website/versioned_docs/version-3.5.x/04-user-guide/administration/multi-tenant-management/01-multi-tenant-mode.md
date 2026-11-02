---
title: "Multi-tenant mode"
sidebar_position: 1
---

By default, connectCenter does not use multi-tenant mode.
Users with the Admin right can switch the mode on and off from the ["Application settings" page](../06-application-settings.md#multi-tenant-mode).

## Enable multi-tenant mode

To enable multi-tenant mode:

1. Click your account name at the top-right of the page.

2. Choose "Settings" from the drop-down list.

3. Click "Application settings" on the left side of the page.

4. Click "Enable" for "Multi-tenant mode" in the "Application Features" section.

5. In the "Enable multi-tenant mode?" dialog, review the warning that
   some functionalities such as creating user extensions, BIE reuse,
   BIE uplifting, and the business term function will be unavailable,
   and click "Enable".
   Click "Cancel" if you do not want to change the mode.

   ![Enable multi-tenant mode confirmation dialog warning that some functionalities will be unavailable, with Cancel and Enable buttons](/img/user-guide/settings_feature_toggle_dialog.png)

6. Wait for the "Updated" message.

Once the mode is on:

* The "Module" and "Library" menus disappear from the top menu, the
  "Context" menu is shown only to users with the Admin right, and a
  "Tenant" menu item appears under the "Admin" menu.
  See [Multi-tenant mode feature restrictions](./04-multi-tenant-mode-feature-restrictions.md).

* The business term function is automatically disabled, and its
  "Enable"/"Disable" buttons on the "Application settings" page stay
  unavailable while multi-tenant mode is on.

Some changes take effect for other users after they sign in again.

## Add a tenant

To add a tenant:

1. Choose "Admin" and then "Tenant".

   ![Admin menu opened with the Account, Transfer Ownership, Pending SSO, and Tenant menu items; the top menu has no Module or Library menu](/img/user-guide/tenant_admin_menu.png)

2. On the "Tenant Roles" page, click the "New Tenant" button.

   ![Tenant Roles page with the Search by Name bar, one tenant row with the Manage Users and Manage Business Context buttons, and the New Tenant button](/img/user-guide/tenant_roles_page.png)

3. On the "Create Tenant" page, enter a value in the *Name* field.
   The "Name" field is a free form text of at most 50 characters.
   This field is mandatory; the "Create" button stays disabled until a
   name is entered.

   ![Create Tenant page with the Name field filled out and the Create button](/img/user-guide/tenant_create.png)

4. Click the "Create" button.
   A "Created" message confirms the creation and the "Tenant Roles"
   page is reloaded.

On the "Tenant Roles" page, use the "Search by Name" bar at the top of
the page to search for the desired tenant, and click the "Tenant Name"
column header to sort the tenants.

## Rename a tenant

1. On the "Tenant Roles" page, click on the name of the tenant.

2. On the returned "Edit Tenant" page, change the *Name* field.

   ![Edit Tenant page with the Name field and the Update and Discard buttons](/img/user-guide/tenant_edit.png)

3. Click "Update" to save the change.
   The button stays disabled until the name is actually changed.

## Discard a tenant

To discard a tenant:

1. On the "Tenant Roles" page, click on the name of the tenant.

2. On the returned "Edit Tenant" page, click the "Discard" button.

3. In the returned "Discard Tenant?" dialog, confirm your intention by
   clicking "Discard"; or click "Cancel" to go back.
   The tenant and its user and business context associations are
   permanently removed.

   ![Discard Tenant confirmation dialog warning that the tenant will be permanently removed, with the Cancel and Discard buttons](/img/user-guide/tenant_discard_dialog.png)

## Disable multi-tenant mode

To disable multi-tenant mode, click "Disable" for "Multi-tenant mode"
on the "Application settings" page.
The "Disable multi-tenant mode?" dialog warns that you may lose some
data in regard to tenants; click "Disable" to confirm.

![Disable multi-tenant mode confirmation dialog warning about possible loss of tenant data; the Business term function toggle behind is struck through and unavailable](/img/user-guide/tenant_disable_dialog.png)

Note that the business term function stays disabled after multi-tenant
mode is switched off; re-enable it separately if it was in use before.
