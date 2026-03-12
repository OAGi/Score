# Test Suite 5

**OAGIS developer access right to Score core functions**


## Test Case 5.1

**OAGIS developer's authorized management of context categories**

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
The developer cannot discard a context category that has a context scheme referencing it.

#### Test Assertion #5.1.11
The search feature works for the `Updater`, `Name`, and `Description` fields.

#### Test Assertion #5.1.12
The developer can select a Context Category from the Context Category List page, navigate through the pages of the paginator while the forenamed Context Category remains selected.

### Test Step Pre-condition:

1. The test creates developer, developer admin, end user, and end user admin accounts and their context categories as needed.
2. One context category is referenced by a context scheme for the discard-negative scenario.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer creates a context category with only the `Name` field specified and verifies that the saved category has no description. (Assertion [#1](#test-assertion-511))
3. The developer creates a context category with both `Name` and `Description` specified and verifies that both fields are saved correctly. (Assertion [#2](#test-assertion-512))
4. The developer tries to create a context category with only `Description` specified and verifies that creation is blocked. (Assertion [#3](#test-assertion-513))
5. The developer opens the Context Category list and verifies that context categories created by a developer, a developer admin, an end user, and an end user admin are all visible. (Assertion [#4](#test-assertion-514))
6. The developer opens context categories created by those different user types and verifies that they are editable. (Assertion [#5](#test-assertion-515))
7. The developer updates a context category by changing the `Name` and removing the `Description`, then verifies that only the required information remains. (Assertion [#6](#test-assertion-516))
8. The developer updates a context category with a new `Name` and `Description`, then verifies that both values are saved correctly. (Assertion [#7](#test-assertion-517))
9. The developer clears the `Name` field of an existing context category and verifies that the update is blocked. (Assertion [#8](#test-assertion-518))
10. The developer discards unreferenced context categories created by different user types and verifies that they are removed from the list. (Assertion [#9](#test-assertion-519))
11. The developer attempts to discard a context category that is referenced by a context scheme and verifies that the discard operation is blocked and an error message is shown. (Assertion [#10](#test-assertion-5110))
12. The developer verifies that search works with the `Updater`, `Name`, and `Description` filters. (Assertion [#11](#test-assertion-5111))
13. The developer selects a context category, navigates to another paginator page and back, and verifies that the selection is retained. (Assertion [#12](#test-assertion-5112))

## Test Case 5.2

**OAGi developer's authorized management of context schemes**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #5.2.1
The developer can create a context scheme without context scheme values.

#### Test Assertion #5.2.2
The developer can create a context scheme with all information specified.

#### Test Assertion #5.2.3
Adding and removing scheme values on the creation page works.

#### Test Assertion #5.2.4
The developer cannot create a context scheme when required header fields or a required context scheme value field are missing.

#### Test Assertion #5.2.5
The developer cannot create a context scheme when the uniqueness requirement is not met. See the design document for the uniqueness requirement.

#### Test Assertion #5.2.6
The application gives the correct warning when the developer tries to create a context scheme with the same `Scheme ID` and `Agency ID` as an existing context scheme but a different `Name`.

#### Test Assertion #5.2.7
The developer can see, in the context scheme list, context schemes created by a developer, a developer admin, an end user, and an end user admin.

#### Test Assertion #5.2.8
The developer can edit a context scheme created by any user.

#### Test Assertion #5.2.9
The developer can update a context scheme without context scheme values.

#### Test Assertion #5.2.10
The developer can update a context scheme with all information specified.

#### Test Assertion #5.2.11
The developer cannot update a context scheme with missing required information.

#### Test Assertion #5.2.12
The developer can discard context schemes created by any user provided that there is no business context referencing it.

#### Test Assertion #5.2.13
The developer cannot discard a context scheme that is referenced by a business context.

#### Test Assertion #5.2.14
The developer can update a context scheme created by any user, even when there is already a business context referencing it. Also, verify that the respective business context is updated accordingly.

#### Test Assertion #5.2.15
The developer cannot remove a Context Scheme value if it is used by a Business Context.

#### Test Assertion #5.2.16
The developer cannot add a duplicate context scheme value.

#### Test Assertion #5.2.17
The search feature works for the `Updater` and `Name` fields.

#### Test Assertion #5.2.18
The developer can load context scheme values from code lists and save the resulting context scheme. Test for:

##### Test Assertion #5.2.18.a
Developer code list in the latest release
##### Test Assertion #5.2.18.b
Developer code list in an older release
##### Test Assertion #5.2.18.c
Derived end user code list in the latest release
##### Test Assertion #5.2.18.d
Derived end user code list in an older release

#### Test Assertion #5.2.19
The developer can load values from derived end user code lists in both the latest and older releases as part of the `Load from Code List` scenarios above.

#### Test Assertion #5.2.20
The developer can change the context scheme values added by a code list.

#### Test Assertion #5.2.21
The developer can add a value to a context scheme after loading values from a code list.

#### Test Assertion #5.2.22
The developer can delete a value from a context scheme added by a selected code list.

#### Test Assertion #5.2.23
The developer cannot use the `Load from Code List` function if a value of the context scheme is used by a business context.

#### Test Assertion #5.2.24
The developer can select a context scheme from the Context Scheme List page, navigate through different paginator pages, and keep that context scheme selected.

#### Test Assertion #5.2.25
The developer can search the Context Scheme List by an exact `Name` value and reliably retrieve the matching context scheme.

### Test Step Pre-condition:

1. The test creates developer, developer admin, end user, and end user admin accounts and their context schemes as needed.
2. Referenced business contexts and code lists are created as needed for the discard, update, and load-from-code-list scenarios.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer creates a context scheme without context scheme values and verifies that the header fields are saved correctly. (Assertion [#1](#test-assertion-521))
3. The developer creates a context scheme with context scheme values and verifies that all saved values are available on the edit page. (Assertion [#2](#test-assertion-522))
4. On the create page, the developer adds multiple context scheme values, removes some of them, saves the scheme, and verifies that only the remaining values are stored. (Assertion [#3](#test-assertion-523))
5. The developer verifies that creation is blocked when required header fields or the required `Value` field of a context scheme value are missing. (Assertion [#4](#test-assertion-524))
6. The developer attempts to create another context scheme with the same `Scheme ID`, `Agency ID`, and `Version` as an existing scheme and verifies that creation is rejected. (Assertion [#5](#test-assertion-525))
7. The developer creates a context scheme with the same `Scheme ID` and `Agency ID` as an existing scheme but a different `Name` and `Version`, verifies that a warning is shown, and completes creation with `Create anyway`. (Assertion [#6](#test-assertion-526))
8. The developer verifies that context schemes created by a developer, a developer admin, an end user, and an end user admin are all visible in the list. (Assertion [#7](#test-assertion-527))
9. The developer opens context schemes created by those different user types, verifies that they are editable, and updates their header fields without context scheme values. (Assertions [#8](#test-assertion-528), [#9](#test-assertion-529))
10. The developer updates a context scheme with new header fields and new context scheme values and verifies that all changes are saved. (Assertion [#10](#test-assertion-5210))
11. The developer verifies that update is blocked when required header fields or the required `Value` field of a context scheme value are missing. (Assertion [#11](#test-assertion-5211))
12. The developer discards unreferenced context schemes created by different user types and verifies that they are removed from the list. (Assertion [#12](#test-assertion-5212))
13. The developer attempts to discard a context scheme referenced by a business context and verifies that the discard operation is blocked. (Assertion [#13](#test-assertion-5213))
14. The developer updates a context scheme that is already referenced by a business context and verifies that the related business context reflects the new category, scheme, and value data. (Assertion [#14](#test-assertion-5214))
15. The developer attempts to remove a context scheme value that is used by a business context and verifies that the removal is blocked. (Assertion [#15](#test-assertion-5215))
16. The developer attempts to add a duplicate context scheme value and verifies that duplicate creation is blocked. (Assertion [#16](#test-assertion-5216))
17. The developer verifies that search works with the `Updater` and `Name` filters. (Assertion [#17](#test-assertion-5217))
18. The developer loads values from published developer code lists in the latest and older releases and verifies that the imported values and scheme metadata are populated correctly. (Assertions [#18.a](#test-assertion-5218a), [#18.b](#test-assertion-5218b))
19. The developer loads values from derived end user code lists in the latest and older releases and verifies that the imported values and scheme metadata are populated correctly, thereby also covering the dedicated derived-code-list assertion. (Assertions [#18.c](#test-assertion-5218c), [#18.d](#test-assertion-5218d), [#19](#test-assertion-5219))
20. After loading values from a code list, the developer changes imported values, adds manual values, and deletes imported values, then verifies that the saved context scheme reflects those changes. (Assertions [#20](#test-assertion-5220), [#21](#test-assertion-5221), [#22](#test-assertion-5222))
21. The developer attempts to use `Load from Code List` for a context scheme whose value is already used by a business context and verifies that the action is blocked. (Assertion [#23](#test-assertion-5223))
22. The developer selects a context scheme in the list, navigates to another paginator page and back, and verifies that the selection is retained. (Assertion [#24](#test-assertion-5224))
23. The developer searches the Context Scheme List using the exact `Name` of a saved context scheme and verifies that the correct matching row is returned reliably. (Assertion [#25](#test-assertion-5225))

## Test Case 5.3

**OAGi developer's authorized management of business contexts**

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
The developer can see, in the business context list, all business contexts created by any user.

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
The developer cannot discard a business context that is referenced by a BIE.

#### Test Assertion #5.3.13
The developer can update a business context created by any user, even when there is already a BIE referencing it. Verify that the business context name of the respective BIE has been updated accordingly.

#### Test Assertion #5.3.14
The search feature works for the `Name` field.

#### Test Assertion #5.3.15
The developer can select a business context from the Business Context List page, navigate through different paginator pages, and keep that business context selected.

#### Test Assertion #5.3.16
The all-information business context creation scenario verifies that all business context values are listed on the Edit Business Context page.

#### Test Assertion #5.3.17
The all-information business context creation scenario verifies that the Business Context Value dialog displays all details of the selected Context Category, Context Scheme, and Context Scheme Value.

### Test Step Pre-condition:

1. The test creates developer, developer admin, end user, and end user admin accounts and their business contexts as needed.
2. Referenced BIEs are created as needed for the discard-negative and propagation scenarios.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer creates a business context with only the required `Name` field and verifies that it is saved successfully. (Assertion [#1](#test-assertion-531))
3. The developer creates a business context while adding several context values and removing some of them during creation, then verifies that only the remaining values are saved. (Assertion [#2](#test-assertion-532))
4. The developer creates a business context with multiple context values and verifies that the edit page lists those values and that each Business Context Value dialog shows the related category, scheme, and scheme value details. (Assertions [#3](#test-assertion-533), [#16](#test-assertion-5316), [#17](#test-assertion-5317))
5. The developer tries to create a business context with the `Name` field missing and verifies that creation is blocked. (Assertion [#4](#test-assertion-534))
6. The developer verifies that business contexts created by a developer, a developer admin, an end user, and an end user admin are all visible in the list. (Assertion [#5](#test-assertion-535))
7. The developer opens business contexts created by those different user types, verifies that they are editable, and updates them with only the required information by changing the `Name` field. (Assertions [#6](#test-assertion-536), [#7](#test-assertion-537))
8. The developer updates a business context with a new `Name` and a new set of business context values and verifies that all changes are saved. (Assertion [#8](#test-assertion-538))
9. The developer attempts to add a duplicate business context value and verifies that the duplicate is rejected. (Assertion [#9](#test-assertion-539))
10. The developer clears the `Name` field of an existing business context and verifies that the update is blocked. (Assertion [#10](#test-assertion-5310))
11. The developer discards unreferenced business contexts created by different user types and verifies that they are removed from the list. (Assertion [#11](#test-assertion-5311))
12. The developer attempts to discard a business context that is referenced by a BIE and verifies that the discard operation is blocked. (Assertion [#12](#test-assertion-5312))
13. The developer updates BIE-referenced business contexts created by different user types and verifies that the new business context name is reflected in related BIE search results. (Assertion [#13](#test-assertion-5313))
14. The developer verifies that search works with the `Name` filter. (Assertion [#14](#test-assertion-5314))
15. The developer selects a business context in the list, navigates to another paginator page and back, and verifies that the selection is retained. (Assertion [#15](#test-assertion-5315))

## Test Case 5.4

**Retired in 2.0 - OAGi developer authorized access to code list management functions.**

> There is an entire code list management test suite that replaces this test case.

Pre-condition: N/A


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

**OAGi developer authorized management of BIE**

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
Has an additional ASCC to an ASCCP that is a group.
##### Test Assertion #5.5.2.f
Max cardinality of a BCC and an ASCC changed from unbounded to 1.
##### Test Assertion #5.5.2.g
One of its BCCPs has its BDT changed.
##### Test Assertion #5.5.2.h
One of its ASCCPs is deprecated.
##### Test Assertion #5.5.2.i
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
The developer can update a BIE, which has different types of nodes enabled, with all their fields specified with valid data types. During the creation, he expands the BIE tree and he enables and disables nodes using both the pane depicting the tree structure and the Details pane.

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
The developer can assign multiple business contexts to a BIE not the WIP state.

#### Test Assertion #5.5.38
The developer can assign multiple business contexts to a BIE in not the WIP state and he does not own via updating it (via View/Edit BIE page).

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
The former `Exclude SCs` option is no longer available in the UI. This behavior is not automated because the option has been removed.

##### Test Assertion #5.5.55.a
Not automated.
##### Test Assertion #5.5.55.b
Not automated.

#### Test Assertion #5.5.56
The developer can see the cardinalities of nodes in the BIE tree.

##### Test Assertion #5.5.56.a
The developer can hide and display the cardinalities of the nodes in the BIE tree.
##### Test Assertion #5.5.56.b
Changed cardinalities of the nodes must be correctly shown in the BIE tree and remain visible after reopening the page.

#### Test Assertion #5.5.57
When a BIE is created from adjacent releases where a related BCCP was renamed between releases, the BBIE node must preserve the correct default-value and value-constraint behavior for each release.

### Test Step Pre-condition:

1. The test provisions the developer, business contexts, top-level concepts, releases, BIEs, and related core components that it needs for each assertion.
2. For assertions about revised core components, the test creates or revises the underlying ACCs, ASCCPs, BCCPs, and related objects in adjacent published releases before creating the BIE.
3. For assertions about ownership or state transitions, the test creates BIEs in the required owner and state combinations before opening the relevant BIE page.

### Test Step:

1. The developer signs in, opens the Create BIE flow, selects published top-level concepts from non-latest published releases, creates BIEs, and verifies that each created BIE opens in the selected release branch. This covers Assertion [#5.5.1](#test-assertion-551).
2. The developer creates a BIE from a revised top-level concept whose based ACC has changed and verifies that the generated BIE tree reflects the revised based ACC structure. This covers Assertion [#5.5.2.a](#test-assertion-552a).
3. The developer creates a BIE from a revised top-level concept whose ACC has additional BCCs and ASCCs and verifies that the additional descendant nodes are present in the BIE tree. This covers Assertion [#5.5.2.b](#test-assertion-552b).
4. The developer creates a BIE from a revised top-level concept whose BCCP was revised to be not nillable and verifies that the corresponding BBIE node is displayed as not nillable and not editable for nillability. This covers Assertion [#5.5.2.c](#test-assertion-552c).
5. The developer creates a BIE from a revised top-level concept whose ASCCP was revised to be not nillable and verifies that the corresponding ASBIE node is displayed as not nillable and not editable for nillability. This covers Assertion [#5.5.2.d](#test-assertion-552d).
6. The developer creates a BIE from a revised top-level concept that adds an ASCC to an ASCCP that is a group and verifies that the group content is reflected in the resulting BIE tree. This covers Assertion [#5.5.2.e](#test-assertion-552e).
7. The developer creates a BIE from a revised top-level concept whose BCC and ASCC cardinality max changed from `unbounded` to `1` and verifies that the resulting BIE nodes display max cardinality `1`. This covers Assertion [#5.5.2.f](#test-assertion-552f).
8. The developer creates a BIE from a revised top-level concept whose BCCP has a changed BDT and verifies that the descendant supplementary component structure matches the revised BDT. This covers Assertion [#5.5.2.g](#test-assertion-552g).
9. The developer creates a BIE from a revised top-level concept whose ASCCP is deprecated and verifies that the corresponding BIE node is marked as deprecated. This covers Assertion [#5.5.2.h](#test-assertion-552h).
10. The developer creates a BIE from a revised top-level concept whose BCCP default value changed from none to a default value and verifies that the resulting BBIE node shows the default value. This covers Assertion [#5.5.2.i](#test-assertion-552i).
11. The developer opens the Create BIE flow and verifies that published release branches are available for top-level concept selection while a `Working` branch top-level concept cannot be used to create a BIE. This covers Assertion [#5.5.3](#test-assertion-553).
12. The developer opens the BIE List page and verifies that BIEs owned by any user are listed. This covers Assertion [#5.5.4](#test-assertion-554).
13. The developer opens his own WIP BIE and verifies that the BIE details can be viewed and edited. This covers Assertion [#5.5.5](#test-assertion-555).
14. The developer attempts to open another user's WIP BIE and verifies that the edit view is not available. This covers Assertion [#5.5.6](#test-assertion-556).
15. The developer opens QA-state BIEs owned by himself or another user and verifies that the details can be viewed but cannot be changed. This covers Assertion [#5.5.7](#test-assertion-557).
16. The developer opens Production-state BIEs owned by himself or another user and verifies that the details can be viewed but cannot be changed. This covers Assertion [#5.5.8](#test-assertion-558).
17. The developer saves changes to his own WIP BIE and then changes its state from WIP to QA. This covers Assertion [#5.5.9](#test-assertion-559).
18. The developer makes additional unsaved changes to his own WIP BIE and verifies that the state cannot be changed from WIP to QA until the BIE is updated successfully. This covers Assertion [#5.5.10](#test-assertion-5510).
19. The developer changes the state of his own QA BIE back to WIP. This covers Assertion [#5.5.11](#test-assertion-5511).
20. The developer changes the state of his own QA BIE to Production. This covers Assertion [#5.5.12](#test-assertion-5512).
21. The developer opens his own Production BIE and verifies that no change can be made. This covers Assertion [#5.5.13](#test-assertion-5513).
22. The developer updates his own WIP BIE and verifies that the changes are saved successfully. This covers Assertion [#5.5.14](#test-assertion-5514).
23. The developer opens his own QA or Production BIE and verifies that the BIE cannot be updated. This covers Assertion [#5.5.15](#test-assertion-5515).
24. The developer attempts to discard a BIE owned by another user and verifies that the discard action is not allowed. This covers Assertion [#5.5.16](#test-assertion-5516).
25. The developer attempts to discard his own Production BIE and verifies that the discard action is not allowed. This covers Assertion [#5.5.17](#test-assertion-5517).
26. The developer discards his own WIP BIE and verifies that the BIE is removed from the list. This covers Assertion [#5.5.18](#test-assertion-5518).
27. The developer enables root, ASBIE, BBIE, and SC nodes in a WIP BIE and verifies that the root node fields `Version`, `Status`, `Business Term`, `Remark`, and `Context Definition` are editable. This covers Assertion [#5.5.19.a](#test-assertion-5519a).
28. The developer selects enabled ASBIE nodes and verifies that `Used`, `Min`, `Max`, `Nillable`, `Business Term`, `Remark`, and `Context Definition` are editable, while derived definition fields remain derived from the underlying core component. This covers Assertion [#5.5.19.b](#test-assertion-5519b).
29. The developer selects enabled BBIE and SC nodes and verifies that `Used`, `Min`, `Max`, `Nillable`, `Fixed Value`, `Business Term`, `Remark`, value-domain-related fields, and `Context Definition` are editable, while derived definition fields remain derived from the underlying core component. This covers Assertion [#5.5.19.c](#test-assertion-5519c).
30. The developer enters blank values and non-integer strings into `Cardinality Min` and `Cardinality Max` and verifies that validation errors are shown and the Update button is disabled. This covers Assertion [#5.5.20](#test-assertion-5520).
31. The developer changes `Cardinality Min` from `0` to `1` where allowed and verifies that the change is saved. This covers Assertion [#5.5.20.a](#test-assertion-5520a).
32. The developer changes `Cardinality Min` to a positive integer greater than zero when `Cardinality Max` is `unbounded` and verifies that the change is saved. This covers Assertion [#5.5.20.b](#test-assertion-5520b).
33. The developer changes `Cardinality Max` from `1` to `0` where allowed and verifies the resulting warning when `Context Definition` is blank. This covers Assertion [#5.5.20.c](#test-assertion-5520c).
34. The developer attempts to change `Cardinality Min` or `Cardinality Max` to `0` when the original minimum cardinality is `1` and verifies that validation blocks the change. This covers Assertion [#5.5.20.d](#test-assertion-5520d).
35. The developer attempts to change `Cardinality Min` to `-1` and `-5` and verifies that validation blocks both values. This covers Assertion [#5.5.20.e](#test-assertion-5520e).
36. The developer changes `Cardinality Max` from `unbounded` to a finite positive integer and verifies that the change is saved. This covers Assertion [#5.5.20.f](#test-assertion-5520f).
37. The developer attempts to set `Cardinality Min` greater than `Cardinality Max` and verifies that validation blocks the change. This covers Assertion [#5.5.20.g](#test-assertion-5520g).
38. The developer sets `Cardinality Max` to `-1` and verifies that the value is treated as `unbounded`. This covers Assertion [#5.5.20.h](#test-assertion-5520h).
39. The developer sets `Cardinality Max` to `unbounded` and verifies that the value is saved as `unbounded`. This covers Assertion [#5.5.20.i](#test-assertion-5520i).
40. The developer sets a `Value Domain Type` without supplying the corresponding value-domain selection and verifies that the BIE cannot be updated. This covers Assertion [#5.5.21.a](#test-assertion-5521a).
41. The developer selects `Value Domain` and verifies that only primitives compatible with the underlying core component are offered. This covers Assertion [#5.5.21.b](#test-assertion-5521b).
42. The developer selects `Code` and verifies that only code lists compatible with the underlying core component are offered. This covers Assertion [#5.5.21.c](#test-assertion-5521c).
43. The developer opens the code-list selection for a BBIE whose default value domain is a code list and verifies that only published, compatible code lists in the same release are listed and that each candidate displays its version. This covers Assertions [#5.5.22](#test-assertion-5522) and [#5.5.22.a](#test-assertion-5522a).
44. The developer clicks a node that is not used and verifies that its fields cannot be updated. This covers Assertion [#5.5.23](#test-assertion-5523).
45. The developer checks `Hide unused` and verifies that unused nodes disappear from the BIE tree, unchecks `Hide unused` and verifies that unused nodes are shown again, then changes a node while toggling the option and verifies that the change is preserved. This covers Assertion [#5.5.24](#test-assertion-5524).
46. The developer opens the Copy BIE page and verifies that a WIP BIE created by another user is not available for copy. This covers Assertion [#5.5.25](#test-assertion-5525).
47. The developer opens the Copy BIE page, copies his own WIP BIE, and verifies that the copied BIE is created successfully. This covers Assertion [#5.5.26](#test-assertion-5526).
48. The developer opens the Copy BIE page, copies QA and Production BIEs owned by another developer, and copies QA and Production BIEs owned by an end user with extension content, then verifies that copying is allowed and that descendant BIEs added by the end user to Extension BIEs are ignored. This covers Assertion [#5.5.27](#test-assertion-5527).
49. The developer opens the transfer-ownership function for a WIP BIE and verifies that only developer accounts are available as transfer targets. This covers Assertion [#5.5.28](#test-assertion-5528).
50. The developer attempts to transfer ownership of his BIE in WIP, QA, and Production states and verifies that transfer is allowed only in WIP. This covers Assertion [#5.5.29](#test-assertion-5529).
51. In the Create BIE flow, the developer verifies that business context search works with the `Updater`, `Name`, `Update Start Date`, and `Update End Date` fields, and that top-level concept search works with the `DEN`, `Definition`, and `Module` fields. This covers Assertion [#5.5.30](#test-assertion-5530).
52. In the BIE List page, the developer verifies that search works with the `State`, `Update Start Date`, `Update End Date`, `DEN`, and `Business Context` fields. This covers Assertion [#5.5.31](#test-assertion-5531).
53. In the Copy BIE flow, the developer verifies that business context search works with the `Updater`, `Name`, `Update Start Date`, and `Update End Date` fields, and that BIE search works with the `DEN`, `Business Context`, and `State` fields. This covers Assertion [#5.5.32](#test-assertion-5532).
54. The developer opens a BIE whose tree contains same-name nodes and verifies that expanding one node does not incorrectly auto-expand another same-name node. This covers Assertion [#5.5.33](#test-assertion-5533).
55. The developer opens the Copy BIE page, counts the returned BIEs, and verifies that the count matches the page index summary. This covers Assertion [#5.5.34](#test-assertion-5534).
56. The developer creates a BIE while assigning multiple business contexts and verifies that all selected business contexts are assigned to the created BIE. This covers Assertion [#5.5.35](#test-assertion-5535).
57. The developer opens his own WIP BIE, assigns multiple business contexts by updating the BIE, and verifies that the assignments are saved. This covers Assertion [#5.5.36](#test-assertion-5536).
58. The developer assigns multiple business contexts to a BIE that is not in WIP and verifies that the assignments are saved. This covers Assertion [#5.5.37](#test-assertion-5537).
59. The developer opens a BIE that is not in WIP and is owned by another user, assigns multiple business contexts, and verifies that the assignments are saved. This covers Assertion [#5.5.38](#test-assertion-5538).
60. The developer attempts to assign the same business context more than once to a BIE and verifies that the duplicate assignment is blocked. This covers Assertion [#5.5.39](#test-assertion-5539).
61. The developer removes one assigned business context from his own WIP BIE and verifies that the selected business context is removed while the remaining assignments are kept. This covers Assertion [#5.5.40](#test-assertion-5540).
62. The developer attempts to remove all assigned business contexts from a BIE and verifies that at least one business context must remain assigned. This covers Assertion [#5.5.41](#test-assertion-5541).
63. The developer opens BBIE and BBIE_SC nodes and verifies that an `Example` input field exists and that example data can be entered and saved. This covers Assertion [#5.5.42](#test-assertion-5542).
64. The developer sets a fixed value on a node with a value-constraint field and verifies that a default value cannot be selected at the same time, then sets a default value and verifies that a fixed value cannot be selected at the same time. This covers Assertion [#5.5.43](#test-assertion-5543).
65. The developer opens BCC and ASCC nodes whose minimum cardinality is greater than zero and verifies that those nodes are enabled by default, cannot be disabled, and may still appear under unused parent nodes. This covers Assertion [#5.5.44](#test-assertion-5544).
66. The developer selects a BIE in the BIE List page, changes paginator pages, and verifies that the selection remains checked after navigation. This covers Assertion [#5.5.45](#test-assertion-5545).
67. The developer opens a BIE and verifies that neither local extension nor global extension can be created from it. This covers Assertion [#5.5.46](#test-assertion-5546).
68. The developer attempts to create a BIE without assigning any business context and verifies that creation is rejected. This covers Assertion [#5.5.47](#test-assertion-5547).
69. The developer edits the Version metadata field and verifies that the fixed value of the `Version Identifier` node is synchronized automatically. This covers Assertion [#5.5.48](#test-assertion-5548).
70. The developer changes the fixed value of the `Version Identifier` node after synchronization and verifies that the node value can still be edited independently. This covers Assertion [#5.5.49](#test-assertion-5549).
71. The developer opens primitive date time BBIE nodes and verifies that the default value-domain selection is `Primitive` with the value `date time`. This covers Assertion [#5.5.50](#test-assertion-5550).
72. The developer attempts to create a BIE from an ASCCP whose ACC has a group component type and verifies that creation is not allowed. This covers Assertion [#5.5.51](#test-assertion-5551).
73. The developer uses the `Enable Children` action on ASBIE and BBIE nodes and verifies that all child nodes become enabled. This covers Assertion [#5.5.52](#test-assertion-5552).
74. The developer uses the `Set Max Cardinality 1` action and verifies that the maximum cardinality of all child nodes is set to `1`. This covers Assertion [#5.5.53](#test-assertion-5553).
75. The developer uses the detail reset action for top-level, ASBIE, and BBIE nodes, confirms the reset, and verifies that node values are restored to their initial values derived from the corresponding core component. This covers Assertion [#5.5.54](#test-assertion-5554).
76. The developer verifies that the former `Exclude SCs` option is no longer present in the current UI, so the former behavior is not automated. This covers Assertion [#5.5.55](#test-assertion-5555).
77. The developer opens a BIE tree and verifies that node cardinality labels such as `1..1`, `0..1`, and `0..∞` are displayed in the tree. This covers Assertion [#5.5.56](#test-assertion-5556).
78. The developer toggles `Hide cardinality` and verifies that the cardinality labels are hidden and shown again. This covers Assertion [#5.5.56.a](#test-assertion-5556a).
79. The developer changes node cardinalities, updates the BIE, reopens the page, and verifies that the cardinality labels in the tree reflect the saved values. This covers Assertion [#5.5.56.b](#test-assertion-5556b).
80. The developer creates BIEs from adjacent releases where a related BCCP has been renamed between releases and verifies that the earlier-release BBIE node keeps the revised default value while the later-release node keeps the correct `Value Constraint` state. This covers Assertion [#5.5.57](#test-assertion-5557).

## Test Case 5.6

**OAGi developer authorized access to BIE Expression generation**

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
The developer cannot generate an expression of a single BIE, in JSON Schema, in the same package, with any of the BIE CCTS Meta Data, Include CCTS_Definition Tag, BIE GUID, Business Context, BIE OAGi/Score Meta Data, or Include WHO Columns annotations selected. `Based CC Meta Data` remains available for JSON Schema and is unchecked by default.

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

#### Test Assertion #5.6.46
The developer can generate an XML Schema expression of a single BIE while including the BIE version in the generated filename.

#### Test Assertion #5.6.47
The developer can refresh the Express BIE page during OpenAPI generation setup and still select a Meta Header BIE for the POST Operation Template before generating the expression.

### Test Step Pre-condition:

1. There are some BIEs created by users, namely BIEa, BIEb, BIEc, which are in Editing, Candidate and Published correspondingly. Additionally, there are some BIEs created by a developer, namely BIE0, BIE1, BIE2, which are in Editing, Candidate and Published correspondingly. The name of the BIE1 is “Receive Item”. Finally, there is a BIE, BIE3, created by a developer with multiple business contexts assigned.
2. A developer-owned BIE with a version value is available or is created during the test for generated-filename verification.

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
20. The developer generates an expression from BIE2, in JSON Schema, attempting to select BIE CCTS Meta Data, Include CCTS_Definition Tag, BIE GUID, Business Context, BIE OAGi/Score Meta Data, and Include WHO Columns. He also verifies that `Based CC Meta Data` remains available but unchecked by default.
21. Verify that the former annotations cannot be selected and that `Based CC Meta Data` remains enabled but unchecked. (Assertion [#13](#test-assertion-5613))
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
51. The developer generates Open API 3.0 in JSON for a single BIE using GET Operation Template with Meta Header and Pagination Response and POST Operation Template with Meta Header, selecting developer-owned Meta Header and Pagination Response BIEs in any state.
52. Verify that the expression is successfully generated. (Assertion [#23](#test-assertion-5624))
53. The developer generates Open API 3.0 in YAML for a single BIE using GET Operation Template with Meta Header, Pagination Response, and Make Array and POST Operation Template with Meta Header and Make Array, selecting other users' Meta Header and Pagination Response BIEs only in QA or Production state.
54. Verify that the expression is successfully generated. (Assertion [#24](#test-assertion-5625))
55. The developer generates Open API 3.0 in YAML with Code Generation Friendly for a single BIE in a single file.
56. Verify that the expression is successfully generated. (Assertion [#25](#test-assertion-5626))
57. The developer generates Open API 3.0 in YAML with Code Generation Friendly for multiple BIEs saved in different packages.
58. Verify that the expressions are successfully generated. (Assertion [#26](#test-assertion-5627))
59. The developer generates Open API 3.0 in JSON with Code Generation Friendly for a single BIE in a single file.
60. Verify that the expression is successfully generated. (Assertion [#27](#test-assertion-5628))
61. The developer generates Open API 3.0 in JSON with Code Generation Friendly for multiple BIEs saved in different packages.
62. Verify that the expressions are successfully generated. (Assertion [#28](#test-assertion-5629))
63. The developer generates Open API 3.0 in YAML with Code Generation Friendly for a single BIE using GET Operation Template with Make Array in a single file.
64. Verify that the expression is successfully generated. (Assertion [#29](#test-assertion-5630))
65. The developer generates Open API 3.0 in YAML with Code Generation Friendly for multiple BIEs using GET Operation Template with Make Array in different packages.
66. Verify that the expressions are successfully generated. (Assertion [#30](#test-assertion-5631))
67. The developer generates Open API 3.0 in YAML with Code Generation Friendly for a single BIE using GET Operation Template with Meta Header in a single file.
68. Verify that the expression is successfully generated. (Assertion [#31](#test-assertion-5632))
69. The developer generates Open API 3.0 in YAML with Code Generation Friendly for multiple BIEs using GET Operation Template with Meta Header in different packages.
70. Verify that the expressions are successfully generated. (Assertion [#32](#test-assertion-5633))
71. The developer generates Open API 3.0 in YAML with Code Generation Friendly for a single BIE using GET Operation Template with Meta Header and Pagination Response in a single file.
72. Verify that the expression is successfully generated. (Assertion [#33](#test-assertion-5634))
73. The developer generates Open API 3.0 in YAML with Code Generation Friendly for multiple BIEs using GET Operation Template with Meta Header and Pagination Response in different packages.
74. Verify that the expressions are successfully generated. (Assertion [#34](#test-assertion-5635))
75. The developer generates Open API 3.0 in YAML with Code Generation Friendly for a single BIE using POST Operation Template with Make Array in a single file.
76. Verify that the expression is successfully generated. (Assertion [#35](#test-assertion-5636))
77. The developer generates Open API 3.0 in YAML with Code Generation Friendly for multiple BIEs using POST Operation Template with Make Array in different packages.
78. Verify that the expressions are successfully generated. (Assertion [#36](#test-assertion-5637))
79. The developer generates Open API 3.0 in YAML with Code Generation Friendly for a single BIE using POST Operation Template with Meta Header in a single file.
80. Verify that the expression is successfully generated. (Assertion [#37](#test-assertion-5638))
81. The developer generates Open API 3.0 in YAML with Code Generation Friendly for multiple BIEs using POST Operation Template with Meta Header in different packages.
82. Verify that the expressions are successfully generated. (Assertion [#38](#test-assertion-5639))
83. The developer generates Open API 3.0 in YAML with Code Generation Friendly for a single BIE using GET Operation Template with Meta Header, Pagination Response, and Make Array in a single file.
84. Verify that the expression is successfully generated. (Assertion [#39](#test-assertion-5640))
85. The developer generates Open API 3.0 in YAML with Code Generation Friendly for multiple BIEs using GET Operation Template with Meta Header, Pagination Response, and Make Array in different packages.
86. Verify that the expressions are successfully generated. (Assertion [#40](#test-assertion-5641))
87. The developer generates Open API 3.0 in YAML with Code Generation Friendly for a single BIE using both GET and POST Operation Templates with Meta Header, Pagination Response, and Make Array in a single file.
88. Verify that the expression is successfully generated. (Assertion [#41](#test-assertion-5642))
89. The developer generates Open API 3.0 in YAML with Code Generation Friendly for multiple BIEs using both GET and POST Operation Templates with Meta Header, Pagination Response, and Make Array in different packages.
90. Verify that the expressions are successfully generated. (Assertion [#42](#test-assertion-5643))
91. The developer generates Open API 3.0 in JSON with Code Generation Friendly for a single BIE using both GET and POST Operation Templates with Meta Header, Pagination Response, and Make Array in a single file.
92. Verify that the expression is successfully generated. (Assertion [#43](#test-assertion-5644))
93. The developer generates Open API 3.0 in JSON with Code Generation Friendly for multiple BIEs using both GET and POST Operation Templates with Meta Header, Pagination Response, and Make Array in different packages.
94. Verify that the expressions are successfully generated. (Assertion [#44](#test-assertion-5645))
95. The developer generates an XML Schema expression for a single BIE while enabling the option to include the BIE version in the filename.
96. Verify that the expression is successfully generated and that the filename includes the BIE version. (Assertion [#46](#test-assertion-5646))
97. The developer begins OpenAPI generation for a BIE, selects a Meta Header for the POST Operation Template, refreshes the Express BIE page, reselects the same BIE and OpenAPI options, and verifies that the Meta Header can still be selected.
98. Verify that the expression is successfully generated after refreshing the page. (Assertion [#47](#test-assertion-5647))
