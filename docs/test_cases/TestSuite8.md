# Test Suite 8

For now, we don’t have to test for separate user roles b/c we only want to cover the search and view function and both user roles have the same right.

## Test Case 8.1

> 

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #8.1.1
The developer can filter Core Components based on the branch.

#### Test Assertion #8.1.2
If all types and states are selected, all CCs in the branch shall be returned along with the User Extension Group Aggregate Core Components (UEGACC). The UEGASCC and UEGASCCP must not be shown.

#### Test Assertion #8.1.3
The developer can filter Core Components based on their Type.

#### Test Assertion #8.1.4
The developer can filter Core Components based on their State.

#### Test Assertion #8.1.5
The developer can filter Core Components based on their Updated Date.

#### Test Assertion #8.1.6
The developer can search for Core Components based only on their DEN.

#### Test Assertion #8.1.7
The developer can search for Core Components based only on their Definition.

#### Test Assertion #8.1.8
The developer can search for Core Components based only on their Module.

#### Test Assertion #8.1.9
The developer can search for Core Components based only on their Component Type.

#### Test Assertion #8.1.10
The developer can see in the list, Core Components that he owns, does not owns and in any state.

#### Test Assertion #8.1.11
The developer can view details of a Core Component that he owns regardless of its state.

#### Test Assertion #8.1.12
If a working branch is selected, no UEGCC shall be listed.

#### Test Assertion #8.1.13
If a specific branch is selected, UEGACC shall be listed but UEGASCCP and UGASCC shall not be listed.

#### Test Assertion #8.1.14
The developer cannot view the detail of a user extension Core Component which is in WIP state and created by another user.

#### Test Assertion #8.1.15
The developer can view a UEGACC which is in QA state and created by another user.

#### Test Assertion #8.1.16
The developer can view a production UEGACC created by another user.

### Test Step Pre-condition:

1. There is an ACC, say ACCa, which DEN is “Identifiers”, Definition is “A group that contains the identifier for the associated component or noun.” and Module is “Model\Platform\2_6\Common\Components\Components”.
2. There are user extension Core Components, say ACC0c, ASCC0c, BCC0c, created by an end user during the extension of a BIE and which are in editing state.
3. There are user extension Core Components, say ACC1c, ASCC1c, BCC1c, created by an end user during the extension of a BIE and which are in candidate state.
4. There are user extension Core Components, say ACC2c, ASCC2c, BCC2c, created by an end user during the extension of a BIE and which are in published state.
5. There are user extension Core Components, say ACC0d, ASCC0d, BCC0d, created by the developer during the extension of a BIE and which are in editing state.
6. There are user extension Core Components, say ACC1d, ASCC1d, BCC1d, created by the developer during the extension of a BIE and which are in Candidate state.
7. There are user extension Core Components, say ACC2d, ASCC2d, BCC2d, created by the developer during the extension of a BIE and which are in Published state.
8. There are the Core Components CC0 “Acknowledge Allocate Resource. Details”, CC1 “Acknowledge BOM Data Area. Details”, CC2 “Acceptable Name Value Range. Name Value Range” belonging to the 10.6 branch.
9. There are the Core Component ASCCP0 “ASN Reference. Document Reference”, BCCP0 “Absence Type Code. Code”, ACC0 “Account Information. Details”, BCC0 “Account Information. Name On Account. Name”, and ASCC0 “Accounting Period Base. Effective Time Period. Time Period” belonging to the 10.6 branch.

### Test Step:

1. An OAGi developer logs into the system.
2. He visits the Core Component page.
3. The developer selects branch 10.6.
4. The developer filters Core Components based on their branch while all types and states are selected.
5. Verify that at least the CC0, CC1, CC2 are returned in the result page. Also verify that only UEG ACC mentioned in the preconditions are returned. (Assertion [#1](#test-assertion-811), [#2](#test-assertion-812))
6. The developer chooses “Working” branch.
7. Verify that no UEG CC is listed. (Assertion [#1](#test-assertion-811), [#2](#test-assertion-812))
8. The developer filters Core Components having selected all types and states.
9. Verify that at least all the Core Components mentioned in the precondition section are returned in the result page. Also verify that all types of CCs exist in the result, namely BCC, ASCC, ASCCP, ACC and BCCP. Also verify that no UEGASCC and UEGASCCP is returned. (Assertion [#2](#test-assertion-812))
10. The developer filters Core Components based on type ACC while having selected all states.
11. Verify that at least the ACC0 is returned and that there is no ASCCP, BCCP, ASCC, BCC in the first 100 results of the returned list. not ...... in the result page. (Assertion [#3](#test-assertion-813))
12. The developer filters Core Components based on type ASCC while having selected all states
13. Verify that at least the ASCC0 is returned and that there is no ACC, ASCCP, BCCP, BCC in the first 100 results of the returned list. (Assertion [#3](#test-assertion-813))
14. The developer filters Core Components based on type BCC while having selected all states.
15. Verify that at least the BCC0 is returned that there is no ACC, ASCC, ASCCP, BCCP, BCC in the first 100 results of the returned list. (Assertion [#3](#test-assertion-813))
16. The developer filters Core Components based on type ASCCP while having selected all states.
17. Verify that at least the ASCCP0 is returned and that there is no ACC, ASCC, BCCP, BCC in the first 100 results of the returned list. (Assertion [#3](#test-assertion-813))
18. The developer filters Core Components based on type BCCP while having selected all states.
19. Verify that at least the BCCP0 is returned and that there is no ACC, ASCC, ASCCP, BCC in the first 100 results of the returned list. (Assertion [#3](#test-assertion-813))
20. The developer filters CCs having selected all types and states.
21. Verify that at least one ACC, ASCC, ASCCP, BCCP and BCC are returned. (Assertion [#3](#test-assertion-813))
22. The developer filters CCs having selected all states but no type.
23. Verify that at least one ACC, ASCC, ASCCP, BCCP and BCC are returned. (Assertion [#3](#test-assertion-813))
24. The developer filters Core Components base on Editing state while having selected all types.
25. Verify that at least the ACC0c and ACC0d are returned and not the ACC1c, ACC2c, ACC1d, ACC2d. Also verify that the is no CC in Candidate or Published state (in the first results) (Assertion [#4](#test-assertion-814))
26. The developer filters Core Components base on Candidate state while having selected all types.
27. Verify that at least the ACC1c and ACC1d are returned and not the ACC0c, ACC2c, ACC0d, ACC2d in the result page. Also, verify that there is no CC in Editing or Published state. (Assertion [#4](#test-assertion-814))
28. The developer filters Core Components base on Published state while having selected all types.
29. Verify that at least the ACC2c and ACC2d are returned and not the ACC0c, ACC1c, ACC0d, ACC1d in the result page. Also, verify that there is no CC in Editing or Candidate state. (Assertion [#4](#test-assertion-814))
30. The developer filters Core Components having selected all states and all types.
31. Verify that at least the ACC0c, ACC0d, ACC1c, ACC1d, ACC2c and ACC2d are returned. (Assertion [#4](#test-assertion-814))
32. The developer filters Core Components having selected all types but no state.
33. Verify that at least the ACC0c, ACC0d, ACC1c, ACC1d, ACC2c and ACC2d are returned. (Assertion [#4](#test-assertion-814))
34. The developer selects the first day of the current month of the Update start and clicks Search button.
35. Verify that the CCs of the preconditions are displayed but not others created during the initialization of the database (e.g. OAGIS10 Nouns. Details) (Assertion # 5)
36. The developer filters Core Components selecting all types and states.
37. He enters the search term “Revised BOM Extension” into the DEN field that partially matches some CC’s names.
38. Verify that at least the “Extension. Revised BOM Extension” and the “Extension. Revised BOM Component Extension” are returned. (Assertion [#6](#test-assertion-816))
39. The developer clears the DEN search field.
40. He enters the term “”Revised BOM Extension”” and clicks the search button.
41. Verify that the “Extension. Revised BOM Extension”  is returned but not the “Extension. Revised BOM Component Extension”. (Assertion [#6](#test-assertion-816))
42. He clears the DEN search field, enters the term “”Action Code”” and clicks the search button.
43. Verify that the BCCP “Action Code. Code” is returned but not the “Corrective Action Type Code. Code””. (Assertion [#6](#test-assertion-816))
44. The developer clears the DEN search field.
45. He enters the search term “Notice Document” into the Definition search field and clicks the search button.
46. Verify that the ASCCP “ASN Reference. Document Reference” and the “Show Receive Delivery. Show Receive Delivery” are returned. (Assertion [#7](#test-assertion-817))
47. The developer clears the Definition search field.
48. He enters the search term “”Notice Document”” into the Definition search field and clicks the search button.
49. Verify that the ASCCP “ASN Reference. Document Reference” is returned but not the “Show Receive Delivery. Show Receive Delivery”. (Assertion [#7](#test-assertion-817))
50. The developer clears the Definition search field.
51. He enters the search term “business entity” into the Definition search field and clicks the search button.
52. Verify that the BCCP “Beneficial Ownership Form Indicator. Indicator” and the “GL Entity Identifier. Identifier” are returned. (Assertion [#7](#test-assertion-817))
53. The developer clears the Definition search field.
54. He enters the search term ““business entity”” into the Definition search field and clicks the search button.
55. Verify that the BCCP “Beneficial Ownership Form Indicator. Indicator” is returned but not the “GL Entity Identifier. Identifier”. (Assertion [#7](#test-assertion-817))
56. He enters the search term “identifier for the associated component” into the Definition field that partially matches some CC’s Definition.
57. Verify that at least the ACCa is returned. (Assertion [#7](#test-assertion-817))
58. The developer clears the Definition search field.
59. He enters the search term “Model\Platform\2_6\Common\Components\Components” into the Module field that partially matches some CC’s modules.
60. Verify that at least the ACCa is returned and that in the result list (at least in the first page) there are CCs belonging to the Module: “Model\Platform\2_6\Common\Components\Components”. (Assertion [#8](#test-assertion-818))
61. The developer clears the Module search field.
62. He enters the search term “common fields” into the Module field that partially matches some CC’s modules.
63. Verify that that in the result list (at least in the first page) there are CCs belonging to the Module: “Model\Platform\2_6\Common\Components\Fields”. (Assertion [#8](#test-assertion-818))
64. The developer visits the CC view page
65. Verify that there are CCs owned by different users (e.g. usera, devx and oagis) and CCs in Editing, Candidate and Published state. (Assertion [#10](#test-assertion-8110))
66. The developer selects all the filters and clears all the search fields.
67. He clicks to view ACCa.
68. He expands the ACCa’s tree and clicks to view the details of different nodes.
69. Verify that he can view the details of the ACCa and those of the nodes selected. (Assertion [#11](#test-assertion-8111))
70. He visits the CC view page.
71. He selects the working branch.
72. Verify that the UEG CC mentioned in the preconditions are not listed. (Assertion [#12](#test-assertion-8112))
73. He selects the 10.6 branch.
74. Verify that the UEGACC mentioned in the preconditions are listed but no UEGASCCP and UGASCC are returned/listed. (Assertion [#13](#test-assertion-8113))
75. The developer clicks to view detail of ACC0c.
76. Verify that he cannot view the detail of ACC0c. There is no link to the ACC0c (Assertion [#14](#test-assertion-8114))
77. The developer goes back to the Core Components page.
78. He clicks to view detail of ACC0d.
79. Verify that he can view the detail of ACC0d. (Assertion [#15](#test-assertion-8115))
80. The developer goes back to the Core Components page.
81. He clicks to view detail of ACC1c.
82. Verify that he can view the detail of ACC1c. (Assertion [#16](#test-assertion-8116))
83. The developer goes back to the Core Components page.
84. He clicks to view detail of ACC1d.
85. Verify that he can view the detail of ACC1d. (Assertion [#16](#test-assertion-8116))
86. The developer goes back to the Core Components page.
87. He clicks to view ACC2c.
88. Verify that he can view the detail of ACC2c. (Assertion [#17](#test-assertion-8117))
89. The developer goes back to the Core Components page.
90. He clicks to view ACC2d.
91. Verify that he can view the detail of ACC2d. (Assertion [#17](#test-assertion-8117))