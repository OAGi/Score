---
title: "Application Settings"
sidebar_position: 6
---

Only users with the Admin right can access "Application settings" from the Settings area.
Administrators can change application settings from the Settings area.
The "Application settings" page currently includes Application Features, SMTP settings, Filename Expressions, and Web Page Settings.
Each feature can be enabled or disabled from this page, while the other sections provide editable settings and preview areas.

To change configurations:

1. On the right side of the top menu of the page, click the account’s name.

2. Choose "Settings" from the drop-down list.

3. Click "Application settings" on the left side of the page.

The following subsections explain each setting.

## Multi-tenant Mode

Multi-tenant management would be activated if the multi-tenant mode configuration is enabled.
Note that some functionalities such as creating user extensions, BIE reuse, BIE uplifting, and business term function will be unavailable in the multi-tenant mode.
See [Multi-tenant mode feature restrictions](../multi-tenant-management/04-multi-tenant-mode-feature-restrictions.md) for more details.

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

## Business term function

Business term management would be activated if the business term configuration is enabled.
Note that users cannot see legacy business term data in BIEs, and may need to migrate it manually.
See [Manage Business Terms](../bie-management/09-manage-business-terms.md) for more details.

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

## BIE inverse mode

The BIE inverse mode would be activated if the BIE inverse mode configuration is enabled.
Note that the BIE expression could be failed if system does not have enough memory to express a large size of BIEs.
See [Restrict a BIE](../bie-management/06-manage-bie.md#restrict-a-bie) for more details.

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

## Email-Based Workflows

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

## Browse Standard mode

Browse Standard mode can be enabled or disabled for end users.
When enabled, end users use the "Browse Standard" menu instead of the regular "Core Component" menu.
See [Browser View Mode](../core-component-management/03-search-and-browse-cc-library.md#browser-view-mode) for more details about the resulting browsing experience.

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

## SMTP settings

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

## Filename Expressions

Administrators can configure filename expressions for generated BIE schema files and BIE Package schema files.
The page includes editable expressions, duplicate-handler expressions, and sample filename previews.
Open the built-in "Guide" area on the page to see the same syntax reference while editing.

The filename expression syntax supports the following forms:

| Syntax | Meaning |
|---|---|
| `{Placeholder}` | Inserts the value of a placeholder directly into the generated filename. |
| `(optional-group)` | Wraps a part of the expression that may be omitted when its contents do not produce a value. |
| `{Placeholder?flag}` | Includes the placeholder only when the specified flag is enabled. |
| `{Placeholder:separator('-')}` | Joins multiple values from the placeholder by using the given separator. |
| `{Placeholder:replace('regex', 'replacement')}` | Replaces text in the placeholder value by using the given regular expression and replacement string. |

The duplicate-handler expression supports values such as `-{Incremental}` and `~{BIE ID}`.
Use this expression to generate a different filename when the main filename already exists.

The available flags are `includeBusinessContext` and `includeVersion`.
These flags can be used in conditional placeholder syntax such as `{BIE Version?includeVersion}`.

Common placeholders include `BIE Property Term`, `BIE Display Name`, `BIE Version`, `BIE DEN`, `BIE Status`, `BIE Remark`, `BIE ID`, `Business Context Names`, and `Business Context Name[0]`.
BIE Package expressions also support `BIE Package Name`, `BIE Package Version ID`, and `BIE Package Version Name`.
Duplicate-handler expressions support placeholders such as `Incremental` and `BIE ID`.

Example BIE Schema expression:

```text
{BIE Property Term:separator('-')}(-{Business Context Name[0]?includeBusinessContext})(-{BIE Version?includeVersion:replace('\.', '_')})
```

Example BIE Package Schema expression:

```text
{BIE Package Name}-{BIE Package Version ID}_{Business Context Names:replace('\s+', ''):separator('+')}_{BIE Property Term}([{BIE Display Name}])(-{BIE Version})
```

To update filename expressions:

1. Edit the "BIE Schema Expression" and "Duplicate Handler Expression" fields as needed.

2. Edit the "BIE Package Schema Expression" and "Duplicate Handler Expression" fields as needed.

3. Review the sample filename previews for both sections.
   The previews update while you edit the expressions.

4. Click the "Update" button.

5. If the expressions are valid, wait for the "Updated" message.
   If an expression is invalid, an error message appears and the update is not applied.

## Web Page Settings

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
