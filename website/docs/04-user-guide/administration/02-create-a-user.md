---
title: "Create a User"
sidebar_position: 2
---

Only a user with the Admin right can create a user account.
The "Admin" menu used below appears in the top menu only for users with the Admin right.

1. First, sign in using an account that has the Admin right.

2. On the top menu, click the "Admin" menu and click "Account" from the drop-down list.

   ![Admin drop-down menu expanded, showing the Account, Transfer Ownership, and Pending SSO items](/img/user-guide/admin_menu.png)

3. On the "Account" page, click the "New Account" button.

4. On the "Create Account" page, fill out the following fields:

    1. "Login ID", which is the username of the account (Mandatory).
       When you leave the field, connectCenter checks that the Login ID is not already taken.
       The Login ID can later be changed, but only by a user with the Admin right on the "Edit Account" page (see [Update User’s Information](./03-update-users-information.md)).

    2. "Name" (Optional).

    3. "Organization" (Optional).

    4. Leave the "Standard Developer" checkbox unchecked to create an End User.
       Check the checkbox to create a Standard Developer.

    5. Use the checkbox named "Admin" to assign the Admin right to the user account being created.
       The Admin right allows the user to manage other user accounts.

    6. "Password" (Mandatory).

    7. "Confirm password" (Mandatory).

       :::note
       The password must be at least five characters.
       :::

   ![Create Account page with the Login ID, Name, and Organization fields filled out, the Standard Developer checkbox checked, and the two password fields entered](/img/user-guide/admin_create_account.png)

5. Click the "Create" button.
   The "Create" button stays disabled until the mandatory fields are valid and the two passwords match.
   After the account is created, connectCenter shows a "Created" message and returns to the "Account" page.

## Creating an account from a pending SSO request

When the account is created from a pending SSO request (see [Using Single Sign-On](./08-using-single-sign-on.md)), the "Create Account" page differs in a few ways:

- A read-only "Pending Account Information" section appears at the top, showing the Name, Nickname, Preferred Username, and Email from the pending profile when they are available.
  The editable fields below it start empty.
- The "Password" and "Confirm password" fields are not shown because the user signs in through the external identity provider.
- The "Standard Developer" checkbox is disabled: accounts created from SSO requests are always End Users.

![Create Account page opened from a pending SSO request, with the read-only Pending Account Information section at the top, the disabled Standard Developer checkbox, and no password fields](/img/user-guide/admin_create_account_from_pending.png)

For more information about disabling, enabling, and removing user accounts, see [Enable or Disable User Account](./04-enable-or-disable-user-account.md).
