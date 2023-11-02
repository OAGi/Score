# Test Suite 43

**OpenAPI Document Management**

## Test Case 43.1
**Define OpenAPI Document Definition**

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
The end user cannot discard an OpenAPI Document in "OpenAPI Document" page if it is used in assignments. 

#### Test Assertion #43.1.10
The end user can discard an OpenAPI Document in View/Edit OpenAPI Document  page if it is not in any assignments. 


### Test Step Pre-condition:

1. There are at least three BIEs created by end user and developer and they are in different states (WIP, QA, Production).

### Test Step:

1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks the "View/Edit OpenAPI Document " from the dropdown list.
4. Verify that the title in the new page is "OpenAPI Document " (Assertion [#1](#test-assertion-4311)). 
5. He clicks on the "New OpenAPI Document " button from the right above on the "OpenAPI Document " page. 
6. He provides the required inputs for OpenAPI Document , then "Create" button will be enabled. 
7. He clicks on the "Create" button. Repeat the steps 5-7 a few times to create several business terms.
8. Verify that those business terms are listed in the "OpenAPI Document " page (Assertion [#2](#test-assertion-4312)). 
9. Repeat the step 5-6, but don't provide the required input for OpenAPI Document  or External Reference URI on step 6. 
10. Verify that "Create" button is still disabled. (Assertion [#3](#test-assertion-4313)). 
11. The end user will go back to "OpenAPI Document " page.
12. He will search the newly created business terms by putting some common word in the "Term" field and hits Search button. 
13. Verify that only business terms containing the search word are listed in the table (Assertion [#4](#test-assertion-4314)). 
14. He will search the newly created business terms by putting the external reference URI field and hits Search button.
15. Verify that only business terms containing the search URI are listed in the table (Assertion [#5](#test-assertion-4315))
16. He clicks on any newly created business term in the table and the "Edit OpenAPI Document " page will open. He can modify any fields except "Definition", and the Update button will be enabled after modification. He clicks on the Update button and the page will go back to OpenAPI Document  page. 
17. Verify that the updated business term can be filtered based on the updated field information by searching on the new information. (Assertion [#6](#test-assertion-4316))
18. He clicks on any newly created business term in "OpenAPI Document " page and the "Edit OpenAPI Document " page will open. Verify that the "Definition" field is not editable. (Assertion [#7](#test-assertion-4317)).
19. The end user goes back to the home page and clicks on the "View/Edit OpenAPI Document " from the BIE dropdown list. 
20. He will create a new business term with the same information twice. Verify that the second time, when he clicks on the Create button on "Create OpenAPI Document " page, he will get a pop-up error message: Invalid parameters, another business term with the same business term and external reference URI already exists! (Assertion [#8](#test-assertion-4318)).
21. The end user goes back to the home page and clicks on the "View/Edit OpenAPI Document " from the BIE dropdown list. 
22. He selects a business term which has been assigned to some BIEs. He clicks on the Discard button. Verify that he will get an error message: Discard's forbidden! The business term is used. (Assertion [#9](#test-assertion-4319)).
23. He selects a business term without any assignments. He clicks on the Discard button and he will get a confirmation dialog which reminds that the business term will be permanently removed. He confirms by clicking on the Discard button on the confirmation dialog. Verify that the businss term is discarded from the table. (Assertion [#10](#test-assertion-43110)).

## Test Case 43.2

**Add BIE*


### Test Assertion:

#### Test Assertion #43.2.1
The "Add BIE For OpenAPI Document" menu should open the page titled with "Add BIE For OpenAPI Document". 

#### Test Assertion #43.2.2
On Add BIE For OpenAPI Document page, the end user can view BIEs available to be assigned. This can be duplicate of previously assigned, but only if assigned to a separate Verb and Body Type combination. 

#### Test Assertion #43.2.3
On Add BIE For OpenAPI Document page, the end user can search BIEs by Branch, State, Owner, Updater, DEN, Business Context, Updated Start Date, and Updated End Date. 

#### Test Assertion #43.2.4
On OpenAPI Document  Assignment page, . 

#### Test Assertion #43.2.5
On OpenAPI Document  Assignment page, the end user can filter only preferred business terms.  

#### Test Assertion #43.2.6
On OpenAPI Document  Assignment page, the end user can select a BIE in the table and view all the business term assignments for that BIE. 

#### Test Assertion #43.2.7
On Assign OpenAPI Document  page, the end user can select any BIE to view all the OpenAPI Document s available for assignments. 

#### Test Assertion #43.2.8
On Assign OpenAPI Document  page, the end user can filter business terms that are already assigned to the same core component. 

#### Test Assertion #43.2.9

On the Select OpenAPI Document  step/page of the business term assignment, the application shall allow or disallow duplicate  business term and type code (or no type code) assignment based on the conditions below.  

OpenAPI Document      | Type Code     | Dis/Allow 
Same              |  Same         | Disallow
Same              |  Diff         | Allow
Diff              |  Same         | Allow  
Diff              |  Diff         | Allow
 

#### Test Assertion #43.2.10
For each BIE, there can only be one preferred business term assignment. 


### Test Step Pre-condition:

1. There are at least three BIEs (ASBIE, BBIE) created by end user, and they are in different states (WIP, QA, Production). 
2. There are at least five business terms created. Three of them are assigned to different BIEs. Two of them are not assigned to any BIEs. 

### Test Step:

1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks on the "OpenAPI Document  Assignment" from the dropdown list.
4. Verify that the title in the open page is "OpenAPI Document  Assignment". (Assertion [#1] (#test-assertion-4321))
5. Verify that three business terms in the table. (Assertion [#2] (#test-assertion-4322)).
6. He clicks on the "Assign OpenAPI Document " button and verify that all BIEs created in the preconditions are available for assignments. (Assertion [#3] (#test-assertion-4323)).
7. The end user goes back to "OpenAPI Document  Assignment" from the BIE dropdown list. 
8. Verify that he can search the business term assignments based on BIE DEN, business term, extenal reference URI or type code. (Assertion [#4](#test-assertion-4324)).
9. Verify that he can filter out the preferred business term assignments by checking Preferred Only checkbox. (Assertion [#5](#test-assertion-4325)).
10. He selects any BIE in the "Businss Term Assignment" page, then clicks on the "Search by Selected BIE", verify that only business terms for the selected BIE are listed. (Assertion [#6](#test-assertion-4326)).
11. He clicks on the "Assign OpenAPI Document " button. Then select any BIE in the new table and click "Next". Verify that he can see all the busines terms available for assignment. (Assertion [#7](#test-assertion-4327)).
12. On the same "Assign OpenAPI Document " page, the end user can filter out all the business term already assigned to the same core component by checking the "Filter by same CC" checkbox. then hits "Search" button. 
13. Verify the search results match the business term asssignments in preconditions(Assertion [#8](#test-assertion-4328)).
14. The end user goes back to "OpenAPI Document  Assignment" from the BIE dropdown list. 
15. He clicks on the "Assign OpenAPI Document " button. Then select any BIE in the table and click "Next". He will select any business term in the new table, the "Create" button will be enabled. He can put a type code and hits "Create" to add the assignment. Then repeat the whole process for the same BIE with the same business term and using the same type code. Verify that the second time he clicks on the "Create" button, an error message will pop up: Invalid parameters, Another business term assignment for the same BIE and type code already exists!. (Assertion [#9](#test-assertion-4329)).
16. The end user goes back to "OpenAPI Document  Assignment" from the BIE dropdown list. 
17. He clicks on the "Assign OpenAPI Document " button. Then select any BIE in the table and click "Next". He will select any business term in the new table,the "Create" button will be enabled. He can put a type code and hits "Create" to add the assignment. Then repeat the whole process for the same BIE with the same business term but using a different type code. Verify that the second time he clicks on the "Create" button, the assignment is created. (Assertion [#9](#test-assertion-4329)).
18. The end user goes back to "OpenAPI Document  Assignment" from the BIE dropdown list. 
19. He clicks on the "Assign OpenAPI Document " button. Then select any BIE in the table and click "Next". He will select any business term in the new table, the "Create" button will be enabled. He can put a type code and hits "Create" to add the assignment. Then repeat the whole process for the same BIE with a different business term and using the same type code. Verify that the second time he clicks on the "Create" button, the assignment is created. (Assertion [#9](#test-assertion-4329)).
20. The end user goes back to "OpenAPI Document  Assignment" from the BIE dropdown list. 
21. He clicks on the "Assign OpenAPI Document " button. Then select any BIE in the table and click "Next". He will select any business term in the new table, the "Create" button will be enabled. He can put a type code and hits "Create" to add the assignment. Then repeat the whole process for the same BIE with a different business term and using a different type code. Verify that the second time he clicks on the "Create" button, the assignment is created. (Assertion [#9](#test-assertion-4329)).
22. The end user goes back to "OpenAPI Document  Assignment" from the BIE dropdown list. 
23. He will select any BIE from the "OpenAPI Document  Assignment" page. Then clicks on the "Search by Selected BIE" buton on the right above. On the new page, verify that at most one True value in the column titled with "Preferred OpenAPI Document ". (Assertion [#10](#test-assertion-43210)).
24. Repeat step 23, if no true value in the column titled with "Preferred OpenAPI Document ". Click any BIE to open the "Edit OpenAPI Document  Assignment" page, then click the checkbox: Preferred OpenAPI Document . Finally hits "Update" button. 
25. Repeat step 23, verify that only one True value in the column titled with "Preferred OpenAPI Document ". (Assertion [#10](#test-assertion-43210)).
26. Repeat step 23, click any BIE with the false value in the column titled with "Preferred OpenAPI Document " to open the "Edit OpenAPI Document  Assignment" page, then click the checkbox: Preferred OpenAPI Document . Finally hits "Update: button. Verify that a warning dialog will pop up with the message: Overwrite previous preferred business term?The preferred business term already exists for selected BIE and type code. Are you sure you want to do the update and overwrite the previous preferred business term assignment? (Assertion [#10](#test-assertion-43210)).

## Test Case 43.3

**BIE Usage Detail page**

Pre-condition: A BIE is assigned to the Open API Document.

### Test Assertion:

#### Test Assertion #43.3.1
The end user can see all business terms assigned to a descendant BIE node.  

#### Test Assertion #43.3.2

Hovering over the "Show OpenAPI Document s" button in the detail pane of a descendant BIE node shows up to five business term assigned to that node. 

#### Test Assertion #43.3.3

The end user can assign business terms to a descendant BIE node in the BIE detail page by clicking the "Assign OpenAPI Document " button in BIE detail pane. 


### Test Step Pre-condition:
1. There are at least three BIEs created by end user and developer and they are in different states (WIP, QA, Production). 
2. There are at least five business terms created. Three of them are assigned to different BIEs. At least Two of them are not assigned to any BIEs. 


### Test Step:
1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks on the "View/Edit BIE" from the dropdown list.
4. He clicks on any BIE from the table. The BIE detail page will open.
5. He expands the root level in the left sidenav tree and clicks on any used node in the second level, the detail for that node will display on the right.
6. He will click on "Show OpenAPI Document s" button to view all the business terms that already assigned to that node.
7. Verify that the list of business terms in the new window is indeed assigned to the selected node (Assertion [#1](#test-assertion-4331)).
8. He closes the new window and goes back to previous window.
9. He will hover over "Show OpenAPI Document s" button without clicking. A hint bar will display to show the number of business terms assigned to the node. Verify the number is the same as in the previosu step. (Assertion [#2](#test-assertion-4332)).
10. He will click on the "Assign OpenAPI Document " button right next to the "Show OpenAPI Document s" button on the same page.
11. He will select any availabe business term in the new window,  set the Type Code, and click on "Create" button.
12. He will close the new window and go back to previous window. 
13. He will hover over "Show OpenAPI Document s" button without clicking again. Verify the number in the hint bar is increased by one. (Assertion [#3](#test-asssertion-4333)).


