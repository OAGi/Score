# Test Suite 5

> OAGIS developer access right to Score core functions


## Test Case 5.1

> OAGIS developer's authorized management of context categories

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #5.1.1
The developer can create a context category with only required information. See Create a Context Category in Score User Guide for Mandatory/Optional fields.

#### Test Assertion #5.1.2
The developer can create a context category with all information specified.

#### Test Assertion #5.1.3
The developer cannot create a context category with missing required information.

#### Test Assertion #5.1.4
The developer can see, in the context category list, all context categories created by any user.

#### Test Assertion #5.1.5
The developer can see/edit a context category created by any user.

#### Test Assertion #5.1.6
The developer can update a context category with only required information.

#### Test Assertion #5.1.7
The developer can update a context category with all information specified.

#### Test Assertion #5.1.8
The developer cannot update a context category with missing required information.

#### Test Assertion #5.1.9
The developer can discard context categories created by any user provided that there is no context scheme referencing it.

#### Test Assertion #5.1.10
The developer cannot discard a context category that has a context scheme referencing it. The system shall indicate context schemes referencing the context category when the developer tries to delete the context category.

#### Test Assertion #5.1.11
The search feature is at least working.

#### Test Assertion #5.1.12
The developer can select a Context Category from the Context Category List page, navigate through the pages of the paginator while the forenamed Context Category remains selected.

### Test Step Pre-condition:

1. There are context categories already created by various end users, say CATa and CATb, and also created developers, CATx and CATy, with all fields populated. CATa is already used by a context scheme. CATx has the name “Business Process Context”. There is also the context category “Business Search Process” created by the devx developer.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer creates a new context category, say CAT0, specifying only the Name field.
3. Verify that CAT0 exists on the “Context Category” page. (Assertion [#1](#test-assertion-511))
4. The developer creates a new context category, say CAT1, specifying the Name field and Description field.
5. The developer opens CAT1 to view its detail. Verify that both the Name and Description are correct. (Assertion [#2](#test-assertion-512))
6. The developer tries to create a new context category, say CAT3, specifying only the Description field.
7. Verify that the “Create” button is disabled. (Assertion [#3](#test-assertion-513))
8. The developer visits the “Context Category” page.
9. Verify that the developer can see in the list CATa, CATb, CATx, CATy, CAT0 and CAT1 (Assertion [#4](#test-assertion-514))
10. Verify that the developer can open a CATa and that it is editable. (Assertion [#5](#test-assertion-515))
11. The developer visits the “Context Category” page again.
12. Verify that the developer can open CATx and that it is editable. (Assertion [#5](#test-assertion-515))
13. The developer removes all the content in the Description field of CATx and click “Update”.
14. Verify that both the Description content in the “Context Category” page match the input value of the previous change. (Assertion [#6](#test-assertion-516))
15. The developer opens a context category CATa.
16. The developer changes both the Name and Description and click “Update”.
17. Verify that both the Name and Description contents in the “Context Category” page and in the “Context Category Detail” page match the new input values. (Assertion [#7](#test-assertion-517))
18. The developer opens CATb.
19. The developer removes the content in the Name field and tries to click “Update” button.
20. Verify that the “Update” button is disabled. (Assertion [#8](#test-assertion-518))
21. The developer opens CATb.
22. Clicks “Discard”.
23. Verify that CATb does not shown up on the “Context Category” page. (Assertion [#9](#test-assertion-519))
24. The developer opens CATy.
25. Clicks “Discard”.
26. Verify that CATy does not shown up on the “Context Category” page. (Assertion [#9](#test-assertion-519))
27. The developer opens CATa.
28. Clicks “Discard”.
29. Verify that the system indicates that the context category cannot be discarded and that it is still listed on the “Context Category” page. Also verify that the checkbox located in front of the CATa in the “Context Category” page is disabled.  (Assertion [#10](#test-assertion-5110))
30. The developer visits the “Context Category” page.
31. Developer enters the search term “usiness” that is partially match some names of the context categories into the Name field.
32. Verify that at least CATx is returned. (Assertion [#11](#test-assertion-5111))
33. The developer enters the search term “searchDesc” that is partially match some names of the context categories into the Description field.
34. Verify that at least CATa is returned. (Assertion [#11](#test-assertion-5111))
35. The developer enters the term “business process” to the Name field.
36. Verify that at least the “Business Process Context” and the “Business Search Process” are returned. (Assertion [#11](#test-assertion-5111))
37. The developer enters the term “”business process”” to the Name field.
38. Verify that the “Business Process Context” is returned but not the “Business Search Process”. (Assertion [#11](#test-assertion-5111))
39. The developer visits the Context Category page, he selects a Context Category (clicking the corresponding checkbox), goes to the next paginator pages and then returns back.
40. Verify that the checkbox of the selected Context Category is checked. (Assertion [#12](#test-assertion-5112))

## Test Case 5.2

> OAGi developer's authorized management of context schemes

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #5.2.1
The developer can create a context scheme with only required information. See the Score User Guide for Mandatory/Optional fields.

#### Test Assertion #5.2.2
The developer can create a context scheme with all information specified.

#### Test Assertion #5.2.3
Adding and removing scheme values on the creation page works.

#### Test Assertion #5.2.4
The developer cannot create a context scheme with missing required information including those of a context scheme value.

#### Test Assertion #5.2.5
The developer cannot create a context scheme when the uniqueness requirement is not met. See the design document for the uniqueness requirement.

#### Test Assertion #5.2.6
The application gives correct warning when the developer tries to create a context scheme with the Scheme ID and Agency ID that are the same as those off an existing context scheme but gives a different Name.

#### Test Assertion #5.2.7
The developer can see, in the context scheme list, all context schemes created by any user.

#### Test Assertion #5.2.8
The developer can edit a context scheme created by any user.

#### Test Assertion #5.2.9
The developer can update a context scheme with only required information.

#### Test Assertion #5.2.10
The developer can update a context scheme with all information specified.

#### Test Assertion #5.2.11
The developer cannot update a context scheme with missing required information.

#### Test Assertion #5.2.12
The developer can discard context schemes created by any user provided that there is no business context referencing it.

#### Test Assertion #5.2.13
The developer cannot discard a context scheme that has a business context referencing it. The system shall indicate business contexts referencing the context category when the developer tries to delete the context scheme. Checkbox is disabled.

#### Test Assertion #5.2.14
The developer can update a context scheme created by any user, even when there is already a business context referencing it. Also, verify that the respective business context is updated accordingly.

#### Test Assertion #5.2.15
The developer cannot remove a Context Scheme value if it is used by a Business Context.

#### Test Assertion #5.2.16
The developer cannot add a duplicate context scheme value.

#### Test Assertion #5.2.17
The search feature is at least working.

#### Test Assertion #5.2.18
The developer can add to a context scheme code list values from Published or Production Code Lists. (The code values are simply copied into the context scheme). The fields Scheme ID, Agency ID and Version can be still changed. The developer shall be notified that existing values will be removed. Test for: 

##### Test Assertion #5.2.18.a
Developer code list in the latest release
##### Test Assertion #5.2.18.b
Developer code list in an older release
##### Test Assertion #5.2.18.c
End user code list the latest release that is derived from a developer code list
##### Test Assertion #5.2.18.d
End user code list in an older release that is derived from another end user code list

#### Test Assertion #5.2.19
The developer can add to a context scheme code list values from Production End User Code Lists. Test for a non-latest release and the latest release. The fields Scheme ID, Agency ID and Version can be still changed.

#### Test Assertion #5.2.20
The developer can change the context scheme values added by a code list.

#### Test Assertion #5.2.21
The developer can add a value to a context scheme after he has loaded values from a code list.

#### Test Assertion #5.2.22
The developer can delete a value from a context scheme added by a selected code list.

#### Test Assertion #5.2.23
The developer cannot use the “Load from code list” function, if a value of this Context Scheme is used by a Business Context.

#### Test Assertion #5.2.24
The developer can select a Context Scheme from the Context Scheme List page, navigate thought different paginator pages while the forenamed Context Scheme remains checked.

### Test Step Pre-condition:

1. There are context schemes already created by various end users, say CSa and CSb, and also created by developers, namely CSx and CSy, with all fields populated. CSa and CSx are already used by a business context. CSb has the name “Business Process Context Schema”. CSa has a value named “csaValue” which is used by a Business Context, say BCa.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer creates new context schemes, say CS0 and CS1, with the Context Category, Name, Scheme ID, Agency ID, and Version specified with some texts, while leaving the Description field blank. Ensure that both CS0 and CS1 use context categories created by different users. Add some scheme values to CS1 (but not CS0) without specifying the meaning.
3. Verify that both CS0 and CS1 are successfully recorded by the application by verifying that their values exists both in the “Context Schemes” page and “Context Scheme Detail” page. (Assertion [#1](#test-assertion-521))
4. The developer creates a new context scheme, say CS2, with all data fields specified. Use a context category created by the developer himself. Add some scheme values with the meaning specified. During the creation, also do some scheme value deletions. Moreover, he tries to add a value that already exists.
5. Verify that CS2 is successfully recorded by the application and that he could not add one value more than one times. (Assertion [#2](#test-assertion-522), [#3](#test-assertion-523), [#16](#test-assertion-5216))
6. The developer tries to create a new context scheme with all fields specified except the Context Category field.
7. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-524))
8. The developer tries to create a new context scheme with all fields specified except the Name field.
9. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-524))
10. The developer tries to create a new context scheme with all fields specified except the Scheme ID field.
11. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-524))
12. The developer tries to create a new context scheme with all fields specified except the Agency ID field.
13. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-524))
14. The developer tries to create a new context scheme with all fields specified except the Version field.
15. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-524))
16. The developer tries to create a new context scheme with all fields specified by trying to add a new context scheme value with all fields specified except the value field.
17. Verify that the “Add” button value is disabled. (Assertion [#4](#test-assertion-524))
18. The developer tries to create a new context scheme by giving the Scheme ID, Agency ID, and Version the same as those of CS0.
19. Verify that the application indicates an error message to the extent that the same context scheme version cannot be created. (Assertion [#5](#test-assertion-525))
20. The developer creates a new context scheme and gives the Name, Scheme ID, Agency ID the same as those of CS0, while keeping the Version different.
21. Verify that the application gives a warning message to the extent that the user is creating a context scheme with the same attributes while giving a different name and that he/she should confirm his/her intention. Also verify that the new context scheme is successfully created. (Assertion [#5](#test-assertion-525))
22. The developer creates a new context scheme and gives the Scheme ID and Agency ID the same as those of CS0, while keeping the Name and Version different.
23. Verify that the application gives a warning message to the extent that the user is creating a context scheme with the same Scheme ID and Agency ID while giving a different name and that he/she should confirm the intention. Also verify that the new context scheme is successfully created. (Assertion [#6](#test-assertion-526))
24. The developer visits the Context Scheme page.
25. Verify that he can see in the list CS0, CS1, CS2,CS3, CSa, CSb, CSx, and CSy. (Assertion [#7](#test-assertion-527))
26. The developer clicks to view CSa.
27. Verify that fields are editable including those of its values, and that the Update and Discard buttons are present. (Assertion [#8](#test-assertion-528))
28. Go back to the Context Scheme page.
29. The developer clicks to view CSy.
30. Verify that fields are editable including those of its values, and the Update and Discard buttons are present. (Assertion [#8](#test-assertion-528))
31. The developer deletes all scheme values and Description so that only mandatory field is present; and click Update.
32. Verify that the context scheme has been successfully updated with the new content. (Assertion [#9](#test-assertion-529))
33. The developer opens CS0.
34. The developer populates all the data fields, adds some context scheme values to CS0 and clicks Update. Moreover, he tries to add a value that already exists.
35. Verify that CS0 is successfully updated with the new content and that he could not add a value that already exists. (Assertion [#10](#test-assertion-5210), [#16](#test-assertion-5216))
36. The developer opens CS0.
37. Changes the content of some fields of some context scheme values and clicks Update.
38. Verify that CS0 is successfully updated with the new content. (Assertion [#10](#test-assertion-5210))
39. The developer tries to update CS0 with all fields specified except the Name field.
40. Verify that the Update button is disabled. (Assertion [#11](#test-assertion-5211))
41. The developer tries to update CS0 with all fields specified except the Scheme ID field.
42. Verify that the Update button is disabled. (Assertion [#11](#test-assertion-5211))
43. The developer tries to update CS0 with all fields specified except the Agency ID field.
44. Verify that the Update button is disabled. (Assertion [#11](#test-assertion-5211))
45. The developer tries to update CS0 with all fields specified except the Version field.
46. Verify that the Update button is disabled. (Assertion [#11](#test-assertion-5211))
47. The developer tries to update CS0 with all fields specified except the Value field of a Context Scheme Value.
48. Verify that the Edit button of the value is disabled. (Assertion [#11](#test-assertion-5211))
49. The developer discards CS0 (which should have no business context using it).
50. Verify that CS0 is no longer on the Context Scheme page. (Assertion [#12](#test-assertion-5212))
51. The developer opens CSx.
52. The developer tries to discard CSx.
53. Verify that the application shows an error message and that CSx still exists on the Context Scheme page. (Assertion [#13](#test-assertion-5213))
54. The developer opens CSa and makes some valid updates.
55. Verify that CSa is successfully updated. Also, verify that the corresponding value of the BCa has been updated (i.e., the content of the Context Scheme and Context Scheme values fields has been changed) (Assertion [#14](#test-assertion-5214))
56. The developer opens CSa.
57. The developer tries to discard a context scheme value of the CSa which is used by a Business Context.
58. Verify that the application shows an error message and that this value still exists. (Assertion [#15](#test-assertion-5215))
59. The developer goes to the Context Scheme page and enter the keyword “usera” to the search drop-down box of the “Updater” search field.
60. Verify that the user “usera” appears only. Also verify that “oagi”, “devx” and the “userb” users do not appear. (Assertion [#17](#test-assertion-5217))
61. The developer clears the field and enters an all lowercase search term “process”.
62. Verify that CSb is returned in the search result. (Assertion [#17](#test-assertion-5217))
63. The developer clears the search filters and he choose devx as value of the Updater selector.
64. Verify that CSx is returned in the search result. (Assertion [#17](#test-assertion-5217))
65. The developer clears the search filters and he choose today's date as value of the Updated start date selector.
66. Verify that CSx is returned in the search result. (Assertion [#17](#test-assertion-5217))
67. The developer clears Name field and enters the term “Process Context”.
68. Verify that at least the “CSBusiness Process Schema Context” and the “CSBusiness Process Context Schema” are returned.  (Assertion [#17](#test-assertion-5217))
69. The developer clears Name field and enters the term ““Process Context””.
70. Verify that at least the “CSBusiness Process Context Schema” is returned but not the “CSBusiness Process Schema Context”.  (Assertion [#17](#test-assertion-5217))
71. The developer opens the Context Scheme CSy and clicks on the “Code List” field to select a code list to import values from.
72. He enters the keyword “dateformat” into the search drop-down list field.
73. Verify that the clm6DateFormatCode1_DateFormatCode appears but not the clm6TimeFormatCode1_TimeFormatCode. (Assertion [#17](#test-assertion-5217))
74. The developer creates a Code List, say CL1CodeList, and adds some values into it.
75. He creates a new context scheme, say cscl1, and he chooses the code list clm6ConditionTypeCode1_ConditionTypeCode to add values from.
76. Verify that he cannot choose the CL1CodeList as it is in Editing state. (Assertion [#18](#test-assertion-5218))
77. Verify that he cannot change the fields “Scheme ID”, “Agency ID” and “Version” as he has selected a code list to import values from. (Assertion [#19](#test-assertion-5219))
78. He creates the context scheme by clicking the “Create” button.
79. Verify that the cscl1 was successfully created and that it contains all the values of the code list clm6ConditionTypeCode1. Also verify that the Agency ID and the Version are automatically stored, and they are the same as the Code list clm6ConditionTypeCode1_ConditionTypeCode. (Assertion [#18](#test-assertion-5218))
80. The developer opens the context scheme cscl1 and tries to edit a value.
81. Verify that he cannot click on a value and that the dialog box used for editing values is not displayed. (Assertion [#19](#test-assertion-5219))
82. The developer tries to add a new value.
83. Verify that the Add button is disabled. (Assertion [#20](#test-assertion-5220))
84. He tries to remove a value.
85. Verify that the checkbox used for deleting context scheme values is disabled. (Assertion [#21](#test-assertion-5221))
86. The developer opens CSa.
87. Verify that both the checkbox “Import from Code List” and the field where you can select a code list are disabled. (Assertion [#22](#test-assertion-5222))
88. The developer opens BCa and adds a new value from cscl1.
89. He opens cscl1 and tries to uncheck the checkbox “Import from Code List” so that he can add more values.
90. Verify that the checkbox “Import from Code List” is disabled. (Assertion [#22](#test-assertion-5222))
91. The developer tries to select another code list to add values from.
92. Verify that he cannot select another code list by verifying that the mat-select button used to select a code list is disabled. (Assertion [#22](#test-assertion-5222))
93. The developer opens BCa and removes all its values.
94. The developer opens cscl1and use the “Import from Code List” checkbox and adds values from a different code list.
95. Verify that the cscl1 updated with the new values. (Assertion [#22](#test-assertion-5222))
96. The developer opens cscl1, unchecks the "Import from Code List” and adds some values manually.
97. Verify that the cscl1 updated with the new values. (Assertion [#22](#test-assertion-5222))
98. The developer opens CSa and tries to add values from another Code List.
99. Verify that the “Import from Code List” checkbox is disabled. (Assertion [#23](#test-assertion-5223))
100. The developer removes all values and then adds values from a Code List. Afterwards, he updates the CSa.
101. Verify that the CSa has been updated successfully. (Assertion [#23](#test-assertion-5223))
102. The developer creates a new Business Context and uses a value of the Context Scheme CSa.
103. He opens the Context Scheme CSa and tries to import values by selecting a different Code list.
104. Verify that the selector field used for selecting the Code List to import values from is disabled. (Assertion [#24](#test-assertion-5224))
105. The developer visits the Context Scheme List page, he selects a Context Scheme (clicking the corresponding checkbox), goes to the next paginator pages and then returns back.
106. Verify that the checkbox of the selected Context Scheme is checked. (Assertion [#25](#test-assertion-5225))

## Test Case 5.3

> OAGi developer's authorized management of business contexts

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #5.3.1
The OAGi developer can create a business context with only required information. See Create a Business Context in Score User Guide for Mandatory/Optional fields.

#### Test Assertion #5.3.2
The developer can remove a context value during the business context creation.

#### Test Assertion #5.3.3
The OAGi developer can create a business context with all information specified including those of the business context value table.

#### Test Assertion #5.3.4
The OAGi developer cannot create a business context with missing required information.

#### Test Assertion #5.3.5
The developer can see, in the business context list, all business context created by any user.

#### Test Assertion #5.3.6
The OAGi developer can edit a business context created by any user.

#### Test Assertion #5.3.7
The developer can update a business context with only required information.

#### Test Assertion #5.3.8
The developer can update a business context with all information specified.

#### Test Assertion #5.3.9
The developer cannot add a duplicate context value.

#### Test Assertion #5.3.10
The developer cannot update a business context with missing required information.

#### Test Assertion #5.3.11
The developer can discard a business context created by any user provided that there is no BIE referencing it.

#### Test Assertion #5.3.12
The developer cannot discard a business context that a BIE is referencing it. The Discard button should be disable and there should be a tooltip on it providing the reason it cannot be deleted or what actions are needed to discard the BC.

#### Test Assertion #5.3.13
The developer can update a business context created by any user, even when there is already a BIE referencing it. Verify that the business context name of the respective BIE has been updated accordingly.

#### Test Assertion #5.3.14
The search feature is at least working including the exact match feature.

#### Test Assertion #5.3.15
The developer can select a Business Context from the Business Context List page, navigate thought different paginator pages while the forenamed Business Context remains checked.

#### Test Assertion #5.3.16
Verify that all the business context values (i.e., their Context Category, Context Scheme and Context Scheme Value) are listed in the Edit Business Context page.

#### Test Assertion #5.3.17
In the dialog used for adding a business context value, verify that all the details of the Context Category, Context Scheme and Context Scheme Value are displayed.

### Test Step Pre-condition:

1. There are business contexts already created by various end users, say BCa and BCb, and also created by developers, namely BCx and BCy, with all fields populated. BCa and BCx are already used by a BIE. BCa has the name “Business Process Business Context”.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer creates new business contexts, say BC0 and BC1, with the Name field specified. Also, he adds some business context values with all their fields specified to BC0 (but not to BC1). Moreover, he tries to add a new value which is the same as an existing one.
3. Verify that the BC0 and BC1 are successfully recorded by the application and that he couldn’t add a duplicate value. (Assertion [#1](#test-assertion-531), [#9](#test-assertion-539))
4. The developer creates a new business context, say BC2, with the Name field specified, while adding some business context values and removing some of them during the business’s context creation.
5. Verify that the business context is successfully recorded by the application (Assertion [#2](#test-assertion-532))
6. The developer creates a new business context, say BC3, with the Name field specified and adds some business context values, with all their fields specified.
7. Verify that the business context is successfully recorded by the application. (Assertion [#3](#test-assertion-533))
8. The developer tries to create a new business context leaving the Name field bank and without adding any business context values.
9. Verify that the Create button is disabled. (Assertion [#4](#test-assertion-534))
10. The developer tries to create a new business context, leaves the Name field blank and adds a business context value with all its fields specified.
11. Verify that the Create button is disabled. (Assertion [#4](#test-assertion-534))
12. The developer tries to create a new business context with the Name field specified and adds a business context value without any field specified.
13. Verify that the Add button is disabled. (Assertion [#4](#test-assertion-534))
14. The developer tries to create a new business context with the Name field specified and adds a value with all its fields specified except the context scheme value field.
15. Verify that the Add button is disabled. (Assertion [#4](#test-assertion-534))
16. The developer tries to create a new business context with the Name field specified and adds a value with only the context category field specified.
17. Verify that the Add button is disabled. (Assertion [#4](#test-assertion-534))
18. The developer visits the Business Context page.
19. Verify that he can see in the list BC0, BC1, BC2, BC3, BCa, BCb, BCx, and BCy. (Assertion [#5](#test-assertion-535))
20. The developer clicks to view BCa.
21. Verify that all fields are editable, and the Update button is present. (Assertion [#6](#test-assertion-536))
22. Go back to the Business Context page.
23. The developer clicks to view BC1.
24. Verify that fields are editable, and the Update button is present. (Assertion [#6](#test-assertion-536))
25. Go back to the Business Context page.
26. The developer clicks to view BC0.
27. Verify that fields are editable, and the Update button is present. (Assertion [#6](#test-assertion-536))
28. Go back to the Business Context page.
29. The developer clicks to view BCx.
30. Verify that fields are editable, and the Update button is present. (Assertion [#6](#test-assertion-536))
31. The developer changes the Name of the BC1 and clicks Update.
32. Verify that the BC1 has been successfully updated with the new content. (Assertion [#7](#test-assertion-537))
33. The developer clicks to view BCx.
34. The developer deletes all business context values so that only the mandatory Name field is present and clicks Update.
35. Verify that the BCx has been successfully updated with the new content. (Assertion [#7](#test-assertion-537))
36. The developer opens BC0.
37. He changes the content of the Name field, adds some business context values with all their fields specified and clicks Update.
38. Verify that BC0 is successfully updated with the new content. (Assertion [#8](#test-assertion-538))
39. The developer opens BC0.
40. He tries to add a duplicate value, namely a value that has the same Context Category, Context Scheme and Context Scheme value with an existing one.
41. Verify that the new value was not added. (Assertion [#9](#test-assertion-539))
42. The developer clicks to view BC0.
43. The developer removes the content of the Name field and tries to click update.
44. Verify that the Update button is disabled. (Assertion [#10](#test-assertion-5310))
45. The developer undoes the changes of the previous step and tries to add a business context value without specifying its context.
46. Verify that the Add value button is disabled. (Assertion [#10](#test-assertion-5310))
47. The developer undoes the changes of the previous step and tries to add a business context value without specifying its context scheme value.
48. Verify that the Add value button is disabled. (Assertion [#10](#test-assertion-5310))
49. The developer undoes the changes of the previous step and tries to add a business context value with no field specified.
50. Verify that the Add value button is disabled. (Assertion [#10](#test-assertion-5310))
51. The developer discards BC0.
52. Verify that BC0 is no longer on the Business Context page. (Assertion [#11](#test-assertion-5311))
53. Go back to the Business Context page.
54. The developer clicks to view BCb.
55. The developer discards BCb.
56. Verify that BCb is no longer on the Business Context page. (Assertion [#11](#test-assertion-5311))
57. Go back to the Business Context page.
58. The developer clicks to view BCa.
59. The developer tries to discard BCa.
60. Verify that the there is a tooltip providing the reason that the BCa cannot be discarded. (Assertion [#12](#test-assertion-5312))
61. The developer clicks to view BCx.
62. The developer opens BCx, change the content of the Name field and clicks update.
63. Verify that BCx is successfully updated. Also, verify that the change is reflected to the BIE that uses the BCx (Assertion [#13](#test-assertion-5313))
64. The developer visits the Business Context page.
65. He enters the keyword “userb” to the search drop-down box of the “Updater” search field.
66. Verify that the user “userb” appears only. Also verify that “oagi”, “devx” and “usera” users do not appear.
67. The developer clears everything, and he enters the search term “usiness” that is partially match some names of the business contexts.
68. Verify that at least BCa is returned. (Assertion [#14](#test-assertion-5314))
69. The developer clears the search filters and he choose “oagis” as value of the Updater selector.
70. Verify that BCx is returned in the search result. (Assertion [#14](#test-assertion-5314))
71. The developer clears the search filters and he choose today’s date as value of the Updated start date.
72. Verify that BCx is returned in the search result. (Assertion [#14](#test-assertion-5314))
73. The developer clears all fields and enters the term “Process Business” to the Name field.
74. Verify that at least the BCa and the BCs are returned. (Assertion [#14](#test-assertion-5314))
75. He clears the field and enters the term “”Process Business””.
76. Verify that the BCa is returned but not the BCs. (Assertion [#14](#test-assertion-5314))
77. The developer visits the Business Context List page, he selects a Business Context (clicking the corresponding checkbox), goes to the next paginator pages and then returns back.
78. Verify that the checkbox of the selected Business Context is checked. (Assertion [#15](#test-assertion-5315))

## Test Case 5.4

> Retired in 2.0-OAGi developer authorized access to code list management functions.

Pre-condition: N/A
There is an entire code list management test suite that replaces this test case.


### Test Assertion:

#### Test Assertion #5.4.1
The developer can create a code list without base with only required information. See Create a Brand New Code List in Score User Guide for Mandatory/Optional fields. In addition, Code list value cannot be duplicated.

#### Test Assertion #5.4.2
The developer can create a code list without base with all information specified and multiple code values.

#### Test Assertion #5.4.3
The developer can remove a code value during the code list without base creation.

#### Test Assertion #5.4.4
The developer cannot create a code list without base with missing required information including missing required information in the code value table.

#### Test Assertion #5.4.5
The developer cannot create a code list without base, when it does not meet a uniqueness constraint. See Create a Brand New Code List in Score User Guide for the uniqueness constraint.

#### Test Assertion #5.4.6
The developer can see, in the code list page, all code lists created by any user.

#### Test Assertion #5.4.7
The developer has access to edit an unpublished code list created by any user.

#### Test Assertion #5.4.8
The developer can view the detail of a published code list created by any user.

#### Test Assertion #5.4.9
The developer cannot update a published code list created by any user.

#### Test Assertion #5.4.10
The developer can update a code list without base, which has only required information including adding and removing code values.

#### Test Assertion #5.4.11
The developer can update a code list without base that has all information specified including adding and removing code values.

#### Test Assertion #5.4.12
The developer cannot update a code list without base that has missing required information including missing required information in the code value.

#### Test Assertion #5.4.13
The developer can publish a previously saved code list without base, which has only required information.

#### Test Assertion #5.4.14
The developer can publish a previously saved a code list without base with all information specified including those in code values.

#### Test Assertion #5.4.15
The developer cannot publish a previously saved code list without base that has missing required information including missing required information in the code value.

#### Test Assertion #5.4.16
Only published code lists without base can be used in BIE (and CC – we will not test for CC at this time as CC Editing function is not ready).

#### Test Assertion #5.4.17
The developer cannot discard a code list once it has been published.

#### Test Assertion #5.4.18
The developer can create a code list with base that has only required information. See Create a Brand New Code List in Score User Guide for Mandatory/Optional fields.

#### Test Assertion #5.4.19
The developer can create a code list with base with all information specified and with some code value restrictions and extensions added. The developer cannot change any field of the inherited code value.

#### Test Assertion #5.4.20
The developer can remove a code value during the code list with base creation.

#### Test Assertion #5.4.21
The developer cannot create a code list with base with missing required information including missing required information in the code value table.

#### Test Assertion #5.4.22
The developer cannot create a code list with base, when it does not meet a uniqueness constraint. See Create a Brand New Code List in Score User Guide for the uniqueness constraint.

#### Test Assertion #5.4.23
The developer can update a code list with base that has only required information including adding, removing and restricting, unrestricting code values. The developer cannot change any field of the inherited code value.

#### Test Assertion #5.4.24
The developer can update a code list with base that has all information specified including adding, removing, restricting, unrestricting code values. The developer cannot change any field of the inherited code value.

#### Test Assertion #5.4.25
The developer cannot update a code list with base that has missing required information including missing required information in the code value.

#### Test Assertion #5.4.26
The developer can publish a previously saved a code list with base.

#### Test Assertion #5.4.27
The developer cannot publish a previously saved code list with base that has missing required information including missing required information in the code value.

#### Test Assertion #5.4.28
Only published code lists with base can be used in BIE (and CC – we will not test for CC at this time as CC Editing function is not ready).

#### Test Assertion #5.4.29
Restricted code value in the base code list is displayed but cannot be enabled.

#### Test Assertion #5.4.30
The search feature is at least working.

#### Test Assertion #5.4.31
Only extensible code list can be a base code list.

#### Test Assertion #5.4.32
Unpublished code list cannot be used as a base.

#### Test Assertion #5.4.33
A confirmation message should be returned when creating a Code List with the same name as another Code List.

#### Test Assertion #5.4.34
A confirmation message should be returned when updating a Code List specifying a name which is the same with another Code List.

#### Test Assertion #5.4.35
The developer can select a Code List from the Code List page, navigate thought different paginator pages while the forenamed Code List remains checked.

#### Test Assertion #5.4.36
The developer can select a Code List Value from a Code List, navigate thought different paginator pages storing the code lists values while the forenamed Code List value remains checked.

### Test Step Pre-condition:

1. There are code lists already created by various end users, namely CLa and CLb, and also created by developers, namely CLx and CLy. CLa and CLx are in Editing state and they have all their fields specified including those of their code list values, whilst CLb and CLy are in Published state. CLx has the name “Code List Test Editing” and CLy has the name “Code List Testing”.
2. There is a code list that has all required information specified and the switch “Extensible” is not enabled so it cannot be extended. The name of the code list is “Non Extensible Code List” and it is in the Published state.
3. There is a BIE, namely BIEa, in Editing state and uses a node whose Primitive Type field can be set.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer creates new code lists without base, say CL0 and CL1, with the Name, List ID, Agency ID, Version specified, while the Definition, Definition Source, Remark and the Extensible switch fields left blank. Moreover, he adds some code list values to CL0 (but not to CL1) with only their Code and Short Name fields specified. Also, he tries to add a new value with its properties same as an existing value.
3. Verify that both CL0 and CL1 are successfully recorded by the application and that he couldn’t add a duplicate value to CL0. (Assertion [#1](#test-assertion-541))
4. The developer creates a new code list without base, say CL2, with all data fields specified. Moreover, he adds multiple code list values with all their fields specified and he deletes some of these code list values during the creation of the code list.
5. Verify that the code list is successfully recorded by the application. (Assertion [#2](#test-assertion-542), [#3](#test-assertion-543))
6. The developer tries to create a code list without base with all fields specified except the Name field.
7. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-544))
8. The developer tries to create a code list without base with all fields specified except the List ID field.
9. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-544))
10. The developer tries to create a code list without base with all fields specified except the Agency ID field.
11. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-544))
12. The developer tries to create a code list without base with all fields specified except the Version field.
13. Verify that the “Create” button is disabled. (Assertion [#4](#test-assertion-544))
14. The developer tries to create a code list without base with all fields specified, adds a new code list value with all its fields specified except the Code Field.
15. Verify that the “Add” button is disabled. (Assertion [#4](#test-assertion-544))
16. The developer tries to create a code list without base with all fields specified, adds a new code list value with all its fields specified except the Short Name Field.
17. Verify that the “Add” button is disabled. (Assertion [#4](#test-assertion-544))
18. The developer creates a code list without base, say CL3, with only the required field specified.
19. The developer creates a code list without base, say CL4 specifying only the required field but the List ID, Agency ID and Version fields are the same as those of the CL3.
20. Verify that the application indicates an error and that the new code list is not recorded By verifying that the CL4 is not displayed in the “Code Lists” page. (Assertion [#5](#test-assertion-545))
21. The developer visits the Code List page.
22. Verify that he can see in the list CL0, CL1, CL2, CLa, CLb, CLx, and CLy. (Assertion [#6](#test-assertion-546))
23. The developer clicks to view CLa.
24. Verify that all fields are editable, and the Update button is present. (Assertion [#7](#test-assertion-547))
25. The developer goes back to the Code List page.
26. Clicks to view CLx.
27. Verify that all fields are editable, and the Update button is present. (Assertion [#7](#test-assertion-547))
28. The developer goes back to the Code List page.
29. Clicks to view CLb.
30. Verify that no field is editable and that the Update, Discard and Publish buttons are not displayed. (Assertion [#8](#test-assertion-548), [#9](#test-assertion-549))
31. The developer goes back to the Code List page.
32. Clicks to view CLy.
33. Verify that no field is editable, and the Update, Discard and Publish buttons are displayed. (Assertion [#8](#test-assertion-548), [#9](#test-assertion-549))
34. Go back to the Code List page.
35. The developer clicks to view CL0.
36. The developer changes the content of the Name, List ID, Agency ID and Version fields, adds some code list values with only the Code and Short Name fields specified, removes some of these values and clicks Update. Also, he tries to add a new value with its properties same as an existing value.
37. Verify that CL0 is successfully updated with the new content and that he cannot add a duplicate value (Assertion [#7](#test-assertion-547), [#10](#test-assertion-5410))
38. The developer clicks to view CL2.
39. The developer changes the content of all data fields including those of the Code List value table. Moreover, he adds and removes some code list values and clicks Update.
40. Verify that CL2 is successfully updated with the new content. (Assertion [#11](#test-assertion-5411))
41. The developer clicks to view CLx.
42. The developer deletes the content of the Name field and clicks Update.
43. Verify that the “Update” button is disabled. (Assertion [#12](#test-assertion-5412))
44. The developer undoes the changes of the previous step, deletes the content of the List ID field and tries to click Update.
45. Verify that the “Update” button is disabled. (Assertion [#12](#test-assertion-5412))
46. The developer undoes the changes of the previous step, deletes the content of the Agency ID field and tries to click Update.
47. Verify that the “Update” button is disabled. (Assertion [#12](#test-assertion-5412))
48. The developer undoes the changes of the previous step, deletes the content of the Version field and tries to click Update.
49. Verify that the “Update” button is disabled. (Assertion [#12](#test-assertion-5412))
50. The developer undoes the changes of the previous step, deletes the content of the Code field of a Code List value and tries to click Edit.
51. Verify that the “Edit” button is disabled. (Assertion [#12](#test-assertion-5412))
52. The developer undoes the changes of the previous step, deletes the content of the Short Name field of the Code List value and tries to click Edit.
53. Verify that the “Edit” button is disabled. (Assertion [#12](#test-assertion-5412))
54. The developer goes back to the Code List page.
55. Clicks to view CL0.
56. The developer publishes the Code List.
57. Verify that the CL0 is in published state. (Assertion [#13](#test-assertion-5413))
58. The developer clicks to view CLa.
59. The developer publishes the Code List.
60. Verify that the CLa is in published state. (Assertion [#14](#test-assertion-5414))
61. The developer clicks to view CLx.
62. The developer deletes the content of the Name field and tries to publish it.
63. Verify that the “Publish” button is disabled. (Assertion [#15](#test-assertion-5415))
64. The developer undoes the changes of the previous step, deletes the content of the List ID field and tries to publish it.
65. Verify that the “Publish” button is disabled. (Assertion [#15](#test-assertion-5415))
66. The developer undoes the changes of the previous step, deletes the content of the Agency ID field and tries to publish it.
67. Verify that the “Publish” button is disabled. (Assertion [#15](#test-assertion-5415))
68. The developer undoes the changes of the previous step, deletes the content of the Version field and tries to publish it.
69. Verify that the “Publish” button is disabled. (Assertion [#15](#test-assertion-5415))
70. The developer undoes the changes of the previous step, deletes the content of the Code field of a Code List value and tries to click the “Edit” button.
71. Verify that the “Edit” button is disabled. (Assertion [#15](#test-assertion-5415))
72. The developer undoes the changes of the previous step, deletes the content of the Short Name field of the Code List value and tries to click the “Edit” button.
73. Verify that the “Edit” button is disabled. (Assertion [#15](#test-assertion-5415))
74. The developer goes to the List of BIEs page.
75. Clicks to view BIEa.
76. The developer expands the BIE tree, chooses a leaf node that has token as a default primitive, chooses Code as a Primitive type and tries to select “Code List Test Editing” as the content of the Code field.
77. Verify that the code List “Code List Test Editing” is not present in the list. (Assertion [#16](#test-assertion-5416))
78. The developer goes to the Code List page.
79. Verify that the checkbox used to discard the CL0 is disabled. (Assertion [#17](#test-assertion-5417))
80. Clicks to view CL0.
81. Verify that Discard button is absent. (Assertion [#17](#test-assertion-5417))
82. The developer opens the CL1, turns on the Extensible switch, adds some code list values with only the Code and Short name fields specified and publishes the code list.
83. The developer goes to the Code List page.
84. Clicks to view CL1 and then he clicks the Derive Code List based on this button.
85. The developer clicks Save.
86. Verify that the code list, say CL1_e0, is successfully recorded by the application. (Assertion [#18](#test-assertion-5418))
87. The developer goes back to the Code List page.
88. He opens and publishes the CL2.
89. He goes back to the Code List page.
90. Clicks to view CL2 and then clicks the Derive Code List based on this button.
91. Verify that the inherited code list values are not editable and so they cannot be changed (Assertion [#19](#test-assertion-5419))
92. The developer restricts some code list values, adds some more, removes some of the added values, makes sure that all code list fields and code value fields are populated, and finally click Save to create a Code List, say CL2_e0. Also, he tries to add a new value with its properties same as an existing value.
93. Verify that the CL2_e0 is successfully recorded by the application and that he could not add a duplicate value. (Assertion [#19](#test-assertion-5419), [#20](#test-assertion-5420))
94. The developer access the code list with base creation by clicking “Code List” and “Create Code List from another” menu.
95. The developer search for CL2 and choose it as a base. (Assertion [#31](#test-assertion-5431))
96. The developer specifies all fields except the Name field and tries to click the “Create” button.
97. Verify that the “Create” button is disabled. (Assertion [#21](#test-assertion-5421))
98. The developer tries to create a code list with base (extending the CL2), with all fields specified except the List ID field.
99. Verify that the “Create” button is disabled. (Assertion [#21](#test-assertion-5421))
100. The developer tries to create a code list with base (extending the CL2), with all fields specified except the Agency ID field.
101. Verify that the “Create” button is disabled. (Assertion [#21](#test-assertion-5421))
102. The developer tries to create a code list with base (extending the CL2), with all fields specified except the Version field.
103. Verify that the “Create” button is disabled. (Assertion [#21](#test-assertion-5421))
104. The developer tries to create a code list with base (extending the CL2), with all fields specified, adds a new code list value with all its fields specified except the Code Field.
105. Verify that the “Add” button is disabled. (Assertion [#21](#test-assertion-5421))
106. The developer tries to create a code list with base (extending the CL2), with all fields specified, adds a new code list value with all its fields specified except the Short Name Field.
107. Verify that the “Add” button is disabled. (Assertion [#21](#test-assertion-5421))
108. The developer tries to create a code list with base (extending the CL2), with all fields specified but the List ID, Agency ID and Version fields are the same as those of the CL3.
109. Verify that the application indicates an error and that the new code list is not recorded by verifying that it is not displayed in the “Code Lists” page. (Assertion [#22](#test-assertion-5422))
110. The developer goes back to Code List page.
111. Clicks to view CL1_e0.
112. Verify that the code list values fields inherited from CL1 are not editable. (Assertion [#23](#test-assertion-5423))
113. The developer changes the content of some fields, restricts some of the code list values, adds some more with all their fields specified, removes some of them and finally updates the code list.
114. Verify that the code list is successfully updated with the new content. (Assertion [#23](#test-assertion-5423))
115. The developer goes back to Code List page.
116. Clicks to view CL2_e0.
117. Verify that the code list values fields inherited from CL2 are not editable. (Assertion [#24](#test-assertion-5424))
118. The developer changes the content of some fields, restricts some of the code list values, adds some more with all their fields specified, removes some of them and finally updates the code list.
119. Verify that the code list is successfully updated with the new content. (Assertion [#24](#test-assertion-5424))
120. The developer goes back to the Code List page.
121. Clicks to view CL1_e0.
122. The developer deletes the content of the Name field and tries to update the code list.
123. Verify that the “Update” button is disabled. (Assertion [#25](#test-assertion-5425))
124. The developer undoes the changes of the previous step, deletes the content of the List ID field and tries to update the code list.
125. Verify that the “Update” button is disabled. (Assertion [#25](#test-assertion-5425))
126. The developer undoes the changes of the previous step, deletes the content the Agency ID field and tries to update the code list.
127. Verify that “Update” button is disabled. (Assertion [#25](#test-assertion-5425))
128. The developer undoes the changes of the previous step, deletes the content the Version field and tries to update the code list.
129. Verify that the “Update” button is disabled. (Assertion [#25](#test-assertion-5425))
130. The developer undoes the changes of the previous step, adds a new code list value with all its fields specified except the Code field and tries to update the code list.
131. Verify that the “Add” button is disabled. (Assertion [#25](#test-assertion-5425))
132. The developer undoes the changes of the previous step, adds a new code list value with all its fields specified except the Short Name field and tries to update the code list.
133. Verify that the “Add” button is disabled. (Assertion [#25](#test-assertion-5425))
134. The developer goes back to Code List page.
135. Clicks to view CL1_e0.
136. The developer publishes the code list.
137. Verify that the code list is in Published state. (Assertion [#26](#test-assertion-5426))
138. The developer goes back to the Code List page.
139. Clicks to view CL2_e0
140. The developer deletes the content of the Name field and tries to publish the code list.
141. Verify that the “Publish” button is disabled. (Assertion [#27](#test-assertion-5427))
142. The developer undoes the change in the previous step and deletes the content of the List ID field and tries to publish the code list.
143. Verify that the “Publish” button is disabled. (Assertion [#27](#test-assertion-5427))
144. The developer undoes the changes of the previous step, deletes the content the Agency ID field and tries to publish the code list.
145. Verify that the “Publish” button is disabled. (Assertion [#27](#test-assertion-5427))
146. The developer undoes the changes of the previous step, deletes the content the Version field and tries to publish the code list.
147. Verify that the “Publish” button is disabled. (Assertion [#27](#test-assertion-5427))
148. The developer undoes the changes of the previous step and tries to add a new code list value with all its fields specified except the Code field.
149. Verify that the “Add” button is disabled. (Assertion [#27](#test-assertion-5427))
150. The developer undoes the changes of the previous step and tries to add a new code list value with all its fields specified except the Short Name field.
151. Verify that the “Add” button is disabled. (Assertion [#27](#test-assertion-5427))
152. The developer goes to the List of BIEs page.
153. Clicks to view BIEa.
154. The developer expands the BIE tree, chooses a leaf node that has Primitive as Primitive Type and anything but token or string as Primitive (the reason to choose other primitive is for testing purpose only; this should work for any primitive), chooses Code as a Primitive Type and tries to select “Code List Test Editing” as the content of the Code field.
155. Verify that “Code List Test Editing” Code List is not present in the list and that the CL1_e0 is displayed. (Assertion [#28](#test-assertion-5428))
156. The developer goes to the Code List page.
157. Clicks to view CL2_e0, he restricts some code values inherited from CL2 and he publishes the code list.
158. The developer creates a Code list based on CL2_e0, say CL2_e0e0.
159. Verify that the restricted code values of the CL2 are not displayed. (Assertion [#29](#test-assertion-5429), [#21](#test-assertion-5421))
160. The developer goes to the Code List page.
161. He enters the keyword “userb” to the search drop-down box of the “Updater” search field.
162. Verify that the user “userb” appears only. Also verify that “oagi”, “devx” and “usera” user does not appear.
163. He clears everything and he enters a search term “xtensibl” that partially matches some code list names.
164. Verify that at least the code list “Non Extensible Code List” is returned. (Assertion [#30](#test-assertion-5430))
165. The developer selects “oagis” as Updater and clicks search button.
166. Verify that at least the code list “Non Extensible Code List” is returned. (Assertion [#30](#test-assertion-5430))
167. The developer selects 1/1/2019 as Updated start date.
168. Verify that at least the code list “oacl_UnitCode” is returned. (Assertion [#30](#test-assertion-5430))
169. He opens the code list CL2_e0e0 and he clicks on the Agency selector field.
170. He enters the keyword “FDA” to the search drop-down list input field.
171. Verify that the “US, FDA (Food and Drug Administration)” appears but not the “CCC (Customs Co-operation Council)”. (Assertion [#30](#test-assertion-5430))
172. The developer clicks to view this code list.
173. Verify that the Create Code List based on this button is absent and so the code list cannot be extended. (Assertion [#31](#test-assertion-5431))
174. Click on the menu Code List -> Create Code List based on Another. Enter in the search box “extensible”. (Assertion [#30](#test-assertion-5430))
175. Verify that “Non Extensible Code List” does not show up for selection. (Assertion [#31](#test-assertion-5431))
176. Ensure that CL2_e0e0 is extensible but unpublished.
177. The developer clicks to view CL2_e0e0.
178. Verify that the Create Code List based on this button is absent and so the code list cannot be extended. (Assertion [#32](#test-assertion-5432))
179. Click on the menu Code List -> Create Code List based on Another. Use the Search box to find CL2_e0e0.
180. Verify that CL2_e1e1 does not show up for selection. (Assertion [#32](#test-assertion-5432))
181. The developer creates a new code list, say CL5 without base specifying only the required fields and with the same name as the Code List CL2
182. Verify that a confirmation message is returned (Assertion [#33](#test-assertion-5433)).
183. The developer confirms his intention.
184. Verify that the code list is recorded (Assertion [#33](#test-assertion-5433))
185. The developer updates the new code list by changing the name to a new one which is the same as the code list “Non Extensible Code List”
186. Verify that a confirmation message is returned (Assertion [#34](#test-assertion-5434))
187. The developer confirms his intention
188. Verify that the code list is updated (Assertion [#34](#test-assertion-5434))
189. The developer creates a new code list, say CL2_e1 extending the CL2 Code List, specifying only the required fields and with the same name as the Code List CL2
190. Verify that a confirmation message is returned (Assertion [#33](#test-assertion-5433)).
191. The developer confirms his intention.
192. Verify that the code list is recorded (Assertion [#33](#test-assertion-5433))
193. The developer updates the code list CL2_e1 by changing the name to a new one which is the same as the code list “Non Extensible Code List”
194. Verify that a confirmation message is returned (Assertion [#34](#test-assertion-5434)) where the developer confirms his intention
195. Verify that the code list is updated (Assertion [#34](#test-assertion-5434))
196. The developer visits the Code List page, he selects a Code List (clicking the corresponding checkbox), goes to the next paginator pages and then returns back.
197. Verify that the checkbox of the selected Code List is checked. (Assertion [#35](#test-assertion-5435))
198. The developer opens the Code List CL2_e0e0, he selects a Code List value (clicking the corresponding checkbox), goes to the next paginator pages and then returns back.
199. Verify that the checkbox of the selected Code List value is checked. (Assertion [#36](#test-assertion-5436))

## Test Case 5.5

> OAGi ­­­­developer authorized management of BIE

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #5.5.1
The developer can create a BIE without restricting it based on ASCCP1 in the non-latest published release branch resulting in BIE1.

#### Test Assertion #5.5.2
The developer can create a BIE without restricting it based on ASCCP1 that has its ACC1 revised in the latest published release branch resulting in BIE1.1. Also verify that BIE1.1 is different from BIE1 as expected (e.g., has additional descendant BIE nodes). Test for the cases where ACC1 has been revised by:

##### Test Assertion #5.5.2.a
Changed based ACC.
##### Test Assertion #5.5.2.b
Has additional BCC and ASCC.
##### Test Assertion #5.5.2.c
One of its BCCPs was revised to be NOT nillable.
##### Test Assertion #5.5.2.d
One of its ASCCPs was revised to be NOT nillable.

##### Test Assertion #5.5.2.e
Has an addition ASCC to an ASCCP that is a group.
##### Test Assertion #5.5.2.f
Max cardinality of a BCC and an ASCC changed from unbounded to 1.
##### Test Assertion #5.5.2.g
One of its BCCPs has its BDT changed.
##### Test Assertion #5.5.2.h
One of its BCCPs has its BDT that tied to a code list changed to another BDT tied to another code list (verified that code lists available in the BIE changes).
##### Test Assertion #5.5.2.i
One of its ASCCPs is deprecated.
##### Test Assertion #5.5.2.j
A new ASCC is inserted in between previously existed associations.
##### Test Assertion #5.5.2.k
One of its BCCPs has its default value revised from no default value to a default value.

#### Test Assertion #5.5.3
The developer cannot create a BIE based on a working branch ASCCP.

#### Test Assertion #5.5.4
The developer can see, in the BIE list page, all BIEs owned by any user.

#### Test Assertion #5.5.5
The developer can view the details of a BIE that is in WIP state and owned by him.

#### Test Assertion #5.5.6
The developer cannot view the details of a BIE that is in WIP state and owned by another user.

#### Test Assertion #5.5.7
The developer can view the details of a BIE that is in QA state and owned by any user but cannot make any change.

#### Test Assertion #5.5.8
The developer can view the details of a production BIE owned by any user but cannot make any change.

#### Test Assertion #5.5.9
The developer can change the state of his own BIE, after all changes have been saved, from WIP to QA.

#### Test Assertion #5.5.10
The developer cannot change BIE’s state from WIP to QA if he has already made some changes. BIE has been updated successfully.

#### Test Assertion #5.5.11
The developer can change the state of his own BIE from QA back to WIP.

#### Test Assertion #5.5.12
The developer can move his own BIE which is in QA state to Production state.

#### Test Assertion #5.5.13
The developer cannot make any change to his own production BIE.

#### Test Assertion #5.5.14
The developer can update his own BIE that is in WIP state.

#### Test Assertion #5.5.15
The developer cannot update his own BIE which is in QA or production state.

#### Test Assertion #5.5.16
The developer cannot discard a BIE owned by another user.

#### Test Assertion #5.5.17
The developer cannot discard a Production BIE he owns.

#### Test Assertion #5.5.18
The developer can discard his own BIE that is in WIP.

#### Test Assertion #5.5.19
The developer can update a BIE, which has different type of nodes enabled, with all their fields specified with valid data type. During the creation, he expands the BIE tree and he enables and disables nodes using both the pane depicting the tree structure and the Details pane.

##### Test Assertion #5.5.19.a
For root BIE node, the following fields should be editable; Version, Status, Business Term, Remark and Context Definition.
##### Test Assertion #5.5.19.b
For ASBIE node, the following fields should be editable; Used, Min, Max, Nillable, Business Term, Remark and Context Definition. However, Nillable must be editable only if ASCCP’s Nillable is True. The default value of the following fields; Association Definition, Component Definition and Type Definition, should be properly derived from the based CC.

##### Test Assertion #5.5.19.c
For BBIE and SC nodes, the following fields should be editable; Used, Min, Max, Nillable, Fixed Value, Business Term, Remark, Value Domain Restriction and Context Definition. However, Nillable must be editable only if BCCP’s Nillable is True. The default value of the following fields; Association and Component Definition, should be properly derived from the based CC.

#### Test Assertion #5.5.20
The developer cannot update a BIE, which has different types of nodes enabled, by specifying Min and Max fields with invalid data types (String instead of Integer) including leaving them unspecified. The only string value that he can type is “unbounded” to the Cardinality Max field.

##### Test Assertion #5.5.20.a
When the original Min Cardinality is 0, the developer can specify 1.
##### Test Assertion #5.5.20.b
When the original Min Cardinality is 0 and Max Cardinality is unbounded, the developer can change the Min to any integer more than zero.
##### Test Assertion #5.5.20.c
When the original Min Cardinality is 0 and Max Cardinality is 1, the developer can change the Max to 0. And the application shall give warning if the Context Definition field is blank.
##### Test Assertion #5.5.20.d
When the original Min Cardinality is 1, the Min or the Max cannot be changed to zero.
##### Test Assertion #5.5.20.e
Min Cardinality cannot be changed to -1 or -5.
##### Test Assertion #5.5.20.f
When the original Min Cardinality is 0 and Max Cardinality is unbounded or -1, the developer can change Max to any number between (0, and INF).
##### Test Assertion #5.5.20.g
The developer can never change the Min to be more than the Max.
##### Test Assertion #5.5.20.h
The developer can set the Max cardinality to -1.
##### Test Assertion #5.5.20.i
The developer can set the Max cardinality to “unbounded”.

#### Test Assertion #5.5.21
The developer cannot update a BIE, which has different types of nodes enabled,

##### Test Assertion #5.5.21.a
By specifying Value Domain Type field but not its corresponding field, namely Value Domain.
##### Test Assertion #5.5.21.b
Using an incompatible primitive with the based CC, when Value Domain is selected for Value Domain Type (i.e., only compatible value domains shall be offered in the dropdown list).
##### Test Assertion #5.5.21.c
Using an incompatible code list with the based CC, when Code is selected for Value Domain Type (i.e., only compatible code lists shall be offered in the dropdown list).

#### Test Assertion #5.5.22
A code list which is an extension of a default code list (aka compatible code lists) used by a BBIE must show up for the code list selection, when the Value Domain Type is set to “Code”. Specifically:

##### Test Assertion #5.5.22.a
Only published, compatible code lists in the same release as the BIE shall be included, i.e., a code list exists only in a newer release shall not be included. The version of the code list should be also displayed.

#### Test Assertion #5.5.23
The developer cannot update a BIE by changing the fields of a node that is not used.

#### Test Assertion #5.5.24
The developer can hide and unhide unused nodes in the BIE tree.

#### Test Assertion #5.5.25
The developer cannot copy a BIE created by another user and which is in the WIP state.

#### Test Assertion #5.5.26
The developer can copy a BIE that he owns, and which is in WIP state.

#### Test Assertion #5.5.27
The developer can copy a BIE which is in QA or Production state and owned by another user. In case that the BIE is owned by an end user, any descendant BIEs added by the end user to the Extension BIEs are ignored.

#### Test Assertion #5.5.28
The developer can transfer ownership of a BIE only to another developer.

#### Test Assertion #5.5.29
The developer can transfer ownership of the BIE he owns only when it is in WIP state, i.e., not when it is in QA or Production state.

#### Test Assertion #5.5.30
The search feature in the Create BIE page is at least working

#### Test Assertion #5.5.31
The search feature in the BIE List page is at least working.

#### Test Assertion #5.5.32
The search feature in the Copy BIE page is at least working.

#### Test Assertion #5.5.33
A node cannot be expanded automatically if a same-name node has been expanded (git issue #653).

#### Test Assertion #5.5.34
The number of BIEs in Copy BIE page are the same with the number of the BIEs displayed on the right bottom index of the page.

#### Test Assertion #5.5.35
The developer can assign multiple business contexts to a BIE during the BIE creation.

#### Test Assertion #5.5.36
The developer can assign multiple business contexts to a BIE in the WIP state he owns via updating it (via View/Edit BIE page).

#### Test Assertion #5.5.37
The developer cannot assign multiple business contexts to a BIE not the WIP state.

#### Test Assertion #5.5.38
The developer cannot assign multiple business contexts to a BIE in not the WIP state and he does not own via updating it (via View/Edit BIE page).

#### Test Assertion #5.5.39
The developer cannot assign the same business context more than one times in a BIE.

#### Test Assertion #5.5.40
The developer can remove an assigned business context from a BIE in the WIP state he owns.

#### Test Assertion #5.5.41
The developer cannot remove all business contexts from a BIE. There must be at least one business context assigned.

#### Test Assertion #5.5.42
An example input text field should exist in BBIEPs and BBIE_SC where an example of data of node can be inserted.

#### Test Assertion #5.5.43
The Fixed and Default values should be mutually exclusive.

#### Test Assertion #5.5.44
If Cardinality Min is greater than zero in a BCC or an ASCC they must be enabled, and they cannot be disabled/unused. Their parent nodes can be unused though.

#### Test Assertion #5.5.45
The developer can select a BIE from the BIE List page, navigate thought different paginator pages while the forenamed BIE remains checked.

#### Test Assertion #5.5.46
The developer can neither create a local or global extension to the BIE.

#### Test Assertion #5.5.47
The developer cannot create a BIE without a Business Context assigned.

#### Test Assertion #5.5.48
The developer can set the Version metadata field and automatically the Fixed Value of the Version Identifier node is synchronized.

#### Test Assertion #5.5.49
The developer can change the Fixed Value of the Version Identifier node even if it was previously synchronized.

#### Test Assertion #5.5.50
The default value of the Primitive Date Time BCCPs should be date time.

#### Test Assertion #5.5.51
Developer cannot create a new BIE from an ASCCP whose ACC has a group component type.

#### Test Assertion #5.5.52
The developer can visit a BIE node and choose the option “Enable Children” in order to enable all Children nodes of this BIE node at once. The “Used” field of these nodes is checked.

#### Test Assertion #5.5.53
The developer can visit a BIE node and choose the option “Set Max Cardinality 1” in order to set the Max Cardinality field of all the children nodes to 1.

#### Test Assertion #5.5.54
The developer can click the Detail Reset button of a specific BIE node to reset the values back to initial the values of the BIE node. These values are based on the corresponding CC of the BIE node. A confirmation dialog should be also returned in order for the developer to confirm his intension of resetting the BIE node values

#### Test Assertion #5.5.55
The developer can Exclude SCs or not from the Searching Field by checking or unchecking the “Exclude SCs” checkbox accordingly.

##### Test Assertion #5.5.55.a
If the “Exclude SCs” checkbox is enabled (i.e., checked) the SCs are excluding from the searching field
##### Test Assertion #5.5.55.b
If the “Exclude SCs” checkbox is disabled (i.e., unchecked) the SCs are excluding from the searching field

### Test Step Pre-condition:

1. There are BIEs already created and owned by various end users, say BIEa, BIEb, BIEc, which are in editing, candidate and published state respectively.
2. There are BIEs already created and owned by a developer different than the one in the test steps below, say BIE0, BIE1 and BIE2, which are in editing state, candidate and published state respectively.
3. There is a BIE already created by the developer same as the one in the test step below, say BIE3, which is in candidate state and its Name is “Service Elements”.
4. There is an ASCCP with Property Term “Acknowledge Production Order”
5. There is the Module “Model\BODs\AcknowledgeProductionOrder”.
6. There is no extension to oacl_LanguageCode.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer creates a BIE, say BIE4, with name “Cancel Require Product”, having chosen a Business Context and an ASCCP. Finally, he leaves the Edit BIE page having done no changes.
3. Verify that the BIE is successfully recorded by the application. (Assertion [#1](#test-assertion-551))
4. The developer visits the BIE List page.
5. Verify that he can see in the list BIE0, BIE1, BIE2, BIE3, BIE4, BIEa, BIEb and BIEc. (Assertion [#2](#test-assertion-552))
6. The developer clicks to view BIE4.
7. Verify that he can view the BIE4 and that he can expand the BIE tree so that the four different types of nodes are displayed (Assertion [#3](#test-assertion-553))
8. The developer goes back to the BIE List page.
9. He clicks to view BIEa.
10. Verify that he cannot view the BIEa by verifying that there is no link (<a>) to the BIEa and that he could not visit the Edit BIE page. (Assertion [#4](#test-assertion-554))
11. The developer clicks to view BIEb and tries to update it.
12. Verify that he can view the BIEb, but the fields of BIE’s nodes are not editable. (Assertion [#5](#test-assertion-555))
13. The developer clicks to view BIE1 and tries to update it.
14. Verify that he can view the BIE1, but the fields of BIE’s nodes are not editable. (Assertion [#5](#test-assertion-555))
15. The developer goes back to the BIE List page.
16. He clicks to view BIEc and tries to update it.
17. Verify that he can view the BIEc, but the fields of BIE’s nodes are not editable. (Assertion [#6](#test-assertion-556))
18. The developer goes back to the BIE List page.
19. He clicks to view BIE2 and tries to update it.
20. Verify that he can view the BIE2, but the fields of BIE’s nodes are not editable. (Assertion [#6](#test-assertion-556))
21. The developer goes back to the BIE List page.
22. The developer clicks to view BIE4, makes some changes, updates the BIE and then sets BIE’s state to Candidate.
23. Verify the BIE is successfully updated with the new content and that the BIE’s state is successfully recorded by the application. (Assertion [#7](#test-assertion-557))
24. The developer clicks to view BIE4, change it back to Editing state, reopen it and makes some changes.  (Assertion [#9](#test-assertion-559))
25. Verify that the Candidate button is present. (Assertion [#8](#test-assertion-558))
26. The developer clicks Update and then clicks Candidate.
27. He reopens BIE4 and he publishes it.
28. Verify that the BIE’s state is successfully recorded by the application. (Assertion [#10](#test-assertion-5510))
29. He clicks to view BIE4.
30. Verify that the Back to Editing, Candidate and Publish buttons are not present and no field is editable. (Assertion [#11](#test-assertion-5511))
31. The developer creates a new BIE, BIE5.
32. He clicks to view BIE5.
33. Verify that he can enable some nodes of the BIE and make some changes to them. Verify that the BIE has been stored successfully. (Assertion [#12](#test-assertion-5512))
34. He sets the BIE’s state to candidate.
35. Verify that the fields of the BIE’s nodes are not editable. (Assertion [#13](#test-assertion-5513))
36. He publishes the BIE5.
37. Verify that the fields of the BIE’s nodes are not editable. (Assertion [#13](#test-assertion-5513))
38. The developer goes back to the BIE List page.
39. Verify that the Discard button is not enabled for BIEa, BIEb, BIEc, BIE0, BIE1, BIE2, BIE4 and BIE5. (Assertion [#14](#test-assertion-5514), [#15](#test-assertion-5515))
40. The developer creates BIE6.
41. The developer discards BIE6 by clicking the Discard button and accepting the warning message.
42. Verify that the BIE6 has  successfully been discarded by verifying it is not displayed in the BIE List page. (Assertion [#16](#test-assertion-5516))
43. The developer discards BIE3 by clicking the Discard button and accepting the warning message.
44. Verify that the BIE3 has successfully been discarded by verifying it is not displayed in the BIE List page neither it is returned using the Property Term search field and searching for it. (Assertion [#16](#test-assertion-5516))
45. The developer creates a BIE, say BIE7. Afterwards, he expands the BIE tree so that all different types of nodes are displayed and enables an ASBIE and a BBIE node by clicking its corresponding box existing in tree structure and an SC node using the Used checkbox. Finally, he updates the BIE.
46. Verify that the Version, Status, Business Term, Remark and Context Definition fields of the root BIE node are editable. (Assertion [[#1](#test-assertion-551)7.a](#test-assertion-5517a))
47. Verify that the Min, Max, Nillable, Business Term, Remark and Context Definition fields of the ASBIE node are editable and that the Association Definition, Component Definition and Type Definition fields are not. (Assertion [[#1](#test-assertion-551)7.b](#test-assertion-5517b))
48. Verify that the Min, Max, Nillable, Fixed Value, Business Term, Remark, Primitive Restriction and Context Definition fields of the BBIE node are editable and that the Association and Component Definition fields are not. (Assertion [[#1](#test-assertion-551)7.c](#test-assertion-5517c))
49. Verify that the Min, Max, Nillable, Fixed Value, Business Term, Remark, Primitive Restriction and Context Definition fields of the SC node are editable, and the Association and Component Definition fields are not. (Assertion [[#1](#test-assertion-551)7.c](#test-assertion-5517c))
50. The developer clicks Update, go to the BIE List page, and reopen BIE7.
51. Verify that the BIE7 is successfully updated with the new content specified in step 45. (Assertion [#17](#test-assertion-5517))
52. The developer clicks to view BIE7 and enables different descendant nodes.
53. He tries to fill out the Min and Max fields with some string value.
54. Verify that a message is returned that the string value is not accepted and that the Update button is disabled. (Assertion [#18](#test-assertion-5518))
55. He fills out the Max field with the value “unbounded”.
56. Verify that the field is successfully updated. (Assertion [#18](#test-assertion-5518))
57. He deletes the content of Min and Max fields.
58. Verify that the application indicated an error and that the Update button is disabled. (Assertion [#18](#test-assertion-5518))
59. The developer enables a node (e.g. Version Identifier) which has Min Cardinality 0 and changes its Min cardinality from 0 to 7 and clicks Update.
60. Verify that an error is returned and that the Update button is disabled (Assertion [[#1](#test-assertion-551)8.a](#test-assertion-5518a))
61. He enables some nodes with original Min Cardinality 0, he changes it to 1 and Updates the BIE.
62. Verify that the BIE is successfully updated with the new content. (Assertion [[#1](#test-assertion-551)8.a](#test-assertion-5518a) [#1](#test-assertion-551)8.2)
63. The developer enables a node (e.g. Intermediary) whose Min cardinality is 0 and Max is unbounded, sets its Min Cardinality to 10 and updates the BIE.
64. Verify that the BIE is successfully updated with the new content. (Assertion [[#1](#test-assertion-551)8.b](#test-assertion-5518b))
65. He sets the above Min Cardinality to -1.
66. Verify that the application indicates an error and that the Update button is disabled. (Assertion [[#1](#test-assertion-551)8.b](#test-assertion-5518b))
67. The developer enables a node (e.g. System Environment Code) whose Min Cardinality is 0, Max is 1 and the Context definition field is blank, set the Max Cardinality to 0 and updates the BIE.
68. Verify that the application gives a warning message that the Context Definition field is blank. (Assertion [[#1](#test-assertion-551)8.c](#test-assertion-5518c))
69. The developer enables a node (e.g. Release Identifier) whose Min and Max Cardinality are 1, sets the Min to 0 and updates the BIE.
70. Verify that the application indicates an error and that the Update button is disabled (Assertion [[#1](#test-assertion-551)8.d](#test-assertion-5518d))
71. Then he sets the Max Cardinality to 0.
72. Verify that the application indicates an error and that the Update button is disabled. (Assertion [[#1](#test-assertion-551)8.d](#test-assertion-5518d))
73. Then he sets Min Cardinality to -1.
74. Verify that the application indicates an error and that the Update button is disabled. (Assertion [[#1](#test-assertion-551)8.e](#test-assertion-5518e))
75. Then He sets Min Cardinality to -5.
76. Verify that the application indicates an error and that the Update button is disabled. (Assertion [[#1](#test-assertion-551)8.e](#test-assertion-5518e))
77. The developer enables a node (e.g. Intermediary) whose Min cardinality is 0 and Max is unbounded, sets Max Cardinality to 10 and updates the BIE.
78. Verify that the BIE is successfully updated with the new content. (Assertion [[#1](#test-assertion-551)8.f](#test-assertion-5518f))
79. He sets the Min Cardinality of a node (e.g. Release Identifier) to a higher value than the value of Max Cardinality.
80. Verify that the application indicates an error and that the Update button is disabled. (Assertion [[#1](#test-assertion-551)8.g](#test-assertion-5518g))
81. He sets the Max Cardinality of different nodes to -1.
82. Verify that the BIE is updated with the new content and that Max Cardinality field is set to “unbounded”. (Assertion [[#1](#test-assertion-551)8.h](#test-assertion-5518h))
83. He restores the previous values of the elements and he sets their Max Cardinality to “unbounded”.
84. Verify that the BIE is updated with the new content and that Max Cardinality field is set to “unbounded”. (Assertion [[#1](#test-assertion-551)8.i](#test-assertion-5518i))
85. The developer enables Creation Date Time node.
86. Set its Primitive Type to “Code” (without specifying a code).
87. He enables another node on the tree.
88. The developer clicks Update.
89. Verify that the application reset the Primitive Type of the Creation Date Time node to “Primitive” and the Primitive is reset back to “Token”. (Assertion [[#1](#test-assertion-551)9.a](#test-assertion-5519a))
90. The developer sets its Primitive Type to “Agency”.
91. He clicks Update.
92. Verify that the application reset the Primitive Type of the Creation Date Time node to “Primitive” and the Primitive is reset back to “Token”. (Assertion [[#1](#test-assertion-551)9.a](#test-assertion-5519a))
93. Verify that “string” and “integer” are not included in the Primitive drop down list. (Partially covering Assertion [[#1](#test-assertion-551)9.b](#test-assertion-5519b))
94. The developer logs out.
95. An end user logs in. Create a new code list, say oacl_ LanguageCodeExtension based on oacl_LanguageCode and publishes it.
96. The end user logs out. A developer logs in. He opens up a top-level BIE, which has a Language Code BBIE.
97. The developer enables and clicks on the Language Code node.
98. He sets the Primitive Type to “Code”.
99. Verify that only oacl_LanguageCode, clm56392A20081107_LanguageCode, and oacl_ LanguageCodeExtension are present in the dropdown list. (Assertion [[#1](#test-assertion-551)9.c](#test-assertion-5519c), [#20](#test-assertion-5520))
100. The developer opens BIE7 for editing.
101. He expands the BIE7 tree and clicks on the node which has not been enabled as used.
102. Verify that the fields of node on the right pane are not editable. (Assertion [#20](#test-assertion-5520))
103. The developer ensures that a few nodes in the same hierarchy are enabled (e.g., if the top-level BIE is a BOD, the developer enables Application Area, Sender, and Type Code underneath).
104. Verify that the fields of the unused nodes cannot be changed. (Assertion [#21](#test-assertion-5521))
105. The developer opens BIE7 clicks the Hide unused checkbox.
106. Verify that only the nodes that are enabled/used are displayed when expanding the BIE7’s tree structure. (Assertion [#22](#test-assertion-5522))
107. The developer clicks Hide unused checkbox.
108. Verify that at least a few of unused nodes of BIE7 are visible. (Assertion [#22](#test-assertion-5522))
109. The developer makes a change. For instance, he enables the “System Environment Code”, change the “Business Term” field and clicks the Hide unused checkbox.
110. Verify that the changes has successfully recorder and they have not been lost. (Assertion [#22](#test-assertion-5522))
111. The developer goes to Copy BIE page.
112. Verify that the BIEa is not displayed in the list. (Assertion [#23](#test-assertion-5523))
113. He chooses a Business Context and in the next page chooses the BIE7.
114. Verify that the BIE is successfully copied (by checking using some nodes enabled in BIE7 are also enabled in the new BIE) and recorded by the application. (Assertion [#24](#test-assertion-5524))
115. The developer goes to Copy BIE page.
116. Verify that the BIEb, BIEc, BIE1, and BIE2 are available for copying. (Assertion [#25](#test-assertion-5525))
117. The developer logouts and the developer devx logins
118. He opens BIE0 for editing and changes it to the Candidate state.
119. Verify that the transfer ownership button is not available for BIE0 and BIE1. (Assertion [#26](#test-assertion-5526), [#27](#test-assertion-5527))
120. The developer opens BIE0. Change it back to the Editing state.
121. He clicks the transfer ownership button on BIE0.
122. Verify that there is no username of an end user available for selection.
123. He selects a username of another developer (e.g. devy).
124. Verify that the BIE’s ownership is transferred to the new user and there is no transfer button available for BIE0 on the BIE List page. (Assertion [#26](#test-assertion-5526), 27)
125. The developer goes to the Create BIE page.
126. He enters the keyword “userb” to the search drop-down box of the “Updater” search field.
127. Verify that the user “userb” appears only. Also verify that “oagi”, “devx” and “usera” users do not appear. (Assertion [#28](#test-assertion-5528))
128. He chooses a Business Context and goes to the second page where he can select a Top-Level Concept to create a BIE.
129. He enters the keyword “userb” to the search drop-down box of the “Updater” search field.
130. Verify that the user “userb” appears only. Also verify that “oagi”, “devx” and “usera” users do not appear. (Assertion [#28](#test-assertion-5528))
131. The developer clears everything and he enters the search term “BOM Extension” into the Property Term field.
132. Verify that at least the “Revised BOM Component Extension” and the “Revised BOM Extension” are returned.  (Assertion [#28](#test-assertion-5528))
133. He clears everything and he enters the search term “”BOM Extension”” into the Property Term field.
134. Verify that the “Revised BOM Extension” is returned and not the “Revised BOM Component Extension”.  (Assertion [#28](#test-assertion-5528))
135. The developer clears everything and he enters the search term “Acknowledge Production” into the Property Term field that partially matches some ASCCPs’ Property Terms names.
136. Verify that at least the ASCCP Property Term “Acknowledge Production Order” is returned. (Assertion [#28](#test-assertion-5528))
137. The developer enters the search term “AcknowledgeProduction” into the Module field that partially matches some Modules’ names.
138. Verify that at least the Module “ModelBODsAcknowledgeProductionOrder” is returned. (Assertion [#28](#test-assertion-5528))
139. The developer enters the search keyword “cknowledgeProductionOrder Business Object Document is to” into the Definition field that partially matches some definitions of ASCCP.
140. Verify that at least the ASCCP Property Term “Acknowledge Production Order”. (Assertion [#28](#test-assertion-5528))
141. The developer opens a BIE, say the BIE7, he selects “Code” as a “Primitive type” and he enters the keyword “Extension” into the drop-down search field.
142. Verify that the oacl_LanguageCode_Extension appears but not the clm56392A20081107_LanguageCode. (Assertion [#28](#test-assertion-5528))
143. The developer goes to BIE List page.
144. He enters the keyword “userb” to the search drop-down box of the “Updater” search field.
145. Verify that the user “usberb” appears only. Also verify that “oagi”, “devx” and “usera” users do  not appear. (Assertion [#29](#test-assertion-5529))
146. The developer clears everything and he enters the search term “BOM Extension” into the Property Term field.
147. Verify that at least the “Revised BOM Component Extension” and the “Revised BOM Extension” are returned.  (Assertion [#29](#test-assertion-5529))
148. He clears everything and he enters the search term “”BOM Extension”” into the Property Term field.
149. Verify that the “Revised BOM Extension” is returned and not the “Revised BOM Component Extension”.  (Assertion [#29](#test-assertion-5529))
150. He clears everything and he enters the search term “aster” that partially matches some BIEs’ names.
151. Verify that at least the BIE “Sync Project Master” is returned. (Assertion [#29](#test-assertion-5529))
152. He enters the search term “initBC” in the “Business Context” search field.
153. Verify that at least the BIE “Sync Project Master” is returned. (Assertion [#29](#test-assertion-5529)).
154. He chooses "Editing” state to filter BIEs.
155. Verify that there is at least one BIE in Editing state and no BIE in Candidate or Published state (Assertion [#29](#test-assertion-5529)).
156. He chooses "Candidate” state to filter BIEs.
157. Verify that there is at least one BIE in Candidate state and no BIE in Editing or Candidate state (Assertion [#29](#test-assertion-5529)).
158. He chooses "Published” state to filter BIEs.
159. Verify that there is at least one BIE in Published state and no BIE in Editing or Candidate state (Assertion [#29](#test-assertion-5529)).
160. He clears everything and he chooses “oagis” user in the “Owner” selector field.
161. Verify that there is at least one BIE with the Oagi user as the owner but no BIEs with other owners (Assertion [#29](#test-assertion-5529)).
162. He clears everything and he chooses “oagis” user in the “Updater” selector field.
163. Verify that there is at least one BIE with the Oagi user as the updater but no BIEs with other updaters (Assertion [#29](#test-assertion-5529)).
164. The developer goes to Copy BIE page.
165. He enters the keyword “userb” to the search drop-down box of the “Updater” search field.
166. Verify that the user “userb” appears only. Also verify that “oagi”, “devx” and “usera” users do not appear. (Assertion [#30](#test-assertion-5530))
167. The developer clears everything and he enters the search term “BOM Extension” into the Property Term field.
168. Verify that at least the “Revised BOM Component Extension” and the “Revised BOM Extension” are returned.  (Assertion [#30](#test-assertion-5530))
169. He clears everything and he enters the search term “”BOM Extension”” into the Property Term field.
170. Verify that the “Revised BOM Extension” is returned and not the “Revised BOM Component Extension”.  (Assertion [#30](#test-assertion-5530))
171. He clears everything and he enters the search term “redit” that partially matches some BIEs’ names.
172. Verify that at least the BIE “Show Credit” is returned. (Assertion [#30](#test-assertion-5530))
173. The developer creates a new BIE using the Property Term “Tool Actual”.
174. He opens the BIE tree, expands Identifier node and the Identifier Set node.
175. Check that the Identifier node is not displayed again inside the Identifier Set and so there is no loop of the same-name node. (Assertion 31)
176. The developer goes to Copy BIE page, selects a random Business Context and then clicks next button. Afterwards, he counts the BIEs to check if they are the same as the index number displaying at the bottom right of the page. (Assertion [#32](#test-assertion-5532))
177. The developer creates some business contexts.
178. The developer creates a new BIE, say BIE9, and during its creation he chooses multiple business contexts.
179. Verify that these business contexts are successfully assigned to the BIE9. (Assertion [#33](#test-assertion-5533))
180. He opens BIE BIE8 and adds some business contexts to it (e.g BusCon0, BusCon1).
181. Verify that the business contexts have successfully assigned to the BIE8. (Assertion [#34](#test-assertion-5534))
182. The developer opens BIE8 and he tries to assign a business context already assigned (e.g. BusCon0)
183. Verify that the business context BusCon0 is not available for assignment. (Assertion [#35](#test-assertion-5535))
184. The developer opens BIE8 and removes the BusCon0.
185. Verify that the no was successfully removed via BIE list, View/Edit and BIE expression page. Also verify that the others assigned business contexts was not removed. (Assertion [#36](#test-assertion-5536))
186. The developer opens BIE8 and removes all business contexts.
187. Verify that there is at least one business process assigned and displayed in BIE list, View/Edit and BIE expression page. (Assertion [#37](#test-assertion-5537))
188. The developer opens BIE8, enables some BBIEPs and BBIE_SC nodes that have an “Example” field (e.g. Action Code, Total Cost Amount, Unit Code), he adds some values and Updates the BIE.
189. Verify that the BIE has been successfully updated.  (Assertion [#38](#test-assertion-5538))
190. The developer opens BIE8, enables a node that has Value Constraint field (e.g. Action Code). Afterwards, he selects the Fixed value as a value of that field, adds a value to its corresponding input field and clicks update.
191. Verify that he could not choose Default value as a value of the Constraint field and that the BIE has been successfully updated.  (Assertion [#39](#test-assertion-5539))
192. The developer opens BIE8, enables a node that has Value Constraint field (e.g. Action Code). Afterwards, he selects the Default value as a value of that field, adds a value to its corresponding input field and clicks update.
193. Verify that he could not choose Fixed value as a value of the Constraint field and that the BIE has been successfully updated.  (Assertion [#39](#test-assertion-5539))
194. The developer opens BIE9 and clicks on a node that has Min Cardinality 1 (e.g. Release Identifier)
195. Verify that it is enabled by default and that it cannot be disabled. (Assertion [#40](#test-assertion-5540))
196. The developer expands the BIE tree and clicks a child node that has Min Cardinality 1 (e.g. Identifier of Document Identifier Set of Match Document Header)
197. Verify that the node is enabled by default and that it cannot be disabled. Also, verify that some of its parent nodes are disabled. (Assertion [#40](#test-assertion-5540))
198. The developer visits the BIE List page, he selects a BIE (clicking the corresponding checkbox), goes to the next paginator pages and then returns back.
199. Verify that the checkbox of the selected BIE is checked. (Assertion [#41](#test-assertion-5541))

## Test Case 5.6

> OAGi developer authorized access to BIE Expression generation

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #5.6.1
The developer can generate an expression of a BIE in a non-latest release that he owns in any state.

#### Test Assertion #5.6.2
The developer can generate an expression of a BIE in the latest release that he owns in any state. Verify that BIE from the non-latest release does not show up in the latest release.

#### Test Assertion #5.6.3
The developer cannot generate an expression of a BIE which is in WIP state and is owned by another user (both end user and developer).

#### Test Assertion #5.6.4
The developer can generate an expression of a BIE which is in QA state that is owned by another user.

#### Test Assertion #5.6.5
The developer can generate an expression of a Production BIE owned by another user.

#### Test Assertion #5.6.6
The developer can generate an expression of multiple BIEs, in XML Schema, in the same package, with no annotation selected. There shall be only one <xsd:schema> element.

#### Test Assertion #5.6.7
The developer can generate an expression of a single BIE, in XML Schema, in the same package, with all the annotations selected.

#### Test Assertion #5.6.8
The developer can generate an expression of a single BIE, in XML Schema, in the same package, with the BIE CCTS Meta Data annotation selected but not the Include CCTS_Definition Tag annotation.

#### Test Assertion #5.6.9
The developer can generate an expression of a single BIE, in XML Schema, in the same package, with the BIE OAGi/Score Meta Data annotation selected but not the Include WHO Columns annotation.

#### Test Assertion #5.6.10
The developer cannot generate an expression of a single BIE, in XML Schema, with the Include CCTS_Definition Tag annotation selected but not the BIE CCTS Meta Data annotation.

#### Test Assertion #5.6.11
The developer cannot generate an expression of a single BIE, in XML Schema, with the Include WHO Columns annotation selected but not the BIE OAGi/Score Meta Data annotation.

#### Test Assertion #5.6.12
The developer can generate an expression of a single BIE, in JSON Schema, in the same package, with no annotation selected.

#### Test Assertion #5.6.13
The developer can generate an expression of a single BIE, in JSON Schema, in the same package, with the BIE Definition annotation selected.

#### Test Assertion #5.6.14
The developer cannot generate an expression of a single BIE, in JSON Schema, in the same package, with any of the BIE CCTS Meta Data, Include CCTS_Definition Tag, BIE GUID, Business Context, BIE OAGi/Score Meta Data, Include WHO Columns, Based CC Meta Data annotations selected.

#### Test Assertion #5.6.15
The developer can generate an expression of multiple BIEs, in multiple XML Schemas, saved in the same package, with some annotations selected.

#### Test Assertion #5.6.16
The developer can generate an expression of multiple BIEs, in XML Schemas, saved in different packages, with some annotations selected.

#### Test Assertion #5.6.17
The developer can generate an expression of multiple BIEs, in JSON Schemas, saved in the same package, with the BIE Definition selected.

#### Test Assertion #5.6.18
The developer can generate an expression of multiple BIEs, in JSON Schemas, saved in different packages, with the BIE Definition selected.

#### Test Assertion #5.6.19
The developer can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header (Include Meta Header

#### Test Assertion #5.6.20
The developer can generate an expression of multiple BIEs, in JSON Schemas, saved in different packages, while selecting a Meta Header (Include Meta Header).

#### Test Assertion #5.6.21
The search function is at least working.

##### Test Assertion #5.6.21.a
Verify that BIE from the non-latest release does not show up in the latest release after searching.

#### Test Assertion #5.6.22
The number of BIEs in the Express BIE page should be the same with the number of BIEs displaying in the index box located at the bottom right of the page.

#### Test Assertion #5.6.23
The developer can generate an expression of a single BIE with multiple business contexts assigned, in XML Schema, in the same package, with the Business Context annotation selected.

#### Test Assertion #5.6.24
The developer can generate Open API 3.0 in JSON with GET Operation Template that includes Meta Header and Pagination Response and POST Operation Template that includes Meta Header. The developer must be able to select Meta Header BIE and Pagination Response BIE owned by him in any state.

#### Test Assertion #5.6.25
The developer can generate Open API 3.0 in YAML with GET Operation Template that includes Meta Header, Pagination Response, and Make Array option and POST Operation Template that includes Meta Header and Make Array option. The developer must be able to select Meta Header BIE and Pagination Response BIE owned by other users only in QA or Production state.

#### Test Assertion #5.6.26
The developer can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, in single file.

#### Test Assertion #5.6.27
The developer can generate an expression of a multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly saved in different packages.

#### Test Assertion #5.6.28
The developer can generate an expression of a single BIE, in Open API 3.0 in JSON with Code Generation Friendly, in single file.

#### Test Assertion #5.6.29
The developer can generate an expression of a multiple BIEs, in Open API 3.0 in JSON with Code Generation Friendly saved in different packages.

#### Test Assertion #5.6.30
The developer can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template and Make Array option, in single file.

#### Test Assertion #5.6.31
The developer can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template and Make Array option, in different packages.

#### Test Assertion #5.6.32
The developer can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, in single file.

#### Test Assertion #5.6.33
The developer can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, in different packages.

#### Test Assertion #5.6.34
The developer can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header and Pagination Response, in single file.

#### Test Assertion #5.6.35
The developer can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header and Pagination Response, in different packages.

#### Test Assertion #5.6.36
The developer can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, POST Operation Template and Make Array option, in single file.

#### Test Assertion #5.6.37
The developer can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, POST Operation Template and Make Array option, in different packages.

#### Test Assertion #5.6.38
The developer can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, POST Operation Template that includes Meta Header, in single file.

#### Test Assertion #5.6.39
The developer can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, POST Operation Template that includes Meta Header, in different packages.

#### Test Assertion #5.6.40
The developer can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option, in single file.

#### Test Assertion #5.6.41
The developer can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option, in different packages.

#### Test Assertion #5.6.42
The developer can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option as well as POST Operation Template that includes Meta Header, Pagination Response, and Make Array option, in single file.

#### Test Assertion #5.6.43
The developer can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option as well as POST Operation Template that includes Meta Header, Pagination Response, and Make Array option, in different packages.

#### Test Assertion #5.6.44
The developer can generate an expression of a single BIE, in Open API 3.0 in JSON with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option as well as POST Operation Template that includes Meta Header, Pagination Response, and Make Array option, in single file.

#### Test Assertion #5.6.45
The developer can generate an expression of multiple BIEs, in Open API 3.0 in JSON with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option as well as POST Operation Template that includes Meta Header, Pagination Response, and Make Array option, in different packages.

### Test Step Pre-condition:

1. There are some BIEs created by users, namely BIEa, BIEb, BIEc, which are in Editing, Candidate and Published correspondingly. Additionally, there are some BIEs created by a developer, namely BIE0, BIE1, BIE2, which are in Editing, Candidate and Published correspondingly. The name of the BIE1 is “Receive Item”. Finally, there is a BIE, BIE3, created by a developer with multiple business contexts assigned.

### Test Step:

1. An oagi developer logs into the system.
2. He goes to the Generate Expression page.
3. Verify that he cannot view the BIEa and so he cannot generate an expression of it, while he can view BIEb, BIEc, BIE0, BIE1 and BIE2 and so he can generate an expression of them. (Assertion [#1](#test-assertion-561) [#2](#test-assertion-562) [#3](#test-assertion-563) [#4](#test-assertion-564))
4. The developer generates an expression from BIEb, in XML Schema, selecting no annotation and that the scheme will be saved in the same package.
5. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#5](#test-assertion-565))
6. The developer generates an expression from BIEc, in XML Schema, selecting all the annotations and that the scheme will be saved in the same package.
7. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#6](#test-assertion-566))
8. The developer generates an expression from BIE1, in XML Schema, selecting the BIE CCTS Meta Data annotation but not the Include CCTS_Definition Tag annotation and that the scheme will be saved in the same package.
9. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#7](#test-assertion-567))
10. The developer generates an expression from BIE2, in XML Schema, selecting the BIE OAGi/Score Meta Data annotation but not the Include WHO Columns annotation and that the schema will be saved in the same package.
11. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#8](#test-assertion-568))
12. The developer generates an expression from BIE2, in XML Schema, selecting the Include CCTS_Definition Tag annotation but not the BIE CCTS Meta Data annotation.
13. Verify that the Include CCTS_Definition Tag annotation cannot be selected without selecting the BIE CCTS Meta Data annotation first. (Assertion [#9](#test-assertion-569))
14. The developer generates an expression from BIE2, in XML Schema, selecting the Include WHO Columns annotation but not the BIE OAGi/Score Meta Data annotation.
15. Verify that the Include WHO Columns annotation cannot be selected without selecting the BIE OAGi/Score Meta Data annotation first. (Assertion [#10](#test-assertion-5610))
16. The developer generates an expression from BIEb, in JSON Schema, selecting that the schema will be saved in the same package and without selecting any annotation.
17. Verify that the Schema is successfully generated. (Assertion [#11](#test-assertion-5611))
18. The developer generates an expression from BIE1, in JSON Schema, selecting the BIE Definition annotation and that the schema will be saved in the same package.
19. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#12](#test-assertion-5612))
20. The developer generates an expression from BIE2, in JSON Schema, selecting the BIE CCTS Meta Data, Include CCTS_Definition Tag, BIE GUID, Business Context, BIE OAGi/Score Meta Data, Include WHO Columns, Based CC Meta Data annotations.
21. Verify that the aforementioned annotations cannot be selected. (Assertion [#13](#test-assertion-5613))
22. The developer generates an expression from BIEb and BIE1, in XML Schemas, selecting some annotations and that the XML Schemas will be saved in the same package.
23. Verify that the Schemas are successfully generated and saved in the same package. (Assertion [#14](#test-assertion-5614))
24. The developer generates an expression from BIEb and BIE1, in XML Schemas, selecting some annotations and that the XML Schemas will be saved in different packages.
25. Verify that the Schemas are successfully generated and saved in different packages. (Assertion [#15](#test-assertion-5615))
26. The developer generates an expression from BIEb and BIE1, in JSON Schemas, selecting the BIE definition annotation and that the JSON Schemas will be saved in the same package.
27. Verify that the Schemas are successfully generated and saved in the same package. (Assertion [#16](#test-assertion-5616))
28. The developer generates an expression from BIEb and BIE1, in JSON Schemas, selecting the BIE Definition annotation and that the JSON Schemas will be saved in different packages.
29. Verify that the Schemas are successfully generated and saved in different packages. (Assertion [#17](#test-assertion-5617))
30. The developer generates an expression from BIE1, in JSON Schema, selecting the BIE Definition annotation and that the schema will be saved in the same package. Also, he selects Include Meta Header and selects a corresponding BIE.
31. Verify that the Schema is successfully generated (Assertion [#18](#test-assertion-5618))
32. The developer generates an expression from BIEb and BIE1, in JSON Schemas, selecting the BIE Definition annotation and that the schemas will be saved in different packages. Also, he selects Include Meta Header and selects a corresponding BIE.
33. Verify that the Schemas are successfully generated (Assertion [#19](#test-assertion-5619))
34. The developer enters the search term “eceive” in the Property Term search field that partially matches some BIEs’ names.
35. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-5620))
36. The developer clears all filters and he chooses user devx at the Owner search filter select.
37. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-5620))
38. The developer clears all filters and he chooses devx at the Updater search filter.
39. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-5620))
40. The developer clears all filters and he chooses a recent day at the Updated start date search filter.
41. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-5620))
42. The developer clears all filters and he chooses the Candidate state at the State search filter.
43. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-5620))
44. The developer clears all filters and enters the search term “initBC” in Business Context Search field that matches some BIE’s business contexts.
45. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-5620))
46. The developer opens the “Express BIE” page.
47. Verify that the number of BIEs is the same with the number of BIEs displaying in the index box located at the bottom right of the page by visiting each index page. (Assertion [#21](#test-assertion-5621))
48. The developer opens the “Express BIE” page.
49. The developer generates an expression from BIE3, in XML Schema, selecting the Business Context annotation.
50. Verify that the Schema is successfully generated. (Assertion [#22](#test-assertion-5622))