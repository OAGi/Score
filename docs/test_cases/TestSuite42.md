# Test Suite 42

**Business Term Management**

## Test Case 42.1
**View or Edit Business Term**

### Test Assertion:

#### Test Assertion #42.1.1
The "View/Edit Business Term" menu under BIE menu should open the page titled with "Business Term". 

#### Test Assertion #42.1.2
The end user can create a Business Term with only required fields including both Business Term and External Reference URI field. 

#### Test Assertion #42.1.3
The end user cannot create a Business Term if any required field is not provided. 

#### Test Assertion #42.1.4
The end user can search for a Business Term based only on its term. 

#### Test Assertion #42.1.5
The end user can search for a Business Term based on the external reference URI.

#### Test Assertion #42.1.6
The end user can open a Business Term on the "Business Term" page to update its details in "Edit Business Term" page. 

#### Test Assertion #42.1.7
The end user cannot change Definition field in "Edit Business Term" page. 

#### Test Assertion #42.1.8
The end user cannot save a Business Term with an already existing term and URI in "Edit Business Term" page.

#### Test Assertion #42.1.9
The end user cannot discard a Business Term in View/Edit Business Term page if it is used in assignments. 

#### Test Assertion #42.1.10
The end user can discard a Business Term in View/Edit Business Term page if it is not in any assignments. 


### Test Step Pre-condition:

1. There are at least three BIEs created by end user and developer and they are in different states (WIP, QA, Production).

### Test Step:

1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks the "View/Edit Business Term" from the dropdown list.
4. Verify that the title in the new page is "Business Term" (Assertion [#1](#test-assertion-4211)). 
5. He clicks on the "New Business Term" button from the right above on the "Business Term" page. 
6. He provides the required inputs for Business Term, then "Create" button will be enabled. 
7. He clicks on the "Create" button. Repeat the steps 5-7 a few times to create several business terms.
8. Verify that those business terms are listed in the "Business Term" page (Assertion [#2](#test-assertion-4212)). 
9. Repeat the step 5-6, but don't provide the required input for Business Term or External Reference URI on step 6. 
10. Verify that "Create" button is still disabled. (Assertion [#3](#test-assertion-4213)). 
11. The end user will go back to "Business Term" page.
12. He will search the newly created business terms by putting some common word in the "Term" field and hits Search button. 
13. Verify that only business terms containing the search word are listed in the table (Assertion [#4](#test-assertion-4214)). 
14. He will search the newly created business terms by putting the external reference URI field and hits Search button.
15. Verify that only business terms containing the search URI are listed in the table (Assertion [#5](#test-assertion-4215))
16. He clicks on any newly created business term in the table and the "Edit Business Term" page will open. He can modify any fields except "Definition", and the Update button will be enabled after modification. He clicks on the Update button and the page will go back to Business Term page. 
17. Verify that the updated business term can be filtered based on the updated field information by searching on the new information. (Assertion [#6](#test-assertion-4216))
18. He clicks on any newly created business term in "Business Term" page and the "Edit Business Term" page will open. Verify that the "Definition" field is not editable. (Assertion [#7](#test-assertion-4217)).
19. The end user goes back to the home page and clicks on the "View/Edit Business Term" from the BIE dropdown list. 
20. He will create a new business term with the same information twice. Verify that the second time, when he clicks on the Create button on "Create Business Term" page, he will get a pop-up error message: Invalid parameters, another business term with the same business term and external reference URI already exists! (Assertion [#8](#test-assertion-4218)).
21. The end user goes back to the home page and clicks on the "View/Edit Business Term" from the BIE dropdown list. 
22. He selects a business term which has been assigned to some BIEs. He clicks on the Discard button. Verify that he will get an error message: Discard's forbidden! The business term is used. (Assertion [#9](#test-assertion-4219)).
23. He selects a business term without any assignments. He clicks on the Discard button and he will get a confirmation dialog which reminds that the business term will be permanently removed. He confirms by clicking on the Discard button on the confirmation dialog. Verify that the businss term is discarded from the table. (Assertion [#10](#test-assertion-42110)).

## Test Case 42.2

**Business Term Assignment**


### Test Assertion:

#### Test Assertion #42.2.1
The "Business Term Assignment" menu should open the page titled with "Business Term Assignment". 

#### Test Assertion #42.2.2
On Business Term Assignment page, the end user can view all the business terms with assignments. 

#### Test Assertion #42.2.3
On Business Term Assignment page, the end user can view the ASBIEs, BBIEs and Top-Level BIEs available for Business Term assignments. 

#### Test Assertion #42.2.4
On Business Term Assignment page, the end user can search business term assignments by BIE type and den, business term, URI and type code. 

#### Test Assertion #42.2.5
On Business Term Assignment page, the end user can filter only preferred business terms.  

#### Test Assertion #42.2.6
On Business Term Assignment page, the end user can select a BIE in the table and view all the business term assignments for that BIE. 

#### Test Assertion #42.2.7
On Assign Business Term page, the end user can select any BIE to view all the Business Terms available for assignments. 

#### Test Assertion #42.2.8
On Assign Business Term page, the end user can filter business terms that are already assigned to the same core component. 

#### Test Assertion #42.2.9

On the Select Business Term step/page of the business term assignment, the application shall allow or disallow duplicate  business term and type code (or no type code) assignment based on the conditions below.  

Business Term     | Type Code     | Dis/Allow 
Same              |  Same         | Disallow
Same              |  Diff         | Allow
Diff              |  Same         | Allow  
Diff              |  Diff         | Allow
 

#### Test Assertion #42.2.10
For each BIE, there can only be one preferred business term assignment. 


### Test Step Pre-condition:

1. There are at least three BIEs (ASBIE, BBIE) created by end user, and they are in different states (WIP, QA, Production). 
2. There are at least five business terms created. Three of them are assigned to different BIEs. Two of them are not assigned to any BIEs. 

### Test Step:

1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks on the "Business Term Assignment" from the dropdown list.
4. Verify that the title in the open page is "Business Term Assignment". (Assertion [#1] (#test-assertion-4221))
5. Verify that three business terms in the table. (Assertion [#2] (#test-assertion-4222)).
6. He clicks on the "Assign Business Term" button and verify that all BIEs created in the preconditions are available for assignments. (Assertion [#3] (#test-assertion-4223)).
7. The end user goes back to "Business Term Assignment" from the BIE dropdown list. 
8. Verify that he can search the business term assignments based on BIE DEN, business term, extenal reference URI or type code. (Assertion [#4](#test-assertion-4224)).
9. Verify that he can filter out the preferred business term assignments by checking Preferred Only checkbox. (Assertion [#5](#test-assertion-4225)).
10. He selects any BIE in the "Businss Term Assignment" page, then clicks on the "Search by Selected BIE", verify that only business terms for the selected BIE are listed. (Assertion [#6](#test-assertion-4226)).
11. He clicks on the "Assign Business Term" button. Then select any BIE in the new table and click "Next". Verify that he can see all the busines terms available for assignment. (Assertion [#7](#test-assertion-4227)).
12. On the same "Assign Business Term" page, the end user can filter out all the business term already assigned to the same core component by checking the "Filter by same CC" checkbox. then hits "Search" button. 
13. Verify the search results match the business term asssignments in preconditions(Assertion [#8](#test-assertion-4228)).
14. The end user goes back to "Business Term Assignment" from the BIE dropdown list. 
15. He clicks on the "Assign Business Term" button. Then select any BIE in the table and click "Next". He will select any business term in the new table, the "Create" button will be enabled. He can put a type code and hits "Create" to add the assignment. Then repeat the whole process for the same BIE with the same business term and using the same type code. Verify that the second time he clicks on the "Create" button, an error message will pop up: Invalid parameters, Another business term assignment for the same BIE and type code already exists!. (Assertion [#9](#test-assertion-4229)).
16. The end user goes back to "Business Term Assignment" from the BIE dropdown list. 
17. He clicks on the "Assign Business Term" button. Then select any BIE in the table and click "Next". He will select any business term in the new table,the "Create" button will be enabled. He can put a type code and hits "Create" to add the assignment. Then repeat the whole process for the same BIE with the same business term but using a different type code. Verify that the second time he clicks on the "Create" button, the assignment is created. (Assertion [#9](#test-assertion-4229)).
18. The end user goes back to "Business Term Assignment" from the BIE dropdown list. 
19. He clicks on the "Assign Business Term" button. Then select any BIE in the table and click "Next". He will select any business term in the new table, the "Create" button will be enabled. He can put a type code and hits "Create" to add the assignment. Then repeat the whole process for the same BIE with a different business term and using the same type code. Verify that the second time he clicks on the "Create" button, the assignment is created. (Assertion [#9](#test-assertion-4229)).
20. The end user goes back to "Business Term Assignment" from the BIE dropdown list. 
21. He clicks on the "Assign Business Term" button. Then select any BIE in the table and click "Next". He will select any business term in the new table, the "Create" button will be enabled. He can put a type code and hits "Create" to add the assignment. Then repeat the whole process for the same BIE with a different business term and using a different type code. Verify that the second time he clicks on the "Create" button, the assignment is created. (Assertion [#9](#test-assertion-4229)).
22. The end user goes back to "Business Term Assignment" from the BIE dropdown list. 
23. He will select any BIE from the "Business Term Assignment" page. Then clicks on the "Search by Selected BIE" buton on the right above. On the new page, verify that at most one True value in the column titled with "Preferred Business Term". (Assertion [#10](#test-assertion-42210)).
24. Repeat step 23, if no true value in the column titled with "Preferred Business Term". Click any BIE to open the "Edit Business Term Assignment" page, then click the checkbox: Preferred Business Term. Finally hits "Update" button. 
25. Repeat step 23, verify that only one True value in the column titled with "Preferred Business Term". (Assertion [#10](#test-assertion-42210)).
26. Repeat step 23, click any BIE with the false value in the column titled with "Preferred Business Term" to open the "Edit Business Term Assignment" page, then click the checkbox: Preferred Business Term. Finally hits "Update: button. Verify that a warning dialog will pop up with the message: Overwrite previous preferred business term?The preferred business term already exists for selected BIE and type code. Are you sure you want to do the update and overwrite the previous preferred business term assignment? (Assertion [#10](#test-assertion-42210)).

## Test Case 42.3

**Business Term from BIE detail page**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #42.3.1
The end user can see all business terms assigned to a descendant BIE node.  

#### Test Assertion #42.3.2

Hovering over the "Show Business Terms" button in the detail pane of a descendant BIE node shows up to five business term assigned to that node. 

#### Test Assertion #42.3.3

The end user can assign business terms to a descendant BIE node in the BIE detail page by clicking the "Assign Business Term" button in BIE detail pane. 


### Test Step Pre-condition:
1. There are at least three BIEs created by end user and developer and they are in different states (WIP, QA, Production). 
2. There are at least five business terms created. Three of them are assigned to different BIEs. At least Two of them are not assigned to any BIEs. 


### Test Step:
1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks on the "View/Edit BIE" from the dropdown list.
4. He clicks on any BIE from the table. The BIE detail page will open.
5. He expands the root level in the left sidenav tree and clicks on any used node in the second level, the detail for that node will display on the right.
6. He will click on "Show Business Terms" button to view all the business terms that already assigned to that node.
7. Verify that the list of business terms in the new window is indeed assigned to the selected node (Assertion [#1](#test-assertion-4231)).
8. He closes the new window and goes back to previous window.
9. He will hover over "Show Business Terms" button without clicking. A hint bar will display to show the number of business terms assigned to the node. Verify the number is the same as in the previosu step. (Assertion [#2](#test-assertion-4232)).
10. He will click on the "Assign Business Term" button right next to the "Show Business Terms" button on the same page.
11. He will select any availabe business term in the new window,  set the Type Code, and click on "Create" button.
12. He will close the new window and go back to previous window. 
13. He will hover over "Show Business Terms" button without clicking again. Verify the number in the hint bar is increased by one. (Assertion [#3](#test-asssertion-4233)).


## Test Case 42.4

**Load Business Terms from an External Source**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #42.4.1
The end user can download a template for the external csv file to be uploaded from "Business Term" page. 

#### Test Assertion #42.4.2

The end user can upload and attach the csv file with the correct format as specified in the template from "Business Term" page. 

#### Test Assertion #42.4.3

The end user can not upload a csv file if the csv file does not obey the format as specified in the template such as missing columns, missing header row, extra columns and invalid data format. 

#### Test Assertion #42.4.4

For bulk upload through "Upload Business Terms", if the business term is uploaded with new externalReferenceUri, a new business term will be created. 

#### Test Assertion #42.4.5

For bulk upload through "Upload Business Terms", if the business term is uploaded with an existent externalReferenceUri, the previous business term with the same URI will be updated with the new information. 


### Test Step Pre-condition:
N/A

### Test Steps:

1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks on the "View/Edit Business Term" from the dropdown list.
4. He clicks on the "Upload Business Terms" button. 
5. On the "Upload Business Terms" page, he will click on "Download template" on the right corner. Verify that a csv file named "businessTermTemplateWithExample.csv" is downloaded and saved into local drive. (Assertion [#1](#test-assertion-4241)).
6. On the same page, he will click on the attach button. An window will open for the end user to select the updated csv files with all the business term information for upload. He will select that file to upload. 
7. Verify that all the business terms in the uploaded csv file are listed in the table after uploaded. (Assertion [#2](#test-assertion-4242)).
8. Verify that a warning message will pop up if the csv file does not follow the format as specified in the template. (Assertion [#3](#test-assertion-4243)).
9. The end user will fix the format errors in the csv file and re-upload. 
10. Verify that a new business term is created in the table if the external reference uri is new in the uploaded csv file. (Assertion [#4](#test-assertion-4244)).
11. Verify that an existent business term is updated with the new information if the external reference uri is the same as the exitent one in the uploaded csv file. 
(Assertion [#5](#test-assertion-4245)).
12. Re-upload the same csv file for a second time. Verify that no more changes in all business terms. 
