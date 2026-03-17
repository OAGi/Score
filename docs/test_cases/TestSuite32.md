# Test Suite 32

**History functionality**


## Test Case 32.1

**ACC history**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #32.1.1
On ACC history, history comparison shows the correct owner after paging through history records.

#### Test Assertion #32.1.2
If a revise action is canceled for an ACC, the history record keeps revision number 1 and does not record the revision action as "Revised".

#### Test Assertion #32.1.3
If a revise action is canceled for an ASCCP, the history record keeps revision number 1 and does not record the revision action as "Revised".

#### Test Assertion #32.1.4
If a revise action is canceled for a BCCP, the history record keeps revision number 1 and does not record the revision action as "Revised".

### Test Step Pre-condition:
1. The users, branches, releases, namespaces, and records needed to exercise this test case are available in connectCenter.
2. Published ACC, ASCCP, and BCCP records are available for revise-and-cancel validation.

### Test Step:
1. Sign in as a developer and open **Core Component > View/Edit Core Component** in the Working branch.
2. For Test Assertion #32.1.1, create and update an ACC, move it through Draft/Candidate/WIP multiple times, open **History**, move to the next history page, compare records, and verify the owner shown in the comparison panel.
3. For Test Assertions #32.1.2 to #32.1.4, open each published ACC, ASCCP, and BCCP, click **Revise**, and then click **Cancel**.
4. Open **History** for each component and verify that revision number remains `1` and revision action is not `Revised`.
