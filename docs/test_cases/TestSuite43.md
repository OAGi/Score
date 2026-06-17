# Test Suite 43

**OpenAPI Document Management**

## Test Case 43.1

**Define OpenAPI Document Definition**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #43.1.1
The "OpenAPI Document" menu under BIE menu should open the page titled with "OpenAPI Document".

#### Test Assertion #43.1.2
The end user can create an OpenAPI Document with only required fields including Open API version, Title, and Document Version fields.

#### Test Assertion #43.1.3
The end user cannot create an OpenAPI Document if any required field is not provided.

#### Test Assertion #43.1.4
The end user can search for an OpenAPI Document based only on its title.

#### Test Assertion #43.1.5
The end user can search for an OpenAPI Document based on the description.

#### Test Assertion #43.1.6
The end user can open an OpenAPI Document on the "OpenAPI Document" page to update its details in "Edit OpenAPI Document" page.

#### Test Assertion #43.1.7
The end user can change all fields in "Edit OpenAPI Document" page.

#### Test Assertion #43.1.8
The end user cannot discard an OpenAPI Document in "OpenAPI Document" page if it is already used.

#### Test Assertion #43.1.10
The end user can discard an OpenAPI Document in View/Edit OpenAPI Document page if it is not in any assignments.

### Test Step Pre-condition:
1. An end-user account that can access the BIE menu is available in connectCenter.
2. OpenAPI Document records needed for create, search, edit, discard, and used-document validation scenarios are available or can be created during the test.
3. At least one OpenAPI Document is prepared so that it is used by a dependent OpenAPI-related record or assignment.
4. At least one OpenAPI Document is prepared so that it is not used by any dependent OpenAPI-related record or assignment.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open the `BIE` menu and open the `OpenAPI Document` menu item.
3. Verify that the page title is `OpenAPI Document`. (Assertion [#1](#test-assertion-4311))
4. Start creation of a new OpenAPI Document and create one record by entering only the required fields: OpenAPI Version, Title, and Document Version.
5. Verify that the OpenAPI Document is created successfully and appears in the OpenAPI Document list. (Assertion [#2](#test-assertion-4312))
6. Start creation of another OpenAPI Document and attempt to save it while omitting one or more required fields.
7. Verify that the OpenAPI Document is not created when required fields are missing. (Assertion [#3](#test-assertion-4313))
8. Search for an existing OpenAPI Document by Title only.
9. Verify that the correct OpenAPI Document is returned. (Assertion [#4](#test-assertion-4314))
10. Search for an existing OpenAPI Document by Description.
11. Verify that the correct OpenAPI Document is returned. (Assertion [#5](#test-assertion-4315))
12. Open an existing OpenAPI Document from the list into the Edit OpenAPI Document page.
13. Verify that the Edit OpenAPI Document page shows the current values of the selected OpenAPI Document. (Assertion [#6](#test-assertion-4316))
14. Update the editable fields of the selected OpenAPI Document and save the changes.
15. Reopen or re-search the same OpenAPI Document and verify that the updated values are persisted. (Assertion [#7](#test-assertion-4317))
16. Select an OpenAPI Document that is used in dependent OpenAPI-related records or assignments and attempt to discard it from the OpenAPI Document list page.
17. Verify that the discard is rejected for the used OpenAPI Document. (Assertion [#8](#test-assertion-4318))
18. Open an OpenAPI Document that is not used in any dependent OpenAPI-related records or assignments in the View/Edit OpenAPI Document page and discard it.
19. Verify that the discard succeeds and that the discarded OpenAPI Document no longer appears in the OpenAPI Document list. (Assertion [#10](#test-assertion-43110))

## Test Case 43.2

**Add BIE To OpenAPI Document**

Pre-condition: There are at least three BIEs created by end user and developer, and they are in different states such as WIP, QA, and Production. There are at least five OpenAPI-document-related assignments or candidate records prepared for searching and assignment scenarios.

### Test Assertion:

#### Test Assertion #43.2.1
The "Add BIE For OpenAPI Document" menu should open the page titled with "Add BIE For OpenAPI Document".

#### Test Assertion #43.2.2
On Add BIE For OpenAPI Document page, the end user can view BIEs available to be assigned and select BIE rows for assignment.

#### Test Assertion #43.2.3
On Add BIE For OpenAPI Document page, the end user can search BIEs by DEN, Business Context, Version, Remark, Updated Start Date, and Updated End Date. Branch, State, Owner, and Updater controls may also be available depending on the environment.

#### Test Assertion #43.2.4
On Add BIE For OpenAPI Document page, when `GET` or `DELETE` is selected as Verb, `Request` is not available as Message Body, and the Add action remains unavailable until both Verb and Message Body are specified for every selected BIE.

#### Test Assertion #43.2.5
On Add BIE For OpenAPI Document page, the end user can add a selected BIE to the current OpenAPI Document when a valid Verb and Message Body combination is specified.

#### Test Assertion #43.2.6
On Edit OpenAPI Document page, the end user can view the BIE assignments currently attached to that OpenAPI Document.

#### Test Assertion #43.2.7
On Edit OpenAPI Document page, the end user can remove selected BIE assignments from the current OpenAPI Document.

#### Test Assertion #43.2.8
The same BIE can be assigned again to the same OpenAPI Document when it uses a different valid operation combination.

#### Test Assertion #43.2.9
The same BIE can be assigned again to the same OpenAPI Document when the generated operation identifier differs, including cases where the UI creates a distinct generated operation combination for the same Verb and Message Body.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: candidate BIEs created by end users and developers are available across relevant states for assignment and search scenarios.
2. At least five candidate BIE records are prepared for assignment, filtering, and duplicate-operation scenarios.
3. At least one OpenAPI Document exists and can be opened for assignment management.
4. Test data exists to validate duplicate and non-duplicate combinations of BIE, Verb, and Message Body.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open an existing OpenAPI Document and open the `Add BIE For OpenAPI Document` page or dialog.
3. Verify that the page or dialog title is `Add BIE For OpenAPI Document`. (Assertion [#1](#test-assertion-4321))
4. Review the BIE list available for assignment.
5. Verify that assignable BIE rows are listed and can be selected for assignment. (Assertion [#2](#test-assertion-4322))
6. Search the candidate BIEs by DEN, Business Context, Version, Remark, Updated Start Date, and Updated End Date.
7. Verify that each search filter returns the expected BIE results. If Branch, State, Owner, or Updater filters are available in the environment, verify those as additional checks. (Assertion [#3](#test-assertion-4323))
8. Select a BIE row, choose a Verb, and inspect the Message Body choices.
9. Verify that `Request` is not available when the Verb is `GET` or `DELETE`, and verify that the Add action is unavailable until all selected BIE rows have both Verb and Message Body values. (Assertion [#4](#test-assertion-4324))
10. Select a BIE row with a valid Verb and Message Body combination and add it to the current OpenAPI Document.
11. Verify that the add operation succeeds. (Assertion [#5](#test-assertion-4325))
12. Return to the Edit OpenAPI Document page and review the BIE list.
13. Verify that the added BIE assignment appears in the current OpenAPI Document. (Assertion [#6](#test-assertion-4326))
14. Select one or more assigned BIE rows on the Edit OpenAPI Document page and remove them.
15. Verify that the selected BIE assignments are removed from the current OpenAPI Document. (Assertion [#7](#test-assertion-4327))
16. Reopen the `Add BIE For OpenAPI Document` page or dialog and add the same BIE again using a different valid Verb and Message Body combination.
17. Verify that the additional assignment succeeds when it uses a different valid operation combination. (Assertion [#8](#test-assertion-4328))
18. Reopen the `Add BIE For OpenAPI Document` page or dialog and attempt to add the same BIE again using the same Verb and Message Body combination that is already assigned.
19. Verify that the assignment still succeeds when the current UI generates a distinct operation identifier for that additional assignment. (Assertion [#9](#test-assertion-4329))

## Test Case 43.3

**Discard Button Placement on the Edit OpenAPI Document Page**

Pre-condition: At least one OpenAPI Document that is not used by any assignment exists and can be opened in the Edit OpenAPI Document page by the signed-in end user.

### Test Assertion:

#### Test Assertion #43.3.1
On the Edit OpenAPI Document page, the `Discard` button appears at the left of the top toolbar immediately after the page title, while the `Update` button appears at the far right of the same toolbar, so the two buttons are visually separated and not adjacent.

#### Test Assertion #43.3.2
On the Edit OpenAPI Document page, the `Discard` button keeps its warning (red) styling and the `Update` button keeps its primary styling, and the `Update` button is disabled when there are no unsaved changes (no field edits and no operation-row edits).

#### Test Assertion #43.3.3
On the Edit OpenAPI Document page, clicking the relocated `Discard` button does not immediately delete the document; it opens a confirmation dialog titled `Discard OpenAPI Doc?` with the message that the OpenAPI Doc will be permanently removed, a `Discard` action button, and a `Cancel` button.

#### Test Assertion #43.3.4
On the Edit OpenAPI Document page, choosing `Cancel` in the confirmation dialog (or closing the dialog) leaves the OpenAPI Document intact and keeps the end user on the Edit OpenAPI Document page with no change to the document.

#### Test Assertion #43.3.5
On the Edit OpenAPI Document page, confirming with the `Discard` action of the relocated button on a document that is not used in any assignment removes the document, shows the `Discarded` confirmation, and returns to the OpenAPI Document list where the document no longer appears.

#### Test Assertion #43.3.6
On the Edit OpenAPI Document page, confirming the discard with the relocated button on a document that is used by an assignment is rejected and the end user is informed with the message `Discard's forbidden! The OpenAPI Doc is used.`, and the document remains in the OpenAPI Document list.

### Test Step Pre-condition:
1. An end-user account that can access the BIE menu and open the OpenAPI Document feature is available in connectCenter.
2. At least one OpenAPI Document that is not used in any assignment is available so it can be discarded from the Edit OpenAPI Document page.
3. At least one OpenAPI Document that is used by a dependent assignment (for example, a BIE/operation is assigned to it) is available to verify that discard is rejected.
4. The selected OpenAPI Document can be opened from the OpenAPI Document list into the Edit OpenAPI Document page.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open the `BIE` menu, open the `OpenAPI Document` menu item, and open an unused OpenAPI Document from the list into the Edit OpenAPI Document page.
3. On the Edit OpenAPI Document page, observe the top toolbar and confirm that the `Discard` button is positioned immediately after the page title at the left end of the toolbar and that the `Update` button is positioned at the far right end of the toolbar, with the two buttons clearly separated and not adjacent. (Assertion [#1](#test-assertion-4331))
4. Without making any changes, observe the styling and state of the toolbar buttons, and confirm that the `Discard` button uses warning (red) styling, that the `Update` button uses primary styling, and that the `Update` button is disabled while no field edits or operation-row edits have been made. (Assertion [#2](#test-assertion-4332))
5. Click the `Discard` button on the Edit OpenAPI Document page and confirm that the document is not deleted immediately and that a confirmation dialog titled `Discard OpenAPI Doc?` opens, stating that the OpenAPI Doc will be permanently removed and offering a `Discard` action button and a `Cancel` button. (Assertion [#3](#test-assertion-4333))
6. Choose `Cancel` in the confirmation dialog (or close the dialog) and confirm that the OpenAPI Document is left intact, that the end user remains on the Edit OpenAPI Document page, and that no change is made to the document. (Assertion [#4](#test-assertion-4334))
7. With an unused OpenAPI Document open in the Edit OpenAPI Document page, click the `Discard` button again, then confirm the action by choosing `Discard` in the dialog, and verify that the `Discarded` confirmation is shown, that the page returns to the OpenAPI Document list, and that the discarded document no longer appears in the list. (Assertion [#5](#test-assertion-4335))
8. Open an OpenAPI Document that is used by a dependent assignment into the Edit OpenAPI Document page, click the `Discard` button, and confirm the action by choosing `Discard` in the dialog. Verify that the discard is rejected, that the message `Discard's forbidden! The OpenAPI Doc is used.` is shown, and that the document remains in the OpenAPI Document list. (Assertion [#6](#test-assertion-4336))

## Test Case 43.4

**Add an Operation Without a BIE to an OpenAPI Document**

Pre-condition: At least one OpenAPI Document exists and can be opened by the end user in the "Edit OpenAPI Document" page, and the document is in an editable (not discarded/read-only) state.

### Test Assertion:

#### Test Assertion #43.4.1
On the Edit OpenAPI Document page, the section that lists a document's operations is titled "Endpoint Details" and exposes both an "Add BIE" button and a separate "Add Operation" button.

#### Test Assertion #43.4.2
Clicking "Add Operation" opens a dialog titled "Add Operation" whose subtitle reads "Define an API operation (endpoint) that does not reference a BIE." and which shows the fields Verb, Resource Name (Path), Operation ID, Tag (optional), and Summary (optional).

#### Test Assertion #43.4.3
In the Add Operation dialog, the Verb field offers only DELETE and PATCH as choices (no GET, PUT, or POST), and Verb defaults to DELETE.

#### Test Assertion #43.4.4
In the Add Operation dialog, after a Verb and a Resource Name (Path) are entered, the Operation ID field is auto-populated as the verb action plus the capitalized last non-variable path segment (for example, PATCH "/production-order/{id}" yields "updateProductionOrder", and DELETE "/item/{id}" yields "deleteItem"), and the hint "Auto-generated from the verb and path; you can override it." is shown.

#### Test Assertion #43.4.5
In the Add Operation dialog, the end user can override the auto-generated Operation ID by typing into it, and once edited the value stops being overwritten when Verb or Resource Name (Path) is subsequently changed.

#### Test Assertion #43.4.6
In the Add Operation dialog, the "Add" button stays disabled until Verb, Resource Name (Path), and Operation ID all have values, and "Cancel" closes the dialog without adding anything.

#### Test Assertion #43.4.7
Submitting a valid Add Operation dialog closes it, shows the snackbar "Operation added", and the new operation appears as a row in the Endpoint Details table of the same OpenAPI Document.

#### Test Assertion #43.4.8
A bodyless (no-BIE) operation row renders an empty DEN cell (no BIE name or GUID link) while showing its Verb, Resource Name, Operation ID, and Tag Name values.

#### Test Assertion #43.4.9
For a bodyless operation row, the Array Indicator checkbox, the Suppress Root checkbox, and the Message Body selector are disabled (not editable), unlike rows backed by a BIE.

#### Test Assertion #43.4.10
The end user can edit the bodyless operation row's Resource Name and Operation ID inline; clearing the Operation ID surfaces the inline error "Operation ID is required." and entering an Operation ID already used in the document surfaces "Operation ID must be unique within the document."

#### Test Assertion #43.4.11
After editing a bodyless operation row and saving via the Update action, the snackbar "Updated" is shown and the edited values persist when the OpenAPI Document is reopened.

#### Test Assertion #43.4.12
Selecting a bodyless operation row and clicking "Remove" opens a confirmation dialog (header "Remove selected BIE from the OpenAPI Doc?", action "Remove"); confirming removes the row, shows the snackbar "Removed", and the operation no longer appears after the document is reopened.

### Test Step Pre-condition:
1. An end-user account that can access the BIE menu and edit OpenAPI Documents is available in connectCenter.
2. At least one OpenAPI Document exists and can be opened into the Edit OpenAPI Document page in an editable state.
3. A path/resource value containing a variable segment (for example, "/production-order/{id}") is available to enter so the auto-derived Operation ID can be observed.
4. At least one operation (BIE-backed or bodyless) already exists in the target document, or can be added during the test, so the Operation-ID uniqueness error can be triggered against an existing Operation ID.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open an existing, editable OpenAPI Document into the Edit OpenAPI Document page and locate the operations section.
3. Verify that the operations section is titled `Endpoint Details` and that it exposes both an `Add BIE` button and a separate `Add Operation` button. (Assertion [#1](#test-assertion-4341))
4. Click `Add Operation` to open the dialog.
5. Verify that the dialog is titled `Add Operation`, its subtitle reads `Define an API operation (endpoint) that does not reference a BIE.`, and it shows the fields Verb, Resource Name (Path), Operation ID, Tag (optional), and Summary (optional). (Assertion [#2](#test-assertion-4342))
6. Inspect the Verb field choices and its default value.
7. Verify that the Verb field offers only `DELETE` and `PATCH` (no `GET`, `PUT`, or `POST`) and that it defaults to `DELETE`. (Assertion [#3](#test-assertion-4343))
8. Enter a Resource Name (Path) containing a variable segment, such as `/production-order/{id}`, with Verb `PATCH`, and observe the Operation ID field; then change the Verb to `DELETE` with a path such as `/item/{id}`.
9. Verify that the Operation ID is auto-populated from the verb action plus the capitalized last non-variable path segment (for example `updateProductionOrder` and `deleteItem`) and that the hint `Auto-generated from the verb and path; you can override it.` is shown. (Assertion [#4](#test-assertion-4344))
10. Type a custom value into the Operation ID field, then change the Verb and the Resource Name (Path) again.
11. Verify that the overridden Operation ID is retained and is no longer overwritten when Verb or Resource Name (Path) is changed. (Assertion [#5](#test-assertion-4345))
12. Clear one or more of the Verb, Resource Name (Path), and Operation ID fields and observe the `Add` button; then click `Cancel`.
13. Verify that the `Add` button stays disabled until Verb, Resource Name (Path), and Operation ID all have values, and that `Cancel` closes the dialog without adding anything. (Assertion [#6](#test-assertion-4346))
14. Reopen the Add Operation dialog, provide a valid Verb, Resource Name (Path), and Operation ID, and click `Add`.
15. Verify that the dialog closes, the snackbar `Operation added` is shown, and the new operation appears as a row in the Endpoint Details table of the same OpenAPI Document. (Assertion [#7](#test-assertion-4347))
16. Inspect the newly added bodyless operation row in the Endpoint Details table.
17. Verify that the DEN cell is empty (no BIE name or GUID link) while the row shows its Verb, Resource Name, Operation ID, and Tag Name values. (Assertion [#8](#test-assertion-4348))
18. On the same bodyless operation row, inspect the Array Indicator checkbox, the Suppress Root checkbox, and the Message Body selector, and compare them with a BIE-backed row.
19. Verify that the Array Indicator checkbox, the Suppress Root checkbox, and the Message Body selector are disabled (not editable) for the bodyless row, unlike rows backed by a BIE. (Assertion [#9](#test-assertion-4349))
20. Edit the bodyless operation row inline: change the Resource Name and Operation ID, then clear the Operation ID, and then enter an Operation ID that is already used by another operation in the document.
21. Verify that the Resource Name and Operation ID can be edited inline, that clearing the Operation ID surfaces the inline error `Operation ID is required.`, and that a duplicate Operation ID surfaces `Operation ID must be unique within the document.` (Assertion [#10](#test-assertion-43410))
22. Restore valid inline values, save the changes using the Update action, then reopen the OpenAPI Document.
23. Verify that the snackbar `Updated` is shown and that the edited values persist when the OpenAPI Document is reopened. (Assertion [#11](#test-assertion-43411))
24. Select the bodyless operation row, click `Remove`, confirm the confirmation dialog (header `Remove selected BIE from the OpenAPI Doc?`, action `Remove`), and then reopen the OpenAPI Document.
25. Verify that confirming removes the row, the snackbar `Removed` is shown, and the operation no longer appears after the document is reopened. (Assertion [#12](#test-assertion-43412))

## Test Case 43.5

**Generate OpenAPI With Bodyless Operations**

Pre-condition: An OpenAPI Document already exists and the signed-in end user can open it for editing (not in a disabled or read-only state), and the user is able to add operations and BIE assignments to it.

### Test Assertion:

#### Test Assertion #43.5.1
On the Edit OpenAPI Document page, the operations table is titled `Endpoint Details` and exposes both an `Add BIE` button and an `Add Operation` button above it.

#### Test Assertion #43.5.2
Clicking `Add Operation` opens a dialog titled `Add Operation` whose Verb selector offers only `DELETE` and `PATCH`, and whose Operation ID field is auto-filled from the verb plus the last non-variable path segment of the Resource Name (Path); for example, `PATCH /production-order/{id}` yields `updateProductionOrder` and `DELETE /production-order/{id}` yields `deleteProductionOrder`.

#### Test Assertion #43.5.3
After adding a bodyless operation, it appears as a new row in the `Endpoint Details` table with an empty DEN cell and with the Array Indicator, Suppress Root, and Message Body controls disabled, while its Verb, Resource Name, Operation ID, and Tag Name remain visible and editable.

#### Test Assertion #43.5.4
For a document containing a bodyless DELETE operation, clicking `Generate` downloads a YAML OpenAPI document whose path and method entry for that operation contains no requestBody key and declares a status-only response under `202` with description `Accepted`.

#### Test Assertion #43.5.5
For a document containing a bodyless PATCH operation, the generated and downloaded OpenAPI document's path and method entry for that operation contains no requestBody key and declares a status-only response under `204` with description `No Content`.

#### Test Assertion #43.5.6
In the generated OpenAPI document, the operationId emitted under each bodyless operation's method matches the Operation ID value shown in that row, including any manual override entered before generation.

#### Test Assertion #43.5.7
For a bodyless operation whose Resource Name (Path) contains a variable segment in braces such as `{id}`, the generated OpenAPI document emits that path with the brace segment as a path parameter and omits any request body for that operation.

#### Test Assertion #43.5.8
A document that mixes at least one BIE-backed operation added via `Add BIE` and at least one bodyless operation added via `Add Operation` generates successfully with no error message: the download is produced, and the resulting OpenAPI document contains both the BIE-backed operation, whose request and response body references a component schema, and the bodyless operation, which has no requestBody.

### Test Step Pre-condition:
1. An end-user account that can access the BIE menu and open an existing OpenAPI Document for editing is available in connectCenter.
2. At least one OpenAPI Document exists that the end user can open on the Edit OpenAPI Document page in an editable (not disabled) state.
3. At least one assignable BIE in a usable state, such as Production or owned/WIP for the user, is available so a BIE-backed operation can be added to the same document for the mixed-document scenario.
4. The OpenAPI Document is editable so the `Generate` button is enabled, noting that `Generate` is disabled when the document is read-only or the `Endpoint Details` table is empty.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open an existing OpenAPI Document into the Edit OpenAPI Document page.
3. Verify that the operations table is titled `Endpoint Details` and that both an `Add BIE` button and an `Add Operation` button appear above it. (Assertion [#1](#test-assertion-4351))
4. Click `Add Operation`, inspect the dialog title and Verb selector, enter a Resource Name (Path) such as `/production-order/{id}`, and observe the auto-filled Operation ID for both `PATCH` and `DELETE`.
5. Verify that the dialog is titled `Add Operation`, that the Verb selector offers only `DELETE` and `PATCH`, and that the Operation ID is auto-filled from the verb and the last non-variable path segment (for example `updateProductionOrder` for `PATCH /production-order/{id}` and `deleteProductionOrder` for `DELETE /production-order/{id}`). (Assertion [#2](#test-assertion-4352))
6. Add a bodyless DELETE operation and a bodyless PATCH operation to the document, then review the resulting rows in the `Endpoint Details` table.
7. Verify that each bodyless operation appears as a new row with an empty DEN cell and with the Array Indicator, Suppress Root, and Message Body controls disabled, while its Verb, Resource Name, Operation ID, and Tag Name remain visible and editable. (Assertion [#3](#test-assertion-4353))
8. Click `Generate` and open the downloaded YAML OpenAPI document in a text editor, locating the bodyless DELETE operation's path and method entry.
9. Verify that the bodyless DELETE operation's entry contains no requestBody key and declares a status-only response under `202` with description `Accepted`. (Assertion [#4](#test-assertion-4354))
10. In the same downloaded OpenAPI document, locate the bodyless PATCH operation's path and method entry.
11. Verify that the bodyless PATCH operation's entry contains no requestBody key and declares a status-only response under `204` with description `No Content`. (Assertion [#5](#test-assertion-4355))
12. Before generating, override the Operation ID of one bodyless row with a custom value, then click `Generate` and inspect the operationId emitted under each bodyless operation's method.
13. Verify that the emitted operationId matches the Operation ID value shown in each row, including the manual override. (Assertion [#6](#test-assertion-4356))
14. In the downloaded OpenAPI document, locate the path emitted for a bodyless operation whose Resource Name (Path) contains a variable segment in braces such as `{id}`.
15. Verify that the path is emitted with the brace segment as a path parameter and that no request body is present for that operation. (Assertion [#7](#test-assertion-4357))
16. Open the `Add BIE For OpenAPI Document` page or dialog, add at least one BIE-backed operation to the same document so it contains both a BIE-backed operation and a bodyless operation, then click `Generate`.
17. Verify that generation succeeds with no error message, that the download is produced, and that the resulting OpenAPI document contains both the BIE-backed operation referencing a component schema for its request and response body and the bodyless operation with no requestBody. (Assertion [#8](#test-assertion-4358))

## Test Case 43.6

**Operation Identifier Naming**

Pre-condition: An OpenAPI Document exists, and at least one end-user BIE in an assignable state is available so the user can add it to the document; at least two BIE assignments can be created within one OpenAPI Document so a duplicate Operation ID can be produced.

### Test Assertion:

#### Test Assertion #43.6.1
When a BIE is added to an OpenAPI Document with a chosen Verb, the resulting assignment's Operation ID is the verb word followed by the BIE property term (for example, `POST` on a BIE named Purchase Order yields `createPurchaseOrder`), where the verb words are `query` for `GET`, `create` for `POST`, `replace` for `PUT`, `update` for `PATCH`, and `delete` for `DELETE`.

#### Test Assertion #43.6.2
The generated Operation ID contains no business-context prefix, no underscore, and no other separator character; it is a single concatenated camel-case token of the verb word plus the BIE name.

#### Test Assertion #43.6.3
When the added BIE assignment has its Array Indicator checked at add time, the generated Operation ID ends with the suffix `List` (for example, `createPurchaseOrderList`).

#### Test Assertion #43.6.4
On the Edit OpenAPI Document page, changing the Verb of an existing BIE assignment live-updates the Operation ID by swapping only the leading verb word while preserving the BIE-name segment.

#### Test Assertion #43.6.5
On the Edit OpenAPI Document page, toggling the Array Indicator of an existing BIE assignment adds or removes the trailing `List` suffix on the Operation ID accordingly.

#### Test Assertion #43.6.6
On the Edit OpenAPI Document page, the Operation ID column is a free-text input that the end user can edit to any value.

#### Test Assertion #43.6.7
When two BIE assignments in the same OpenAPI Document have identical Operation ID values, an inline error `Operation ID must be unique within the document.` is shown on the affected Operation ID fields.

#### Test Assertion #43.6.8
When an Operation ID is cleared (left blank), an inline error `Operation ID is required.` is shown on that Operation ID field.

#### Test Assertion #43.6.9
Attempting to save the document with a blank Operation ID is blocked and the message `Operation ID is required.` is shown, so the blank value is not persisted.

#### Test Assertion #43.6.10
After the end user edits an Operation ID to a custom value and saves, that exact Operation ID value is persisted and appears verbatim in the file produced by the Generate action.

### Test Step Pre-condition:
1. An end-user account that can access the BIE menu and the OpenAPI Document feature is available in connectCenter.
2. At least one OpenAPI Document exists and can be opened on the Edit OpenAPI Document page.
3. At least one end-user BIE in an assignable state is available, with a known property term (BIE name), so the expected Operation ID can be predicted (for example, a BIE whose property term is `Purchase Order`).
4. It is possible to create at least two BIE assignments within one OpenAPI Document whose verb word plus BIE name collide, so a duplicate Operation ID can be produced (for example, the same BIE added twice with the same Verb, or two BIEs sharing a property term).
5. The two colliding assignments are kept on the same loaded page (the assignment list is paginated at page size 10), and the environment allows downloading the file produced by Generate and inspecting its contents.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open an existing OpenAPI Document, then open the `Add BIE For OpenAPI Document` page or dialog and select an assignable BIE whose property term is known (for example `Purchase Order`).
3. Choose the Verb `POST`, choose a valid Message Body, leave the Array Indicator unchecked, and add the BIE to the current OpenAPI Document.
4. Return to the Edit OpenAPI Document page and inspect the Operation ID of the new assignment, then verify that the Operation ID is the verb word `create` followed by the BIE name (for example `createPurchaseOrder`), and that the same mapping holds when the BIE is added with `GET` (`query`), `PUT` (`replace`), `PATCH` (`update`), or `DELETE` (`delete`). (Assertion [#1](#test-assertion-4361))
5. Verify that the generated Operation ID has no business-context prefix, no underscore, and no other separator character, appearing as a single concatenated camel-case token of the verb word plus the BIE name. (Assertion [#2](#test-assertion-4362))
6. Open the `Add BIE For OpenAPI Document` page or dialog again, select the BIE, choose a Verb (for example `POST`), check the Array Indicator, and add it; then verify that the generated Operation ID ends with the suffix `List` (for example `createPurchaseOrderList`). (Assertion [#3](#test-assertion-4363))
7. On the Edit OpenAPI Document page, change the Verb of an existing BIE assignment (for example from `POST` to `GET`), and verify that the Operation ID live-updates by swapping only the leading verb word while preserving the BIE-name segment (for example `createPurchaseOrder` becomes `queryPurchaseOrder`). (Assertion [#4](#test-assertion-4364))
8. On the Edit OpenAPI Document page, toggle the Array Indicator of an existing BIE assignment on and then off, and verify that the trailing `List` suffix is added when checked and removed when unchecked. (Assertion [#5](#test-assertion-4365))
9. Click in the Operation ID field of an assignment and type a custom value, and verify that the Operation ID column is a free-text input that accepts the typed value. (Assertion [#6](#test-assertion-4366))
10. Arrange two BIE assignments on the same loaded page so their Operation ID values are identical (for example by editing one Operation ID to match another, or by adding the same BIE twice with the same Verb), and verify that the inline error `Operation ID must be unique within the document.` is shown on the affected Operation ID fields. (Assertion [#7](#test-assertion-4367))
11. Clear the Operation ID of one assignment so it is blank, and verify that the inline error `Operation ID is required.` is shown on that Operation ID field. (Assertion [#8](#test-assertion-4368))
12. With one Operation ID still blank, attempt to save the document, and verify that the save is blocked, the message `Operation ID is required.` is shown, and the blank value is not persisted. (Assertion [#9](#test-assertion-4369))
13. Edit an Operation ID to a custom value, save the document successfully, then trigger the Generate action and open the downloaded file, and verify that the custom Operation ID value is persisted and appears verbatim in the generated file. (Assertion [#10](#test-assertion-43610))

## Test Case 43.7

**Avoid Duplicate BIE Schema in the Generated OpenAPI Document**

Pre-condition: An end-user account that can sign in and open the BIE > OpenAPI Document menu exists, and at least one top-level BIE with a known BIE/property-term name is available to assign to an OpenAPI Document.

### Test Assertion:

#### Test Assertion #43.7.1
On the Endpoint Details section of an OpenAPI Document, the same BIE can be added twice so that one assigned operation has its Array Indicator checkbox checked, producing an Operation ID that carries a trailing "List", and the other operation has Array Indicator unchecked as a single operation.

#### Test Assertion #43.7.2
When both operations have the same Suppress Root setting, clicking Generate downloads a single YAML (.yml) OpenAPI document and the generation succeeds with no error snackbar.

#### Test Assertion #43.7.3
In the downloaded YAML, the components/schemas section contains exactly one schema named for that BIE ("<BIEName>") for the shared inner object, and contains no orphan "<BIEName>ListEntry" schema.

#### Test Assertion #43.7.4
In the downloaded YAML, the array (list) operation uses the array schema "<BIEName>List" whose items reference "#/components/schemas/<BIEName>", and the non-array (single) operation references that same "#/components/schemas/<BIEName>" schema, so both operations share one BIE schema.

#### Test Assertion #43.7.5
When the same BIE backs an array operation and a non-array operation but the two operations have different Suppress Root settings, clicking Generate produces a distinct "<BIEName>ListEntry" schema for the non-array operation, so the schemas are not over-merged.

### Test Step Pre-condition:
1. An end-user account that can access the BIE menu and open the OpenAPI Document menu is available in connectCenter.
2. At least one OpenAPI Document exists, or can be created, and can be opened in the Edit OpenAPI Document page showing the Endpoint Details table.
3. At least one top-level BIE with a known name is available so it can be assigned through the Add BIE For OpenAPI Document dialog.
4. The chosen BIE is added to the document twice so that it backs one operation with Array Indicator checked and one operation with Array Indicator unchecked; for the over-merge check, a second scenario is prepared in which the same BIE backs an array operation and a non-array operation that differ in their Suppress Root setting.
5. Every assigned operation has a valid Verb, a valid Message Body, and a non-blank unique Operation ID so the Generate button is enabled.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open an existing OpenAPI Document into the Edit OpenAPI Document page and locate the Endpoint Details section.
3. Open the `Add BIE For OpenAPI Document` dialog and add the chosen BIE so that it appears as an operation in the Endpoint Details table, then add the same BIE a second time so that it appears as a second operation.
4. For one of the two operations, check the Array Indicator checkbox and leave it unchecked for the other, and confirm that the array operation's Operation ID carries a trailing `List` while the single operation's Operation ID does not.
5. Verify that the same BIE backs two operations, one with Array Indicator checked and one with Array Indicator unchecked. (Assertion [#1](#test-assertion-4371))
6. Set both operations to the same Suppress Root setting, ensure each operation has a valid Verb, Message Body, and a non-blank unique Operation ID, then click `Generate`.
7. Verify that a single YAML file with the `.yml` extension is downloaded and that no error snackbar appears. (Assertion [#2](#test-assertion-4372))
8. Open the downloaded YAML file in a text or YAML viewer and inspect the components/schemas section.
9. Verify that components/schemas contains exactly one schema named `<BIEName>` for the shared inner object and contains no `<BIEName>ListEntry` schema. (Assertion [#3](#test-assertion-4373))
10. In the same downloaded YAML, inspect the request and response schemas referenced by each of the two operations.
11. Verify that the array operation uses the array schema `<BIEName>List` whose items reference `#/components/schemas/<BIEName>`, and that the non-array operation references the same `#/components/schemas/<BIEName>` schema, so both operations share one BIE schema. (Assertion [#4](#test-assertion-4374))
12. Prepare the second scenario in which the same BIE backs an array operation and a non-array operation that differ in their Suppress Root setting, then click `Generate` and open the downloaded YAML file.
13. Verify that the downloaded YAML contains a distinct `<BIEName>ListEntry` schema for the non-array operation, confirming that the schemas are not over-merged when the Suppress Root settings differ. (Assertion [#5](#test-assertion-4375))

## Test Case 43.8

**Configure OpenAPI Security Schemes**

Pre-condition: An end-user account that can access the BIE menu exists, and at least one OpenAPI Document already has at least two BIE operations assigned and uses OpenAPI Version 3.0.3, so that Generate is enabled and a per-operation Security cell is editable.

### Test Assertion:

#### Test Assertion #43.8.1
On the Edit OpenAPI Document page, a `Security Schemes` section appears with an `Add Security Scheme` button and a `Document Security: None` button, and while no scheme is configured it shows the hint `No scheme configured — the default OAuth 2.0 scheme will be used.`.

#### Test Assertion #43.8.2
Clicking `Add Security Scheme` opens a dialog titled `Add Security Scheme` with a Type selector offering `API Key`, `HTTP`, `OAuth 2.0`, and `OpenID Connect`, plus a `Scheme Name` field and a `Description (optional)` field.

#### Test Assertion #43.8.3
Selecting Type `API Key` reveals an `In` selector with `Query`, `Header`, and `Cookie` choices and a `Name` field, defaults the `Scheme Name` to `ApiKeyAuth`, and seeds example values (In `Header`, Name `X-API-Key`) so the `Add` button is enabled; clearing the `Name` disables `Add`, and refilling it re-enables `Add`.

#### Test Assertion #43.8.4
Selecting Type `HTTP` with Scheme `Bearer` reveals an optional `Bearer Format` field and defaults the `Scheme Name` to `BearerAuth`, while selecting Scheme `Basic` hides the `Bearer Format` field and defaults the `Scheme Name` to `BasicAuth`.

#### Test Assertion #43.8.5
Selecting Type `OAuth 2.0` reveals an `OAuth Flows` editor seeded with an `Authorization Code` flow (with `Authorization URL` and `Token URL`) and read/write/admin scopes, where additional flows and scopes can be added and removed, and defaults the `Scheme Name` to `OAuth2`.

#### Test Assertion #43.8.6
Selecting Type `OpenID Connect` reveals an `OpenID Connect URL` field (seeded with an example issuer URL) and defaults the `Scheme Name` to `OpenID`; clearing the `OpenID Connect URL` disables the `Add` button, and providing a URL re-enables it.

#### Test Assertion #43.8.7
After adding a scheme and confirming, a card showing the scheme's name, type label, and a summary appears in the `Security Schemes` section, clicking the card reopens it in an `Edit Security Scheme` dialog with the saved values, and its remove icon deletes the card.

#### Test Assertion #43.8.8
The dialog's `Add`/`Save` button stays disabled when the `Scheme Name` is blank or duplicates another scheme's name, or when a type's required fields are missing (API Key In and Name, HTTP Scheme, OpenID Connect URL, or at least one valid OAuth 2.0 flow).

#### Test Assertion #43.8.9
Clicking `Document Security` opens a dialog titled `Document Security` where the end user builds a security requirement by selecting schemes joined by `AND`, adding alternatives with `Add Alternative (OR)`, or marking a requirement `Allow anonymous ({})`, and the button label then updates to `Document Security: <summary>`.

#### Test Assertion #43.8.10
The `Document Security` dialog shows a duplicate warning `Duplicate of Requirement N — change or remove it.` and disables `Apply` when two OR alternatives are identical.

#### Test Assertion #43.8.11
In the Endpoint Details table, an operation's Security cell opens an `Operation Security` dialog offering `Use document security`, `No security for this operation`, and `Override with selected schemes`, where choosing override and selecting schemes changes the cell summary from `Inherited` to the chosen requirement, while `No security for this operation` shows `Public`.

#### Test Assertion #43.8.12
After saving with Update, clicking `Generate` downloads a YAML file whose `components.securitySchemes` contains each configured named scheme with its type-specific fields (apiKey type/in/name; http type/scheme and optional bearerFormat; oauth2 type/flows/scopes; openIdConnect type/openIdConnectUrl).

#### Test Assertion #43.8.13
In the generated YAML, the document-level security requirement is emitted as a root-level `security` entry placed right after `info`, a per-operation override is emitted as a `security` entry under that operation, and an operation set to `Use document security` emits no per-operation security key.

#### Test Assertion #43.8.14
When the document has zero configured security schemes, the generated YAML falls back to the legacy single OAuth2 scheme in `components.securitySchemes` with per-operation OAuth2 scopes and no root-level `security` entry.

#### Test Assertion #43.8.15
After setting a per-operation security override and Update, performing a later Update that changes only an unrelated field such as the document Description leaves that operation's override intact, so a subsequent Generate still emits the operation's configured security rather than turning it Public.

#### Test Assertion #43.8.16
A single `Updated` confirmation message is shown after Update completes, and the `Update` button is disabled while any security scheme or requirement is incomplete.

### Test Step Pre-condition:
1. An end-user account that can access the BIE menu is available in connectCenter.
2. At least one OpenAPI Document exists with OpenAPI Version 3.0.3 and at least two BIE operations assigned, so that Generate is enabled and the Security cell is editable for operations.
3. The end user can open that OpenAPI Document into the Edit OpenAPI Document page.
4. The environment allows downloading the generated OpenAPI document file (YAML) for inspection.
5. At least one operation is available to receive a per-operation security override for the regression check.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open the prepared OpenAPI Document into the Edit OpenAPI Document page and locate the `Security Schemes` section.
3. Verify that the `Security Schemes` section shows the `Add Security Scheme` button, the `Document Security: None` button, and the hint `No scheme configured — the default OAuth 2.0 scheme will be used.` while no scheme is configured. (Assertion [#1](#test-assertion-4381))
4. Click `Add Security Scheme`.
5. Verify that a dialog titled `Add Security Scheme` opens with a Type selector offering `API Key`, `HTTP`, `OAuth 2.0`, and `OpenID Connect`, plus a `Scheme Name` field and a `Description (optional)` field. (Assertion [#2](#test-assertion-4382))
6. Select Type `API Key`, inspect the revealed fields, then clear the `Name` and re-enter it.
7. Verify that an `In` selector with `Query`, `Header`, and `Cookie` choices and a `Name` field appear, that `Scheme Name` defaults to `ApiKeyAuth`, that the seeded In/Name keep `Add` enabled, that clearing the `Name` disables `Add`, and that refilling it re-enables `Add`. (Assertion [#3](#test-assertion-4383))
8. Change Type to `HTTP`, choose Scheme `Bearer`, then switch Scheme to `Basic`.
9. Verify that Scheme `Bearer` reveals an optional `Bearer Format` field and defaults `Scheme Name` to `BearerAuth`, and that Scheme `Basic` hides the `Bearer Format` field and defaults `Scheme Name` to `BasicAuth`. (Assertion [#4](#test-assertion-4384))
10. Change Type to `OAuth 2.0` and inspect the `OAuth Flows` editor.
11. Verify that the editor is seeded with an `Authorization Code` flow showing `Authorization URL` and `Token URL` and read/write/admin scopes, that additional flows and scopes can be added and removed, and that `Scheme Name` defaults to `OAuth2`. (Assertion [#5](#test-assertion-4385))
12. Change Type to `OpenID Connect`, clear the seeded `OpenID Connect URL`, then provide a value.
13. Verify that the `OpenID Connect URL` field appears (seeded with an example), that `Scheme Name` defaults to `OpenID`, that clearing the URL disables `Add`, and that providing a URL re-enables it. (Assertion [#6](#test-assertion-4386))
14. Confirm one scheme by clicking `Add`, then click the resulting card and inspect the `Edit Security Scheme` dialog, then use the card's remove icon.
15. Verify that the card shows the scheme's name, type label, and a summary, that clicking it reopens an `Edit Security Scheme` dialog with the saved values, and that the remove icon deletes the card. (Assertion [#7](#test-assertion-4387))
16. Reopen the `Add Security Scheme` dialog and try blank, duplicate, and incomplete entries for each type (API Key without In or Name, HTTP without Scheme, OpenID Connect without URL, OAuth 2.0 without a valid flow).
17. Verify that the `Add`/`Save` button stays disabled while the `Scheme Name` is blank or duplicates another scheme's name, or while a type's required fields are missing. (Assertion [#8](#test-assertion-4388))
18. Add at least two valid schemes, click `Document Security`, build a requirement by selecting schemes joined by `AND`, add an alternative with `Add Alternative (OR)`, and try the `Allow anonymous ({})` option.
19. Verify that the requirement can be built with AND-joined schemes and OR alternatives, that anonymous can be allowed, and that the button label updates to `Document Security: <summary>`. (Assertion [#9](#test-assertion-4389))
20. In the `Document Security` dialog, make two OR alternatives identical.
21. Verify that the dialog shows the warning `Duplicate of Requirement N — change or remove it.` and disables `Apply`. (Assertion [#10](#test-assertion-43810))
22. Resolve the duplicate and apply, then in the Endpoint Details table open an operation's Security cell, choose `Override with selected schemes` and select schemes, and on another operation choose `No security for this operation`.
23. Verify that the `Operation Security` dialog offers `Use document security`, `No security for this operation`, and `Override with selected schemes`, that override changes the cell summary from `Inherited` to the chosen requirement, and that `No security for this operation` shows `Public`. (Assertion [#11](#test-assertion-43811))
24. Click `Update`, then click `Generate` and open the downloaded YAML file.
25. Verify that `components.securitySchemes` contains each configured named scheme with its type-specific fields (apiKey type/in/name; http type/scheme and optional bearerFormat; oauth2 type/flows/scopes; openIdConnect type/openIdConnectUrl). (Assertion [#12](#test-assertion-43812))
26. Inspect the document-level and per-operation security in the same YAML file.
27. Verify that the document-level security requirement appears as a root-level `security` entry right after `info`, that the per-operation override appears as a `security` entry under that operation, and that an operation set to `Use document security` has no per-operation security key. (Assertion [#13](#test-assertion-43813))
28. Remove all configured security schemes and any document-level requirement, click `Update`, then click `Generate` and open the downloaded YAML file.
29. Verify that the YAML falls back to the legacy single OAuth2 scheme in `components.securitySchemes` with per-operation OAuth2 scopes and no root-level `security` entry. (Assertion [#14](#test-assertion-43814))
30. Set a per-operation security override on an operation and click `Update`, then change only an unrelated field such as the document Description and click `Update` again, then click `Generate` and open the downloaded YAML file.
31. Verify that the operation's override survives the later no-op Update and that the generated YAML still emits the operation's configured security rather than turning it Public. (Assertion [#15](#test-assertion-43815))
32. Complete a normal Update with all schemes and requirements valid, then introduce an incomplete security scheme or requirement and observe the `Update` button.
33. Verify that a single `Updated` confirmation message is shown after a valid Update, and that the `Update` button is disabled while any security scheme or requirement is incomplete. (Assertion [#16](#test-assertion-43816))
