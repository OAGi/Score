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
The end user cannot discard a Business Term on the `Edit Business Term` page if it is used in assignments.

#### Test Assertion #42.1.10
The end user can discard a Business Term after its existing assignments have been removed.

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
18. The end user attempts to discard the record.
19. Verify that connectCenter blocks the discard operation because the Business Term is used in assignments. (Assertion [#9](#test-assertion-4219))
20. The end user removes the Business Term assignment from the related BIE node, returns to `View/Edit Business Term`, and discards the same Business Term again.
21. Verify that the Business Term is removed successfully after it is no longer assigned. (Assertion [#10](#test-assertion-42110))

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
