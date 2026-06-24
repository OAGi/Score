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

   1. OpenAPI Version (Mandatory) Note, "3.1.1" is selected as default.
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

7. Check the Array Indicator box if needed. When you add the BIE, an *Operation ID* is generated
   automatically in the form ``<verb><BIEName>`` (for example ``createItemInstance``) using the
   selected *Verb*; checking the Array Indicator box appends a ``List`` suffix (for example
   ``createItemInstanceList``). You can change the *Operation ID* later in the *Endpoint Details* panel.

8. Click the "Add" button.


Configure Security Schemes
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The "Edit OpenAPI Document" page provides a *Security Schemes* section between the
document information fields and the "Endpoint Details" table. Here you can declare the
security schemes that the generated OpenAPI document offers and choose how they apply to
the document and to individual operations. When no scheme is configured, a hint reads
"No scheme configured — the default OAuth 2.0 scheme will be used." and the generated
output keeps the default OAuth 2.0 scheme.

To add a security scheme:

1. Open the desired OpenAPI Document's "Edit OpenAPI Document" page.

2. In the *Security Schemes* section, click the "Add Security Scheme" button.

3. On the returned "Add Security Scheme" dialog, select a *Type* and fill out the
   type-specific fields:

   1. *Type* (Mandatory). One of *API Key*, *HTTP*, *OAuth 2.0*, or *OpenID Connect*.
   2. *Scheme Name*. The name used to reference this scheme in security requirements.
   3. *Description (optional)*.
   4. For *API Key*: *In* (*Query*, *Header*, or *Cookie*) and *Name*.
   5. For *HTTP*: *Scheme* (*Basic* or *Bearer*); when *Bearer* is selected, an optional
      *Bearer Format* (for example, JWT) field appears.
   6. For *OpenID Connect*: *OpenID Connect URL*.
   7. For *OAuth 2.0*: an *OAuth Flows* editor. Click the add icon next to *OAuth Flows*
      to add a flow, choose its *Flow Type*, fill the applicable *Authorization URL*,
      *Token URL* and optional *Refresh URL*, and add one or more *Scopes* (each with a
      *Scope* name and *Description*).

4. Click the "Add" button.

Each saved scheme appears as a card in the *Security Schemes* section. Click a card to
re-open the "Edit Security Scheme" dialog, or click the minus icon on the card to remove
the scheme.

To set the document-level security requirement:

1. In the *Security Schemes* section, click the "Document Security" button. Its label
   shows a summary of the current requirement (or "None").

2. On the returned "Document Security" dialog, build the requirement:

   1. Within a single requirement, click "Add scheme (AND)" to require more than one
      scheme together. For *OAuth 2.0* and *OpenID Connect* schemes you can also select
      the required *Scopes*.
   2. Click "Add Alternative (OR)" to add an alternative requirement.
   3. Check "Allow anonymous ({})" to allow access without authentication.

3. Click the "Apply" button.

4. Click the "Update" button on the "Edit OpenAPI Document" page to save the changes.


Set Operation Security
~~~~~~~~~~~~~~~~~~~~~~~

The "Endpoint Details" table includes a *Security* column that lets you control the
security of each operation. When no security scheme is configured, the column shows a
dash (—); otherwise it shows *Inherited*, *Public*, or a summary of the operation's own
requirement.

To set the security of an operation:

1. In the "Endpoint Details" table, click the *Security* cell of the desired operation.

2. On the returned "Operation Security" dialog, choose one of:

   1. *Use document security* — the operation inherits the document-level requirement.
   2. *No security for this operation* — the operation is public.
   3. *Override with selected schemes* — build a custom requirement (the same
      "Add scheme (AND)", "Add Alternative (OR)" and "Allow anonymous ({})" controls as
      the `Document Security <#configure-security-schemes>`__ dialog).

3. Click the "Apply" button.

4. Click the "Update" button on the "Edit OpenAPI Document" page to save the changes.


Add Operations without a BIE
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Some API operations, such as ``DELETE`` and ``PATCH``, do not need to reference a BIE because they
carry no message body. You can add such operations directly to an OpenAPI Document.

To add an operation that does not reference a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*, *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired
   OpenAPI Document. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the title to open its "Edit OpenAPI Document" page.

4. In the *Endpoint Details* panel, click the "Add Operation" button.

5. On the returned "Add Operation" dialog, fill out the following fields:

   1. *Verb* (Mandatory) Choose ``DELETE`` or ``PATCH``.
   2. *Resource Name (Path)* (Mandatory) The endpoint path, for example ``/production-order/{id}``.
      Path segments wrapped in braces (``{...}``) become path parameters.
   3. *Operation ID* (Mandatory) Auto-generated from the verb and the last non-variable path segment
      (for example, ``PATCH /production-order/{id}`` becomes ``updateProductionOrder``). You can override it.
   4. *Tag* (Optional)
   5. *Summary* (Optional)

6. Click the "Add" button.

The new operation is listed in the *Endpoint Details* panel with an empty *DEN* cell. Because the
operation does not reference a BIE, its *Array Indicator*, *Suppress Root*, and *Message Body*
controls are disabled. You can still edit its *Verb*, *Resource Name*, *Operation ID*, and
*Tag Name*, and remove it like any other row.

When you generate the OpenAPI YML file, the operation is produced with no request body and a
status-only response derived from the verb: ``DELETE`` produces ``202 Accepted`` and ``PATCH``
produces ``204 No Content``.


View/Edit Endpoint Details of an OpenAPI Document
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view / edit all the operations currently assigned to an OpenAPI document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired
   OpenAPI Document. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the title to open its "Edit OpenAPI Document" page.

4. In the *Endpoint Details* panel, select the desired row; the *Resource Name*, *Operation ID*, *Tag Name*
   columns can be modified.

5. Check or uncheck the *Array Indicator* or *Suppress Root* option.

6. Click the "Update" button.


Operation ID naming and validation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In the *Endpoint Details* panel of an OpenAPI Document, each operation has an *Operation ID*. connectCenter
generates it automatically in the form ``<verb><BIEName>`` and keeps it in sync as you edit the
row:

1. The leading verb word comes from the selected *Verb*: ``GET`` becomes *query*, ``POST`` becomes
   *create*, ``PUT`` becomes *replace*, ``PATCH`` becomes *update*, and ``DELETE`` becomes *delete*.
   For the BIE *Item Instance*, for example, the GET operation is ``queryItemInstance`` and the POST
   operation is ``createItemInstance``.

2. When you change the *Verb*, the verb word in the *Operation ID* is updated automatically while the
   BIE-name part is kept (so any name you typed yourself is preserved).

3. When the *Array Indicator* is checked, a ``List`` suffix is added to the *Operation ID* (for
   example ``queryItemInstanceList``); unchecking it removes the suffix.

4. You can override the generated value at any time by typing directly in the *Operation ID* column.

The *Operation ID* is required and must be unique within the document:

1. If the *Operation ID* is left empty, the cell shows "Operation ID is required." and clicking the
   "Update" button is blocked; a notification reading "Operation ID is required." also appears.

2. If two operations in the same document have the same *Operation ID*, the affected cells show
   "Operation ID must be unique within the document."


Remove BIEs from an OpenAPI Document
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To remove BIEs from an OpenAPI Document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired
   OpenAPI Document. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the title to open its "Edit OpenAPI Document" page.

4. In the *Endpoint Details* panel, select the desired BIE node and click the "Remove" button.


OpenAPI YML Expression generation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The end user uses OpenAPI document to represent the selected BIEs into OpenAPI 3.0.3 or 3.1.1 syntax.

To generate an OpenAPI YML file:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*,
   *Updated start date*, or *Updated end date* search filters to help locate the desired
   OpenAPI Document. (see `How to use Search
   Filters <#how-to-use-search-filters>`__). Click on the title to open its "Edit OpenAPI Document" page.

4. Click the "Generate" button.

5. A YML file with the filename format: title-version-timestamp.yml will be saved to the local drive.
   The generated file includes the configured security schemes under ``components.securitySchemes`` together
   with the document-level and per-operation ``security`` requirements. If no security scheme is configured,
   the default OAuth 2.0 scheme is used and the output is unchanged.

