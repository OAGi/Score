# Test Suite 42

**Business Term Management**

## Test Case 42.1
**View or Edit Business Term**

### Test Assertion:

#### Test Assertion #42.1.1
When Business Term is enabled in Application Settings, the `View/Edit Business Term` menu appears under the BIE menu and opens the page titled `Business Term`.

#### Test Assertion #42.1.2
The end user can create a Business Term with only the required fields, including `Business Term` and `External Reference URI`.

#### Test Assertion #42.1.3
The end user cannot create a Business Term if any required field is not provided.

#### Test Assertion #42.1.4
The end user can search for a Business Term based only on its term.

#### Test Assertion #42.1.5
The end user can search for a Business Term based on the external reference URI.

#### Test Assertion #42.1.6
The end user can open a Business Term on the `Business Term` page and update its editable details on the `Edit Business Term` page.

#### Test Assertion #42.1.7
The end user cannot change the `Definition` field on the `Edit Business Term` page.

#### Test Assertion #42.1.8
The end user cannot create or save a Business Term when another Business Term already exists with the same term and external reference URI.

#### Test Assertion #42.1.9
The end user cannot discard a Business Term that is used in assignments: while the term is in use the `Discard` control is hidden on the `Edit Business Term` page (the server populates the in-use flag instead of letting the delete fail with a foreign-key error).

#### Test Assertion #42.1.10
The end user can discard a Business Term after its existing assignments have been removed: once the term is no longer used the `Discard` control reappears and the term is removed successfully.

#### Test Assertion #42.1.11
The end user can save an `External Reference URI` longer than 45 characters on the `Edit Business Term` page, and the full value is persisted (regression guard for the edit-form URI truncation).

#### Test Assertion #42.1.12
The Business Term REST endpoints reject a developer-role user with HTTP 403 for both reads and writes; the server enforces the same gate the navbar applies and does not trust the UI.

#### Test Assertion #42.1.13
A list filter such as `External Reference URI` survives a page reload/bookmark: the filter round-trips through the URL, so both the filter value and the filtered result are preserved after reloading.

#### Test Assertion #42.1.14
Creating a Business Term with a malformed `External Reference URI` is rejected server-side with a clear validation error, even though the create form performs no URI format check.

#### Test Assertion #42.1.15
An import reports a created/updated summary on the import dialog's result step: a row whose `External Reference URI` already exists is counted as updated and a row with a new URI is counted as created (e.g. `1 created`, `1 updated`).

#### Test Assertion #42.1.16
`PUT /business-terms/{id}` honors the path id and rejects a request body that targets a different id with HTTP 400; a body whose id matches the path succeeds.

#### Test Assertion #42.1.17
Catalog uniqueness is enforced server-side, not only in the UI. A Business Term is uniquely identified by the (`Business Term` name + `External Reference URI`) pair: a direct create or update that duplicates that pair is rejected with HTTP 400, while a term that reuses an existing name with a different `External Reference URI` is a distinct term and is accepted. The server does not trust the form checks.

### Test Step Pre-condition:
1. Business Term is enabled in Application Settings in connectCenter.
2. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
3. The test data required to create, edit, assign, and discard Business Terms is available before execution.

### Test Step:
1. The end user signs in to connectCenter and opens `BIE > View/Edit Business Term`.
2. Verify that the page title is `Business Term`. (Assertion [#1](#test-assertion-4211))
3. The end user creates a new Business Term by providing only `Business Term` and `External Reference URI`.
4. Verify that the record is created successfully and can be opened from the list. (Assertion [#2](#test-assertion-4212))
5. The end user opens the create page again and omits one required field at a time.
6. Verify that the create action remains unavailable until all required fields are provided. (Assertion [#3](#test-assertion-4213))
7. The end user searches the list by `Term`.
8. Verify that the matching Business Term is returned. (Assertion [#4](#test-assertion-4214))
9. The end user opens the advanced search panel and searches by `External Reference URI`.
10. Verify that the matching Business Term is returned. (Assertion [#5](#test-assertion-4215))
11. The end user opens a Business Term from the list, updates editable fields such as `Business Term`, `External Reference URI`, `External Reference ID`, or `Comment`, and saves the record.
12. Verify that the updated values are persisted and the record can be reopened using the new values. (Assertion [#6](#test-assertion-4216))
13. The end user reopens the record and inspects the `Definition` field.
14. Verify that the `Definition` field is not editable. (Assertion [#7](#test-assertion-4217))
15. The end user attempts to create another Business Term using the same `Business Term` and `External Reference URI` as an existing record.
16. Verify that the application rejects the duplicate combination. (Assertion [#8](#test-assertion-4218))
17. The end user prepares a Business Term that is assigned to a BIE node and opens that Business Term on the edit page.
18. Verify that the `Discard` control is not shown while the Business Term is used in assignments. (Assertion [#9](#test-assertion-4219))
19. The end user removes the Business Term assignment from the related BIE node and returns to the same Business Term on the edit page.
20. Verify that the `Discard` control reappears once the term is no longer assigned and the Business Term is removed successfully. (Assertion [#10](#test-assertion-42110))
21. The end user opens a Business Term on the `Edit Business Term` page, enters an `External Reference URI` longer than 45 characters, and saves the record.
22. Verify that the full URI is persisted when the record is reopened. (Assertion [#11](#test-assertion-42111))
23. As a developer-role user, an authorized tester issues a direct API read and a direct API write to the Business Term endpoints.
24. Verify that both the read and the write are rejected with HTTP 403. (Assertion [#12](#test-assertion-42112))
25. The end user applies an `External Reference URI` filter on the list, runs the search, and reloads the page.
26. Verify that the filter value and the filtered result are preserved after the reload. (Assertion [#13](#test-assertion-42113))
27. The end user creates a Business Term with a malformed `External Reference URI`.
28. Verify that the create request is rejected with a validation error. (Assertion [#14](#test-assertion-42114))
29. The end user imports a CSV containing one row with an already-existing `External Reference URI` and one row with a new `External Reference URI`.
30. Verify that the import reports one created and one updated. (Assertion [#15](#test-assertion-42115))
31. An authorized tester issues `PUT /business-terms/{id}` with a body id different from the path id, then with a matching id.
32. Verify that the mismatched request is rejected with HTTP 400 and the matching update succeeds. (Assertion [#16](#test-assertion-42116))
33. An authorized tester issues a direct create duplicating an existing (`Business Term` name + `External Reference URI`) pair, a create that reuses the name with a different `External Reference URI`, and an update that points one record at another record's (name + `External Reference URI`) pair.
34. Verify that the duplicate-pair create and update are rejected with HTTP 400 while the same-name/different-URI create is accepted. (Assertion [#17](#test-assertion-42117))

## Test Case 42.2
**Business Term Assignment**

### Test Assertion:

#### Test Assertion #42.2.1
From a selected BIE detail node, the `Show Business Terms` action opens the page titled `Business Term Assignment`.

#### Test Assertion #42.2.2
On the `Business Term Assignment` page, the end user can view Business Terms that already have assignments.

#### Test Assertion #42.2.3
On the `Business Term Assignment` page, the end user can view assignments for BBIE and ASBIE nodes.

#### Test Assertion #42.2.4
On the `Business Term Assignment` page, the end user can search assignments by BIE type, BIE DEN, Business Term, external reference URI, and type code.

#### Test Assertion #42.2.5
On the `Business Term Assignment` page, the end user can filter only preferred Business Terms.

#### Test Assertion #42.2.6
When the page is opened for a selected BIE node, the end user can view assignments for that selected BIE.

#### Test Assertion #42.2.7
On the `Assign Business Term` page for a selected BIE, the end user can view Business Terms available for assignment to that BIE.

#### Test Assertion #42.2.8
On the `Assign Business Term` page, the end user can filter Business Terms already assigned to the same core component by using `Filter by same CC`.

#### Test Assertion #42.2.9
On the `Assign Business Term` page, duplicate assignment behavior for the same selected BIE follows this matrix:

Business Term | Type Code | Allow / Disallow
--- | --- | ---
Same | Same | Disallow
Same | Different | Allow
Different | Same | Allow
Different | Different | Allow

#### Test Assertion #42.2.10
For each selected BIE, creating another preferred Business Term assignment prompts the user to overwrite the previous preferred assignment.

#### Test Assertion #42.2.11
Discarding a Business Term assignment from the assignment detail page removes only that assignment; the underlying catalog Business Term remains in the registry.

#### Test Assertion #42.2.12
After creating a single Business Term assignment for one selected BIE, connectCenter opens the assignment list scoped to that BIE.

#### Test Assertion #42.2.13
Setting a Business Term assignment as preferred on a node that already has a preferred assignment demotes the previously preferred assignment, preserving the one-preferred-per-node rule.

#### Test Assertion #42.2.14
Assigning the identical Business Term to the same BIE node with the same type code twice does not create a duplicate assignment row (server-side find-or-create).

#### Test Assertion #42.2.15
Assigning a Business Term to a nonexistent BIE id is rejected with a clean HTTP 400 rather than a server error.

#### Test Assertion #42.2.16
A batch discard that includes a Business Term still in use rolls back the entire batch — no term in the batch is deleted — and returns HTTP 400.

#### Test Assertion #42.2.17
Preferred is one-per-BIE-node and independent of Type Code: setting an assignment preferred while another preferred assignment with a *different* Type Code already exists on the same node still prompts the overwrite warning and demotes the previously preferred assignment.

### Test Step Pre-condition:
1. Business Term is enabled in Application Settings in connectCenter.
2. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
3. At least one BBIE node and one ASBIE node can be marked as used for assignment testing.
4. Multiple Business Terms are available so the test can verify assignment listing, filtering, duplicate rules, and preferred assignment behavior.

### Test Step:
1. The end user signs in to connectCenter, opens a BIE detail page, selects a used BIE node, and clicks `Show Business Terms`.
2. Verify that the page title is `Business Term Assignment`. (Assertion [#1](#test-assertion-4221))
3. The end user prepares multiple Business Term assignments for used BIE nodes and opens the assignment page again.
4. Verify that assigned Business Terms are listed on the page. (Assertion [#2](#test-assertion-4222))
5. The end user prepares assignments for both BBIE and ASBIE nodes and searches for those assignments on the page.
6. Verify that BBIE and ASBIE assignment rows are both displayed. (Assertion [#3](#test-assertion-4223))
7. The end user searches the assignment page by BIE type, BIE DEN, Business Term, external reference URI, and type code.
8. Verify that each search field returns the expected assignment rows. (Assertion [#4](#test-assertion-4224))
9. The end user marks one assigned Business Term as preferred and then enables `Preferred Only`.
10. Verify that only preferred Business Term assignments remain in the result set. (Assertion [#5](#test-assertion-4225))
11. The end user opens the assignment page for one specific BIE node and compares those results with assignments belonging to another BIE node.
12. Verify that the page opened for the selected BIE shows assignments for that BIE. (Assertion [#6](#test-assertion-4226))
13. The end user clicks `Assign Business Term` for a selected BIE node and searches for multiple Business Terms that are available for assignment.
14. Verify that available Business Terms can be found and selected for the current BIE. (Assertion [#7](#test-assertion-4227))
15. The end user enables `Filter by same CC` on the assignment page.
16. Verify that Business Terms assigned to the same core component remain visible while Business Terms assigned only to different core components are filtered out. (Assertion [#8](#test-assertion-4228))
17. The end user assigns one Business Term to the selected BIE with a specific type code.
18. The end user repeats the assignment flow with the same Business Term and the same type code.
19. Verify that the duplicate assignment is rejected. (Assertion [#9](#test-assertion-4229))
20. The end user repeats the assignment flow with the same Business Term and a different type code.
21. Verify that the assignment is allowed. (Assertion [#9](#test-assertion-4229))
22. The end user repeats the assignment flow with a different Business Term and the original type code.
23. Verify that the assignment is allowed. (Assertion [#9](#test-assertion-4229))
24. The end user repeats the assignment flow with a different Business Term and a different type code.
25. Verify that the assignment is allowed. (Assertion [#9](#test-assertion-4229))
26. The end user creates a preferred Business Term assignment for the selected BIE and then attempts to create another preferred assignment for the same BIE.
27. Verify that connectCenter shows an overwrite warning for the existing preferred assignment. (Assertion [#10](#test-assertion-42210))
28. The end user creates a Business Term assignment, opens that assignment on its detail page, and discards it.
29. Verify that the assignment is removed while the catalog Business Term still exists in `View/Edit Business Term`. (Assertion [#11](#test-assertion-42211))
30. The end user clicks `Assign Business Term` for a single selected BIE node and creates one assignment.
31. Verify that connectCenter opens the assignment list scoped to the selected BIE (the BIE filter is applied). (Assertion [#12](#test-assertion-42212))
32. The end user assigns two Business Terms to the same BIE node, marks the first preferred, then opens the second assignment's detail page and sets it preferred (confirming the overwrite).
33. Verify that the second assignment becomes preferred and the previously preferred assignment is demoted. (Assertion [#13](#test-assertion-42213))
34. An authorized tester assigns the identical Business Term to the same BIE node with the same type code twice via the API.
35. Verify that only one assignment row exists for that Business Term and node. (Assertion [#14](#test-assertion-42214))
36. An authorized tester assigns a Business Term to a nonexistent BIE id via the API.
37. Verify that the response is HTTP 400. (Assertion [#15](#test-assertion-42215))
38. An authorized tester batch-discards a list that contains one unused Business Term and one Business Term that is still in use, via the API.
39. Verify that the response is HTTP 400 and that both Business Terms still exist. (Assertion [#16](#test-assertion-42216))
40. The end user assigns two Business Terms to the same BIE node with *different* type codes, marks the first preferred, then opens the second assignment's detail page and sets it preferred.
41. Verify that connectCenter still shows the overwrite warning and that, after confirming, the second assignment becomes preferred while the previously preferred assignment (with the different type code) is demoted. (Assertion [#17](#test-assertion-42217))

## Test Case 42.3
**Business Term from BIE Detail Page**

### Test Assertion:

#### Test Assertion #42.3.1
The end user can see all Business Terms assigned to a descendant BIE node.

#### Test Assertion #42.3.2
Hovering over the `Show Business Terms` button in the detail pane of a descendant BIE node shows up to five Business Terms assigned to that node.

#### Test Assertion #42.3.3
The end user can assign Business Terms to a descendant BIE node from the BIE detail pane by clicking `Assign Business Term`.

### Test Step Pre-condition:
1. Business Term is enabled in Application Settings in connectCenter.
2. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
3. A BIE with a descendant node that can be marked as used is available for the assignment flow.
4. Multiple Business Terms are available so the test can validate the descendant-node list and assignment behavior.

### Test Step:
1. The end user signs in to connectCenter and opens a BIE detail page.
2. The end user expands the BIE tree and selects a descendant node in the detail pane.
3. The end user assigns multiple Business Terms to that node and then clicks `Show Business Terms`.
4. Verify that the assignment page lists all Business Terms assigned to the selected descendant node. (Assertion [#1](#test-assertion-4231))
5. The end user returns to the BIE detail pane and hovers over `Show Business Terms`.
6. Verify that the hover preview displays up to five assigned Business Terms where the preview popover is available in the current UI. (Assertion [#2](#test-assertion-4232))
7. The end user clicks `Assign Business Term` on the same descendant node, selects an available Business Term, and creates the assignment.
8. The end user opens `Show Business Terms` again for the same node.
9. Verify that the newly assigned Business Term appears in the assignment list. (Assertion [#3](#test-assertion-4233))

## Test Case 42.4
**Load Business Terms from an External Source**

`Upload Business Terms` opens a modal **import dialog** (it is no longer a separate page). The dialog
walks through four steps — **Upload file** (drag &amp; drop or browse a `.csv`, `.tsv`, or `.xlsx` file,
up to 10 MB; the chosen file is shown as a removable tile, and a single-worksheet file advances
automatically to **Map columns** once it is parsed, so no extra `Next` click is needed on the upload
step, whereas a multi-worksheet workbook stays on the upload step and shows a **worksheet picker** so
the user can choose which sheet holds the terms), **Map columns** (the dialog auto-detects the
connectCenter template as well as common commercial Business Glossary exports — Collibra, Alation, and
others — shows an amber notice ("Review the column mapping below before continuing.") prompting the
user to confirm the auto-mapping,
and lets the user remap any column or synthesize the required External Reference URI from a base URL
plus an ID column), **Review &amp; select** (parsed rows are shown in a table; valid rows are
pre-checked, rows that need review are unchecked, flagged, and inline-editable with live
re-validation), and **Result** (a per-row created / updated / failed summary). The file is parsed
server-side without persisting; only the rows the user selects are imported, upserting by External
Reference URI. A top-right close (**X**) button cancels the import at any step before the result step,
and choosing an unsupported/oversized replacement file keeps the file already selected rather than
discarding it.

### Test Assertion:

#### Test Assertion #42.4.1
The end user can download a template for the external CSV file from the import dialog.

#### Test Assertion #42.4.2
The end user can upload a file that follows the template format and import all of its valid rows.

#### Test Assertion #42.4.3
Rows that violate the required format (missing business term, invalid URI, …) are flagged for review
and left unselected, and are not imported; the remaining valid rows in the same file can still be
imported.

#### Test Assertion #42.4.4
When a selected row carries a new External Reference URI, a new Business Term is created.

#### Test Assertion #42.4.5
When a selected row carries an already-existing External Reference URI, the existing Business Term for
that URI is updated (reported as `updated`) instead of creating another record.

#### Test Assertion #42.4.6
A multi-worksheet workbook does not auto-advance: the upload step keeps a worksheet picker visible, and
selecting the worksheet that holds the terms re-parses the file so its rows can be reviewed and imported.

#### Test Assertion #42.4.7
Removing the selected file via the file tile's remove control resets the dialog to the drag-and-drop
zone so a different file can be chosen.

#### Test Assertion #42.4.8
Choosing an unsupported (or oversized) replacement file while a valid file is already selected reports a
message and keeps the existing valid selection instead of discarding it.

#### Test Assertion #42.4.9
A commercial Business Glossary export that has no single URI column (e.g. a Collibra-style export)
auto-maps the term column and selects the "build from base URL + ID" strategy; the map step shows the
amber "Review the column mapping" notice, and once a base URL is supplied each row's External Reference URI is
synthesized as `<base URL><id>` and the rows import.

#### Test Assertion #42.4.10
The import can be cancelled at any step before the result step via the top-right close (X) button, and
cancelling imports nothing.

#### Test Assertion #42.4.11
Within one import, a later row whose `External Reference URI` repeats one already claimed by a selected
row in the same import is flagged as a duplicate and left unselected, so it is not imported and two
rows in one file cannot silently overwrite the same record.

#### Test Assertion #42.4.12
An import containing one invalid row alongside several valid rows imports the valid rows and reports the
single failure: a bad row is isolated per-row and does not roll back the good rows.

#### Test Assertion #42.4.13
An oversized (over 10 MB) or unsupported file is rejected. When a valid file is already selected the
rejection is reported via a snackbar and the valid selection is kept, while an unsupported file chosen
as the first selection shows the inline drop-zone error.

#### Test Assertion #42.4.14
Re-importing a row whose `External Reference URI` matches an existing Business Term but omits the
`Definition`, `Comment`, and `External Reference ID` columns updates the term without blanking those
existing fields (blank-clobber guard).

### Test Step Pre-condition:
1. Business Term is enabled in Application Settings in connectCenter.
2. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
3. The local execution environment can download files and upload files through the browser.

### Test Step:
1. The end user signs in to connectCenter and opens `BIE > View/Edit Business Term`.
2. The end user clicks `Upload Business Terms` to open the import dialog, then clicks `Download template`.
3. Verify that the CSV template file is downloaded successfully. (Assertion [#1](#test-assertion-4241))
4. The end user uploads a file that follows the template format; the dialog auto-advances to `Map columns`. The end user advances through the auto-mapped preview and imports the rows.
5. Verify that the result step reports the rows as created and that the imported Business Terms can be found from the `Business Term` page. (Assertion [#2](#test-assertion-4242))
6. The end user uploads a file containing some malformed rows (missing required values, invalid URI) alongside a valid row.
7. Verify that the malformed rows are flagged for review and unselected, that they are not imported, and that the valid row is imported. (Assertion [#3](#test-assertion-4243))
8. The end user uploads a file containing rows with new external reference URIs and imports them.
9. Verify that new Business Terms are created for those URIs. (Assertion [#4](#test-assertion-4244))
10. With a Business Term already present for a given external reference URI, the end user uploads a row carrying that same URI but new Business Term details, and imports it.
11. Verify that the result reports one `updated` row, that only one record remains for that URI, and that its stored data reflects the uploaded row. (Assertion [#5](#test-assertion-4245))
12. The end user uploads a multi-worksheet `.xlsx` workbook whose terms live on a sheet other than the default one.
13. Verify that the dialog stays on the upload step with a worksheet picker, and that selecting the worksheet holding the terms re-parses the file and lets its rows be imported. (Assertion [#6](#test-assertion-4246))
14. The end user selects a file and then removes it using the file tile's remove control.
15. Verify that the dialog returns to the drag-and-drop zone. (Assertion [#7](#test-assertion-4247))
16. With a valid file already selected, the end user attempts to select an unsupported file.
17. Verify that a message is shown and the previously selected valid file remains selected. (Assertion [#8](#test-assertion-4248))
18. The end user uploads a commercial Business Glossary export that has no URI column, confirms the auto-mapping (the map step shows the amber "Review the column mapping" notice), supplies a base URL, and imports the rows.
19. Verify that each imported Business Term's External Reference URI is the base URL followed by the row's id. (Assertion [#9](#test-assertion-4249))
20. The end user opens the import dialog, advances past the upload step, and clicks the top-right close (X) button.
21. Verify that the dialog closes and nothing is imported. (Assertion [#10](#test-assertion-42410))
22. The end user uploads a file that contains two selected rows sharing the same `External Reference URI` and reaches the `Review & select` step.
23. Verify that the later duplicate row is flagged as a duplicate and left unselected, and that it is not imported. (Assertion [#11](#test-assertion-42411))
24. The end user uploads a file that contains one invalid row alongside several valid rows and imports the selection.
25. Verify that the valid rows are imported and that the result step reports the single failure without rolling back the good rows. (Assertion [#12](#test-assertion-42412))
26. The end user, with a valid file already selected, attempts to select an oversized (over 10 MB) or unsupported replacement file; the end user also opens a fresh dialog and chooses an unsupported file as the first selection.
27. Verify that the replacement rejection is reported via a snackbar and keeps the valid selection, and that the unsupported first selection shows the inline drop-zone error. (Assertion [#13](#test-assertion-42413))
28. With a Business Term already present for a given `External Reference URI`, the end user re-imports a row carrying that same URI but omitting the `Definition`, `Comment`, and `External Reference ID` columns, and imports it.
29. Verify that the existing term is updated without blanking its `Definition`, `Comment`, and `External Reference ID` values. (Assertion [#14](#test-assertion-42414))

## Test Case 42.5
**In-place Business Term Management in the BIE Editor**

Business Terms are managed directly in the BIE editor through a `Business Terms` chip field shown beside
`Remark` on used ASBIE/BBIE nodes. This chip field replaces the standalone `Business Term Assignment`
and `Assign Business Term` pages. Each assigned term is shown as a chip; the `+` (Assign a Business
Term) button opens a multi-select `Assign Business Term` dialog, a chip can be set preferred or removed,
its optional Type Code can be edited inline, and hovering a chip shows a preview card.

### Test Assertion:

#### Test Assertion #42.5.1
The `Business Terms` chip field appears beside `Remark` on used ASBIE/BBIE nodes for an end user when
Business Term is enabled; a developer-role user (or a tenant with Business Term disabled) sees the
legacy `Business Term` text input instead of the chip field.

#### Test Assertion #42.5.2
Business Terms are editable via the chip field regardless of BIE state (WIP, QA, or Production): there is
no ownership or edit-state gate on business-term editing, and editability requires only that the node is
used and not locked or cyclic.

#### Test Assertion #42.5.3
The `+` (Assign a Business Term) button is disabled until the BIE node has been saved (has a persisted
id).

#### Test Assertion #42.5.4
The `Assign Business Term` dialog supports multi-select: a master checkbox selects or clears all
available terms (indeterminate when the selection is partial), the action button reads `Assign (N)`, and
all selected terms are assigned at once.

#### Test Assertion #42.5.5
The optional Type Code lets the same Business Term be assigned to the same BIE more than once when the
Type Code differs; editing a chip's Type Code to one that collides with another assignment on the same
BIE shows the non-blocking inline error `Another business term assignment for the same BIE and type code already exists!`
(not a modal), and the duplicate check ignores the row being edited.

#### Test Assertion #42.5.6
A Business Term already used by another component is still selectable and assignable from the in-place
dialog: the `used` state guards catalog discard, not assignment.

#### Test Assertion #42.5.7
Setting a chip as preferred demotes the previously preferred assignment on the same BIE node, preserving
the one-preferred-per-node rule independent of Type Code.

#### Test Assertion #42.5.8
Removing a chip prompts a confirmation dialog; on confirm only that assignment is removed while the
catalog Business Term remains in the registry, and cancelling removes nothing.

#### Test Assertion #42.5.9
Hovering a chip shows a preview card containing the term link (which opens the Business Term), the
`External Reference URI`, the `External Reference ID`, the `Definition`, and the `Comment`; empty fields
are omitted.

#### Test Assertion #42.5.10
On the base (inherited) tab the chip field is read-only and non-interactive.

### Test Step Pre-condition:
1. Business Term is enabled in Application Settings in connectCenter.
2. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
3. At least one BBIE node and one ASBIE node can be marked as used for in-place management.
4. Multiple Business Terms are available so the test can verify multi-select assignment, Type Code, preferred, and preview behavior.
5. BIE nodes in WIP, QA, and Production states are available so the test can confirm the absence of an edit-state gate.

### Test Step:
1. The end user signs in to connectCenter, opens a BIE detail page, and selects a used ASBIE or BBIE node.
2. Verify that the `Business Terms` chip field appears beside `Remark`, and that a developer-role user (or a tenant with Business Term disabled) sees the legacy `Business Term` text input instead. (Assertion [#1](#test-assertion-4251))
3. The end user opens BIE nodes that are in WIP, QA, and Production states and inspects the chip field on each.
4. Verify that Business Terms remain editable via the chip field in every state, with no ownership or edit-state gate, as long as the node is used and not locked or cyclic. (Assertion [#2](#test-assertion-4252))
5. The end user selects a used BIE node that has not yet been saved and inspects the `+` (Assign a Business Term) button, then saves the node.
6. Verify that the `+` button is disabled until the node has a persisted id and becomes enabled after the save. (Assertion [#3](#test-assertion-4253))
7. The end user clicks `+` (Assign a Business Term), uses the master checkbox to select and clear all available terms, selects a partial set, then selects several terms and clicks the action button.
8. Verify that the master checkbox selects, clears, and shows an indeterminate state for a partial selection, that the action button reads `Assign (N)`, and that all selected terms are assigned at once. (Assertion [#4](#test-assertion-4254))
9. The end user assigns the same Business Term to the same BIE twice using different Type Codes, then edits one chip's Type Code to collide with another assignment on the same BIE.
10. Verify that the two assignments with different Type Codes are allowed, and that the colliding edit shows the non-blocking inline error `Another business term assignment for the same BIE and type code already exists!` while the row being edited is ignored by the duplicate check. (Assertion [#5](#test-assertion-4255))
11. The end user opens the `Assign Business Term` dialog for a node and searches for a Business Term already used by another component.
12. Verify that the term is still selectable and assignable from the in-place dialog. (Assertion [#6](#test-assertion-4256))
13. The end user assigns two Business Terms to the same BIE node, marks the first preferred, then marks the second preferred.
14. Verify that the second chip becomes preferred and the previously preferred chip is demoted, independent of Type Code. (Assertion [#7](#test-assertion-4257))
15. The end user removes a chip, cancels the confirmation dialog, then removes the chip again and confirms.
16. Verify that cancelling removes nothing, that confirming removes only that assignment, and that the catalog Business Term still exists in `View/Edit Business Term`. (Assertion [#8](#test-assertion-4258))
17. The end user hovers over an assigned chip whose term has an `External Reference URI`, `External Reference ID`, `Definition`, and `Comment`, and also over a chip whose optional fields are empty.
18. Verify that the preview card shows the term link that opens the Business Term along with the populated fields, and that empty fields are omitted. (Assertion [#9](#test-assertion-4259))
19. The end user opens the base (inherited) tab of a BIE node and inspects the chip field.
20. Verify that the chip field is read-only and non-interactive on the base tab. (Assertion [#10](#test-assertion-42510))
