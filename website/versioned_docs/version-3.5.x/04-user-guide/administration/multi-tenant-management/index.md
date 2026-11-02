---
title: "Multi-tenant management"
sidebar_position: 0
---

The connectCenter multi-tenant feature enables organizations to offer connectCenter-based BIE development services to multiple member groups, called tenants, on a single instance with visibility limited by tenant.
The visibility is controlled by the business contexts associated with the BIE and the tenant: if a BIE has a business context that is associated with a tenant, then end users in that tenant can see the BIE.
Users can also see BIEs whose business contexts do not belong to any tenant.

Multi-tenant management has three major functions, all available only to users with the Admin right and only while multi-tenant mode is enabled:

1. **Manage tenants** - create, rename, and discard tenants.
   See [Multi-tenant mode](./01-multi-tenant-mode.md).

2. **Manage tenant-user associations** - define which users belong to each tenant.
   See [Manage tenant-user associations](./02-manage-tenant-user-associations.md).

3. **Manage tenant-business context associations** - define which business contexts (and thereby which BIEs) each tenant can see and use.
   See [Manage tenant-business context associations](./03-manage-tenant-business-context-associations.md).

All multi-tenant management pages are reached from the "Tenant" menu item that appears under the "Admin" menu while multi-tenant mode is on.

In multi-tenant mode, several regular connectCenter features are hidden or restricted for all users.
See [Multi-tenant mode feature restrictions](./04-multi-tenant-mode-feature-restrictions.md).

:::note
Multi-tenant mode should be enabled at the first deployment of connectCenter.
While it is possible to switch multi-tenant mode on or off, it is not recommended to go back and forth between the two modes; the confirmation dialog for [disabling the mode](./01-multi-tenant-mode.md#disable-multi-tenant-mode) warns that tenant data may be lost.
:::
