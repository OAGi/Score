# Test Suite 42

**Business Term**

## Test Case 42.1
**View or Edit Business Term**

### Test Assertion:

#### Test Assertion #42.1.1
The end user can create a Business Term with all information specified. 

#### Test Assertion #42.1.2
The end user can search for a Business Term based only on its term. 

#### Test Assertion #42.1.3
The end user can search for a Business Term based on the external reference URI.

#### Test Assertion #42.1.4
The end user can select a Business Term in the table to update its details in Edit Business Term page. 

#### Test Assertion #42.1.5
The end user cannot change Definition field in Edit Business Term page. 

#### Test Assertion #42.1.6
The end user cannot save a Business Term with an already existing term and URI in Edit Business Term page.

#### Test Assertion #42.1.7
The end user cannot discard a Business Term in View/Edit Business Term page if it is used in assignments. 

#### Test Assertion #42.1.8
The end user can discard a Business Term in the table in View/Edit Business Term page if it is not in any assignments. 


### Test Step Pre-condition:

1. There are at least three BIEs created by end user and developer and they are in different states (WIP, QA, Production).

### Test Step:

1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks the "View/Edit Business Term" from the dropdown list.
4. He clicks the "New Business Term" button from the right above on the "Business Term" page. 
5. He put the required inputs for Business Term, then "Create" will be enabled. 
6. He clicks the "Create" button. Repeat the step 3-6 a few times to create several business terms (Assertion [#1](#test-assertion-4211)). 
7. He search the newly created business terms by putting some common word in the "Term" field and click Search button. 
8. Verify that only business terms containing the search word are listed in the table (Assertion [#2](#test-assertion-4212)). 
9. He search the newly created business terms by putting the external reference URI field and click Search button.
10. Verify that only business terms containing the search URI are listed in the table (Assertion [#3](#test-assertion-4213))
11. He clicks on any newly created business term in the table and the Edit Business Term page will open. He can modify any fields except "Definition", and the Update button will be enabled. He clicks on the Update button and the page will go back to Business Term list page. 
12. Verify that the updated business term can be filtered based on the updated field information by searching on the new information. (Assertion [#4](#test-assertion-4214))
13. He clicks on any newly created business term in the table and the Edit Business Term page will open. Verify that the "Definition" field is not editable. (Assertion [#5](#test-assertion-4215)).
14. The end user goes back to the homem page and clicks on the "View/Edit Business Term" from the BIE dropdown list. 
15. He will create a new business term with the same information twice. Verify that the second time, when he clicks on the Create button in "Create Business Term" page, he will get a pop up error message: Invalid parameters, another business term with the same business term and external reference URI already exists! (Assertion [#6](#test-assertion-4216)).
16. The end user goes back to the homem page and clicks on the "View/Edit Business Term" from the BIE dropdown list. 
17. He selects a business term which has been assigned to some BIEs. He clicks on the Discard button and he will get a error message: Discard's forbidden! The business term is used. (Assertion [#7](#test-assertion-4217)).
18. He selects a business term without any assignments. He clicks on the Discard button and he will get a confirmation dialog which reminds that the business term will be permanently removed. He confirms by clicking on the Discard button on the confirmation dialog. The businss term is discarded from the table. (Assertion [#8](#test-assertion-4218)).

## Test Case 42.2

**Business Term Assignments**


### Test Assertion:

#### Test Assertion #42.2.1
On Business Term Assignments page, the end user can view all the business terms with assignments. 

#### Test Assertion #42.2.2
On Business Term Assignments page, the end user can click on the "Assign Business Term" button to view all the BIEs available for Business Term assignments. 

#### Test Assertion #42.2.3
On Business Term Assignments page, the end user can search business term assignments by BIE type and den, business term, URI and type code. 

#### Test Assertion #42.2.4
On Business Term Assignments page, the end user can filter only preferred business terms.  

#### Test Assertion #42.2.5
On Business Term Assignments page, the end user can select a BIE in the table and click "Search by Selected BIE" to view all the business term assignments for that BIE. 

#### Test Assertion #42.2.6
On Assign Business Term page, the end user can select any BIE to view all the Business Terms available for assignments. 

#### Test Assertion #42.2.7
On Assign Business Term page, the end user can filter business terms that are already assigned to the same core component. 

#### Test Assertion #42.2.8

On the Select Business Term step/page of the business term assignment, the application shall allow or disallow duplicate  business term and type code (or no type code) assignment based on the conditions below.  

Business Term | Type Code | Dis/Allow 
Same              |  Same         | Disallow
Same              |  Diff            | Allow
Diff                 |  Same         | Disallow  
Diff                 |  Diff            | Allow
 

#### Test Assertion #42.2.9

On Assign Business Term page, the end user can create the assignment of the same business term with different type codes for the selected BIE. 


### Test Step Pre-condition:

1. There are at least three BIEs created by end user and developer and they are in different states (WIP, QA, Production). 
2. There are at least five business terms created. Three of them are assigned to different BIEs. Two of them are not assigned to any BIEs. 

### Test Step:

1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks on the "Business Term Assignments" from the dropdown list.
4. Verify that three business terms in the table. (Assertion [#1] (#test-assertion-4221)).
5. He clicks on the "Assign Business Term" button and verify that all BIEs created in the preconditions are available for assignments. (Assertion [#2] (#test-assertion-4222)).
6. The end user goes back to "Business Term Assignments" from the BIE dropdown list. 
7. Verify that he can search the business term assignments based on BIE DEN, business term, extenal reference URI or type code. (Assertion [#3](#test-assertion-4223)).
8. Verify that he can filter out the preferred business term assignments by checking Preferred Only checkbox. (
    Assertion [#4](#test-assertion-4224)).
9. He selects any BIE in the table, then clicks on the "Search by Selected BIE", verify that only business terms for the selected BIE are listed. (Assertion [#5](#test-assertion-4225)).
10. He clicks on the "Assign Business Term" button. Then select any BIE in the new table and click "Next". He will see all the busines terms available for assignment. (Assertion [#6](#test-assertion-4226)).
11. On the same "Assign Business Term" page, the end user can filter out all the business term already assigned to the same core component by checking the "Filter by same CC" checkbox. then click "Search" button. (Assertion [#7](#test-assertion-4227)).
12. The end user goes back to "Business Term Assignments" from the BIE dropdown list. 
13. He clicks on the "Assign Business Term" button. Then select any BIE in the table and click "Next". 
He will select any business term in the new table, put a type code and clicks on "Create" button. Then repeat the whole process for the same BIE with the same business term and using the same type code. Verify that the second time he clicks on the "Create" button, an error message will pop up: Invalid parameters, Another business term 
assignment for the same BIE and type code already exists!. (Assertion [#8](#test-assertion-4228)).
14. The end user goes back to "Business Term Assignments" from the BIE dropdown list. 
15.He clicks on the "Assign Business Term" button. Then select any BIE in the table and click "Next". 
He will select any business term in the new table, put a type code and clicks on "Create" button. Then repeat the whole process for the same BIE with the same business term and using a different type code. Verify that the second time he clicks on the "Create" button, the assignment is created. (Assertion [#9](#test-assertion-4229)).


## Test Case 42.3

**Business Term from BIE detail page**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #42.3.1
From View/Edit BIE page, select the desired BIE, select the desired node under the BIE, the end user can click the button "Show Business Terms" to view all the business term assignments for that node. 

#### Test Assertion #42.3.2

From View/Edit BIE page, select the desired BIE, select the desired node under the BIE, the end user can hover over the button "Show Business Terms" without clicking, the business term assignments for that node will show up. 

#### Test Assertion #42.3.3

From View/Edit BIE page, select the desired BIE, select the desired node under the BIE, the end user can click the button "Assign New Business Term" to assign new business terms to that node. 


### Test Step Pre-condition:
1. There are at least three BIEs created by end user and developer and they are in different states (WIP, QA, Production). 
2. There are at least five business terms created. Three of them are assigned to different BIEs. At least Two of them are not assigned to any BIEs. 


### Test Step:
1. An end user logins.
2. He visits the Home page and clicks on the BIE tab.
3. He clicks on the "View/Edit BIE" from the dropdown list.
4. He clicks on any BIE from the table. The BIE detail page will open.
5. He expands the root level in the left sidenav tree and clicks on any used node in the second level, the detail for that node will disply on the right.
6. He will click on "Show Business Terms" button to view all the business terms that already assigned to that node.
7. Verify that the list of business terms in the new window is indeed assigned to the selected node (Assertion [#1](#test-assertion-4231)).
8. He closes the new window and goes back to previous window.
9. He will hover over "Show Business Terms" button without clicking. A hint bar will display to show the number of business terms assigned to the node. Verify the number is the same as in the previosu step. (Assertion [#2](#test-assertion-4232)).
10. He will clicks on the "Assign New Business Term" button right next to "Show Business Term" button on the same page.
11. He will select any availabe business term in the new window,  set the Type Code, check or uncheck the Preferred Business term. Then clicks on "Create".
12. He will close the new window and go back to previous window. 
13. He will hover over "Show Business Terms" button without clicking again. Verify the number in the hint bar is increased by one. (Assertion [#3](#test-asssertion-4233)).


## Test Case 42.4

**Business Term from External Source**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #42.4.1
Under BIE, find View/Edit Business Term,  the end user can click "Upload Business Terms", and then they can click the "Download template" from the "Upload Business Terms from File" page.

#### Test Assertion #42.4.2

Under BIE, find View/Edit Business Term,  the end user can click "Upload Business Terms", and then they can click on the Attach button and choose the file from the computer, and the file should be uploaded. 

#### Test Assertion #42.4.3

For bulk upload through "Upload Business Terms", if the business term is uploaded with new externalReferenceUri, a new business term will be created. 

#### Test Assertion #42.4.4

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
7. Verify that all the business terms in the uploaded csv file are listed in the table. (Assertion [#2](#test-assertion-4242)).
8. Verify that a new business term is created in the table if the external reference uri is new in the uploaded csv file. (Assertion [#3](#test-assertion-4243)).
9. Verify that an existent business term is updated with the new information if the external reference uri is the same as the exitent one in the uploaded csv file. 
(Assertion [#4](#test-assertion-4244)).
10. Re-upload the same csv file for a second time. Verify that no more changes in all business terms. 
