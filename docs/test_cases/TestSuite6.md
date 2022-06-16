# Test Suite 6

> End user access right to Score core functions

Test cases in here should be similar to those in Test Suite 5. However, I'm thinking we don't have to test as comprehensive as Test Suite 5 when the behaviors are expected to be the same regardless of the user role. This test suite should make sure that the end user can see and do and cannot see and do as appropriate to the user role.

## Test Case 6.1

> End user's authorized management of context schemes

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #6.1.1
The end user can see, in the context scheme list, all context schemes created by any user.

#### Test Assertion #6.1.2
The end user can edit a context scheme created by any user.

#### Test Assertion #6.1.3
The end user can add to a context scheme code list values from Published Code Lists. (The code values are simply copied into the context scheme). The end user shall be notified that existing values will be removed. Test for:

##### Test Assertion #6.1.3.a
A developer code list in the latest release.
##### Test Assertion #6.1.3.b
A developer code list in an older release.

#### Test Assertion #6.1.4
The end user can add to a context scheme code list values from Production Code Lists. Test for

##### Test Assertion #6.1.4.a
An end user code list in the latest release that is derived from another developer code list
##### Test Assertion #6.1.4.b
An end user code list in a non latest release that is derived from another developer code list

#### Test Assertion #6.1.5
The end user can select a Context Scheme from the Context Scheme List page, navigate through different paginator pages while the forenamed Context Scheme remains checked.

### Test Step Pre-condition:



### Test Step:

## Test Case 6.2

> End user authorized management of BIE

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #6.2.1
The end user can invoke a local extension when the BIE is in WIP state and there is no corresponding UEGACC (user extension group ACC) in WIP or QA state. The UEGACC shall have an incremental revision number.

#### Test Assertion #6.2.2
When the end user invokes a local extension and the corresponding UEGACC is in WIP or QA state and the user is not the current owner of the UEGACC, display a dialog indicating “The core component is being extended by “ + [the owner of the UEGACC] or similar. If the UEGACC is in QA state, the end user can view its details but cannot make any change.

#### Test Assertion #6.2.3
When the end user invokes a local extension and the corresponding UEGACC is in WIP or QA state and the user is the current owner of the UEGACC, open the UEGACC.

#### Test Assertion #6.2.4
When the end user expands the Extension node of a BIE and there is a corresponding UEGACC not in Production state, the UI must grey out and make all the associations (and their descendants) added to the UEGACC uneditable.

#### Test Assertion #6.2.5
When the end user expands the Extension node of a BIE and there is a corresponding UEGACC in Production state but:

##### Test Assertion #6.2.5.a
It has child association to an end user ASCCP which is not in Production state, that child node and all its descendants shall be greyed out and uneditable.
##### Test Assertion #6.2.5.b
It has a child association to an end user ASCCP that is in the Production state, but the end user ACC (of the ASCCP) is not in the Production state. That child node and all of its descendants shall be greyed out and uneditable.
##### Test Assertion #6.2.5.c
It has child association to an end user BCCP which is not in Production state, that child node and all its descendants shall be greyed out and uneditable.
##### Test Assertion #6.2.5.d
It has a child association to an end user ASCCP that is in the Production state. The end user ACC of the ASCCP is also in the Production state and was amended. The ACC has a child ASCC that points another end user ASCCP2 that is not in the Production state. That ASCC/ASCCP2 node shall be grey out and uneditable.
##### Test Assertion #6.2.5.e
It has a child association to an end user ASCCP that is in the Production state. The end user ACC of the ASCCP has a Group component type and is NOT in the Production state. Either NO children of that group ACC shall be visible, or all children shall be grey out and uneditable.

#### Test Assertion #6.2.6
Do all previous again targeting the same BIE and BIE extension but use another release. Make sure to introduce some revisions in the CCs used by the BIE, BIE extension point, and the UEGACC. Even use the EUCC with the same name but different slightly different content. In addition to verifying 5.1 to 5.5, verify that the expected differences between revisions are there.

#### Test Assertion #6.2.7
Apply test assertion 1 to 5 to the global extension except appending an ASCCP to the global extension. Test also that an ASCCP cannot be appended to the global extension.

#### Test Assertion #6.2.8
A code list which is an extension of a default code list (aka compatible code lists) used by a BBIE must show up for the code list selection, when the Value Domain Type is set to “Code”. Specifically:

##### Test Assertion #6.2.8.a
Only production, compatible code lists in the same release as the BIE shall be included, i.e., a code list exists only in a newer release shall not be included.
##### Test Assertion #6.2.8.b
Compatible end user code lists in the same release as the BIE shall be included. However, if it is in the WIP or QA state, flag that the code list is being changed (maybe use dark yellow and italicized font – yellow like a warning light) (the meaning is the code list is usable but unstable. If the user express BIE, the result is in the limbo status). If the code list is in Deleted state use Strikethrough font.
##### Test Assertion #6.2.8.c
If there is no default code list, all developer code lists in the published state in the same release and end user code lists in the same release shall be included. End user code lists shall be displayed in the same way as described in 8.2.

#### Test Assertion #6.2.9
End user cannot create a new BIE from an ASCCP whose ACC has a group component type.

#### Test Assertion #6.2.10
The end user can click the Detail Reset button of a specific BIE node to initial the values of the BIE node. These values are based on the corresponding CC of the BIE node. A confirmation dialog should be also returned in order for the end user to confirm his intension of resetting the BIE node values.

#### Test Assertion #6.2.11
The end user can Exclude SCs or not from the Searching Field by checking or unchecking the “Exclude SCs” checkbox accordingly.

##### Test Assertion #6.2.11.a
If the “Exclude SCs” checkbox is enabled (i.e., checked) the SCs are excluding from the searching field
##### Test Assertion #6.2.11.b
If the “Exclude SCs” checkbox is disabled (i.e., unchecked) the SCs are excluding from the searching field

#### Test Assertion #6.2.12
The end user can create a BIE from an end user ASCCP providing that it is in Production state. Check that the end user can create BIEs from both ASCCP that he owns or not.

#### Test Assertion #6.2.13
The end user cannot create a new BIE from an ASCCP whose ACC has a group component type.

#### Test Assertion #6.2.14
The end user can edit the BIE if the end user ASCCP is in Production State

##### Test Assertion #6.2.14.a
If the end user ASCCP is amended (i.e., moved to WIP state), the BIE cannot be edited. The fields of the BIE nodes are disabled including the “Used” checkbox.
##### Test Assertion #6.2.14.b
If the end user ASCCP is moved to the QA state, the BIE cannot be edited. The fields of all BIE nodes (ancestors and descendants) are disabled including the “Used” checkbox.
##### Test Assertion #6.2.14.c
If the end user ASCCP is moved to the Deprecated state (i.e., it is deprecated), flag the root node of the BIE to indicate that status.
##### Test Assertion #6.2.14.d
If any of the nodes of the base ACC of the ASCCP is not in Production state, their corresponding BIE nodes cannot be edited. Check the base ACC of the base ACC of the ASCCP. Also check an ASCCP and BCCP node of the base ACC of the base ACC of the ASCCP.
##### Test Assertion #6.2.14.e
If the corresponding CC of a BIE node is Deleted, the BIE node cannot be edited. It can be also flagged as deleted.
##### Test Assertion #6.2.14.f
If any child or descendant properties are from group and the group is not in Production state, those properties have to be locked in the BIE.

### Test Step Pre-condition:



### Test Step:

## Test Case 6.3

> End user authorized access to BIE Expression generation

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #6.3.1
The end user can generate an expression of a BIE that he owns in any state. Test for:

##### Test Assertion #6.3.1.a
When the latest release is selected.
##### Test Assertion #6.3.1.b
When one of the older releases is selected.

#### Test Assertion #6.3.2
The end user cannot generate an expression of a BIE which is in WIP state and owned by another user.

#### Test Assertion #6.3.3
The end user can generate an expression of a BIE which is in QA state and owned by another user.

#### Test Assertion #6.3.4
The end user can generate an expression of a Production BIE owned by another user.

#### Test Assertion #6.3.5
The end user can generate an expression of a single BIE, in XML Schema, in the same package, with no annotation selected.

#### Test Assertion #6.3.6
The end user can generate an expression of a single BIE, in XML Schema, in the same package, with all the annotations selected.

#### Test Assertion #6.3.7
The end user can generate an expression of a single BIE, in XML Schema, in the same package, with the BIE CCTS Meta Data annotation selected but not the Include CCTS_Definition Tag annotation.

#### Test Assertion #6.3.8
The end user can generate an expression of a single BIE, in XML Schema, in the same package, with the BIE OAGi/Score Meta Data annotation selected but not the Include WHO Columns annotation.

#### Test Assertion #6.3.9
The end user cannot generate an expression of a single BIE, in XML Schema, with the Include CCTS_Definition Tag annotation selected but not the BIE CCTS Meta Data annotation.

#### Test Assertion #6.3.10
The end user cannot generate an expression of a single BIE, in XML Schema, with the Include WHO Columns annotation selected but not the BIE OAGi/Score Meta Data annotation.

#### Test Assertion #6.3.11
The end user can generate an expression of a single BIE, in JSON Schema, in the same package, with no annotation selected.

#### Test Assertion #6.3.12
The end user can generate an expression of a single BIE, in JSON Schema, in the same package, with the BIE Definition annotation selected.

#### Test Assertion #6.3.13
The end user cannot generate an expression of a single BIE, in JSON Schema, in the same package, with any of the BIE CCTS Meta Data, Include CCTS_Definition Tag, BIE GUID, Business Context, BIE OAGi/Score Meta Data, Include WHO Columns, Based CC Meta Data annotations selected.

#### Test Assertion #6.3.14
The end user can generate an expression of multiple BIEs, in multiple XML Schemas, saved in the same package, with some annotations selected.

#### Test Assertion #6.3.15
The end user can generate an expression of multiple BIEs, in XML Schemas, saved in different packages, with some annotations selected.

#### Test Assertion #6.3.16
The end user can generate an expression of multiple BIEs, in JSON Schemas, saved in the same package, with the BIE Definition selected.

#### Test Assertion #6.3.17
The end user can generate an expression of multiple BIEs, in JSON Schemas, saved in different packages, with the BIE Definition selected.

#### Test Assertion #6.3.18
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header (Include Meta Header). Also, verify that the “metaheader” property is an object and not an array.

##### Test Assertion #6.3.18.a
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header and the option “Make as an array”. Verify that the “metaheader” property is an object and not an array.
##### Test Assertion #6.3.18.b
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header and a Pagination Response. Verify that the “metaheader” property is an object and not an array.
##### Test Assertion #6.3.18.c
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header, Pagination Response and the option “Make as an array”. Verify that the “metaheader” property is an object and not an array.

#### Test Assertion #6.3.19
The end user can generate an expression of multiple BIEs, in JSON Schemas, saved in different packages, while selecting a Meta Header (Include Meta Header).

#### Test Assertion #6.3.20
The search function is at least working.

#### Test Assertion #6.3.21
The number of BIEs in the Express BIE page should be the same with the number of BIEs displaying in the index box located at the bottom right of the page.

#### Test Assertion #6.3.22
The end user can generate an expression of a single BIE with multiple business contexts assigned, in XML Schema, in the same package, with the Business Context annotation selected.

#### Test Assertion #6.3.23
The end user can generate Open API 3.0 in YAML with GET Operation Template that includes Make Array, Meta Header, and Pagination Response options and POST Operation Template that includes Meta Header and Make Array options. The end user must be able to select Meta Header BIE and Pagination Response BIE owned by another end user only in QA or Production state.

#### Test Assertion #6.3.24
The end user can generate Open API 3.0 in JSON with GET Operation Template that includes Meta Header, Pagination Response, and Make Array option and POST Operation Template that includes Meta Header and Make Array option. The end user must be able to select Meta Header BIE and Pagination Response BIE owned by a developer user only in QA or Production state.

#### Test Assertion #6.3.25
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Pagination Response (Include Pagination Response). Also, verify that the “paginationResponse” property is an object and not an array.

##### Test Assertion #6.3.25.a
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Pagination Response and the option “Make as an array”. Verify that the “paginationResponse” property is an object and not an array.

#### Test Assertion #6.3.26
The end user cannot generate an expression of multiple BIEs, in JSON Schemas, saved in the same package if he has selected a Meta Header or a Pagination Response (the option “Put all schemas in the same file” is disabled).

#### Test Assertion #6.3.27
The end user can express a reusing BIE when the same reused BIE is reused in multiple places. Check both XML and JSON generation.

#### Test Assertion #6.3.28
The end user can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, in single file.

#### Test Assertion #6.3.29
The end user can generate an expression of a multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly saved in different packages.

#### Test Assertion #6.3.30
The end user can generate an expression of a single BIE, in Open API 3.0 in JSON with Code Generation Friendly, in single file.

#### Test Assertion #6.3.31
The end user can generate an expression of a multiple BIEs, in Open API 3.0 in JSON with Code Generation Friendly saved in different packages.

#### Test Assertion #6.3.32
The end user can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template and Make Array option, in single file.

#### Test Assertion #6.3.33
The end user can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template and Make Array option, in different packages.

#### Test Assertion #6.3.34
The end user can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, in single file.

#### Test Assertion #6.3.35
The end user can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, in different packages.

#### Test Assertion #6.3.36
The end user can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header and Pagination Response, in single file.

#### Test Assertion #6.3.37
The end user can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header and Pagination Response, in different packages.

#### Test Assertion #6.3.38
The end user can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, POST Operation Template and Make Array option, in single file.

#### Test Assertion #6.3.39
The end user can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, POST Operation Template and Make Array option, in different packages.

#### Test Assertion #6.3.40
The end user can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, POST Operation Template that includes Meta Header, in single file.

#### Test Assertion #6.3.41
The end user can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, POST Operation Template that includes Meta Header, in different packages.

#### Test Assertion #6.3.42
The end user can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option, in single file.

#### Test Assertion #6.3.43
The end user can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option, in different packages.

#### Test Assertion #6.3.44
The end user can generate an expression of a single BIE, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option as well as POST Operation Template that includes Meta Header, Pagination Response, and Make Array option, in single file.

#### Test Assertion #6.3.45
The end user can generate an expression of multiple BIEs, in Open API 3.0 in YAML with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option as well as POST Operation Template that includes Meta Header, Pagination Response, and Make Array option, in different packages.

#### Test Assertion #6.3.46
The end user can generate an expression of a single BIE, in Open API 3.0 in JSON with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option as well as POST Operation Template that includes Meta Header, Pagination Response, and Make Array option, in single file.

#### Test Assertion #6.3.47
The end user can generate an expression of multiple BIEs, in Open API 3.0 in JSON with Code Generation Friendly, GET Operation Template that includes Meta Header, Pagination Response, and Make Array option as well as POST Operation Template that includes Meta Header, Pagination Response, and Make Array option, in different packages.

### Test Step Pre-condition:

1. There are some BIEs created by users, namely BIEa, BIEb, BIEc, which are in Editing, Candidate and Published correspondingly. Additionally, there are some BIEs created by a developer, namely BIE0, BIE1, BIE2, which are in Editing, Candidate and Published correspondingly. The name of the BIE1 is “Acknowledge Receive Item”. Finally, there is a BIE, BIE3, created by a developer with multiple business contexts assigned.

### Test Step:

1. An end user logs into the system.
2. He goes to the Generate Expression page.
3. Verify that he cannot view the BIE0 and so he cannot generate an expression of it, while he can view BIEa, BIEb, BIEc, BIE1 and BIE2 and so he can generate an expression of them. (Assertion [#1](#test-assertion-631) [#2](#test-assertion-632) [#3](#test-assertion-633) [#4](#test-assertion-634))
4. The user generates an expression from BIEb, in XML Schema, selecting no annotation and that the scheme will be saved in the same package.
5. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#5](#test-assertion-635))
6. The user generates an expression from BIEc, in XML Schema, selecting all the annotations and that the scheme will be saved in the same package.
7. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#6](#test-assertion-636))
8. The user generates an expression from BIE1, in XML Schema, selecting the BIE CCTS Meta Data annotation but not the Include CCTS_Definition Tag annotation and that the scheme will be saved in the same package.
9. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#7](#test-assertion-637))
10. The user generates an expression from BIE2, in XML Schema, selecting the BIE OAGi/Score Meta Data annotation but not the Include WHO Columns annotation and that the schema will be saved in the same package.
11. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#8](#test-assertion-638))
12. The user generates an expression from BIE2, in XML Schema, selecting the Include CCTS_Definition Tag annotation but not the BIE CCTS Meta Data annotation.
13. Verify that the Include CCTS_Definition Tag annotation cannot be selected without selecting the BIE CCTS Meta Data annotation first. (Assertion [#9](#test-assertion-639))
14. The user generates an expression from BIE2, in XML Schema, selecting the Include WHO Columns annotation but not the BIE OAGi/Score Meta Data annotation.
15. Verify that the Include WHO Columns annotation cannot be selected without selecting the BIE OAGi/Score Meta Data annotation first. (Assertion [#10](#test-assertion-6310))
16. The user generates an expression from BIEb, in JSON Schema, selecting that the schema will be saved in the same package and without selecting any annotation.
17. Verify that the Schema is successfully generated. (Assertion [#11](#test-assertion-6311))
18. The user generates an expression from BIE1, in JSON Schema, selecting the BIE Definition annotation and that the schema will be saved in the same package.
19. Verify that the Schema is successfully generated and saved in the same package. (Assertion [#12](#test-assertion-6312))
20. The user generates an expression from BIE2, in JSON Schema, selecting the BIE CCTS Meta Data, Include CCTS_Definition Tag, BIE GUID, Business Context, BIE OAGi/Score Meta Data, Include WHO Columns, Based CC Meta Data annotations.
21. Verify that the aforementioned annotations cannot be selected. (Assertion [#13](#test-assertion-6313))
22. The user generates an expression from BIEb and BIE1, in XML Schemas, selecting some annotations and that the XML Schemas will be saved in the same package.
23. Verify that the Schemas are successfully generated and saved in the same package. (Assertion [#14](#test-assertion-6314))
24. The user generates an expression from BIEb and BIE1, in XML Schemas, selecting some annotations and that the XML Schemas will be saved in different packages.
25. Verify that the Schemas are successfully generated and saved in different packages. (Assertion [#15](#test-assertion-6315))
26. The user generates an expression from BIEb and BIE1, in JSON Schemas, selecting the BIE definition annotation and that the JSON Schemas will be saved in the same package.
27. Verify that the Schemas are successfully generated and saved in the same package. (Assertion [#16](#test-assertion-6316))
28. The user generates an expression from BIEb and BIE1, in JSON Schemas, selecting the BIE Definition annotation and that the JSON Schemas will be saved in different packages.
29. Verify that the Schemas are successfully generated and saved in different packages. (Assertion [#17](#test-assertion-6317))
30. The user generates an expression from BIE1, in JSON Schema, selecting the BIE Definition annotation and that the schema will be saved in the same package. Also, he selects Include Meta Header and selects a corresponding BIE.
31. Verify that the Schema is successfully generated (Assertion [#18](#test-assertion-6318))
32. The user generates an expression from BIEb and BIE1, in JSON Schemas, selecting the BIE Definition annotation and that the schemas will be saved in different packages. Also, he selects Include Meta Header and selects a corresponding BIE.
33. Verify that the Schemas are successfully generated (Assertion [#19](#test-assertion-6319))
34. The user enters the search term “eceive” in the Property Term search field that partially matches some BIEs’ names.
35. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-6320))
36. The user clears all filters and he chooses user devx at the Owner search filter select.
37. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-6320))
38. The user clears all filters and he chooses devx at the Updater search filter.
39. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-6320))
40. The user clears all filters and he chooses a recent day at the Updated start date search filter.
41. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-6320))
42. The user clears all filters and he chooses the Candidate state at the State search filter.
43. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-6320))
44. The user clears all filters and enters the search term “initBC” in Business Context Search field that matches some BIE’s business contexts.
45. Verify that at least the BIE “Receive Item” is returned. (Assertion [#20](#test-assertion-6320))
46. The user opens the “Express BIE” page.
47. Verify that the number of BIEs is the same with the number of BIEs displaying in the index box located at the bottom right of the page by visiting each index page. (Assertion [#21](#test-assertion-6321))
48. The user opens the “Express BIE” page.
49. The user generates an expression from BIE3, in XML Schema, selecting the Business Context annotation.
50. Verify that the Schema is successfully generated. (Assertion [#22](#test-assertion-6322))