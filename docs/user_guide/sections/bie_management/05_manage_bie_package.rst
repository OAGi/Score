Manage BIE Package
------------------
The BIE Package provides the functionality to manage multiple BIEs as a single package.
Like BIEs, BIE Packages also have independent states of WIP, QA, and Production, with functional changes based on ownership.
See `BIE States <#bie-states>`__ for more details.

The table below summarizes the actions and authorizations in each BIE package state.

+-------------+----------------------------------------+----------------------------------------+
| Role State  | Current Owner                          | Other Users                            |
+=============+========================================+========================================+
| WIP         | Restrict the BIE Package.              |                                        |
|             |                                        |                                        |
|             | Change its state to QA.                |                                        |
|             |                                        |                                        |
|             | Express it.                            |                                        |
|             |                                        |                                        |
|             | Transfer ownership.                    |                                        |
|             |                                        |                                        |
|             | Discard it.                            |                                        |
+-------------+----------------------------------------+----------------------------------------+
| QA          | View its details.                      | View its details.                      |
|             |                                        |                                        |
|             | Change its state back to WIP or        |                                        |
|             | advance to Production.                 |                                        |
|             |                                        |                                        |
|             | Express it.                            | Express it.                            |
+-------------+----------------------------------------+----------------------------------------+
| Production  | View its details.                      | View its details.                      |
|             |                                        |                                        |
|             | Express it.                            | Express it.                            |
+-------------+----------------------------------------+----------------------------------------+

Create a BIE package
~~~~~~~~~~~~~~~~~~~~
To create a BIE package:

1. On the top menu of the page, click "BIE".

2. Choose "BIE Package" from the drop-down list.

3. On the returned "BIE Package" page, click the "New BIE Package" button located at the top-right
   of the page.

4. On the returned "Create BIE Package" page, fill out the following
   fields:

   1. *Package Name* (Mandatory). This is the name of the BIE package.

   2. *Version ID* (Mandatory). This is the identifier assigned to the
      package version.

   3. *Version Name* (Mandatory). This is the descriptive name of the
      package version.

   4. *Description* (Optional). This is a free-form description of the
      package.

   The combination of *Package Name*, *Version ID*, and *Version Name*
   must be unique.

5. Click the "Create" button.

Edit a BIE Package
~~~~~~~~~~~~~~~~~~
To edit a BIE package:

1. On the top menu of the page, click "BIE".

2. Choose "BIE Package" from the drop-down list.

3. Use the *Package Name*, *Version ID*, *Version Name*, or *Package Description* to find the desired BIE package.
   Open its "Edit BIE Package" page by clicking the BIE package name in the *Package Name* column. See
   also `How to use the Search field in general <#how-to-use-the-search-field-in-general>`__.

4. You can change the *Package Name*, *Version ID*, *Version Name*, and *Package Description* fields.

5. Click the "Update" button.


Discard a BIE Package
~~~~~~~~~~~~~~~~~~~~~

There are two methods for discarding a BIE package. The first one is:

1. On the top menu of the page, click "BIE".

2. Choose "BIE Package" from the drop-down list.

3. Locate the BIE package you want to discard. Use the *Package Name*, *Version ID*, *Version Name*, *Package Description*,
   *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE package.
   (see `How to use Search Filters <#how-to-use-search-filters>`__). Click on the checkbox on the desired
   BIE package row in the table.

4. Click the "Discard" button at the top-right of the page.

5. A dialog is open where you can confirm or cancel the request.

The second method is:

1. On the top menu of the page, click "BIE".

2. Choose "BIE Package" from the drop-down list.

3. Locate the BIE package you want to discard. Use the *Package Name*, *Version ID*, *Version Name*, *Package Description*,
   *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE package.
   (see `How to use Search Filters <#how-to-use-search-filters>`__). Click on the ellipsis button on the desired
   BIE package row in the table.

4. Click the "Discard" button in the context menu that appears.

5. A dialog is open where you can confirm or cancel the request.


Add BIEs to BIE Package
~~~~~~~~~~~~~~~~~~~~~~~

A top-level BIE must be in the *Production* state before it can be added to a BIE Package.

To add BIEs to a BIE Package:

1. On the top menu of the page, click "BIE".

2. Choose "BIE Package" from the drop-down list.

3. Locate the BIE package you want to update. Use the *Package Name*, *Version ID*, *Version Name*, *Package Description*,
   *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE package.
   (see `How to use Search Filters <#how-to-use-search-filters>`__).
   Click on the package name to open its "Edit BIE Package" page.

4. Click the "Add" button.

5. On the newly opened "Add BIE" page, locate the desired top-level BIE.
   Use the *DEN*, *Business Context*, *Branch*, *Owner*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE.
   (see `How to use Search Filters <#how-to-use-search-filters>`__).
   This page shows only BIEs in the *Production* state.

6. Select the desired BIE node.

7. Click the "Add" button.


Remove BIEs from a BIE Package
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To remove BIEs from a BIE Package:

1. On the top menu of the page, click "BIE".

2. Choose "BIE Package" from the drop-down list.

3. Locate the BIE package you want to discard. Use the *Package Name*, *Version ID*, *Version Name*, *Package Description*,
   *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE package.
   (see `How to use Search Filters <#how-to-use-search-filters>`__).
   Click on the package name to open its "Edit BIE Package" page.

4. In the BIE List, select the desired BIE node, and click the "Remove" button.


BIE Package Schema Expression Generation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

BIE Package schema generation produces schema files for the BIEs that are included in the BIE Package.
The current page supports `XML Schema` and `JSON Schema`.
connectCenter generates schema files for the BIEs that have already been added to the BIE Package.

This page also uses several fixed generation rules.
The package export always generates each BIE schema as an individual file, always uses separate file references for reused schemas,
and always includes the business context and version in generated filenames.
When `JSON Schema` is selected, the JSON Schema version is fixed to `2020-12`.

To generate BIE Package schemas:

1. On the top menu of the page, click "BIE".

2. Choose "BIE Package" from the drop-down list.

3. Locate the BIE package you want to generate from. Use the *Package Name*, *Version ID*, *Version Name*, *Package Description*,
   *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE package.
   (see `How to use Search Filters <#how-to-use-search-filters>`__).
   Click on the package name to open its "Edit BIE Package" page.

4. In the generation options area, choose either `XML Schema` or `JSON Schema`.

5. Click the "Generate" button.

The generated result is downloaded to the local drive.
