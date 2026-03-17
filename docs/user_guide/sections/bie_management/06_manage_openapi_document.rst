Manage OpenAPI Document
------------------------
OpenAPI document management functionality allows end users to generate and merge multiple OpenAPI YML files into
one file. This functionality creates OpenAPI 3.x persistence layer in the connectCenter database and provides
UI to support API Specification Management.

Currently, connectCenter provides the OpenAPI document functionality to end users only.
Developers won't see this functionality once login as developer.
The end users can view, create, edit or discard OpenAPI Document through the OpenAPI Document menu under the BIE menu.

Create an OpenAPI Document
~~~~~~~~~~~~~~~~~~~~~~~~~~
To create an OpenAPI document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. On the returned "OpenAPI Document" page, click "New OpenAPI Document" located at the top-right
   of the page.

4. On the returned "Create OpenAPI Document" page, fill out the following fields:

   1. OpenAPI Version (Mandatory) Note, "3.0.3" is selected as default.
   2. Title (Mandatory)
   3. Document Version (Mandatory)
   4. Terms of Service A URI to the terms of service for the API
   5. Contact Name
   6. Contact URL
   7. Contact Email
   8. License Name
   9. License URL
   10. Description

5. Click the "Create" button.

Edit an OpenAPI Document
~~~~~~~~~~~~~~~~~~~~~~~~~
To edit an OpenAPI document:

1. On the top menu of the page, click "BIE".

2. Click "OpenAPI Document" menu item.

3. Use the *Title* or *Description* to find the desired OpenAPI Document.
   Open its "Edit OpenAPI Document" page by clicking the OpenAPI document title in Title column. See
   also `How to use the Search field in general <#how-to-use-the-search-field-in-general>`__.

4. You can change the *Title*, *Document Version*, *Terms of Service*, *Contact Name*, *Contact URL*, *Contact Email*
   , *License Name*, *License URL* and *Description* fields.

5. Click the "Update" button.


Discard an OpenAPI Document
~~~~~~~~~~~~~~~~~~~~~~~~~~~

There are two methods for discarding an OpenAPI Document. The first one is:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the OpenAPI document you want to discard. Use the *Title*, *Description*,*Updater*, *Updated start date*, or *Updated end date* search filters
   to help locate the desired OpenAPI document.
   (see `How to use Search Filters <#how-to-use-search-filters>`__). Click on the checkbox right before the desired
   OpenAPI document.

4. Click the "Discard" button at the top-right of the page.

5. A dialog is open where you can confirm or cancel the request.

The second method is:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the OpenAPI Document you want to discard. Use the *Title*, *Description*,
   *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired OpenAPI Document.
   (see `How to use Search Filters <#how-to-use-search-filters>`__). Click on the OpenAPI Document title to open its
   "Edit OpenAPI Document" page.

4. Click the "Discard" button.

5. A dialog is open where you can confirm or cancel the request.

Add BIEs to OpenAPI Document
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To add BIEs to an OpenAPI Document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired
   OpenAPI Document. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the title to open its "Edit OpenAPI Document" page.

4. Click the "Add" button.

5. On the newly opened "Add BIE For OpenAPI Document" page, locate the desired top-level BIE. Use the *DEN*, *Business Context*, *Branch*, *State*, *Owner*, *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired BIE. (see `How to use Search
   Filters <#how-to-use-search-filters>`__).

6. Select the desired BIE node. Select the required Verb option and the required Message Body option from
   the dropdown list.

7. Check the Array Indicator box if needed.

8. Click the "Add" button.


View/Edit BIE List of an OpenAPI Document
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view / edit all the BIEs currently assigned to an OpenAPI document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired
   OpenAPI Document. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the title to open its "Edit OpenAPI Document" page.

4. In the BIE List, select the desired BIE node, the *Resource Name*, *Operation ID*, *Tag Name*
   columns can be modified.

5. Check or uncheck the *Array Indicator* or *Suppress Root* option.

6. Click the "Update" button.


Remove BIEs from an OpenAPI Document
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To remove BIEs from an OpenAPI Document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired
   OpenAPI Document. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the title to open its "Edit OpenAPI Document" page.

4. In the BIE List, select the desired BIE node and click the "Remove" button.


OpenAPI YML Expression generation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The end user uses OpenAPI document to represent the selected BIEs into OpenAPI 3.0.3 syntax.

To generate an OpenAPI YML file:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired
   OpenAPI Document. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the title to open its "Edit OpenAPI Document" page.

4. Click the "Generate" button.

5. A YML file with the filename format: title-version-timestamp.yml will be saved to the local drive.

