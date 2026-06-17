# Test Suite 46

**BIE Package Management**

This suite covers the package-level behaviors added for issue [#1733](https://github.com/OAGi/Score/issues/1733): the per-revision **Revision Reason Text** captured on a BIE Package and surfaced in the generated BIE Package Manifest, the per-BIE **Backward Compatibility Indicator** emitted in that manifest, and the **array-vs-object rendering rule** the JSON/OpenAPI generators apply (the array decision follows the based ASCC/BCC cardinality).

> **Scope note (Backward Compatibility Indicator):** The manifest emits a `backwardCompatibility` object per BIE entry. Each field is a boolean where `true` = backward **compatible** and `false` = the change makes a previously valid instance invalid for that target syntax. Test Case 46.2 asserts **only the syntax-dependent indicators** `xmlSchema` and `jsonSchema`; the `syntaxIndependent` indicator is intentionally **out of scope** (its definition is still under review and it is no longer emitted in the manifest). Per issue #1733, the JSON (and OpenAPI) array/object rendering now follows the underlying component's (ASCC/BCC) maximum cardinality, so changing only the BIE's own cardinality does not flip the JSON shape — which is why a cardinality loosening is reported as backward compatible in both syntaxes (Assertion #46.2.6). The two syntax-dependent indicators are stable and are what this suite pins.

## Test Case 46.1

**Revision Reason Text**

Pre-condition: An end-user account that can access the BIE menu and manage BIE Packages is available in connectCenter. At least one BIE Package can be created, populated with at least one Production BIE, promoted to Production, and revised during the test. The "Revision Reason" is a per-package free-text field that only applies to a revised package (one that has a prior version).

### Test Assertion:

#### Test Assertion #46.1.1
On the detail page of a brand-new (first, non-revised) BIE Package, the "Revision Reason" field is not shown.

#### Test Assertion #46.1.2
After a Production BIE Package is revised, the detail page of the new revision shows a "Revision Reason" field directly under the "Description" field.

#### Test Assertion #46.1.3
On a revised BIE Package, the "Revision Reason" field is editable only while the revision is in the `WIP` state and the user has edit permission; in any non-`WIP` state it is read-only.

#### Test Assertion #46.1.4
The end user can enter a Revision Reason on a `WIP` revision and persist it with the Update action; the saved value is shown when the package is reopened.

#### Test Assertion #46.1.5
On a `WIP` revision, clearing the Revision Reason to blank and saving with the Update action removes the previously saved reason.

#### Test Assertion #46.1.6
A saved Revision Reason is preserved across the package state transitions `WIP` → `QA` → `Production`.

#### Test Assertion #46.1.7
The generated BIE Package contains a `manifest.json` whose package-level metadata includes the Revision Reason placed immediately after the prior package version identifier, and the manifest version is `0.3`. The same value is returned by the BIE Package Manifest endpoint.

#### Test Assertion #46.1.8
A revision created from an existing revision (for example rev 2 → rev 3) starts with an empty Revision Reason and does not inherit the prior revision's reason; each revision keeps its own reason independently.

### Test Step Pre-condition:
1. An end-user account that can access the BIE menu and manage BIE Packages is available in connectCenter.
2. At least one Production BIE (top-level ASBIEP) exists so that it can be added to a BIE Package.
3. A BIE Package can be created, populated, promoted through `WIP` → `QA` → `Production`, and revised during the test.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Open the `BIE` menu and open the `BIE Package` menu item.
3. Create a new BIE Package and open its detail page while it is in the `WIP` state.
4. Verify that the "Revision Reason" field is not shown on this brand-new (non-revised) BIE Package. (Assertion [#1](#test-assertion-4611))
5. Add at least one Production BIE to the package and promote the package `WIP` → `QA` → `Production`.
6. Revise the Production BIE Package and open the new `WIP` revision's detail page.
7. Verify that the "Revision Reason" field is shown directly under the "Description" field. (Assertion [#2](#test-assertion-4612))
8. While the revision is in `WIP`, verify that the "Revision Reason" field is editable; then promote the revision out of `WIP` and verify that the field is read-only. (Assertion [#3](#test-assertion-4613))
9. On the `WIP` revision, enter a Revision Reason and save with the Update action, then reopen the package and verify the saved value is shown. (Assertion [#4](#test-assertion-4614))
10. On the `WIP` revision, clear the Revision Reason to blank, save with the Update action, reopen the package, and verify the reason is removed. (Assertion [#5](#test-assertion-4615))
11. Re-enter a Revision Reason on the `WIP` revision, save it, then promote the revision `WIP` → `QA` → `Production` and verify the Revision Reason is unchanged at each state. (Assertion [#6](#test-assertion-4616))
12. Generate the BIE Package and open `manifest.json` from the downloaded package (and/or query the BIE Package Manifest endpoint). Verify that the package-level Revision Reason appears immediately after the prior package version identifier and that the manifest version is `0.3`. (Assertion [#7](#test-assertion-4617))
13. Revise the revised (Production) BIE Package again to create the next revision and open its detail page.
14. Verify that the next revision's "Revision Reason" field is empty (it does not inherit the prior revision's reason) and that the prior revision still shows its own reason. (Assertion [#8](#test-assertion-4618))

## Test Case 46.2

**Backward Compatibility Indicator**

Pre-condition: An end-user (or developer) account that can manage BIEs and BIE Packages is available in connectCenter. For each scenario, two Production BIEs based on the same top-level ASCCP are available: a baseline BIE and a changed BIE that differs from the baseline by exactly one profiling change (a removed element, an added required element, an added optional element, a cardinality change, a facet change, or a value-domain change). A BIE Package can be created with the baseline BIE, promoted to Production, revised, and have the baseline BIE replaced by the changed BIE so that the generated manifest diffs the changed BIE against its prior-package counterpart (matched by ASCCP GUID). Only the `xmlSchema` and `jsonSchema` indicators are asserted (see the suite scope note); `true` = backward compatible, `false` = breaks that syntax.

### Test Assertion:

#### Test Assertion #46.2.1
A BIE that has no counterpart in the prior package version (a brand-new BIE) reports `xmlSchema = false` and `jsonSchema = false`.

#### Test Assertion #46.2.2
Removing an element or supplementary component that existed in the prior version reports `xmlSchema = false` and `jsonSchema = false`.

#### Test Assertion #46.2.3
Adding a required element (cardinality minimum greater than 0) that did not exist in the prior version reports `xmlSchema = false` and `jsonSchema = false`.

#### Test Assertion #46.2.4
Adding an optional element (cardinality minimum 0) that did not exist in the prior version reports `xmlSchema = true` and `jsonSchema = true`.

#### Test Assertion #46.2.5
Tightening the cardinality of an existing element (for example raising the minimum from 0 to 1, or lowering a finite maximum) reports `xmlSchema = false` and `jsonSchema = false`.

#### Test Assertion #46.2.6
Loosening the cardinality of an existing element (raising the maximum from 1 to unbounded or to 2 or more) reports `xmlSchema = true` and `jsonSchema = true` — backward compatible in both syntaxes. The JSON array/object shape follows the underlying component's (ASCC/BCC) maximum cardinality rather than the BIE's own maximum, so narrowing or loosening the BIE cardinality does not change the JSON rendering and is not a break.

#### Test Assertion #46.2.7
Tightening a length/pattern facet of an existing element (for example lowering or adding `maxLength`) reports `xmlSchema = false` and `jsonSchema = false`.

#### Test Assertion #46.2.8
Narrowing the value domain of an existing element in XML only (for example changing the primitive from `normalizedString` to `token`, where the JSON type stays `string`) reports `xmlSchema = false` and `jsonSchema = true` — an XML-only break.

#### Test Assertion #46.2.9
A documentation-only or metadata-only change (for example editing the definition or remark) reports `xmlSchema = true` and `jsonSchema = true`.

### Decision table (asserted indicators)

| Change in the revised package's BIE (vs. prior package version) | xmlSchema | jsonSchema |
|---|---|---|
| Brand-new BIE (no prior counterpart) | `false` | `false` |
| Element / supplementary component removed | `false` | `false` |
| Required element added (`min > 0`) | `false` | `false` |
| Optional element added (`min = 0`) | `true` | `true` |
| Cardinality tightened (`min 0 → 1`, or `max` lowered) | `false` | `false` |
| Cardinality loosened (`max 1 → unbounded`/`≥ 2`) | `true` | `true` |
| Facet tightened (`maxLength` added or lowered) | `false` | `false` |
| Value domain narrowed in XML only (`normalizedString → token`) | `false` | `true` |
| Documentation / metadata only | `true` | `true` |

### Test Step Pre-condition:
1. An end-user (or developer) account that can manage BIEs and BIE Packages is available in connectCenter.
2. For each scenario, a baseline Production BIE and a changed Production BIE based on the same top-level ASCCP are available, differing by exactly the one profiling change under test.
3. A BIE Package can be created with the baseline BIE, promoted to Production, revised, and have its BIE replaced by the changed BIE.
4. The generated BIE Package manifest (or the BIE Package Manifest endpoint) can be inspected for the per-BIE `backwardCompatibility` object.

### Test Step:
1. Sign in to connectCenter as the relevant user.
2. Create a BIE Package, add the baseline BIE for the first scenario, and promote the package `WIP` → `QA` → `Production`.
3. Revise the Production BIE Package and, in the new `WIP` revision, replace the baseline BIE with the changed BIE for the scenario under test.
4. Generate the revised BIE Package (and/or query the BIE Package Manifest endpoint) and locate the changed BIE's entry and its `backwardCompatibility` object.
5. For a brand-new BIE (added in the revision with no prior counterpart), verify `xmlSchema = false` and `jsonSchema = false`. (Assertion [#1](#test-assertion-4621))
6. For a removed element/supplementary component, verify `xmlSchema = false` and `jsonSchema = false`. (Assertion [#2](#test-assertion-4622))
7. For an added required element, verify `xmlSchema = false` and `jsonSchema = false`. (Assertion [#3](#test-assertion-4623))
8. For an added optional element, verify `xmlSchema = true` and `jsonSchema = true`. (Assertion [#4](#test-assertion-4624))
9. For a tightened cardinality, verify `xmlSchema = false` and `jsonSchema = false`. (Assertion [#5](#test-assertion-4625))
10. For a loosened cardinality, verify `xmlSchema = true` and `jsonSchema = true`. (Assertion [#6](#test-assertion-4626))
11. For a tightened facet, verify `xmlSchema = false` and `jsonSchema = false`. (Assertion [#7](#test-assertion-4627))
12. For a value domain narrowed in XML only, verify `xmlSchema = false` and `jsonSchema = true`. (Assertion [#8](#test-assertion-4628))
13. For a documentation-only change, verify `xmlSchema = true` and `jsonSchema = true`. (Assertion [#9](#test-assertion-4629))

## Test Case 46.3

**Array rendering follows the based component's cardinality**

Pre-condition: An end-user account that can manage BIEs and OpenAPI Documents is available in connectCenter. A top-level BIE can be created on an ASCCP whose ACC carries a single text element whose based cardinality maximum is unbounded, the BIE's own child cardinality can be narrowed to a maximum of 1, and the BIE can be assigned to an OpenAPI Document and generated.

This case is the generator-level companion to Test Case 46.2: where 46.2 asserts that the manifest's backward-compatibility *indicator* reflects the issue #1733 rule, 46.3 asserts that the JSON / OpenAPI *generator* actually applies it. Since issue #1733 the array-vs-object decision for a nested child follows the based ASCC/BCC maximum cardinality, not the (possibly narrowed) BIE maximum; the array bounds (`minItems` / `maxItems`) still follow the BIE's own cardinality.

### Test Assertion:

#### Test Assertion #46.3.1
A nested child whose based BCC has an unbounded maximum is rendered as a JSON `type: array` in the generated OpenAPI document even when the BIE narrows the child's own maximum to 1, with the array's `maxItems` reflecting that narrowed maximum. Under the pre-#1733 rule (which read the BIE maximum), the same child would have been rendered as a bare value rather than an array.

### Test Step Pre-condition:
1. An end-user account that can manage BIEs and OpenAPI Documents is available in connectCenter.
2. A top-level BIE exists on an ASCCP whose ACC has a single text element whose based cardinality maximum is unbounded.
3. The BIE's child element has its own cardinality maximum narrowed to 1 while the based component remains unbounded.
4. The BIE can be assigned to an OpenAPI Document and the document generated and downloaded.

### Test Step:
1. Sign in to connectCenter as the relevant end user.
2. Arrange a top-level BIE on an ASCCP whose ACC has a single text element with an unbounded based cardinality, materialize the child, and narrow the child's own cardinality maximum to 1.
3. Create an OpenAPI Document, open its editor, and assign the BIE as a `POST` (request) operation.
4. Generate the OpenAPI Document and open the downloaded YAML.
5. Locate the BIE's component schema and its child property and verify the child property is rendered as `type: array` (its `maxItems` reflecting the narrowed maximum of 1). (Assertion [#1](#test-assertion-4631))
