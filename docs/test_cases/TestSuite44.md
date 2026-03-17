# Test Suite 44

**BIE Inheritance**

## Test Case 44.1

**Create Inherited BIE**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #44.1.1
The end user can create a new Inherited BIE from an existing Base BIE.

#### Test Assertion #44.1.2
The editable fields on the Inherited BIE are initialized with the same values as the Base BIE, and the Base-value panels are displayed as non-editable.

#### Test Assertion #44.1.3
The Inherited BIE displays inherited attribute details and definitions from the Base BIE.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: an end user can create and edit BIEs in the Working branch.
2. A business context exists or can be created for the test BIEs.
3. A Base BIE exists or can be created from a valid `BOM Header. BOM Header` ASCCP.

### Test Step:
1. Sign in to connectCenter as an end user.
2. Create a business context for the test.
3. Create a Base BIE from `BOM Header. BOM Header`.
4. Update top-level values such as Business Term, Remark, Version, Status, and Context Definition on the Base BIE.
5. Enable and update the `Security Classification`, `Status`, and `Effectivity` ASBIEs with explicit cardinality, remark, and context-definition values.
6. Save the Base BIE.
7. Open the BIE list and create an inherited BIE from the Base BIE. (Assertion [#1](#test-assertion-4411))
8. Open the newly created Inherited BIE.
9. Verify that the editable top-level and ASBIE fields on the Inherited BIE are initialized with the same values as the Base BIE. (Assertion [#2](#test-assertion-4412))
10. Verify that the corresponding Base-value panels are displayed and are non-editable. (Assertion [#2](#test-assertion-4412))
11. Verify that the inherited attribute details and definitions are displayed from the Base BIE for the inherited top-level and ASBIE panels. (Assertion [#3](#test-assertion-4413))

## Test Case 44.2

**Use Base BIE**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #44.2.1
A Base BIE can be successfully assigned to an existing BIE.

#### Test Assertion #44.2.2
After assigning a Base BIE, existing editable top-level values on the target BIE remain on the target BIE and are not overwritten by the Base BIE.

#### Test Assertion #44.2.3
Inherited child ASBIE values from the assigned Base BIE are applied, while the Base-value panels are displayed as non-editable.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: two end users can create and edit BIEs.
2. A business context exists or can be created for both BIEs.
3. A Base BIE exists or can be created by one end user, and another end user can create a separate target BIE from the same ASCCP.

### Test Step:
1. Sign in to connectCenter as the first end user.
2. Create a business context for the test.
3. Create a Base BIE from `BOM Header. BOM Header`.
4. Update top-level values on the Base BIE and enable and update the `Security Classification`, `Status`, and `Effectivity` ASBIEs.
5. Save the Base BIE.
6. Sign out.
7. Sign in as another end user.
8. Create a separate target BIE from the same `BOM Header. BOM Header` ASCCP.
9. Open the target BIE and use the `Use Base BIE` action to assign the previously created Base BIE. (Assertion [#1](#test-assertion-4421))
10. Verify that the target BIE remains editable at the top level and that its own existing top-level values are retained instead of being overwritten by the Base BIE. (Assertion [#2](#test-assertion-4422))
11. Verify that the inherited child ASBIE values from the Base BIE are applied to the target BIE. (Assertion [#3](#test-assertion-4423))
12. Verify that the Base-value panels for the assigned Base BIE are displayed and are non-editable. (Assertion [#3](#test-assertion-4423))

## Test Case 44.3

**Create Inherited BIE with Base Reused BIE**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #44.3.1
An inherited BIE also inherits reused BIEs and inherited descendant values from the Base BIE.

#### Test Assertion #44.3.2
The inherited BIE's reused Base BIE can be overridden with an inherited version of that reused Base BIE.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: an end user can create, edit, inherit, and reuse BIEs.
2. A business context exists or can be created for the test BIEs.
3. A Base `BOM Header` BIE and a Base `BOM` BIE can be created from valid ASCCPs in the same release.

### Test Step:
1. Sign in to connectCenter as an end user.
2. Create a business context for the test.
3. Create a Base `BOM Header` BIE and update its top-level values and the inherited `Security Classification`, `Status`, and `Effectivity` ASBIE values.
4. Create a Base `BOM` BIE and update its top-level values and the `BOM Option` ASBIE and `Action Code` BBIE values.
5. Reuse the Base `BOM Header` BIE on the Base `BOM` BIE and save the Base `BOM` BIE.
6. Create an inherited `BOM Header` BIE from the Base `BOM Header` BIE.
7. Create an inherited `BOM` BIE from the Base `BOM` BIE.
8. Open the inherited `BOM` BIE and verify that it still reuses `BOM Header` and that the inherited reused hierarchy displays the expected inherited and read-only base values. (Assertion [#1](#test-assertion-4431))
9. Verify that unrelated inherited values on `BOM Option` and `Action Code` are preserved on the inherited `BOM`. (Assertion [#1](#test-assertion-4431))
10. Update the inherited `BOM Header` BIE with distinguishable inherited remarks and context definitions and save it.
11. Reopen the inherited `BOM` BIE and use `Override Base Reused BIE` on the reused `BOM Header` node to point it to the inherited `BOM Header` BIE. (Assertion [#2](#test-assertion-4432))
12. Verify that the reused `BOM Header` subtree now reflects the inherited `BOM Header` values where the override applies, while the Base-value panels remain read-only. (Assertion [#2](#test-assertion-4432))
13. Verify that the unrelated `BOM Option` ASBIE and `Action Code` BBIE remain unaffected by the override. (Assertion [#2](#test-assertion-4432))

## Test Case 44.4

**State Change Rules Between Base BIE and Inherited BIE**

Pre-condition: N/A

### Test Assertion:

#### Test Assertion #44.4.1
The `Back to WIP` action for the Base BIE remains unavailable while its Inherited BIE remains ahead in `QA`.

#### Test Assertion #44.4.2
The `Move to QA` action for the Inherited BIE remains unavailable before the Base BIE moves to `QA`.

#### Test Assertion #44.4.3
If both the Base BIE and the Inherited BIE are in `QA`, the Inherited BIE can move back to `WIP` first and then the Base BIE can move back to `WIP`.

#### Test Assertion #44.4.4
If both the Base BIE and the Inherited BIE are in `QA`, the `Move to Production` action for the Inherited BIE remains unavailable while the Base BIE remains in `QA`.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: two end users can create and edit BIEs.
2. A business context exists or can be created for the test BIEs.
3. A Base BIE and an Inherited BIE derived from it can be created for the same `BOM Header. BOM Header` ASCCP.

### Test Step:
1. Sign in as one end user and create a Base BIE from `BOM Header. BOM Header`.
2. Sign in as another end user and create an Inherited BIE from that Base BIE.
3. Open the Inherited BIE before the Base BIE is in `QA`.
4. Verify that the `Move to QA` action is unavailable. (Assertion [#2](#test-assertion-4442))
5. Move the Base BIE to `QA`.
6. Move the Inherited BIE to `QA`.
7. Open the Base BIE while the Inherited BIE is still in `QA`.
8. Verify that the `Back to WIP` action is unavailable. (Assertion [#1](#test-assertion-4441))
9. Move the Inherited BIE back to `WIP`.
10. Verify that this rollback succeeds. (Assertion [#3](#test-assertion-4443))
11. Move the Base BIE back to `WIP`.
12. Verify that this rollback now succeeds. (Assertion [#3](#test-assertion-4443))
13. Move the Base BIE to `QA` again.
14. Move the Inherited BIE to `QA` again.
15. Open the Inherited BIE while the Base BIE remains in `QA`.
16. Verify that the `Move to Production` action is unavailable. (Assertion [#4](#test-assertion-4444))
