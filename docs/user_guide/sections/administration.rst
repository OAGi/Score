Administration
==============

Types of Users and Their Rights
-------------------------------

connectCenter has two user roles, namely, End User and connectSpec Developer (or Developer for short).
There is also the Admin right that can be assigned to either an End User or a Developer.
The Admin right provides access to the "Admin" menu.
Depending on the current feature configuration, this menu can include "Account", "Transfer Ownership", "Pending SSO", and "Tenant".
The role is shown next to the user account name at the top-right of the page.
Available functions also depend on the user role, the Admin right, application feature flags, tenant mode, and the state of the entity being worked on.

It is important that developer roles be used only for developing standards particularly the Core Components (CCs).
Standard Development Organizations (SDOs) may wish to also standardize BIEs, in which case the developer role should be used to manage those BIEs as well.
For detailed differences between developer and end user CCs or BIEs, it is important to read `Developer vs. End User Core Components <#developer-vs-end-user-core-components>`__, `Standard/Developer Core Component Management <#standard-developer-core-component-management>`__, and `End user core component management <#end-user-core-component-management>`__, and `BIE Management <#bie-management>`__.

Generally, BIE content cannot be used or reused across developer and end user role.
And developer CCs cannot use end user CCs, but end user CCs can use developer CCs.
This is naturally the case as developer CCs are standard and should not contain non-standard content created by the end user.
However, end user CCs should be able to use standard CCs created by developers.

The table below gives a high-level summary of user rights for different kinds of entities.
Exact availability also depends on application settings and the current UI mode.
Availability of certain actions also depends on the entity state.
The detail in the Other Dev and Other End Users columns should be read in the context of the Dev.
Owner and End User Owner columns, respectively, i.e., it is the rights to the entity they do not currently own.

‘Transfer’ means transfer of ownership.
It is important to note that transfers can occur only between the same user role.

In the table, CRUD = Create/Read/Update/Delete; CRUDE = CRUD and Extend; CURD = Create/Update/Read/Discard, CURDE = CURD and Extend.
Delete is different from Discard in that Delete is only marked as deleted and can be restored.
Discard is permanently purged from the database and cannot be restored.

Changing the role of a user (i.e., from end user to developer and vice versa) is not allowed since it will impact how the connectCenter behaves on the existing content that the user might have already created.

Table summarizing user rights for different entities.
CRUD = Create/Read/Update/Delete.
CRUDE = CRUD and Extend.
CURD = Create/Update/Read/Discard.
CURDE = CURD and Extend.
"Everything but Update, Delete, and Transfer" means "Everything that the Dev. Owner can do (according to the cell to left) but Update, Delete, and Transfer)".

.. list-table::
   :header-rows: 1
   :widths: 18 22 18 18 24

   * - Entity
     - Dev. Owner
     - Other Dev
     - End User Owner
     - Other End Users
   * - Developer CC
     - CRUD, Revise, Restore, Comment, Transfer, Reuse
     - Everything but Update, Delete, and Transfer
     - Cannot own one.
     - Read, Extend (via BIE extension), Use in End User CC
   * - Developer BIE
     - CURD, Copy, Uplift, Transfer, Express, Reuse
     - Everything but Update and Transfer
     - Cannot own one.
     - Read, Copy, Express, Uplift when not in WIP state
   * - Developer Code List
     - CRUD, Revise, Reuse, Transfer
     - Everything but Update and Transfer
     - Cannot own one.
     - Read, Use in EU CC, Use in BIE, Derive an EU Code List
   * - Developer Agency ID List
     - Read, Update, Revise, Reuse, Transfer
     - Read, Reuse
     - Cannot own one.
     - Read, Use in EU CC, Use in BIE, Derive an EU Agency ID List
   * - End User CC
     - Read, Comment, Cannot use in Developer CC
     - Same
     - CRUD, Amend, Restore, Comment, Reuse in EU CC, Transfer
     - Everything but Update, Delete, and Transfer
   * - End User BIE
     - Read, Copy, Express
     - Same
     - CURDE, Copy, Express, Reuse, Uplift, Transfer
     - Everything but Update, Discard, Extend, and Transfer
   * - End User Code List
     - Read
     - Same
     - CRUD, Amend, Restore, Uplift, Reuse in BIE, Comment, Transfer
     - Everything but Update, Delete, and Transfer
   * - End User Agency ID List
     - Read
     - Same
     - CRUD, Amend, Restore, Uplift, Reuse in BIE, Comment, Transfer
     - Everything but Update, Delete, and Transfer
   * - Standard Namespace
     - CURD
     - Same
     - N/A, i.e., End users cannot create it.
     - Read
   * - End User Namespace
     - Read
     - Same
     - CURD
     - Same
   * - Release Management
     - CURD, Publish
     - Same
     - Cannot own a release.
     - Read
   * - Module Management
     - CURD
     - Same
     - Cannot own any module related entity.
     - Read

Create a User
-------------

Only a user with the Admin right can create a user account:

1. First, sign in using an account that has the Admin right.

2. On the top menu, click the "Admin" menu.

3. Click "Account" from the drop-down list.

4. Click the "New Account" button.

5. On the "Create Account" page, fill out the following fields:

   1. *Login ID*, which is the username of the account (it cannot be changed after the account is created) (Mandatory).

   2. *Name* (Optional).

   3. *Organization* (Optional).

   4. Leave the "Standard Developer" checkbox unchecked to create an End User.
      Check the checkbox to create a Standard Developer.

   5. Use the check box named "Admin" to assign the Admin right to the user account being created.
      The admin right allows the user to manage other user accounts.

   6. *Password* (Mandatory).

   7. *Confirm password* (Mandatory).

..

   Note that the password should be at least five characters.

6. Click the "Create" button.

If the account is created from a pending SSO request, some fields may already be prefilled from the pending profile.
For more information about disabling, enabling, and removing user accounts, see `Enable or Disable User Account <#enable-or-disable-user-account>`__.

Update User’s Information (including password reset)
----------------------------------------------------

A user with the admin right can change the Name, Organization, and password of another user.
To do so,

1. Click the "Admin" menu.

2. Choose "Account" from the drop-down list.

3. On the "Account" page, use the search filters on the top to find the desired user account to update.
   Click the "Login ID" of the desired user.

   1. Change the fields such as Login ID, Name, Organization, Admin, and New password as desired.
      The "Standard Developer" checkbox is displayed for reference, but it is not changed on the edit page.

4. Click the "Update" button.

Enable or Disable User Account
------------------------------

A user account can be Enabled or Disabled.
Disabling a user account prevents the user from logging into connectCenter using that account.

Only users with the admin right can disable and re-enable a user account.

To disable a user account:

1. Click the "Admin" menu.

2. Choose "Account" from the drop-down list.

3. On the "Account" page, click the "Login ID" of the user account that
   you want to disable or re-enable.

4. Click "Disable this account" to disable the log in via this account
   or "Enable this account" to re-enable it.

The status of a user account (i.e., Enable or Disable) is displayed on the "Account" list page.
If an account is disabled and has no data created or owned, the admin can also remove it permanently by clicking "Remove this account" on the edit page.

Password Management
-------------------

Change password
~~~~~~~~~~~~~~~

A user can change the password through the account settings page when the account uses local password-based sign-in.
The same page also allows the user to update the email address and resend an email verification request when needed.

To change it:

1. On the right side of the top menu of the page, click the account’s name.

2. Choose "Settings" from the drop-down list.

3. On the "Account" settings page, go to the "Change password" section and fill out the fields:

   1. *Old password*, which is the current password.

   2. *New password.*

   3. *Confirm new password*.

4. Click the "Update" button\ *.*

Forgotten password
~~~~~~~~~~~~~~~~~~

In the event a user has forgotten the password, the user should ask someone that has the Admin right to change or reset it.
Password reset applies to accounts that use local password-based sign-in.

Assuming you are a user with the admin right.
To change/reset someone else’s password:

1. Click the "Admin" menu.

2. Choose "Account" from the drop-down list.

3. On the "Account" page, click the "Login ID" of the user that has
   forgotten his/her password.

4. Enter the new password of the user into the *New password* field.

5. Verify the new password of the user by entering it again into the
   *Confirm new password* field.

6. Click the "Update" button.

Note that you can change the Name and the Organization of a user while changing his/her password (see `Update User’s Information <#update-users-information-including-password-reset>`__).

Application Settings
--------------------

Only users with the Admin right can access "Application settings" from the Settings area.
Administrators can change application settings from the Settings area.
The "Application settings" page currently includes Application Features, SMTP settings, Filename Expressions, and Web Page Settings.
Each feature can be enabled or disabled from this page, while the other sections provide editable settings and preview areas.

To change configurations:

1. On the right side of the top menu of the page, click the account’s name.

2. Choose "Settings" from the drop-down list.

3. Click "Application settings" on the left side of the page.

The following subsections explain each setting.

Multi-tenant Mode
~~~~~~~~~~~~~~~~~

Multi-tenant management would be activated if the multi-tenant mode configuration is enabled.
Note that some functionalities such as creating user extensions, BIE reuse, BIE uplifting, and business term function will be unavailable in the multi-tenant mode.
See `Multi-tenant mode feature restrictions <#multi-tenant-mode-feature-restrictions>`__ for more details.

To enable Multi-tenant mode:

1. Click "Enable" for "Multi-tenant mode".

2. In the "Enable multi-tenant mode?" dialog, review the warning and click "Enable".
   Click "Cancel" if you do not want to change the mode.

3. Wait for the "Updated" message.

To disable Multi-tenant mode:

1. Click "Disable" for "Multi-tenant mode".

2. In the "Disable multi-tenant mode?" dialog, review the warning and click "Disable".
   Click "Cancel" if you do not want to change the mode.

3. Wait for the "Updated" message.

Business term function
~~~~~~~~~~~~~~~~~~~~~~

Business term management would be activated if the business term configuration is enabled.
Note that users cannot see legacy business term data in BIEs, and may need to migrate it manually.
See `Manage Business Terms <#manage-business-terms>`__ for more details.

To enable the business term function:

1. Click "Enable" for "Business term function".

2. In the "Enable business term function?" dialog, review the warning and click "Enable".
   Click "Cancel" if you do not want to change the setting.

3. Wait for the "Updated" message.

To disable the business term function:

1. Click "Disable" for "Business term function".

2. In the "Disable business term function?" dialog, review the warning and click "Disable".
   Click "Cancel" if you do not want to change the setting.

3. Wait for the "Updated" message.

BIE inverse mode
~~~~~~~~~~~~~~~~

The BIE inverse mode would be activated if the BIE inverse mode configuration is enabled.
Note that the BIE expression could be failed if system does not have enough memory to express a large size of BIEs.
See `Restrict a BIE <#restrict-a-bie>`__ for more details.

To enable BIE inverse mode:

1. Click "Enable" for "BIE Inverse Mode".

2. In the "Enable BIE inverse mode?" dialog, review the warning and click "Enable".
   Click "Cancel" if you do not want to change the setting.

3. Wait for the "Updated" message.

To disable BIE inverse mode:

1. Click "Disable" for "BIE Inverse Mode".

2. In the "Disable BIE inverse mode?" dialog, click "Disable".
   Click "Cancel" if you do not want to change the setting.

3. Wait for the "Updated" message.

Email-Based Workflows
~~~~~~~~~~~~~~~~~~~~~

Email-based workflows would be activated if the Email-Based Workflows configuration is enabled.
This feature depends on valid SMTP settings.
If SMTP is not configured correctly, email-based actions may not operate properly.

To enable Email-Based Workflows:

1. Configure and save SMTP settings first.

2. Click "Enable" for "Email-Based Workflows".

3. In the "Enable email-based workflows?" dialog, review the warning and click "Enable".
   Click "Cancel" if you do not want to change the setting.

4. Wait for the "Updated" message.

To disable Email-Based Workflows:

1. Click "Disable" for "Email-Based Workflows".

2. In the "Disable email-based workflows?" dialog, click "Disable".
   Click "Cancel" if you do not want to change the setting.

3. Wait for the "Updated" message.

Browse Standard mode
~~~~~~~~~~~~~~~~~~~~

Browse Standard mode can be enabled or disabled for end users.
When enabled, end users use the "Browse Standard" menu instead of the regular "Core Component" menu.
See `Browser View Mode <#browser-view-mode>`__ for more details about the resulting browsing experience.

To enable Browse Standard mode:

1. Click "Enable" for "Browse Standard mode".

2. In the "Enable Browse Standard mode?" dialog, review the message and click "Enable".
   Click "Cancel" if you do not want to change the setting.

3. Wait for the "Updated" message.

To disable Browse Standard mode:

1. Click "Disable" for "Browse Standard mode".

2. In the "Disable Browse Standard mode?" dialog, review the message and click "Disable".
   Click "Cancel" if you do not want to change the setting.

3. Wait for the "Updated" message.

SMTP settings
~~~~~~~~~~~~~

Administrators can configure SMTP host, port, authentication, STARTTLS, SSL, auth method, auth username, and auth password on the Application settings page.
The page also provides a "Test" button to validate the email settings.

To update SMTP settings:

1. Enter or change the SMTP host, port, and optional authentication settings.

2. Click the "Update" button.

3. Wait for the "Updated" message.

To test SMTP settings:

1. Click the "Test" button.

2. If the test succeeds, the page shows the message "The test message has been sent.".

3. If email-based features do not work as expected, review the auth, STARTTLS, and SSL settings and test again.

Filename Expressions
~~~~~~~~~~~~~~~~~~~~

Administrators can configure filename expressions for generated BIE schema files and BIE Package schema files.
The page includes editable expressions, duplicate-handler expressions, and sample filename previews.
Open the built-in "Guide" area on the page to see the same syntax reference while editing.

The filename expression syntax supports the following forms:

.. list-table::
   :header-rows: 1
   :widths: 28 72

   * - Syntax
     - Meaning
   * - ``{Placeholder}``
     - Inserts the value of a placeholder directly into the generated filename.
   * - ``(optional-group)``
     - Wraps a part of the expression that may be omitted when its contents do not produce a value.
   * - ``{Placeholder?flag}``
     - Includes the placeholder only when the specified flag is enabled.
   * - ``{Placeholder:separator('-')}``
     - Joins multiple values from the placeholder by using the given separator.
   * - ``{Placeholder:replace('regex', 'replacement')}``
     - Replaces text in the placeholder value by using the given regular expression and replacement string.

The duplicate-handler expression supports values such as ``-{Incremental}`` and ``~{BIE ID}``.
Use this expression to generate a different filename when the main filename already exists.

The available flags are ``includeBusinessContext`` and ``includeVersion``.
These flags can be used in conditional placeholder syntax such as ``{BIE Version?includeVersion}``.

Common placeholders include ``BIE Property Term``, ``BIE Display Name``, ``BIE Version``, ``BIE DEN``, ``BIE Status``, ``BIE Remark``, ``BIE ID``, ``Business Context Names``, and ``Business Context Name[0]``.
BIE Package expressions also support ``BIE Package Name``, ``BIE Package Version ID``, and ``BIE Package Version Name``.
Duplicate-handler expressions support placeholders such as ``Incremental`` and ``BIE ID``.

Example BIE Schema expression:

.. code-block:: text

   {BIE Property Term:separator('-')}(-{Business Context Name[0]?includeBusinessContext})(-{BIE Version?includeVersion:replace('\.', '_')})

Example BIE Package Schema expression:

.. code-block:: text

   {BIE Package Name}-{BIE Package Version ID}_{Business Context Names:replace('\s+', ''):separator('+')}_{BIE Property Term}([{BIE Display Name}])(-{BIE Version})

To update filename expressions:

1. Edit the "BIE Schema Expression" and "Duplicate Handler Expression" fields as needed.

2. Edit the "BIE Package Schema Expression" and "Duplicate Handler Expression" fields as needed.

3. Review the sample filename previews for both sections.
   The previews update while you edit the expressions.

4. Click the "Update" button.

5. If the expressions are valid, wait for the "Updated" message.
   If an expression is invalid, an error message appears and the update is not applied.

Web Page Settings
~~~~~~~~~~~~~~~~~

In Web Page Settings, administrators can change the brand logo on the navigation bar, the favicon of the webpage, the sign-in page statement, and colors for component state, release state, and user role.

To update Web Page Settings:

1. Update the "Brand Logo", "Favicon Link (URL)", and "Sign-in Page Statement" fields as needed.

2. Review the previews shown next to each field.

3. Update any desired values in "Component State Colors", "Release State Colors", and "User Role Colors".

4. Click the "Update" button.

5. Wait for the "Updated" message.
   Some changes take effect for users after they sign in again.

To change the brand logo, favicon, sign-in page statement, and colors in Web Page Settings:

1. Brand Logo: Paste the text of the brand logo in SVG format into the "Brand Logo" input field.
A preview of the brand logo will appear on the right side of the input field.

2. Favicon: Paste the URL of the favicon into the "Favicon URL" input field.
A preview of the favicon will appear on the right side of the input field.

3. Sign-in Page Statement: Paste the text of the sign-in page statement in Markdown format into the "Sign-in Page Statement" input field.
A preview of the statement will appear on the right side of the input field.

4. Component State/Release State/User Role Colors: Enter the font and background colors in RGB format (e.g., #000000) for each item.
A preview of the item will appear on the upper side of the input fields.

After changing the settings, click the "Update" button to apply the changes.

Select Terminology
------------------

A user can select either CCTS (Core Component Technical Specification) or connectSpec terminology (or other that may be available in the future).
This will add terms from the selected terminology to menu items and other UI labels as balloons or in parentheses.
To select a terminology:

1. On the right side of the top menu of the page, click the account’s name.

2. Select a terminology, e.g., "CCTS Terminology" or "connectSpec Terminology" from the drop-down list.
   Note that CCTS Terminology is a default/baseline one, i.e., no balloon nor additional term would appear.

3. A check mark appears next to the selected terminology.
   When you hover over a menu item, a terminology balloon appears for terminology options other than CCTS Terminology.

Using Single Sign-On (SSO)
--------------------------

Overview
~~~~~~~~

connectCenter supports the integration of external accounts managed by the third-party identity providers (IdPs) compliant with the OpenID Connect Specification.
If your system administrator has enabled SSO when installing connectCenter, this feature allows the OpenID connect account to link to a connectCenter account.

Connect external accounts to connectCenter
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To connect external IdP accounts to connectCenter, the user must first try to sign in to connectCenter using the IdP account.
When connectCenter does not have a record of the IdP account, it places the account in the pending state.
Users with the Admin right can review these pending accounts from "Admin" > "Pending SSO".
The admin can then do one of the following:

1. Create a new local account from the pending SSO profile.

2. Link the SSO profile to an existing connectCenter account.

3. Reject the pending request.

Review Pending SSO Requests (Admin)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
