# Test Suite 6

**End user access right to Score core functions**

> Test cases in here should be similar to those in Test Suite 5. However, I'm thinking we don't have to test as comprehensive as Test Suite 5 when the behaviors are expected to be the same regardless of the user role. This test suite should make sure that the end user can see and do and cannot see and do as appropriate to the user role.

## Test Case 6.1

**End user's authorized management of context schemes**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #6.1.1
The end user can see, in the context scheme list, all context schemes created by any user.

#### Test Assertion #6.1.2
The end user can open and edit a context scheme created by any user.

#### Test Assertion #6.1.3
The end user can load context scheme values from published developer code lists. Existing context scheme values are removed and replaced, and a confirmation message is shown before loading. Test for:

##### Test Assertion #6.1.3.a
A developer code list in the latest release.
##### Test Assertion #6.1.3.b
A developer code list in an older release.

#### Test Assertion #6.1.4
The end user can load context scheme values from production code lists. WIP and QA code lists are not selectable. Test for:

##### Test Assertion #6.1.4.a
An end user code list in the latest release that is derived from a developer code list.
##### Test Assertion #6.1.4.b
An end user code list in an older release that is derived from a developer code list.

#### Test Assertion #6.1.5
The end user can select a Context Scheme from the Context Scheme List page, navigate through different paginator pages while the forenamed Context Scheme remains checked.

### Test Step Pre-condition:

1. The test creates developer, developer admin, end user, and end user admin accounts and their context schemes as needed.
2. Published developer code lists, production code lists, and derived end user code lists are created in the latest and older releases as needed.
3. A developer code list in the `Working` branch and developer code lists in `WIP` and `QA` states are created for the negative visibility checks.


### Test Step:

1. An end user logs into the system.
2. The end user opens the Context Scheme list and verifies that context schemes created by a developer, a developer admin, an end user, and an end user admin are all visible. (Assertion [#1](#test-assertion-611))
3. The end user opens context schemes created by those different user types and verifies that editable header fields and context scheme value fields are enabled. (Assertion [#2](#test-assertion-612))
4. The end user opens an end-user-owned context scheme, loads values from published developer code lists in the latest and older releases, confirms that existing values will be replaced, and verifies that the loaded values are available. The end user also verifies that a developer code list in the `Working` branch cannot be selected. (Assertions [#3](#test-assertion-613), [#3.a](#test-assertion-613a), [#3.b](#test-assertion-613b))
5. The end user opens an end-user-owned context scheme, loads values from production code lists, and verifies that production developer code lists and derived end user production code lists in the latest and older releases are selectable, while `WIP` and `QA` code lists are not selectable. (Assertions [#4](#test-assertion-614), [#4.a](#test-assertion-614a), [#4.b](#test-assertion-614b))
6. The end user selects a context scheme in the list, navigates to another paginator page and back, and verifies that the selection is retained. (Assertion [#5](#test-assertion-615))

## Test Case 6.2

**End user authorized management of BIE**

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
The end user cannot create a new BIE from a selectable ASCCP when its underlying ACC has a group component type.

#### Test Assertion #6.2.10
The end user can click the Detail Reset button of a specific BIE node to reset the values of the BIE node to the initial values based on the corresponding CC. A confirmation dialog is shown before resetting.

#### Test Assertion #6.2.12
The end user can create a BIE from an end user ASCCP providing that it is in Production state. Check that the end user can create BIEs from both ASCCP that he owns or not.

#### Test Assertion #6.2.13
The end user cannot create a new BIE from an end-user-owned ASCCP in `Production` when that ASCCP's ACC has a group component type.

#### Test Assertion #6.2.14
The end user can edit the BIE if the end user ASCCP is in Production State

##### Test Assertion #6.2.14.a
If the end user ASCCP is amended, the editable fields of the affected BIE nodes are disabled. Current automation shows the `Used` checkbox remaining enabled.
##### Test Assertion #6.2.14.b
If the end user ASCCP is moved to the QA state, the editable fields of the affected BIE nodes are disabled. Current automation shows the `Used` checkbox remaining enabled.
##### Test Assertion #6.2.14.c
If the end user ASCCP is moved to the Deprecated state (i.e., it is deprecated), flag the root node of the BIE to indicate that status.
##### Test Assertion #6.2.14.d
If any of the nodes of the base ACC of the ASCCP is not in Production state, the corresponding BIE nodes have their editable fields disabled. This also applies when the affected node belongs to the base ACC of the base ACC of the ASCCP.
##### Test Assertion #6.2.14.e
If the corresponding CC of a BIE node is Deleted, the BIE node cannot be edited. It can be also flagged as deleted.
Current automation status: not automated yet.
##### Test Assertion #6.2.14.f
If any child or descendant properties are from group and the group is not in Production state, those properties have to be locked in the BIE.
Current automation status: not automated yet.

### Test Step Pre-condition:

1. The tests create end user and developer accounts, core components, BIEs, extensions, business contexts, and code lists as needed for each scenario.
2. Local-extension scenarios use WIP end-user BIEs whose extension points are based on published developer core components.
3. Global-extension scenarios use a separate test class and create user extension group ACC content in multiple releases and states as needed.
4. Code-list selection scenarios create developer and end-user derived code lists in multiple states and releases as needed.


### Test Step:

1. An end user logs into the system and opens a BIE in `WIP` to exercise local-extension behavior. The test verifies the first local extension revision is created only for `WIP` BIEs and that later local-extension revisions increment correctly. (Assertion [#1](#test-assertion-621))
2. The test verifies the local-extension locking behavior when another user already owns the extension in `WIP` or `QA`, including the attention dialog, the read-only `QA` view, and the snackbar path after the owner moves the extension back. (Assertions [#2](#test-assertion-622), [#3](#test-assertion-623))
3. The test appends extension content and verifies the editability restrictions for local extensions that are not in `Production`. (Assertion [#4](#test-assertion-624))
4. The test verifies the same extension-locking patterns for local extensions whose related end-user CC content is not in `Production`, including non-production child ASCCP/BCCP cases and amended or non-production descendant/group cases. (Assertions [#5](#test-assertion-625), [#6](#test-assertion-626))
5. The test repeats the extension-locking scenarios in another release and verifies the expected behavior again with revised CC content. (Assertion [#6](#test-assertion-626))
6. The test exercises the global-extension path in a separate class, including the restriction that ASCCP cannot be appended to the global extension. (Assertion [#7](#test-assertion-627))
7. The test verifies code-list selection for BBIE value domains in `Code` mode, covering compatible production developer and end-user code lists in the same release, warnings for unstable end-user code lists, deleted end-user code lists, and the no-default-code-list fallback behavior. (Assertions [#8](#test-assertion-628), [#8.a](#test-assertion-628a), [#8.b](#test-assertion-628b), [#8.c](#test-assertion-628c))
8. The test verifies that an end user cannot create a BIE from a selectable ASCCP whose ACC has a group component type, can reset a BIE node to its initial values after confirmation, can create BIEs from production end-user ASCCPs regardless of ownership, and still cannot create a BIE when such a production end-user ASCCP is backed by a group-type ACC. (Assertions [#9](#test-assertion-629), [#10](#test-assertion-6210), [#12](#test-assertion-6212), [#13](#test-assertion-6213))
9. The test verifies BIE editability when the end-user ASCCP is in `Production`, and then checks the locking/flagging behavior after amendment, `QA`, `Deprecated`, and non-production base-ACC scenarios. Assertions `6.2.14.e` and `6.2.14.f` are documented but not automated yet. (Assertions [#14](#test-assertion-6214), [#14.a](#test-assertion-6214a), [#14.b](#test-assertion-6214b), [#14.c](#test-assertion-6214c), [#14.d](#test-assertion-6214d), [#14.e](#test-assertion-6214e), [#14.f](#test-assertion-6214f))

## Test Case 6.3

**End user authorized access to BIE Expression generation**

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
The end user cannot generate an expression of a single BIE, in JSON Schema, in the same package, with any of the BIE CCTS Meta Data, Include CCTS_Definition Tag, BIE GUID, Business Context, BIE OAGi/Score Meta Data, or Include WHO Columns annotations selected. `Based CC Meta Data` remains available for JSON Schema and is unchecked by default.

#### Test Assertion #6.3.14
The end user can generate an expression of multiple BIEs, in multiple XML Schemas, saved in the same package, with some annotations selected.

#### Test Assertion #6.3.15
The end user can generate an expression of multiple BIEs, in XML Schemas, saved in different packages, with some annotations selected.

#### Test Assertion #6.3.16
The end user can generate an expression of multiple BIEs, in JSON Schemas, saved in the same package, with the BIE Definition selected.

#### Test Assertion #6.3.17
The end user can generate an expression of multiple BIEs, in JSON Schemas, saved in different packages, with the BIE Definition selected.

#### Test Assertion #6.3.18
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header (Include Meta Header). Also, verify that the `metaHeader` property is an object and not an array.

##### Test Assertion #6.3.18.a
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header and the option “Make as an array”. Verify that the `metaHeader` property is an object and not an array.
##### Test Assertion #6.3.18.b
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header and a Pagination Response. Verify that the `metaHeader` property is an object and not an array.
##### Test Assertion #6.3.18.c
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Meta Header, Pagination Response and the option “Make as an array”. Verify that the `metaHeader` property is an object and not an array.

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
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Pagination Response (Include Pagination Response). Also, verify that the `paginationResponse` property is an object and not an array.

##### Test Assertion #6.3.25.a
The end user can generate an expression of a single BIE, in JSON Schema, having selected a Pagination Response and the option “Make as an array”. Verify that the `paginationResponse` property is an object and not an array.

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

1. The tests create end-user-owned and developer-owned BIEs in multiple releases and states as needed for each expression-generation scenario.
2. Meta Header, Pagination Response, reusable BIE, and multi-business-context scenarios create the required supporting BIEs in `QA` or `Production` as needed.
3. OpenAPI scenarios create the required BIEs, operation-template options, and ownership/state combinations for the referenced Meta Header and Pagination Response BIEs.

### Test Step:

1. An end user logs into the `Express BIE` page.
2. The test verifies which BIEs are selectable for expression generation based on ownership, state, and release, including end-user-owned BIEs in all states, other users' `QA` and `Production` BIEs, and the exclusion of other users' `WIP` BIEs. (Assertions [#1](#test-assertion-631), [#2](#test-assertion-632), [#3](#test-assertion-633), [#4](#test-assertion-634))
3. The test generates single-BIE XML expressions with the relevant annotation combinations and verifies the expected file is produced, including the dependency rules for `Include CCTS_Definition Tag` and `Include WHO Columns`. (Assertions [#5](#test-assertion-635), [#6](#test-assertion-636), [#7](#test-assertion-637), [#8](#test-assertion-638), [#9](#test-assertion-639), [#10](#test-assertion-6310))
4. The test generates single-BIE JSON expressions with the supported annotation combinations and verifies that unsupported JSON-only annotation combinations cannot be selected while `Based CC Meta Data` remains enabled but unchecked. (Assertions [#11](#test-assertion-6311), [#12](#test-assertion-6312), [#13](#test-assertion-6313))
5. The test generates multiple-BIE XML and JSON expressions in same-package and separate-package modes and verifies that the expected single-file or multi-file output is produced. (Assertions [#14](#test-assertion-6314), [#15](#test-assertion-6315), [#16](#test-assertion-6316), [#17](#test-assertion-6317))
6. The test generates JSON expressions with Meta Header and Pagination Response options, including `Make as an array`, and verifies that the generated `metaHeader` and `paginationResponse` properties remain JSON objects rather than arrays. (Assertions [#18](#test-assertion-6318), [#18.a](#test-assertion-6318a), [#18.b](#test-assertion-6318b), [#18.c](#test-assertion-6318c), [#19](#test-assertion-6319), [#25](#test-assertion-6325), [#25.a](#test-assertion-6325a), [#26](#test-assertion-6326))
7. The test verifies the search filters and index count on the `Express BIE` page and confirms that expressions can be generated for a BIE with multiple business contexts. (Assertions [#20](#test-assertion-6320), [#21](#test-assertion-6321), [#22](#test-assertion-6322))
8. The test generates OpenAPI 3.0 YAML and JSON expressions across the supported combinations, including code-generation-friendly mode, GET and POST operation templates, single-file and multi-file output, and Meta Header / Pagination Response / Make Array option combinations. (Assertions [#23](#test-assertion-6323), [#24](#test-assertion-6324), [#28](#test-assertion-6328), [#29](#test-assertion-6329), [#30](#test-assertion-6330), [#31](#test-assertion-6331), [#32](#test-assertion-6332), [#33](#test-assertion-6333), [#34](#test-assertion-6334), [#35](#test-assertion-6335), [#36](#test-assertion-6336), [#37](#test-assertion-6337), [#38](#test-assertion-6338), [#39](#test-assertion-6339), [#40](#test-assertion-6340), [#41](#test-assertion-6341), [#42](#test-assertion-6342), [#43](#test-assertion-6343), [#44](#test-assertion-6344), [#45](#test-assertion-6345), [#46](#test-assertion-6346), [#47](#test-assertion-6347))
9. The test also verifies reuse handling during expression generation when the same reused BIE is referenced in multiple places. (Assertion [#27](#test-assertion-6327))
