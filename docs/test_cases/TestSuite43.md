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
