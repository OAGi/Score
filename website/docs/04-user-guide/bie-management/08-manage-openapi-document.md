---
title: "Manage OpenAPI Document"
sidebar_position: 8
---

OpenAPI document management functionality allows end users to generate and merge multiple OpenAPI YML files into one file. This functionality creates OpenAPI 3.x persistence layer in the connectCenter database and provides UI to support API Specification Management.

Currently, connectCenter provides the OpenAPI document functionality to end users only. Developers won't see this functionality once login as developer. The end users can view, create, edit or discard OpenAPI Document through the OpenAPI Document menu under the BIE menu.

## Create an OpenAPI Document

To create an OpenAPI document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. On the returned "OpenAPI Document" page, click "New OpenAPI Document" located at the top-right of the page.

4. On the returned "Create OpenAPI Document" page, fill out the following fields:

    1. OpenAPI Version (Mandatory) Note, "3.1" is selected as default. You select the minor version ("3.0" or "3.1"); the generated document declares the corresponding canonical patch release ("3.0.4" for "3.0" and "3.1.2" for "3.1").
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

## Edit an OpenAPI Document

To edit an OpenAPI document:

1. On the top menu of the page, click "BIE".

2. Click "OpenAPI Document" menu item.

3. Use the *Title* or *Description* to find the desired OpenAPI Document. Open its "Edit OpenAPI Document" page by clicking the OpenAPI document title in Title column. See also [How to use the Search field in general](./10-common-functions.md#how-to-use-the-search-field-in-general).

4. You can change the *Title*, *Document Version*, *Terms of Service*, *Contact Name*, *Contact URL*, *Contact Email* , *License Name*, *License URL* and *Description* fields.

5. Click the "Update" button.

## Discard an OpenAPI Document

There are two methods for discarding an OpenAPI Document. The first one is:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the OpenAPI document you want to discard. Use the *Title*, *Description*,*Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired OpenAPI document. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the checkbox right before the desired OpenAPI document.

4. Click the "Discard" button at the top-right of the page.

5. A dialog is open where you can confirm or cancel the request.

The second method is:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the OpenAPI Document you want to discard. Use the *Title*, *Description*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired OpenAPI Document. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the OpenAPI Document title to open its "Edit OpenAPI Document" page.

4. Click the "Discard" button.

5. A dialog is open where you can confirm or cancel the request.

## Add BIEs to OpenAPI Document

To add BIEs to an OpenAPI Document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired OpenAPI Document. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the title to open its "Edit OpenAPI Document" page.

4. Click the "Add" button.

5. On the newly opened "Add BIE For OpenAPI Document" page, locate the desired top-level BIE. Use the *DEN*, *Business Context*, *Branch*, *State*, *Owner*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired BIE. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)).

6. Select the desired BIE node. Select the required *Verb* option and the required *Message Body* (`Request` or `Response`) option from the dropdown list. A `Request` body is not available for a `GET` (a `GET` never carries a request body); it is available for every other verb, including `DELETE` (see [DELETE operations with a request body](#delete-operations-with-a-request-body)).

7. Check the Array Indicator box if needed. When you add the BIE, an *Operation ID* is generated automatically in the form `<verb><BIEName>` (for example `createItemInstance`) using the selected *Verb*; checking the Array Indicator box appends a `List` suffix (for example `createItemInstanceList`). You can change the *Operation ID* later in the *Endpoint Details* panel.

8. Click the "Add" button.

## Configure Security Schemes

The "Edit OpenAPI Document" page provides a *Security Schemes* section between the document information fields and the "Endpoint Details" table. Here you can declare the security schemes that the generated OpenAPI document offers and choose how they apply to the document and to individual operations. When no scheme is configured, a hint reads "No scheme configured — the default OAuth 2.0 scheme will be used." and the generated output keeps the default OAuth 2.0 scheme.

To add a security scheme:

1. Open the desired OpenAPI Document's "Edit OpenAPI Document" page.

2. In the *Security Schemes* section, click the "Add Security Scheme" button.

3. On the returned "Add Security Scheme" dialog, select a *Type* and fill out the type-specific fields:

    1. *Type* (Mandatory). One of *API Key*, *HTTP*, *OAuth 2.0*, or *OpenID Connect*.
    2. *Scheme Name*. The name used to reference this scheme in security requirements.
    3. *Description (optional)*.
    4. For *API Key*: *In* (*Query*, *Header*, or *Cookie*) and *Name*.
    5. For *HTTP*: *Scheme* (*Basic* or *Bearer*); when *Bearer* is selected, an optional *Bearer Format* (for example, JWT) field appears.
    6. For *OpenID Connect*: *OpenID Connect URL*.
    7. For *OAuth 2.0*: an *OAuth Flows* editor. Click the add icon next to *OAuth Flows* to add a flow, choose its *Flow Type*, fill the applicable *Authorization URL*, *Token URL* and optional *Refresh URL*, and add one or more *Scopes* (each with a *Scope* name and *Description*).

4. Click the "Add" button.

Each saved scheme appears as a card in the *Security Schemes* section. Click a card to re-open the "Edit Security Scheme" dialog, or click the minus icon on the card to remove the scheme.

To set the document-level security requirement:

1. In the *Security Schemes* section, click the "Document Security" button. Its label shows a summary of the current requirement (or "None").

2. On the returned "Document Security" dialog, build the requirement:

    1. Within a single requirement, click "Add scheme (AND)" to require more than one scheme together. For *OAuth 2.0* and *OpenID Connect* schemes you can also select the required *Scopes*.
    2. Click "Add Alternative (OR)" to add an alternative requirement.
    3. Check "Allow anonymous (\{\})" to allow access without authentication.

3. Click the "Apply" button.

4. Click the "Update" button on the "Edit OpenAPI Document" page to save the changes.

## Set Operation Security

The "Endpoint Details" table includes a *Security* column that lets you control the security of each operation. When no security scheme is configured, the column shows a dash (—); otherwise it shows *Inherited*, *Public*, or a summary of the operation's own requirement.

To set the security of an operation:

1. In the "Endpoint Details" table, click the *Security* cell of the desired operation.

2. On the returned "Operation Security" dialog, choose one of:

    1. *Use document security* — the operation inherits the document-level requirement.
    2. *No security for this operation* — the operation is public.
    3. *Override with selected schemes* — build a custom requirement (the same "Add scheme (AND)", "Add Alternative (OR)" and "Allow anonymous (\{\})" controls as the [Document Security](#configure-security-schemes) dialog).

3. Click the "Apply" button.

4. Click the "Update" button on the "Edit OpenAPI Document" page to save the changes.

## Configure Error Responses

In addition to its success response, every operation that connectCenter generates is given a default set of `4xx`/`5xx` error responses. By default these error responses are *description only* — a status code and a human-readable description with no response body. You can attach a structured error body to an operation through the *Error Response* column of the *Endpoint Details* table.

The set of default error codes generated for an operation depends on its *Verb*, its *Array Indicator*, and the document's *OpenAPI Version*:

- `401`, `403`, `500`, `502`, `503` and `504` are generated for every operation.
- `400` (Bad Request) is generated for `POST`, `PUT` and `PATCH`, and for a `GET` that returns a collection (*Array Indicator* checked).
- `404` (Not Found) is generated for `PUT`, `PATCH` and `DELETE`, and for a `GET` that returns a single item (*Array Indicator* unchecked).
- `409` (Conflict) is generated for `POST`, `PUT`, `PATCH` and `DELETE`.
- `415` (Unsupported Media Type) and `422` (Unprocessable Content) are generated for the body-bearing verbs `POST`, `PUT` and `PATCH` — and for `DELETE` only when the document is **OpenAPI 3.1**, because a `DELETE` carries a request body only in 3.1 (see [DELETE operations with a request body](#delete-operations-with-a-request-body)).

The *Error Response* column of each operation offers three body types:

1. *No Response Body* (the default) — the generated error responses carry only the status code and a description, with no body. Existing documents keep this behavior until you opt in.
2. *IETF Problem Details* — each generated error response carries an `application/problem+json` body that references a shared RFC 9457 `ProblemDetails` schema (with `status`, `title` and `detail` required, and `type` and `instance` optional) together with an illustrative example for the status.
3. *OAGi Confirm Message* — each generated error response carries an `application/json` body that references a Confirm Message BIE that you pick. When you select this body type a *Pick a Confirm Message…* prompt appears in the cell; click the edit icon to choose the Confirm Message BIE. Once picked, the cell shows a *Selected Confirm Message* link that opens the chosen BIE in a new tab.

To set the Error Response body type of a single operation:

1. Open the desired OpenAPI Document's "Edit OpenAPI Document" page.

2. In the *Endpoint Details* table, open the *Error Response* selector of the desired operation and choose *No Response Body*, *IETF Problem Details* or *OAGi Confirm Message*.

3. For *OAGi Confirm Message*, click the edit icon in the cell, pick the Confirm Message BIE and confirm.

4. Click the "Update" button.

To apply one Error Response body type to every operation at once, use the *Set Error Responses* control above the *Endpoint Details* table:

1. Above the *Endpoint Details* table, choose a body type in the *Set Error Responses* selector.

2. Click the "Apply" button.

    - *No Response Body* and *IETF Problem Details* are applied to every operation.
    - *OAGi Confirm Message* first prompts you to select a *Branch* (one of the releases present in the document) and a Confirm Message BIE, then applies it to that release's operations together with any operation that carries no message body.

3. Click the "Update" button.

A newly added operation inherits the document's prevailing Error Response setting: when every existing operation uses the same body type — all *IETF Problem Details*, or all *OAGi Confirm Message* with a single Confirm Message BIE that is unambiguous for the new operation's release — the new operation adopts it; otherwise the new operation defaults to *No Response Body*.

## Add Operations without a BIE

Some API operations, such as `DELETE` and `PATCH`, do not need to reference a BIE because they carry no message body. You can add such operations directly to an OpenAPI Document.

To add an operation that does not reference a BIE:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*, *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired OpenAPI Document. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the title to open its "Edit OpenAPI Document" page.

4. In the *Endpoint Details* panel, click the "Add Operation" button.

5. On the returned "Add Operation" dialog, fill out the following fields:

    1. *Verb* (Mandatory) Choose `DELETE` or `PATCH`.
    2. *Resource Name (Path)* (Mandatory) The endpoint path, for example `/production-order/{id}`. Path segments wrapped in braces (`{...}`) become path parameters.
    3. *Operation ID* (Mandatory) Auto-generated from the verb and the last non-variable path segment (for example, `PATCH /production-order/{id}` becomes `updateProductionOrder`). You can override it.
    4. *Tag* (Optional)
    5. *Summary* (Optional)

6. Click the "Add" button.

The new operation is listed in the *Endpoint Details* panel with an empty *DEN* cell. Because the operation does not reference a BIE, its *Array Indicator*, *Suppress Root*, and *Message Body* controls are disabled. You can still edit its *Verb*, *Resource Name*, *Operation ID*, and *Tag Name*, and remove it like any other row.

When you generate the OpenAPI YML file, the operation is produced with no request body and a status-only response derived from the verb: `DELETE` produces `202 Accepted` and `PATCH` produces `204 No Content`.

## DELETE operations with a request body

A `DELETE` operation backed by a BIE can carry a *Message Body*:

- `Response` — the generated `DELETE` operation returns the BIE in a `200` response, like the other body-bearing verbs.
- `Request` — the generated `DELETE` operation carries the BIE as a request body. Whether the request body is kept depends on the document's *OpenAPI Version*:

  - In **OpenAPI 3.1**, a request body on `DELETE` is honored: the generated operation includes a `requestBody` paired with a status-only `202` (Accepted) success response.
  - In **OpenAPI 3.0**, the specification does not permit a request body on `DELETE`, so it is dropped during generation: the generated operation has no `requestBody` (and leaves no orphan request schema behind), keeping only the status-only `202` (Accepted) success response.

When a `3.0` document contains a `DELETE` operation with a `Request` message body, an amber warning banner appears above the *Endpoint Details* table:

> *A Request Body on a DELETE operation is ignored in OpenAPI 3.0. Change the OpenAPI Version to 3.1 to include it in the generated document.*

To keep the request body, change the document's *OpenAPI Version* to `3.1` and click the "Update" button. (You can change the *OpenAPI Version* at the top of the "Edit OpenAPI Document" page.)

## View/Edit Endpoint Details of an OpenAPI Document

To view / edit all the operations currently assigned to an OpenAPI document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired OpenAPI Document. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the title to open its "Edit OpenAPI Document" page.

4. In the *Endpoint Details* panel, select the desired row; the *Resource Name*, *Operation ID*, *Tag Name* columns can be modified.

5. Check or uncheck the *Array Indicator* or *Suppress Root* option.

6. Click the "Update" button.

## Operation ID naming and validation

In the *Endpoint Details* panel of an OpenAPI Document, each operation has an *Operation ID*. connectCenter generates it automatically in the form `<verb><BIEName>` and keeps it in sync as you edit the row:

1. The leading verb word comes from the selected *Verb*: `GET` becomes *query*, `POST` becomes *create*, `PUT` becomes *replace*, `PATCH` becomes *update*, and `DELETE` becomes *delete*. For the BIE *Item Instance*, for example, the GET operation is `queryItemInstance` and the POST operation is `createItemInstance`.

2. When you change the *Verb*, the verb word in the *Operation ID* is updated automatically while the BIE-name part is kept (so any name you typed yourself is preserved).

3. When the *Array Indicator* is checked, a `List` suffix is added to the *Operation ID* (for example `queryItemInstanceList`); unchecking it removes the suffix.

4. You can override the generated value at any time by typing directly in the *Operation ID* column.

The *Operation ID* is required and must be unique within the document:

1. If the *Operation ID* is left empty, the cell shows "Operation ID is required." and clicking the "Update" button is blocked; a notification reading "Operation ID is required." also appears.

2. If two operations in the same document have the same *Operation ID*, the affected cells show "Operation ID must be unique within the document."

## Remove BIEs from an OpenAPI Document

To remove BIEs from an OpenAPI Document:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired OpenAPI Document. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the title to open its "Edit OpenAPI Document" page.

4. In the *Endpoint Details* panel, select the desired BIE node and click the "Remove" button.

## OpenAPI YML Expression generation

The end user uses OpenAPI document to represent the selected BIEs into OpenAPI 3.0 or 3.1 syntax. The *OpenAPI Version* chosen on the document determines the syntax of the generated file:

- **3.1** (the default) generates JSON Schema 2020-12 shapes — for example, nullability is expressed with a type union or an `anyOf` null branch rather than the OpenAPI 3.0-only `nullable: true` keyword — and honors a request body on a `DELETE` operation.
- **3.0** generates OpenAPI 3.0 shapes and drops any request body on a `DELETE` operation (see [DELETE operations with a request body](#delete-operations-with-a-request-body)).

You choose the minor version; the generated file's root `openapi` field declares the corresponding canonical patch release — `3.1.2` for **3.1** and `3.0.4` for **3.0** (these are errata-only patch releases of the OpenAPI Specification).

To generate an OpenAPI YML file:

1. On the top menu of the page, click "BIE".

2. Choose "OpenAPI Document" from the drop-down list.

3. Locate the desired OpenAPI Document. Use the *Title*, *Description*,  *Updater*, *Updated start date*, or *Updated end date* search filters to help locate the desired OpenAPI Document. (see [How to use Search Filters](./10-common-functions.md#how-to-use-search-filters)). Click on the title to open its "Edit OpenAPI Document" page.

4. Click the "Generate" button. The document is generated from its saved state, so if there are unsaved changes the action is blocked with the notice "There are unsaved changes. Please click Update before generating the document." In that case, click the "Update" button first and then click "Generate" again.

5. A YML file with the filename format: title-version-timestamp.yml will be saved to the local drive. The generated file includes the configured security schemes under `components.securitySchemes` together with the document-level and per-operation `security` requirements. If no security scheme is configured, the default OAuth 2.0 scheme is used and the output is unchanged.
