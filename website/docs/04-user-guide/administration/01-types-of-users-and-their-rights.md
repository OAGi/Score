---
title: "Types of Users and Their Rights"
sidebar_position: 1
---

connectCenter has two user roles, namely, End User and connectSpec Developer (or Developer for short).
There is also the Admin right that can be assigned to either an End User or a Developer.
The Admin right provides access to the "Admin" menu.
Depending on the current feature configuration, this menu can include "Account", "Transfer Ownership", "Pending SSO", and "Tenant".
The role is shown next to the user account name at the top-right of the page.
Available functions also depend on the user role, the Admin right, application feature flags, tenant mode, and the state of the entity being worked on.

It is important that developer roles be used only for developing standards particularly the Core Components (CCs).
Standard Development Organizations (SDOs) may wish to also standardize BIEs, in which case the developer role should be used to manage those BIEs as well.
For detailed differences between developer and end user CCs or BIEs, it is important to read [Developer vs. End User Core Components](../core-component-management/02-key-concepts.md#developer-vs-end-user-core-components), [Standard/Developer Core Component Management](../core-component-management/developer/index.md), and [End user core component management](../core-component-management/end-user/index.md), and [BIE Management](../bie-management/01-bie-in-brief.md).

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

| Entity | Dev. Owner | Other Dev | End User Owner | Other End Users |
|---|---|---|---|---|
| Developer CC | CRUD, Revise, Restore, Comment, Transfer, Reuse | Everything but Update, Delete, and Transfer | Cannot own one. | Read, Extend (via BIE extension), Use in End User CC |
| Developer BIE | CURD, Copy, Uplift, Transfer, Express, Reuse | Everything but Update and Transfer | Cannot own one. | Read, Copy, Express, Uplift when not in WIP state |
| Developer Code List | CRUD, Revise, Reuse, Transfer | Everything but Update and Transfer | Cannot own one. | Read, Use in EU CC, Use in BIE, Derive an EU Code List |
| Developer Agency ID List | Read, Update, Revise, Reuse, Transfer | Read, Reuse | Cannot own one. | Read, Use in EU CC, Use in BIE, Derive an EU Agency ID List |
| End User CC | Read, Comment, Cannot use in Developer CC | Same | CRUD, Amend, Restore, Comment, Reuse in EU CC, Transfer | Everything but Update, Delete, and Transfer |
| End User BIE | Read, Copy, Express | Same | CURDE, Copy, Express, Reuse, Uplift, Transfer | Everything but Update, Discard, Extend, and Transfer |
| End User Code List | Read | Same | CRUD, Amend, Restore, Uplift, Reuse in BIE, Comment, Transfer | Everything but Update, Delete, and Transfer |
| End User Agency ID List | Read | Same | CRUD, Amend, Restore, Uplift, Reuse in BIE, Comment, Transfer | Everything but Update, Delete, and Transfer |
| Standard Namespace | CURD | Same | N/A, i.e., End users cannot create it. | Read |
| End User Namespace | Read | Same | CURD | Same |
| Release Management | CURD, Publish | Same | Cannot own a release. | Read |
| Module Management | CURD | Same | Cannot own any module related entity. | Read |
