---
title: "Multi-tenant mode feature restrictions"
sidebar_position: 4
---

## Features not available

The following features are not available to users when connectCenter is in multi-tenant mode:

* [Module Management](../../core-component-management/developer/module-management/index.md): the "Module" menu is removed from the top menu.

* Library management: the "Library" menu is removed from the top menu.

* [Make BIE reusable](../../bie-management/06-manage-bie.md#make-a-bie-reusable): the context menu item stays disabled in the BIE editor.

* [Create ABIE extension](../../bie-management/06-manage-bie.md#extend-a-bie) locally or globally: the "Create ABIE Extension Locally" and "Create ABIE Extension Globally" context menu items stay disabled in the BIE editor.

* [Business Term Management](../../bie-management/09-manage-business-terms.md): the business term function is automatically disabled when multi-tenant mode is enabled and cannot be re-enabled while the mode is on.

* [End-user Code List management](../../bie-management/03-manage-end-user-code-lists.md) for users without the Admin right: the "View/Edit Code List" and "Uplift Code List" menu items under the "BIE" menu stay disabled.

The BIE menu of an end user reflects these restrictions; note also that the top menu has no "Module", "Library", or "Context" menu:

![BIE menu of an end user in multi-tenant mode with the View/Edit Code List and Uplift Code List items disabled and no Business Term item; the top menu shows only the BIE and Core Component menus](/img/user-guide/eu_tenant_bie_menu.png)

## Features with restricted behavior

* [Create BIE](../../bie-management/06-manage-bie.md#create-a-bie): a user must be associated with a tenant to be able to create a BIE.
  For a user who is not [associated with any tenant](./02-manage-tenant-user-associations.md), the "Create BIE" menu item stays disabled.

  ![BIE menu of a user who is not associated with any tenant, with the Create BIE menu item disabled](/img/user-guide/tenant_create_bie_disabled.png)

* Manage associations between business contexts (BCs) and BIEs:

    * The BCs available to the user are limited by tenant association.
      When creating or copying a BIE, the business context selection
      lists only the BCs of the tenants the user is associated with,
      and a "Tenant" column shows the owning tenant of each BC.

      ![Select Business Contexts step of the Create BIE page listing only the single business context of the user's tenant, with a Tenant column showing the tenant name](/img/user-guide/eu_tenant_create_bie.png)

    * Users who are not associated with a tenant cannot create a BIE
      because they cannot assign a business context during BIE
      creation.

    * Admin users do not have special authorization in this case.

* [Manage context](../../bie-management/05-manage-context.md): the "Context" menu is shown only to users with the Admin right.

* [Transfer BIE ownership](../../bie-management/06-manage-bie.md#transfer-bie-ownership-making-bie-editable-by-another-user): users to whom BIE ownership may be transferred are limited to users associated with tenants associated with business contexts associated with the BIE.

The home page statistics and the BIE lists follow the same visibility rule: users see the BIEs whose business contexts are associated with their tenants, plus the BIEs whose business contexts do not belong to any tenant.
