# Test Suite 47

**BIE Inverse Mode**

This suite covers the global Application Setting for BIE Inverse Mode, the Inverse Mode saved on an individual BIE, component-selection behavior, and BIE Expression generation when disabled Core Component nodes are included. Inverse Mode supplies an enabled-by-default value for component nodes that have no stored BIE selection. An explicitly stored selection remains authoritative: a stored `used = true` stays enabled and a stored `used = false` stays disabled. Inverse Mode does not override Core Component cardinality or value-domain rules.

The expression cases cover recursive Core Component associations. A recursion guard must stop expanding the repeated reference on the current path while preserving other finite paths that happen to contain the same component. A generated expression must complete as a finite artifact; the format-specific representation at the cut point may differ.

## Test Case 47.1

**Admin management of BIE Inverse Mode availability**

Pre-condition: An Admin account and a non-Admin account are available. The Application Setting can be toggled and takes effect after the user's authorization token is refreshed or the user signs in again. At least one editable BIE is available for checking whether its Inverse Mode control is shown.

### Test Assertion:

#### Test Assertion #47.1.1
Only an Admin can access and change the `BIE Inverse Mode` application setting. A non-Admin cannot view or change it, including by opening the application-settings route directly.

#### Test Assertion #47.1.2
Choosing `Enable` and cancelling the confirmation leaves the Application Setting disabled. No BIE editor displays the `Inverse Mode` control.

#### Test Assertion #47.1.3
Confirming `Enable` persists the Application Setting. After refreshing the Admin's authorization token or signing in again, the setting reads enabled and the `Inverse Mode` control is available on an editable BIE root.

#### Test Assertion #47.1.4
Choosing `Disable` and cancelling the confirmation leaves the Application Setting enabled and the control available.

#### Test Assertion #47.1.5
Confirming `Disable` persists the Application Setting. After refreshing the authorization token or signing in again, the control is unavailable. Disabling the Application Setting does not silently change a BIE's previously saved `inverseMode` value.

### Test Step Pre-condition:
1. An Admin account and a non-Admin account can sign in to connectCenter.
2. At least one editable top-level BIE is available.
3. The global BIE Inverse Mode setting starts disabled.

### Test Step:
1. Sign in as the non-Admin and open Application Settings, including by navigating directly to `/settings/application_settings`. Verify that the user cannot view or update the `BIE Inverse Mode` setting. (Assertion [#1](#test-assertion-4711))
2. Sign in as the Admin and open Application Settings. Choose `Enable`, then cancel the confirmation. Verify that the setting remains disabled and the Inverse Mode control is absent from the editable BIE root. (Assertion [#2](#test-assertion-4712))
3. Choose `Enable` again and confirm. Refresh the Admin session or sign in again, then open the editable BIE root. Verify the Application Setting is enabled and the Inverse Mode control is available. (Assertion [#3](#test-assertion-4713))
4. Choose `Disable`, then cancel the confirmation. Verify that the setting remains enabled and the BIE root control remains available. (Assertion [#4](#test-assertion-4714))
5. On an editable BIE, turn on and save Inverse Mode. Return to Application Settings, confirm `Disable`, refresh the session, and reopen the BIE. Verify that the Inverse Mode control is unavailable and that disabling the Application Setting did not erase the BIE's saved value. (Assertion [#5](#test-assertion-4715))

## Test Case 47.2

**Inverse Mode selection defaults, overrides, and persistence**

Pre-condition: The global BIE Inverse Mode setting is enabled. An editable top-level BIE is based on a Core Component tree containing nested ASBIEs, BBIEs, and BBIE supplementary components (BBIE_SCs), with both required and optional associations. The fixture can represent a component absent from the BIE, a component stored with `used = true`, and a component stored with `used = false`. The tree supports lazy loading so the behavior can be checked both before and after expanding descendants.

### Test Assertion:

#### Test Assertion #47.2.1
The Inverse Mode control is available on an editable BIE root when the Application Setting is enabled. Turning it on saves the root's `inverseMode` value, and reopening the BIE shows it enabled.

#### Test Assertion #47.2.2
When the root's Inverse Mode is off, components with no stored selection are unchecked by default. Turning Inverse Mode on makes those unpersisted component checkboxes appear checked, including descendants first materialized by lazy loading.

#### Test Assertion #47.2.3
An explicitly stored `used = true` component remains checked with Inverse Mode on. An explicitly stored `used = false` component remains unchecked; the mode's default applies only when no stored selection exists.

#### Test Assertion #47.2.4
The explicit-state rule applies independently to ASBIE, BBIE, and BBIE_SC nodes. It is not sufficient to verify only one node type.

#### Test Assertion #47.2.5
Changing an individual node while Inverse Mode is on creates or updates that node's explicit selection. After saving and reopening the BIE, that selection overrides the mode default while unmodified, unpersisted descendants remain checked by default.

#### Test Assertion #47.2.6
Turning Inverse Mode off restores the ordinary stored-selection behavior: stored `used = true` nodes remain checked, stored `used = false` nodes remain unchecked, and nodes with no stored selection are unchecked.

#### Test Assertion #47.2.7
The mode does not make an association whose Core Component maximum cardinality is zero usable, and it does not change requiredness, cardinality, primitive restrictions, value constraints, or supplementary-component definitions.

#### Test Assertion #47.2.8
The Inverse Mode control is read-only or unavailable where the user cannot edit the BIE root, including a non-owner viewing a WIP BIE and a read-only inherited/base view. Such a user cannot persist a changed root mode.

### Test Step Pre-condition:
1. The global BIE Inverse Mode setting is enabled.
2. An editable top-level BIE is based on a component tree with multiple nested levels and at least one ASBIE, BBIE, and BBIE_SC of each state: absent, stored `used = true`, and stored `used = false`.
3. The Core Component fixture includes a `max = 0` association and nodes with non-default cardinality or value-domain details.
4. A non-owner account can view a WIP BIE read-only, and a base/inherited BIE view is available.

### Test Step:
1. Sign in as the BIE owner and open the editable BIE root with Inverse Mode off. Expand the tree and verify that unpersisted nodes are unchecked while stored selections reflect their saved values. (Assertions [#2](#test-assertion-4722), [#3](#test-assertion-4723), [#4](#test-assertion-4724), [#6](#test-assertion-4726))
2. Turn on Inverse Mode at the root and save. Reopen the BIE and verify the root mode remains on. Check unpersisted descendants before and after expanding their lazy-loaded parents. (Assertions [#1](#test-assertion-4721), [#2](#test-assertion-4722))
3. Verify that stored-enabled and stored-disabled ASBIE, BBIE, and BBIE_SC nodes keep their respective checked states while Inverse Mode is on. In particular, confirm that an explicit false value is distinguishable from a component with no stored BIE node. (Assertions [#3](#test-assertion-4723), [#4](#test-assertion-4724))
4. While Inverse Mode is on, uncheck one node that had only the mode default and check one explicitly disabled node. Save, reopen, and verify those explicit selections persist while untouched absent nodes still appear checked. (Assertion [#5](#test-assertion-4725))
5. Verify that a `max = 0` association remains unavailable and that requiredness, cardinality, primitive restrictions, value constraints, and supplementary-component metadata are unchanged. (Assertion [#7](#test-assertion-4727))
6. Turn Inverse Mode off, save, reopen, and verify that only stored-enabled nodes appear checked. (Assertion [#6](#test-assertion-4726))
7. Sign in as the non-owner and inspect the WIP BIE, then inspect the read-only base/inherited view. Verify that the root mode cannot be changed or saved in either view. (Assertion [#8](#test-assertion-4728))

## Test Case 47.3

**Expression generation applies Inverse Mode without changing explicit BIE profiling**

Pre-condition: The Application Setting is enabled. An editable top-level BIE has Inverse Mode on and is based on a fixture with multiple component levels. The fixture includes unpersisted ASBIE, BBIE, and BBIE_SC nodes, explicitly selected nodes, explicitly unselected nodes, a group containing descendants, and a `max = 0` association. Each generated artifact can be downloaded and inspected. The same fixture can be generated with Inverse Mode off for a baseline comparison.

### Test Assertion:

#### Test Assertion #47.3.1
With Inverse Mode on, expression generation includes eligible unpersisted ASBIE and BBIE nodes at every traversed level, including descendants reached through a group.

#### Test Assertion #47.3.2
Unpersisted BBIE supplementary components are included according to the source data type's available supplementary components and cardinality.

#### Test Assertion #47.3.3
Explicitly selected components are included and explicitly unselected components are omitted, for ASBIE, BBIE, and BBIE_SC nodes. A persisted false selection is not mistaken for a missing BIE node.

#### Test Assertion #47.3.4
Associations with `max = 0` are omitted even when they have no stored selection and Inverse Mode is on.

#### Test Assertion #47.3.5
The generated result retains the original component names, cardinalities, requiredness, primitive restrictions, value constraints, and supported metadata. Inverse Mode changes selection only.

#### Test Assertion #47.3.6
With Inverse Mode off, generation includes only explicitly selected components and omits absent or explicitly unselected components, matching the pre-Inverse-Mode behavior.

#### Test Assertion #47.3.7
An expression generated from the same BIE after saving and reopening it has the same effective component selection and equivalent content as the expression generated before reopening.

### Test Step Pre-condition:
1. The Application Setting is enabled, and an editable top-level BIE can be saved with Inverse Mode both on and off.
2. A multi-level Core Component fixture contains ASBIE, BBIE, BBIE_SC, groups, explicit true and false BIE selections, unpersisted nodes, and a `max = 0` association.
3. The generated expression can be inspected as structured content rather than only as a non-empty download.

### Test Step:
1. Prepare the BIE with Inverse Mode on. Leave some eligible component nodes absent from the BIE, store explicit true selections on some nodes, and store explicit false selections on others. Include a group with nested descendants and a `max = 0` association.
2. Generate an expression and inspect each level. Verify that absent eligible ASBIE and BBIE nodes and eligible BBIE_SC nodes appear, including descendants under the group. (Assertions [#1](#test-assertion-4731), [#2](#test-assertion-4732))
3. Verify that explicit true nodes appear, explicit false nodes are omitted, and the `max = 0` association is omitted. (Assertions [#3](#test-assertion-4733), [#4](#test-assertion-4734))
4. Compare the generated definitions with the source Core Components and verify that Inverse Mode has not changed names, cardinality, requiredness, value-domain details, or supported metadata. (Assertion [#5](#test-assertion-4735))
5. Generate the same BIE with Inverse Mode off. Verify that only explicitly selected components are included. Save, reopen, and regenerate once with the mode on; verify that effective selection and output content match the first on-mode generation. (Assertions [#6](#test-assertion-4736), [#7](#test-assertion-4737))

## Test Case 47.4

**Recursive references terminate safely in every BIE Expression format**

Pre-condition: A test release contains Core Component fixtures with (a) a direct self-reference, (b) a multi-component cycle such as `A → B → A`, and (c) a finite sibling/diamond graph in which the same component appears on separate paths without a path-local cycle. The root BIE can be generated in inverse and ordinary modes. The expression generator supports XML Schema, JSON Schema Draft-04, JSON Schema 2020-12, OpenAPI 3.0, OpenAPI 3.1, Avro, and ODF Spreadsheet output in the test environment. The test can inspect the downloaded artifacts and capture server errors or timeouts.

### Test Assertion:

#### Test Assertion #47.4.1
With Inverse Mode on, generating an expression from a BIE whose enabled-by-default expansion reaches a direct self-reference completes successfully. It does not fail with stack overflow, unbounded recursion, request timeout, or an internal server error.

#### Test Assertion #47.4.2
The same termination behavior holds for a multi-component cycle such as `A → B → A`.

#### Test Assertion #47.4.3
At a repeated reference on the current expansion path, generation stops expanding that recursive branch after the cycle is detected. The output is finite and does not contain unbounded copies of the cycle.

#### Test Assertion #47.4.4
Cycle detection is path-local. The same component used on separate non-cyclic sibling/diamond paths is still generated on each valid path and is not omitted merely because it appeared elsewhere in the output.

#### Test Assertion #47.4.5
The direct and multi-component cycle cases complete for every supported BIE Expression output format in this suite: XML Schema, JSON Schema Draft-04, JSON Schema 2020-12, OpenAPI 3.0, OpenAPI 3.1, Avro, and ODF Spreadsheet. Each artifact is parseable/openable in its own format.

#### Test Assertion #47.4.6
When Inverse Mode is off and the stored profile does not select the recursive association, generation follows the stored profile and does not synthesize the recursive branch.

#### Test Assertion #47.4.7
Repeated generation of the same recursive BIE produces a finite result each time. Cycle-tracking state from a prior generation does not leak into a later request or suppress unrelated branches.

#### Test Assertion #47.4.8
If a cycle is detected, the generation request succeeds with the normal download response. It does not return a partial/corrupt file or expose an implementation exception to the user.

### Test Step Pre-condition:
1. The global BIE Inverse Mode setting is enabled.
2. A test release contains direct-recursive, multi-component-recursive, and finite repeated-sibling Core Component fixtures.
3. A top-level BIE can be created from each fixture with Inverse Mode on or off.
4. The expression-generation UI or endpoint supports the formats listed in Assertion #47.4.5.

### Test Step:
1. Create or prepare a top-level BIE from the direct self-reference fixture. Leave the recursive association unpersisted and turn on Inverse Mode.
2. Generate the BIE Expression in each supported format. Verify successful downloads, finite output, and format-level parse/open success. Verify that generation does not overflow the call stack, time out, or return an internal server error. (Assertions [#1](#test-assertion-4741), [#3](#test-assertion-4743), [#5](#test-assertion-4745), [#8](#test-assertion-4748))
3. Repeat using the multi-component `A → B → A` cycle fixture. Verify that the recursive branch is cut at the repeated path reference and each format produces a finite parseable artifact. (Assertions [#2](#test-assertion-4742), [#3](#test-assertion-4743), [#5](#test-assertion-4745))
4. Generate from the finite sibling/diamond fixture in Inverse Mode. Verify that both distinct paths to the repeated component are present and fully expanded where their own paths are acyclic. (Assertion [#4](#test-assertion-4744))
5. Turn Inverse Mode off on the direct-cycle BIE while leaving the recursive association unselected. Generate again and verify that the recursive branch is not synthesized. (Assertion [#6](#test-assertion-4746))
6. Generate the same on-mode recursive fixture more than once, then generate a separate non-recursive BIE. Verify all results are finite and the second BIE's branches are unaffected by prior cycle tracking. (Assertion [#7](#test-assertion-4747))

## Test Case 47.5

**Inverse Mode with inherited and reused BIEs**

Pre-condition: The global setting is enabled. A base BIE, an inherited BIE, and a BIE containing a reused BIE are available. Their source Core Components include both recursive and non-recursive associations. The test data can give the owning BIE and referenced/reused BIE distinct Inverse Mode values and distinct explicit used selections.

### Test Assertion:

#### Test Assertion #47.5.1
An inherited BIE displays and generates using its own persisted Inverse Mode value. The base BIE's value is not silently substituted for the inherited BIE's value.

#### Test Assertion #47.5.2
An explicit used or unused selection inherited from the base BIE continues to follow the BIE inheritance rules when Inverse Mode is enabled; the mode does not rewrite the base or inherited BIE's stored component rows.

#### Test Assertion #47.5.3
A reused BIE's explicit profile and its own inverse setting are respected when the reused content is traversed. The owning BIE's Inverse Mode does not overwrite stored selections inside a separately owned reused BIE.

#### Test Assertion #47.5.4
Recursive references reached through inherited or reused BIE content terminate under the same cycle-safety behavior as references reached directly from the root BIE.

#### Test Assertion #47.5.5
Turning Inverse Mode on or off and generating an expression does not persist synthesized/default nodes as new BIE records. Only an explicit user edit creates or changes persisted component selections.

### Test Step Pre-condition:
1. A base BIE and an inherited BIE based on it are available, with distinct persisted Inverse Mode values for at least one scenario.
2. An owning BIE with a reused BIE occurrence is available; the owning and reused BIE can have different Inverse Mode settings and explicit component selections.
3. At least one inherited or reused branch reaches the recursive Core Component fixture from Test Case 47.4.
4. Persisted BIE rows can be inspected before and after expression generation to verify that generation is read-only.

### Test Step:
1. Open the base and inherited BIEs and verify their Inverse Mode values independently. Generate expressions for each and verify that each uses its own persisted setting and inherited component selections. (Assertions [#1](#test-assertion-4751), [#2](#test-assertion-4752))
2. Prepare an owning BIE and a reused BIE with different mode values and a mix of explicit true, explicit false, and absent component rows. Generate an expression and verify the reused content follows its own persisted selections and mode. (Assertion [#3](#test-assertion-4753))
3. Enable the recursive path through an inherited or reused branch and generate the expression. Verify that the recursive branch terminates and the artifact is valid. (Assertion [#4](#test-assertion-4754))
4. Record persisted BIE component rows, generate in Inverse Mode, reopen the BIE, and compare the records. Verify that default-enabled nodes were not persisted merely by viewing or generating the expression. (Assertion [#5](#test-assertion-4755))

## Known coverage gaps

- The exact representation of a cycle cut point may differ by expression format. The format-specific output shape should be asserted once agreed; this suite currently requires finite, valid output and bounded expansion.
- Very large acyclic expansions can consume substantial memory. A separate performance test should define a deterministic size/time budget before asserting capacity limits.
- The Application Setting's effect on expression generation for a BIE that was already saved with `inverseMode = true` is not asserted after the setting is disabled. This suite verifies that the BIE's value is preserved while the control is hidden; the expected runtime policy for that existing value should be decided explicitly before adding an output assertion.
