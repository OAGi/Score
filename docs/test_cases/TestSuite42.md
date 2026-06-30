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
A CSV import reports a created/updated summary: a row whose `External Reference URI` already exists is counted as updated and a row with a new URI is counted as created (e.g. `Imported: 1 created, 1 updated.`).

#### Test Assertion #42.1.16
`PUT /business-terms/{id}` honors the path id and rejects a request body that targets a different id with HTTP 400; a body whose id matches the path succeeds.

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

### Test Assertion:

#### Test Assertion #42.4.1
The end user can download a template for the external CSV file to be uploaded from the `Business Term` page.

#### Test Assertion #42.4.2
The end user can upload a CSV file that follows the template format from the `Business Term` page.

#### Test Assertion #42.4.3
No new Business Term is created when the uploaded CSV file does not obey the required template format.

#### Test Assertion #42.4.4
For bulk upload through `Upload Business Terms`, if a row is uploaded with a new external reference URI, a new Business Term is created.

#### Test Assertion #42.4.5
For bulk upload through `Upload Business Terms`, if a row is uploaded with an existing external reference URI, the previously uploaded Business Term for that URI is updated instead of creating another record.

### Test Step Pre-condition:
1. Business Term is enabled in Application Settings in connectCenter.
2. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
3. The local execution environment can download files and upload CSV files through the browser.

### Test Step:
1. The end user signs in to connectCenter and opens `BIE > View/Edit Business Term`.
2. The end user clicks `Upload Business Terms` and then clicks `Download template`.
3. Verify that the CSV template file is downloaded successfully. (Assertion [#1](#test-assertion-4241))
4. The end user prepares a CSV file that follows the required template format and uploads it.
5. Verify that the upload succeeds and the uploaded Business Terms can be found from the `Business Term` page. (Assertion [#2](#test-assertion-4242))
6. The end user uploads a CSV file that violates the required template format, such as missing required values or invalid URI content.
7. Verify that connectCenter rejects the upload and no records from that invalid file are created. (Assertion [#3](#test-assertion-4243))
8. The end user uploads a CSV file containing rows with new external reference URIs.
9. Verify that new Business Terms are created for those URIs. (Assertion [#4](#test-assertion-4244))
10. The end user uploads a CSV file where a later row reuses an external reference URI that already appears in the file but provides new Business Term details.
11. Verify that only one record remains for that URI and that the stored Business Term data reflects the later uploaded row. (Assertion [#5](#test-assertion-4245))
