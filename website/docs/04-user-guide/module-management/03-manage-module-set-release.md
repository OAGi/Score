---
title: "Manage Module Set Release"
sidebar_position: 3
---

## Find a Module Set Release

To find a Module Set Release:

1. On the "Module" menu at the top, click the "View/Edit Module Set
   Release" menu item if you are a developer, or the "View Module Set
   Release" menu item if you are an end user.

2. The "Module Set Release" page is open listing all Module Set Release.

3. Use the [Search Filters](../bie-management/10-common-functions.md#how-to-use-search-filters) at the top of
   the page to search for the desired Module Set Release.

## View detail of a Module Set Release

1. [Find a Module Set Release](#find-a-module-set-release).

2. Click on the name of the Module Set Release to open the "Edit Module
   Set Release" page.

## Create a new Module Set Release

To create a Module Set Release:

1. Go to the "Module Set Release" page by clicking the "View/Edit Module
   Set Release" menu item under the "Module" menu.

2. Click the "New Module Set Release" button.

3. In the returned "Create Module Set Release Page" set:

    1. *Name*. This is a freeform text field that allows to specify the
       name of the Module Set Release. It is mandatory and if it is left
       blank, it is automatically populated in the form "[Name of the
       Selected Module Set] Module Set Release. A truncation rule is also
       applied to avoid duplication of the "Module Set". For instance, if
       the name of the selected Module Set is "Sample Module", the
       default name of the Module Set Release will be "Sample Module Set
       Release". If there is an existing Module Set Release with the same
       name as the one being created, a dialog is returned in order to
       confirm the intention to set the same name as an existing Module
       Set Release.

    2. *Module Set*. Select the Module Set from the dropdown list. This
       field is mandatory. You might need to [create a new Module
       Set](./02-manage-module-set.md#create-a-new-module-set) first.

    3. *Release*: Select the Release from the dropdown list. You can
       select any Release in any state including the Working branch. This
       field is mandatory. Creating a Module Set Release for the Working
       or a Draft Release branch allows for serialization of CCs before
       the release is published for testing and validation.

    4. *Default*: Check/uncheck the *Default* checkbox to make this new
       Module Set Release the default one for the Release (or branch). If
       a default Mode Set Release already exists for the selected
       release, the system asks to confirm your intention to change. The
       CC assignments in this Module Set Release will be used in related
       CC management functions, e.g., in the Module filter on the
       View/Edit Core Component page.

    5. *Copy CC assignment from Module Set Release*. Checking this
       checkbox will result in the new Module Set Release initialized
       with CC assignments based on a the selected Module Set Release.
       connectCenter will try to match the modules and CCs across the Module Set
       Release and assign the same CCs to the same module. The purpose of
       this is to save a lot of time from manual CC assignments when most
       of CCs are the same as previous release and are typically assigned
       to modules the same way as the previous release. It should be
       noted that this copying can be performed only at this Module Set
       Release creation.

## Assign CCs to a Module File

1. [View detail of a Module Set
   Release](#view-detail-of-a-module-set-release).

2. Click the "Assign CCs" button.

3. The "Core Component Assignment" page is returned. This page consists
   of two tables. The table at the top of the page (i.e., module path
   tree) allows you to navigate through the module path to view the
   Module Files and Modules directories of the Module Set used in the
   Module Set Release. The table at the bottom of the page (i.e., Assign
   CCs table) has two sections on the left and the right. The left
   section (i.e., Unassigned section) lists the CCs in the Release that
   have not been assigned to any module yet . The right section (i.e.,
   Assigned section) lists CCs assigned to a currently selected Module
   File.

4. To assign a particular CC to a Module File.

    1. In the module path tree table find the desired Module File and
       select.

    2. In the Unassigned section, find the CC you want to assign to the
       selected Module File and click on its checkbox. You can select
       multiple CCs to assign the selected Module. You can also use the
       Type and DEN filters on the top of the Unassigned section. See
       [How to use search filters in
       general](../bie-management/10-common-functions.md#how-to-use-the-search-field-in-general).

    3. Click the right arrow to assign the selected the selected CC. At
       this point, you can see the CC listed in the Assigned section.

## Unassign a CC from a Module File

1. [View detail of a Module Set
   Release](#view-detail-of-a-module-set-release).

2. Click the "Assign CCs" button.

3. The "Core Component Assignment" page is returned. This page consists
   of two tables. The table at the top of the page (i.e., module path
   tree) allows you to navigate through the module path to view the
   Module Files and Modules directories of the Module Set used in the
   Module Set Release. The table at the bottom of the page (i.e., Assign
   CCs table) has two sections on the left and the right. The left
   section (i.e., Unassigned section) lists the CCs in the Release that
   have not been assigned to any module yet . The right section (i.e.,
   Assigned section) lists CCs assigned to a currently selected Module
   File.

4. To unassign a particular CC from a Module File.

    1. In the module path tree table find the desired Module File from
       which the CC needs to be unassigned and select it.

    2. In the Assigned section, find the CC you want to unassign from the
       selected Module File and click on its checkbox. You can select
       multiple CCs to unassign from the selected Module. You can also
       use the Type and DEN filters on the top of the Assigned section.
       See [How to use search filters in
       general](../bie-management/10-common-functions.md#how-to-use-the-search-field-in-general).

    3. Click the left arrow to unassign the selected CCs. At this point,
       you can see the CCs moved back from the Assigned section to the
       Unassigned section.

## Validate a Module Set Release

Developers can validate the generated XML Schema or JSON Schema for the Module Set Release.
See [Export a Module Set Release](#export-a-module-set-release) for generating schemas.
To validate:

1. [View detail of a Module Set
   Release](#view-detail-of-a-module-set-release).

2. Click the "Validate" button.

3. Choose either "XML Schema" or "JSON Schema" from the menu.

## Export a Module Set Release

This section describes how to export a Module Set Release as XML Schema or JSON Schema.

1. [View detail of a Module Set
   Release](#view-detail-of-a-module-set-release).

2. Click the "Export" button and choose either "XML Schema" or "JSON Schema".
   If the Module Set Release is properly
   configured, a zip file containing schema is downloaded by the
   browser.

Note that the process of exporting a Module Set Release will take some time.

## Discard a Module Set Release

To discard a Module Set Release:

1. Go to the "Module Set Release" page by clicking the "View/Edit Module
   Set Release" menu item under the "Module" menu.

2. See [Find a Module Set Release](#find-a-module-set-release) for
   help in locating a Module Set Release.

3. Click on the ellipsis in the last column of the desired Module Set
   Release and select the "Discard" menu item in the pop-up menu.

4. In the returned dialog, confirm your intention to discard the Module
   Set Release. All CC assignments within the Module Set Release will be
   lost.
