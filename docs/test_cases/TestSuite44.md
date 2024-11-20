# Test Suite 44

**BIE Inheritance**

This test suite verifies the functionality and rules of BIE inheritance, ensuring that the creation, modification, and management of Base BIEs and Inherited BIEs adhere to the specified constraints and propagation rules. Each test case focuses on a specific feature or rule related to the inheritance mechanism.

## Test Case 44.1

**Create Inherited BIE**

This test case validates that a new Inherited BIE can be created from an existing Base BIE and ensures it complies with the inheritance rules.

### Test Assertions:

#### Test Assertion #44.1.1
Users can create a new Inherited BIE from a Base BIE in any state.

#### Test Assertion #44.1.2
Attributes inherited from the Base BIE retain their values and are marked as non-editable in the Inherited BIE.

#### Test Assertion #44.1.3
The Inherited BIE displays all inherited attributes along with their definitions from the Base BIE.

### Test Steps:

1. Log in as an end user with the required permissions.
2. Create a random business context (e.g., Manufacturing).
3. Create a new Base BIE named "BOM Header" using the business context from step 2.
4. Define random attribute values for the "Status," "Effectivity," and "Security Classification" ASBIE/BBIEs in the "BOM Header" BIE and enable these attributes.
5. Navigate to the "BIE List" page.
6. Open the context menu for the "BOM Header" record and select **Create Inherited BIE** (Assertion [#1](#test-assertion-4411)).
7. Open the newly created Inherited "BOM Header" BIE.
8. Verify the following:
    - **Attributes Check**: The "Status," "Effectivity," and "Security Classification" ASBIE/BBIEs are checked (Assertion [#2](#test-assertion-4412)).
    - **Attribute Consistency**: The values of these attributes match the ones defined in the Base BIE (Assertion [#2](#test-assertion-4412)).
    - **Non-editable Attributes**: The "Status," "Effectivity," and "Security Classification" ASBIE/BBIEs are marked as non-editable (e.g., checkboxes are disabled or locked) (Assertion [#2](#test-assertion-4412)).
    - **Inherited Details**: Attribute details display both the inherited values and their definitions from the Base BIE (Assertion [#3](#test-assertion-4413)).
