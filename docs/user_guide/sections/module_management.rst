Module Management
=================

Module Management has three major functions Manage Module-Release Assignment, Manage CC-Module Assignment, and Manage Module Dependency.
The data associated with these two functions are used for serializing the developer CCs (including DT, Code Lists, and Agency ID List) into files.
The data can be managed only by developers.
End users can access Module Management related pages and data but they cannot make any change.

**Note:** Because entities related module management have no ownership.
Multiple developers may edit them at the same time.

Overview of Module Management Entities
--------------------------------------

Module
~~~~~~

Module is generally a path to a directory or a file.
Files are shown without a file extension such as ‘xsd’ or ‘json’, because they are meant to be syntax independent.

A module can belong to one and only one module set.
Therefore, even though a module may have the same path, they are different module entities.

Module Set
~~~~~~~~~~

Module Set is a collection modules.
Typically, at least one module set is created per a release of CCs although this is not necessary as a new release may reuse prior module set particularly when there is no new file.
Multiple module sets may be created for a release for different directory structures or CC assignments are needed, for example, for different expressions.
An expression means a serialization of the CCs into files.
One syntax may have more than one expression.
For example, the connectSpec standard has multiple expressions in XML schema, one using global-type-global-element pattern, another using global-type-local-element pattern.

Module Set Release
~~~~~~~~~~~~~~~~~~

Module Set Release associates a module set with a release.
It allows a module set to be reused across releases.
This can save time when there is no new file or changes in the directory structure in a newer release.
The entity also facilitates the CC assignments to (file) modules in the module set as CC assignments have to be done in a context of a release.

Manage Module Set
-----------------

The Module Set Management functionality allows developers to create and assign modules to a module set.

Find a Module Set
~~~~~~~~~~~~~~~~~

To find a Module Set:

1. On the "Module" menu on the top, click the "View/Edit Module Set"
   menu item if you are a developer, or the "View Module Set" menu item
   if you are an end user.

2. The "Module Set" page is open listing all Module Sets.

3. Use the `Search Filters <#how-to-use-search-filters>`__ at the top of
   the page to search for the desired Module Set.

View detail of a Module Set
~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. `Find a Module Set <#find-a-module-set>`__.

2. Click on the name of the Module Set to open the "Edit Module Set"
   page.

Create a new Module Set
~~~~~~~~~~~~~~~~~~~~~~~

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

Edit detail of a Module Set
~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. `View detail of a Module Set <#view-detail-of-a-module-set>`__.

2. The fields of the Module Set may be updated as follows:

   1. In the top panel of the page, the detail of a Module Set can be
      edited as follows:

      1. *Name*. Specify the name of the Module Set. The *Name* field is
         a free form text. This field is mandatory.

      2. *Definition*: Specify the description of the Module Set. The
         *Definition* field is a free form text. This field is optional.

   2. In the bottom panel of the page, you can:

      1. `Add a Module File or Module
         Directory <#add-a-module-file-or-module-directory>`__.

      2. `Edit detail of a Module <#edit-detail-of-a-module>`__ File.

      3. `Edit detail of a Module
         Directory <#edit-detail-of-a-module-directory>`__.

      4. `Discard a Module File or Module
         Directory <#discard-a-module-file-or-module-directory>`__.

Add a Module File or Module Directory
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To add a Module to a Module Set you should be on the page where you `edit detail of a Module Set <#create-a-new-module-set>`__.
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
         list. Only `developer Namespaces <#namespace-management>`__ can
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

Edit detail of a Module
~~~~~~~~~~~~~~~~~~~~~~~

To edit detail of a Module:

1. Mouse over the module and click on the pencil icon next to the
   desired Module.

2. In the returned dialog, you can edit:

   a. *Name*. This is a freeform text field that allows to specify the
      name of the Module. It is mandatory and its value should be unique
      within the module path (i.e., the Module Directory and column).

   b. *Namespace*: Choose the namespace of the Module from the dropdown
      list. Only `developer Namespaces <#namespace-management>`__ can be
      selected. This field is optional.

   c. *Version*. This field is a freeform text representing the version
      of the Module. This field is optional.

3. Click "Update" to store the changes.

Edit detail of a Module Directory
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To edit detail of a Module Directory:

1. Click on the pencil icon next to the desired Module Directory

2. In the returned dialog, edit the *Name* field. This is a freeform
   text field for the directory name. It is mandatory and its value
   should be unique within the module path (i.e., the column).

3. Click "Update" to save the change.

Discard a Module File or Module Directory
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A Module or Module Directory can be discarded only if it is not used (i.e., no CC assigned to) in a Module Set Release.

To discard a Module File or Module Directory:

1. Click on the pencil icon next to the desired Module File or Module
   Directory

2. In the returned dialog, click the "Discard" button.

3. Confirm your intention to discard the Module File or Module
   Directory.

When discarding a Module File that is already used in a Module Set Release and has some CCs assigned, the user should confirm the intention for discarding the CCs’ assignments as well.
The same applies when discarding a Module Directory that contains in its path a Module File used in a Module Set Release and has some CCs assigned.

Discard a Module Set
~~~~~~~~~~~~~~~~~~~~

A Module Set can be discarded only if it is not assigned to any `Module Set Release <#manage-module-set-release>`__.

To discard a Module Set:

1. Go to the "Module Set" page by clicking the "View/Edit Module Set"
   menu item under the "Module" menu.

2. See `Find a Module Set <#find-a-module-set>`__ for help in locating a
   Module Set.

3. Click on the ellipsis in the last column of the desired Module Set
   and select the "Discard" menu item in the pop-up menu.

4. In the returned dialog, confirm your intention to discard the Module
   Set.

Manage Module Set Release
-------------------------

Find a Module Set Release
~~~~~~~~~~~~~~~~~~~~~~~~~

To find a Module Set Release:

1. On the "Module" menu at the top, click the "View/Edit Module Set
   Release" menu item if you are a developer, or the "View Module Set
   Release" menu item if you are an end user.

2. The "Module Set Release" page is open listing all Module Set Release.

3. Use the `Search Filters <#how-to-use-search-filters>`__ at the top of
   the page to search for the desired Module Set Release.

View detail of a Module Set Release
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. `Find a Module Set Release <#find-a-module-set-release>`__.

2. Click on the name of the Module Set Release to open the "Edit Module
   Set Release" page.

Create a new Module Set Release
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
      field is mandatory. You might need to `create a new Module
      Set <#create-a-new-module-set>`__ first.

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

Assign CCs to a Module File
~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. `View detail of a Module Set
   Release <#view-detail-of-a-module-set-release>`__.

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
      `How to use search filters in
      general <#how-to-use-the-search-field-in-general>`__.

   3. Click the right arrow to assign the selected the selected CC. At
      this point, you can see the CC listed in the Assigned section.

Unassign a CC from a Module File
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. `View detail of a Module Set
   Release <#view-detail-of-a-module-set-release>`__.

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
      See `How to use search filters in
      general <#how-to-use-the-search-field-in-general>`__.

   3. Click the left arrow to unassign the selected CCs. At this point,
      you can see the CCs moved back from the Assigned section to the
      Unassigned section.

Validate a Module Set Release
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Developers can validate the generated XML Schema or JSON Schema for the Module Set Release.
See `Export a Module Set Release <#export-a-module-set-release>`__ for generating schemas.
To validate:

1. `View detail of a Module Set
   Release <#view-detail-of-a-module-set-release>`__.

2. Click the "Validate" button.

3. Choose either "XML Schema" or "JSON Schema" from the menu.

Export a Module Set Release
~~~~~~~~~~~~~~~~~~~~~~~~~~~

This section describes how to export a Module Set Release as XML Schema or JSON Schema.

1. `View detail of a Module Set
   Release <#view-detail-of-a-module-set-release>`__.

2. Click the "Export" button and choose either "XML Schema" or "JSON Schema".
   If the Module Set Release is properly
   configured, a zip file containing schema is downloaded by the
   browser.

Note that the process of exporting a Module Set Release will take some time.

Discard a Module Set Release
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To discard a Module Set Release:

1. Go to the "Module Set Release" page by clicking the "View/Edit Module
   Set Release" menu item under the "Module" menu.

2. See `Find a Module Set Release <#find-a-module-set-release>`__ for
   help in locating a Module Set Release.

3. Click on the ellipsis in the last column of the desired Module Set
   Release and select the "Discard" menu item in the pop-up menu.

4. In the returned dialog, confirm your intention to discard the Module
   Set Release. All CC assignments within the Module Set Release will be
   lost.
