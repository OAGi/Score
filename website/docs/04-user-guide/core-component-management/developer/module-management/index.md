---
title: "Module Management"
sidebar_position: 0
---

Module Management controls how the developer CCs (including DTs, Code Lists, and Agency ID Lists) are serialized into schema files.
It has three major functions:

1. **Manage Module Sets** - define a hierarchy of module directories and module files.
   See [Manage Module Set](./02-manage-module-set.md).

2. **Manage Module Set Releases** - associate a module set with a release, so that the module set can be reused across releases.
   See [Manage Module Set Release](./03-manage-module-set-release.md).

3. **Manage CC-Module assignments** - assign the CCs of the release to the module files.
   See [Assign CCs to a Module File](./03-manage-module-set-release.md#assign-ccs-to-a-module-file).

The data associated with these functions is used for [exporting a Module Set Release](./03-manage-module-set-release.md#export-a-module-set-release) as XML Schema or JSON Schema files.

All Module Management pages are reached from the "Module" menu at the top of the page.
For developers the menu items are "View/Edit Module Set" and "View/Edit Module Set Release".

![Module menu of a developer with the View/Edit Module Set and View/Edit Module Set Release menu items over the Module Set page](/img/user-guide/module_menu_developer.png)

The data can be managed only by developers.
End users can open the same pages, but the menu items read "View Module Set" and "View Module Set Release", the pages open in a read-only view, and all editing controls (the "New ..." buttons, the row context menus, the input fields, and the assign/unassign arrows) are hidden or disabled.
End users can still [validate](./03-manage-module-set-release.md#validate-a-module-set-release) and [export](./03-manage-module-set-release.md#export-a-module-set-release) a Module Set Release.

![Module menu of an end user with the View Module Set and View Module Set Release menu items; the Module Set page behind has no New Module Set button](/img/user-guide/eu_module_menu.png)

:::note
Module Management entities have no ownership, unlike CCs and BIEs.
Multiple developers may edit the same module set or module set release at the same time; the last saved change wins.
:::

Module sets and module set releases belong to the library selected with the library selector next to the page title, and the "Module" menu is not available when the multi-tenant mode is enabled.
