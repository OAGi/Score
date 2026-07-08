---
title: "Manage Module Set"
sidebar_position: 2
---

The Module Set Management functionality allows developers to create and assign modules to a module set.

## Find a Module Set

To find a Module Set:

1. On the "Module" menu on the top, click the "View/Edit Module Set"
   menu item if you are a developer, or the "View Module Set" menu item
   if you are an end user.

2. The "Module Set" page is open listing all Module Sets.

3. Use the [Search Filters](../bie-management/10-common-functions.md#how-to-use-search-filters) at the top of
   the page to search for the desired Module Set.

## View detail of a Module Set

1. [Find a Module Set](#find-a-module-set).

2. Click on the name of the Module Set to open the "Edit Module Set"
   page.

## Create a new Module Set

To create a Module Set:

1. Go to the "Module Set" page by clicking the "View/Edit Module Set"
   menu item under the "Module" menu.

2. Click the "New Module Set" button.

3. In the returned "Create Module Set" page:

    1. *Name*. Specify the name of the Module Set. The *Name* field is a
       free form text. This field is mandatory.

    2. *Definition*: Specify the description of the Module Set. The
       *Description* field is a free form text. This field is optional.

    3. *Create Module Set Release*. This checkbox allows to create a
       Module Set Release and assign to it this new Module Set. This
       checkbox is optional. However, if it is checked, the following
       fields must also be set:

        1. *Release*. Select the Release that you want to use in the new
           Module Set Release by selecting it from the dropdown list.

        2. *Copy CC assignment from Module Set Release*. Select a Module
           Set Release from the dropdown list. If the module set release
           already have the CCs assignments, connectCenter will copy those
           assignments to the new Module Set Release where the CC GUIDs
           and the modules across the Module Set Releases match.

It should be noted that Module Set Release can be also created and CC assignment can be also copied later on.

## Edit detail of a Module Set

1. [View detail of a Module Set](#view-detail-of-a-module-set).

2. The fields of the Module Set may be updated as follows:

    1. In the top panel of the page, the detail of a Module Set can be
       edited as follows:

        1. *Name*. Specify the name of the Module Set. The *Name* field is
           a free form text. This field is mandatory.

        2. *Definition*: Specify the description of the Module Set. The
           *Definition* field is a free form text. This field is optional.

    2. In the bottom panel of the page, you can:

        1. [Add a Module File or Module
           Directory](#add-a-module-file-or-module-directory).

        2. [Edit detail of a Module](#edit-detail-of-a-module) File.

        3. [Edit detail of a Module
           Directory](#edit-detail-of-a-module-directory).

        4. [Discard a Module File or Module
           Directory](#discard-a-module-file-or-module-directory).

## Add a Module File or Module Directory

To add a Module to a Module Set you should be on the page where you [edit detail of a Module Set](#create-a-new-module-set).
A Module File or a Module Directory can be added to a Module Directory creating a hierarchical files and directory structure.

This hierarchical structure of Module Directories and Modules is displayed at the bottom of the page via different columns.
The first column at the left side is the root.

1. A Module Directory or Module File can be added in the first column or
   in any other column that corresponds to a Module Directory. To do so,
   click "+Add" located inside the desired column.

2. A dialog pops up where the following selections can be made.

    1. *Create new module file*. Click on this option to create a new
       Module File within the column. The following fields can be edited:

        1. *Name*:. This is a freeform text field that specifies the name
           of the file. It is mandatory and its value should be unique
           within the column.

        2. *Namespace*: Choose the namespace of the Module of the dropdown
           list. Only [developer Namespaces](../core-component-management/developer/02-namespace-management.md) can
           be selected. It may be used in certain expressions. This field
           is required.

        3. *Version*. This field is a freeform text representing the
           version of the Module. It may be serialized as a version number
           of the file in certain expressions. This field is optional.

    2. *Create new module directory*. Click on this option to create a
       new Module Directory within the column. In the *Name* field,
       specify the directory name. It is a freeform text and is
       mandatory. The name of a Module Directory should be unique in the
       module path (i.e., within the column).

    3. *Copy from a module set*. This option allows for adding modules
       from an existing Module Set. This functionality copies the
       selected Module Files and Module Directories from an existing
       Module Set to the current Module Set. Copy function is idempotent,
       i.e., the developer can copy the same Module Directory or Module
       File multiple times and only the ones that are not already
       existing in the current Module Set are added. To copy a Module
       File or a Module Directory:

        1. From the dropdown list, select the existing Module Set from
           which you want to copy.

        2. In the returned table that depicts the hierarchical structure
           of the Modules of the selected Module Set, you can navigate
           through the Module Directories to find the Module File or
           Module Directory you want to copy. Make sure that it is
           selected, click "Copy". If a Module Directory is selected,
           there is an checkbox next to the "Copy" button to specify if
           you want to copy all of its submodules. When the checkbox is
           enabled, connectCenter adds the selected Module Directory and all its
           submodules ensuring that there are no duplicate values.

## Edit detail of a Module

To edit detail of a Module:

1. Mouse over the module and click on the pencil icon next to the
   desired Module.

2. In the returned dialog, you can edit:

    a. *Name*. This is a freeform text field that allows to specify the
       name of the Module. It is mandatory and its value should be unique
       within the module path (i.e., the Module Directory and column).

    b. *Namespace*: Choose the namespace of the Module from the dropdown
       list. Only [developer Namespaces](../core-component-management/developer/02-namespace-management.md) can be
       selected. This field is optional.

    c. *Version*. This field is a freeform text representing the version
       of the Module. This field is optional.

3. Click "Update" to store the changes.

## Edit detail of a Module Directory

To edit detail of a Module Directory:

1. Click on the pencil icon next to the desired Module Directory

2. In the returned dialog, edit the *Name* field. This is a freeform
   text field for the directory name. It is mandatory and its value
   should be unique within the module path (i.e., the column).

3. Click "Update" to save the change.

## Discard a Module File or Module Directory

A Module or Module Directory can be discarded only if it is not used (i.e., no CC assigned to) in a Module Set Release.

To discard a Module File or Module Directory:

1. Click on the pencil icon next to the desired Module File or Module
   Directory

2. In the returned dialog, click the "Discard" button.

3. Confirm your intention to discard the Module File or Module
   Directory.

When discarding a Module File that is already used in a Module Set Release and has some CCs assigned, the user should confirm the intention for discarding the CCs’ assignments as well.
The same applies when discarding a Module Directory that contains in its path a Module File used in a Module Set Release and has some CCs assigned.

## Discard a Module Set

A Module Set can be discarded only if it is not assigned to any [Module Set Release](./03-manage-module-set-release.md).

To discard a Module Set:

1. Go to the "Module Set" page by clicking the "View/Edit Module Set"
   menu item under the "Module" menu.

2. See [Find a Module Set](#find-a-module-set) for help in locating a
   Module Set.

3. Click on the ellipsis in the last column of the desired Module Set
   and select the "Discard" menu item in the pop-up menu.

4. In the returned dialog, confirm your intention to discard the Module
   Set.
