---
title: "Update User’s Information (including password reset)"
sidebar_position: 3
---

A user with the Admin right can change the Login ID, Name, Organization, Admin right, and password of another user.
To do so,

1. Click the "Admin" menu.

2. Choose "Account" from the drop-down list.

3. On the "Account" page, use the search bar at the top to find the desired user account.
   The search bar matches the Login ID; click the arrow at its right end to open additional filters for the Name, Organization, Role, and Status.

   ![Account page listing user accounts with the advanced search filters expanded above the Login ID, Role, Name, Organization, and Status columns](/img/user-guide/admin_account_page.png)

4. Click the "Login ID" of the desired user to open the "Edit Account" page.

   ![Edit Account page showing the Login ID, Name, and Organization fields, the disabled Standard Developer checkbox, the Admin checkbox, the New password fields, and the Update and Disable this account buttons](/img/user-guide/admin_edit_account.png)

5. Change the fields such as Login ID, Name, Organization, Admin, and New password as desired.
   The "Standard Developer" checkbox is displayed for reference, but it cannot be changed on the edit page.

6. Click the "Update" button.
   connectCenter shows an "Updated" message and returns to the "Account" page.

:::note
Leaving the "New password" and "Confirm new password" fields empty keeps the user's current password.
A new password must be at least five characters, and the "Update" button stays disabled until the two password fields match (see [Password Management](./05-password-management.md)).
For accounts associated with SSO, the password fields are not shown at all; the page instead displays the read-only SSO profile and a "Disassociate SSO" button (see [Using Single Sign-On](./08-using-single-sign-on.md)).
:::

The same page also provides the buttons for disabling, re-enabling, and removing the account; see [Enable or Disable User Account](./04-enable-or-disable-user-account.md).
