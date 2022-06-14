# Test Suite 7


## Test Case 7.1

> OAGi Navigation Menu

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #7.1.1
The BIE menu should have a title element with the content “Manage Profiled Component, Noun, BOD”.

#### Test Assertion #7.1.2
The Create BIE option of BIE menu should have a title element with the content “Profile a Component, Noun, BOD”.

#### Test Assertion #7.1.3
The View/Edit BIE option of BIE menu should have a title element with the content “List of Profiled Components, Nouns, BODs”.

#### Test Assertion #7.1.4
The Copy BIE option of BIE menu should have a title element with the content “Copy a Profiled Component, Noun, BOD”.

#### Test Assertion #7.1.5
The Express BIE option of BIE menu should have a title element with the content “Generate Expression for a Profiled Component, Noun, BOD”.

#### Test Assertion #7.1.6
The Core Component menu should have a title element with the content “Manage Model Library”.

### Test Step Pre-condition:



### Test Step:

1. An OAGi developer logs into the system.
2. He visits the homepage.
3. Verify that the navigation menu buttons have the 1-6 titles when the mouse hovers on them. (Assertion [#1](#test-assertion-711)-[#6](#test-assertion-716))

## Test Case 7.2

> OAGi Terminology View/Edit BIE Page

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #7.2.1
The name of the page should be “BIEs (Profiled Components, Nouns, BODs)”.

### Test Step Pre-condition:



### Test Step:

1. An OAGi developer logs into the system.
2. He visits the View/Edit BIE page.
3. Verify that the title is “BIEs (Profiled Components, Nouns, BODs)” (Assertion [#1](#test-assertion-721)).

## Test Case 7.3

> OAGi Terminology Create BIE Page

Pre-condition: OAGi terminology has been selected


### Test Assertion:

#### Test Assertion #7.3.1
The first page used in creating a BIE, namely the page where a Business Context is selected should have the title “Create BIE (Profiled Component, Noun, BOD)”.

#### Test Assertion #7.3.2
The second page used in creating a BIE, namely the page where a top level concept is selected, should have the title “Create BIE (Profiled Component, Noun, BOD)”.

### Test Step Pre-condition:



### Test Step:

1. An OAGi developer logs into the system.
2. He visits the Create BIE page.
3. Verify that the title is “Profiled Component, Noun, BOD”. (Assertion [#1](#test-assertion-731))
4. He chooses a business context and clicks the Next button
5. Verify that the title is “Profiled Component, Noun, BOD”. (Assertion [#2](#test-assertion-732))

## Test Case 7.4

> OAGi Terminology Copy BIE Page

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #7.4.1
The Copy BIE page (BIE -> Copy BIE) should have the name “Copy BIE (Profiled Component, Noun, BOD)”.

#### Test Assertion #7.4.2
The subtitle of the second step/page of creating a BIE should be “Select BIE (Profiled Component, Noun, BOD)”.

### Test Step Pre-condition:



### Test Step:

1. An OAGi developer logs into the system.
2. He visits the Copy BIE page
3. Verify that the title is “Copy BIE (Profiled Component, Noun, BOD)”. (Assertion [#1](#test-assertion-741))
4. He chooses a business context and click the Next button.
5. Verify that the subtitle of the second page is “Select BIE (Profiled Component, Noun, BOD)”. (Assertion [#2](#test-assertion-742))

## Test Case 7.5

> OAGi Terminology Append an Association Dialog

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #7.5.1
The Den field of the dialog where you append an ASCCP while editing an extension (local or global) should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.5.2
The Module field filter of the dialog where you append an ASCCP while editing an extension (local or global) should be “Module (Part of schema file path, no extension)”.

#### Test Assertion #7.5.3
The Den field of the dialog where you append a BCCP while editing an extension (local or global) should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.5.4
The Module field filter of the dialog where you append a BCCP while editing an extension (local or global) should be “Module (Part of schema file path, no extension)”.

### Test Step Pre-condition:

1. There is the BIE BIE0 created by the developer account used in asserting this test case.

### Test Step:

1. An OAGi developer logs into the system.
2. The developer opens the BIE0 and extends it locally.
3. He opens the BIE0’s local extension.
4. He appends an ASCCP.
5. Verify that the dialog where you select an ASCCP to append has the search field “DEN (Directory Entry Name)”. (Assertion [#1](#test-assertion-751))
6. Verify that the dialog where you select an ASCCP to append has the search field “Module (Part of schema file path, no extension)”. (Assertion [#2](#test-assertion-752))
7. He closes the dialog and tries to append a BCCP.
8. Verify that the dialog where you select an BCCP to append has the search field “DEN (Directory Entry Name)”. (Assertion [#3](#test-assertion-753))
9. Verify that the dialog where you select an BCCP to append has the search field “Module (Part of schema file path, no extension)”. (Assertion [#4](#test-assertion-754))

## Test Case 7.6

> OAGi Terminology Core Component

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #7.6.1
The name of the Core Component page should be “Core Components (Model Library)”.

#### Test Assertion #7.6.2
The Den field filter of the Core Component page should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.6.3
The Module field filter of the Core Component page should be “Module (Part of schema file path, no extension)”.

#### Test Assertion #7.6.4
The title of the left pane where the tree of an ACC is displayed should be “ACC (Component Type or Group Definition)”.

#### Test Assertion #7.6.5
The Object Class Term of an ACC should be “Object Class Term (Space Separated Name)”.

#### Test Assertion #7.6.6
The Den of an ACC should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.6.7
The title of the left pane where the tree of an ASCCP is displayed should be “ASCCP (Component or Group Definition)”.

#### Test Assertion #7.6.8
The title of the right pane where the details of an ASCCP are displayed should be “ASCCP (Associated Component) Detail”.

#### Test Assertion #7.6.9
The Property Term of an ASCCP should be “Property Term (Component Name)”.

#### Test Assertion #7.6.10
The Den of an ASCCP should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.6.11
The title of the right pane where the details of a BCCP are displayed should be “BCCP (Associated Field) Detail”.

#### Test Assertion #7.6.12
The Property Term of a BCCP should be “Property Term (Field Name)”.

#### Test Assertion #7.6.13
The Den of a BCCP should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.6.14
The title of the right pane where the details of an ASCC are displayed should be “ASCC (Component Association) Detail”. (It cannot be checked yet)

#### Test Assertion #7.6.15
The Max Cardinality field of an ASCC should be “Cardinality Max (-1 for unbounded)”.

#### Test Assertion #7.6.16
The Den of an ASCC should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.6.17
The title of the right pane where the details of a BCC are displayed should be “BCC (Field Association) Detail”

#### Test Assertion #7.6.18
The Max Cardinality field of a BCC should be “Cardinality Max (-1 for unbounded)”.

#### Test Assertion #7.6.19
The Den of n BCC should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.6.20
The title of the right pane where the details of a supplementary component are displayed should be “Supplementary Component (Field Metadata)”. (It cannot be checked yet)

#### Test Assertion #7.6.21
The Max Cardinality field of a supplementary component should be “Cardinality Max (-1 for unbounded)”.

#### Test Assertion #7.6.22
The Den of a supplementary component should be “DEN (Dictionary Entry Name)”.

#### Test Assertion #7.6.23
The Den of a Data type component should be “DEN (Dictionary Entry Name)”.

### Test Step Pre-condition:

1. There is the BIE BIEa created by the developer account used in asserting this test case. The BIEa is locally and globally extended. These extensions contain an ASCCP and a BCCP association.
2. There is the ACC0, ASCCP0, BCCP0, ASCC0

### Test Step:

1. An OAGi developer logon into the system.
2. He visits the Core Component page.
3. Verify that the title of the page is “Core Components (Model Library)”. (Assertion [#1](#test-assertion-761))
4. Verify that there is the search field “DEN (Directory Entry Name)”. (Assertion [#2](#test-assertion-762))
5. Verify that there is the search field “Module (Part of schema file path, no extension)”. (Assertion [#3](#test-assertion-763))
6. He opens the ACC0
7. Verify that the left panel where the tree of the ACC0 is displayed has the title “ACC (Component Type or Group Definition)”. (Assertion [#4](#test-assertion-764))
8. Verify that Object Class Term of the ACC0 is “Object Class Term (Space Separated Name)”. (Assertion [#5](#test-assertion-765))
9. Verify that there is the “DEN (Dictionary Entry Name)” input field. (Assertion [#6](#test-assertion-766))
10. The developer visits the Core Component page.
11. He opens the ASCCP0
12. Verify that the left panel where the tree of the ASCCP0 is displayed has the title “ASCCP (Associated Component) Detail”. (Assertion [#7](#test-assertion-767))
13. Verify that the title of the right panel where the details of an ASCCP are displayed is “ASCCP (Associated Component) Detail”. (Assertion [#8](#test-assertion-768))
14. Verify that there is the field “Property Term (Component Name)”. (Assertion [#9](#test-assertion-769))
15. Verify that there is the “DEN (Dictionary Entry Name)” input field. (Assertion [#10](#test-assertion-7610))
16. The developer opens the BIEa’s local extension and clicks to view an ASCCP.
17. Verify that Object Class Term is “ASCCP (Associated Component) Detail”. (Assertion [#8](#test-assertion-768))
18. Verify that there is the “DEN (Dictionary Entry Name)” input field. (Assertion [#10](#test-assertion-7610))
19. Verify that there is the field “Property Term (Component Name)”. (Assertion [#9](#test-assertion-769))
20. The developer opens the BIEa’s global extension and clicks to view an ASCCP
21. Verify that Object Class Term is “ASCCP (Associated Component) Detail (Assertion [#8](#test-assertion-768))
22. Verify that there is the “DEN (Dictionary Entry Name)” input field. (Assertion [#10](#test-assertion-7610))
23. Verify that there is the field “Property Term (Component Name)”. (Assertion [#9](#test-assertion-769))
24. The developer visits the Core Component page.
25. He opens the BCCP0
26. Verify that the object class term is “BCCP (Associated Field) Detail”. (Assertion [#11](#test-assertion-7611))
27. Verify that there is the field “Property Term (Component Name)”. (Assertion [#12](#test-assertion-7612))
28. Verify that there is the “DEN (Dictionary Entry Name)” input field. (Assertion [#13](#test-assertion-7613))
29. The developer opens the BIEa’s local extension and clicks to view an BCCP.
30. Verify that the class term is “BCCP (Associated Field) Detail”. (Assertion [#11](#test-assertion-7611))
31. Verify that there is the field “Property Term (Component Name)”. (Assertion [#12](#test-assertion-7612))
32. Verify that there is the “DEN (Dictionary Entry Name)” input field. (Assertion [#13](#test-assertion-7613))
33. The developer opens the BIEa’s global extension and clicks to view an BCCP.
34. Verify that the class term is “BCCP (Associated Field) Detail”. (Assertion [#11](#test-assertion-7611))
35. Verify that there is the field “Property Term (Component Name)”. (Assertion [#12](#test-assertion-7612))
36. Verify that there is the “DEN (Dictionary Entry Name)” input field. (Assertion [#13](#test-assertion-7613))
37. The developer opens the BIEa’s local extension and clicks to view an ASCC.
38. Verify that the class term is “ASCC (Component Association) Detail”. (Assertion [#14](#test-assertion-7614))
39. Verify that the name of the Max Cardinality field is “Cardinality Max (-1 for unbounded)”. (Assertion [#15](#test-assertion-7615))
40. Verify that there is the field “DEN (Dictionary Entry Name)”. (Assertion [#16](#test-assertion-7616))
41. The developer opens the BIEa’s global extension and clicks to view an ASCC.
42. Verify that the class term is “ASCC (Component Association) Detail”. (Assertion [#14](#test-assertion-7614))
43. Verify that the name of the Max Cardinality field is “Cardinality Max (-1 for unbounded)”. (Assertion [#15](#test-assertion-7615))
44. Verify that there is the field “DEN (Dictionary Entry Name)”. (Assertion [#16](#test-assertion-7616))
45. The developer opens the BIEa’s local extension and clicks to view a BCC.
46. Verify that the class term is “BCC (Field Association) Detail”. (Assertion [#17](#test-assertion-7617))
47. Verify that the name of the Max Cardinality field is “Cardinality Max (-1 for unbounded)”. (Assertion [#18](#test-assertion-7618))
48. Verify that there is the field “DEN (Dictionary Entry Name)”. (Assertion [#19](#test-assertion-7619))
49. The developer opens the BIEa’s global extension and clicks to view a BCC.
50. Verify that the class term is “BCC (Field Association) Detail”. (Assertion [#17](#test-assertion-7617))
51. Verify that the name of the Max Cardinality field is “Cardinality Max (-1 for unbounded)”. (Assertion [#18](#test-assertion-7618))
52. Verify that there is the field “DEN (Dictionary Entry Name)”. (Assertion [#19](#test-assertion-7619))
53. The developer opens the BIEa’s local extension and clicks to view a supplementary component.
54. Verify that the name of the Max Cardinality field is “Cardinality Max (-1 for unbounded)”. (Assertion [#21](#test-assertion-7621))
55. Verify that there is the field “DEN (Dictionary Entry Name)”. (Assertion [#22](#test-assertion-7622))
56. The developer opens the BIEa’s global extension and clicks to view a supplementary component.
57. Verify that the name of the Max Cardinality field is “Cardinality Max (-1 for unbounded)”. (Assertion [#21](#test-assertion-7621))
58. Verify that there is the field “DEN (Dictionary Entry Name)”. (Assertion [#22](#test-assertion-7622))
59. The developer opens the BIEa’s local extension and clicks to view a Data Type component.
60. Verify that there is the field “DEN (Dictionary Entry Name)”. (Assertion [#23](#test-assertion-7623))
61. The developer opens the BIEa’s global extension and clicks to view a Data Type component
62. Verify that there is the field “DEN (Dictionary Entry Name)”. (Assertion [#23](#test-assertion-7623))