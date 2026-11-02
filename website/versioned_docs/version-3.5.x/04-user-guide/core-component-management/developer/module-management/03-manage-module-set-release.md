---
title: "Manage Module Set Release"
sidebar_position: 3
---

## Find a Module Set Release

To find a Module Set Release:

1. On the "Module" menu at the top, click the "View/Edit Module Set
   Release" menu item if you are a developer, or the "View Module Set
   Release" menu item if you are an end user.

2. The "Module Set Release" page opens, listing the Module Set Releases
   of the library selected next to the page title.
   The "Default" column shows a "Y" for the default Module Set Release
   of each release, and the "Release Num" column links to the release.

   ![Module Set Release page listing module set releases with the Name, Release Num, Default, and Updated on columns and the New Module Set Release button](/img/user-guide/module_set_release_page.png)

3. Use the "Search by Name" bar at the top of the page to search for
   the desired Module Set Release.
   The chevron-down button at the right end of the search bar expands
   advanced filters: "Updater", "Updated start date", and "Updated end
   date".
   See also [How to use Search Filters](../../../bie-management/10-common-functions.md#how-to-use-search-filters).

## View detail of a Module Set Release

1. [Find a Module Set Release](#find-a-module-set-release).

2. Click on the name of the Module Set Release to open the "Edit Module
   Set Release" page (titled "View Module Set Release" for end users).

## Create a new Module Set Release

To create a Module Set Release:

1. Go to the "Module Set Release" page by clicking the "View/Edit
   Module Set Release" menu item under the "Module" menu.

2. Click the "New Module Set Release" button.

3. In the returned "Create Module Set Release" page, set:

    1. *Name*. This is a freeform text field for the name of the Module
       Set Release. It is optional; if it is left blank, it is
       automatically populated in the form "[Name of the selected
       Module Set] Module Set Release". A truncation rule is applied to
       avoid duplication of overlapping words. For instance, if the
       name of the selected Module Set is "Sample Module", the default
       name of the Module Set Release will be "Sample Module Set
       Release". If there is an existing Module Set Release with the
       same name as the one being entered for the same Release, the
       "Create module set release?" dialog is returned in order to
       confirm the intention to reuse the name.

    2. *Description*. Specify the description of the Module Set
       Release. This field is a free form text and is optional.

    3. *Module Set*. Select the Module Set from the dropdown list. This
       field is mandatory. You might need to [create a new Module
       Set](./02-manage-module-set.md#create-a-new-module-set) first.

    4. *Release*. Select the Release from the dropdown list. You can
       select any Release in any state, including the Working branch.
       This field is mandatory. Creating a Module Set Release for the
       Working or a draft Release branch allows for serialization of
       CCs before the release is published, for testing and validation.

    5. *Default*. Check the "Default" checkbox to make this new Module
       Set Release the default one for the selected Release (or
       branch). If a default Module Set Release already exists for the
       selected Release, the "Create default module set release?"
       dialog asks to confirm your intention; confirming moves the
       default flag to the new Module Set Release. The CC assignments
       in the default Module Set Release are used in related CC
       management functions, e.g., in the "Module" filter on the
       [View/Edit Core Component page](../../03-search-and-browse-cc-library.md).

    6. *Copy CC assignment from Module Set Release*. Checking this
       checkbox enables the "Module Set Release" dropdown below it,
       where the source Module Set Release must be selected. The new
       Module Set Release is then initialized with the CC assignments
       of the source: connectCenter matches the CCs across the two
       Releases by their GUID and the modules across the two Module
       Sets by their path, and assigns the matched CCs to the matched
       modules. The purpose of this is to save time on manual CC
       assignments, since most CCs of a new release are typically
       assigned to the same modules as in the previous release. It
       should be noted that this copying can be performed only at the
       Module Set Release creation.

       ![Create Module Set Release page with a Module Set and Release selected and the Copy CC assignment from Module Set Release checkbox checked with a source selected](/img/user-guide/module_set_release_create.png)

4. Click "Create". A "Created" message confirms the creation and the
   Module Set Release list page is reloaded.

## Edit detail of a Module Set Release

1. [View detail of a Module Set
   Release](#view-detail-of-a-module-set-release).

   ![Edit Module Set Release page with the Name, Description, Module Set, Release, and Default fields and the Update, Validate, Export, and Assign CCs buttons](/img/user-guide/module_set_release_detail.png)

2. The *Name*, *Description*, and *Default* fields can be updated; the
   *Module Set* and *Release* associations cannot be changed after
   creation. Unchecking *Default* leaves the Release without a default
   Module Set Release; checking it when another default exists asks for
   a confirmation, and confirming moves the default flag.

3. Click "Update" to save the changes. The button stays disabled until
   a change is made, and an "Updated" message confirms the save.

For end users the page is read-only: the "Update" button is not present and the "Assign CCs" button reads "View Assigned CCs", while "Validate" and "Export" remain available.

![View Module Set Release page of an end user with disabled fields and the Validate, Export, and View Assigned CCs buttons](/img/user-guide/eu_module_set_release_detail.png)

## Assign CCs to a Module File

1. [View detail of a Module Set
   Release](#view-detail-of-a-module-set-release).

2. Click the "Assign CCs" button.

3. The "Core Component Assignment" page is returned.
   The area at the top of the page allows you to navigate through the
   module path, column by column, to reach the Module Files and Module
   Directories of the Module Set used in the Module Set Release.
   The "Assign CCs" area at the bottom of the page has two sections:
   the "Unassigned" section on the left lists the CCs in the Release
   (ACCs, ASCCPs, BCCPs, DTs, code lists, agency ID lists, and XML
   schema built-in types) that have not been assigned to any module
   yet, and the "Assigned" section on the right lists the CCs assigned
   to the currently selected Module File.

   ![Core Component Assignment page with the module path columns at the top and the Unassigned and Assigned sections at the bottom](/img/user-guide/module_assign_ccs.png)

4. To assign a particular CC to a Module File:

    1. In the module path area, navigate to the desired Module File and
       select it. The heading of the Assigned section shows the name of
       the selected Module File.

    2. In the Unassigned section, find the CC you want to assign and
       click on its checkbox. You can select multiple CCs, and the
       checkbox in the table header selects all listed CCs. You can
       also use the "Type", "State", and "Den" filters at the top of
       the Unassigned section.

    3. Click the right-arrow button (with the "Assign" tooltip) between
       the two sections. An "Assigned" message confirms the action, and
       the CCs are moved to the Assigned section.

   ![Assign CCs area with the Unassigned section, the Assign and Unassign arrow buttons, and the Assigned section listing the CCs of the selected module file](/img/user-guide/module_assign_ccs_tables.png)

## Unassign a CC from a Module File

1. Open the "Core Component Assignment" page and select the desired
   Module File, as described in [Assign CCs to a Module
   File](#assign-ccs-to-a-module-file).

2. In the Assigned section, find the CC you want to unassign from the
   selected Module File and click on its checkbox. You can select
   multiple CCs and use the "Type", "State", and "Den" filters at the
   top of the Assigned section.

3. Click the left-arrow button (with the "Unassign" tooltip) between
   the two sections. An "Unassigned" message confirms the action, and
   the CCs are moved back to the Unassigned section.

## Validate a Module Set Release

Users can validate the schemas generated for the Module Set Release without downloading them.
To validate:

1. [View detail of a Module Set
   Release](#view-detail-of-a-module-set-release).

2. Click the "Validate" button.

3. Choose either "XML Schema" or "JSON Schema" from the menu.

4. The validation dialog first reports that the schemas are being
   generated, and then shows a progress bar with the running counts of
   valid and invalid files while each generated file is validated.
   Invalid files are listed in bold at the top with their error
   messages, and "All schemas are valid." is reported on success.
   The "Close" and "Copy to clipboard" buttons become available when
   the validation is done.

   ![Module Set Release XML Schema Validation dialog with a progress bar and a list of validated files](/img/user-guide/module_validate_dialog.png)

## Export a Module Set Release

This section describes how to export a Module Set Release as XML Schema or JSON Schema (draft 2020-12) files.

1. [View detail of a Module Set
   Release](#view-detail-of-a-module-set-release).

2. Click the "Export" button and choose either "XML Schema" or "JSON
   Schema" from the menu. If the Module Set Release is properly
   configured, a zip file containing the schema files, named after the
   Module Set, is downloaded by the browser.

   ![Export menu of the Edit Module Set Release page with the XML Schema and JSON Schema menu items](/img/user-guide/module_set_release_export_menu.png)

Note that the process of exporting a Module Set Release will take some time; a spinner is shown while the file is being prepared.

## Discard a Module Set Release

To discard a Module Set Release:

1. Go to the "Module Set Release" page by clicking the "View/Edit
   Module Set Release" menu item under the "Module" menu.

2. See [Find a Module Set Release](#find-a-module-set-release) for
   help in locating a Module Set Release.

3. Click on the ellipsis in the last column of the desired Module Set
   Release and select the "Discard" menu item in the pop-up menu.

4. In the returned "Discard Module Set Release?" dialog, confirm your
   intention by clicking "Discard". All CC assignments within the
   Module Set Release will be lost. The Module Set itself and its
   modules are not affected.
