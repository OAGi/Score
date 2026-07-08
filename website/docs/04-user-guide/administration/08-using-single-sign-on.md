---
title: "Using Single Sign-On (SSO)"
sidebar_position: 8
---

## Overview

connectCenter supports the integration of external accounts managed by the third-party identity providers (IdPs) compliant with the OpenID Connect Specification.
If your system administrator has enabled SSO when installing connectCenter, this feature allows the OpenID connect account to link to a connectCenter account.

## Connect external accounts to connectCenter

To connect external IdP accounts to connectCenter, the user must first try to sign in to connectCenter using the IdP account.
When connectCenter does not have a record of the IdP account, it places the account in the pending state.
Users with the Admin right can review these pending accounts from "Admin" > "Pending SSO".
The admin can then do one of the following:

1. Create a new local account from the pending SSO profile.

2. Link the SSO profile to an existing connectCenter account.

3. Reject the pending request.

## Review Pending SSO Requests (Admin)

If SSO is enabled, users with the Admin right can review pending SSO sign-in requests from the "Admin" menu.
The menu item is labeled "Pending SSO", and it opens the "Pending Account" list page.

To open the pending SSO list:

1. Click the "Admin" menu.

2. Choose "Pending SSO" from the drop-down list.

3. On the "Pending Account" page, use the search bar to search by preferred username.

4. If needed, open Advanced Search and filter by email, provider, created start date, or created end date.

5. Click a pending entry in the list to open the "Review Pending Account" page.

On the "Review Pending Account" page, connectCenter displays the available pending profile fields such as Provider, Email, Name, Nickname, Preferred Username, Phone, and Status.
If the request has already been processed, the Status field shows "Linked" or "Rejected".
If the request is still waiting for review, the Status field shows "Pending".

To create a new account from a pending SSO request:

1. On the "Review Pending Account" page, click "Create new account".

2. The "Create Account" page opens and shows a "Pending Account Information" section when pending profile values are available.

3. Enter a unique value in the *Login ID* field.

4. Complete or adjust the *Name* and *Organization* fields as needed.

5. If needed, check the "Admin" checkbox.
   In this flow, the "Standard Developer" checkbox is disabled.

6. Click the "Create" button.

7. Wait for the "Created" message.

To link a pending SSO request to an existing account:

1. On the "Review Pending Account" page, click "Link to existing account".

2. In the "Link to existing account" dialog, use the search bar to search by Login ID.

3. If needed, open Advanced Search and filter by Name, Organization, or Role.

4. Select one account from the list by checking its selection box.
   The dialog lists only users who are not already linked to an OAuth2 user.

5. Click the "Link" button.

6. Wait for the "Linked" message.

To reject a pending SSO request:

1. On the "Review Pending Account" page, click "Reject".

2. In the "Confirm reject" dialog, click "Reject anyway".
   Click "Cancel" if you do not want to reject the request.

3. Wait for the "Rejected" message.

4. After the request is rejected, connectCenter returns to the pending list page.
