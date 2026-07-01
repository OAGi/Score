# Test Suite 25

**Developer BIE Management**

> Test Case 5.5 and Test Case 5.6 covers developer BIE functions.

## Test Case 25.1

**Reuse a BIE**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #25.1.1
Developer can reuse a BIE. The reused BIE can be in any state and owned by any developer. I.e., developer cannot reuse end user BIE. All top-level BIEs owned by all developers can be show up for selection.

#### Test Assertion #25.1.2
Developer can remove the BIE Reuse from a top-level BIE he owns and editing. This menu option shall be available ONLY on the reused BIE node. After removal:

##### Test Assertion #25.1.2.a
Only the ASBIE details remain on the detail pane of the node.
##### Test Assertion #25.1.2.b
Other children nodes shall be unchecked

#### Test Assertion #25.1.3
The reuse-target node can only be ASBIE/ASBIEP/ABIE node but not any BBIE/BBIEP nor SC node. The root top-level BIE node cannot be a reuse-target node.

#### Test Assertion #25.1.4
The developer can click on the BIE Reuse node to view the details of the reused BIE in a new tab.

#### Test Assertion #25.1.5
On the detail pane of a reused BIE node, Business Term, Context Definition, Remark, Version, Owner, Business Contexts of the reused ASBIEP shall be display. The user can still customize cardinality, context definition, nillable details of the ASBIE (which belongs to the reusing top-level BIE). Details of the reused BIE and its descendant nodes cannot be changed. Nillable is editable ONLY if Nillable of the underlying ASCCP is true.

#### Test Assertion #25.1.6
The developer can view the details of the nodes of a BIE Reuse node but it cannot make any change to them. Verify also that the nodes contain all the details of the reused BIE.

#### Test Assertion #25.1.7
The developer can copy a reusing BIE that has a BIE Reuse node. In this case the new copied BIE it should contain the BIE Reuse node (i.e., it should still reuses the reused BIE).

#### Test Assertion #25.1.8
The developer can view all the reuses of all BIE in the Reuse BIE Report page. When the developer clicks on a reusing BIE, it is opened in a new tab. In this tab, the path of the BIE tree shows the BIE Reuse Node. If the developer clicks on a reused BIE, it is opened in a new tab where he can view its details.

#### Test Assertion #25.1.9
The developer cannot discard a reused BIE that he owned if it is used in another top-level BIE (the reusing BIE can be owned by a different developer).

#### Test Assertion #25.1.10
The developer can move a reusing BIE from WIP state to QA state even if the reused BIE is in WIP state.

#### Test Assertion #25.1.11
The developer can move a reusing BIE from QA state to Production state even if the reused BIE is in QA state.

#### Test Assertion #25.1.12
The developer can move a reusing BIE from WIP state to QA state if the reused BIE is in QA or Production state

#### Test Assertion #25.1.13
The developer can move a reusing BIE from QA state to Production state if the reused BIE is in Production state.

#### Test Assertion #25.1.14
The developer can move a reused BIE from QA state to WIP state even if the reusing BIE is in QA state.

#### Test Assertion #25.1.15
The developer can move a reused BIE from QA state to WIP state if the reusing BIE is in WIP state.

#### Test Assertion #25.1.16
The developer can see the details of a reused BIE node that he does not own, and it is in WIP, QA or Production state. He can also edit the details of the association of the BIE Reuse node.

#### Test Assertion #25.1.17
The developer can express a reusing BIE that reuses a BIE in WIP state and owned by a different developer.

#### Test Assertion #25.1.18
The developer can remove reused BIE references at any level even if there's another same reused BIE at a different level.

#### Test Assertion #25.1.19
Enable the global schema for reused BIE references no matter it has nested reused BIE or not.

#### Test Assertion #25.1.20
Retain all enabled properties under the reused BIE hierarchy when the user clicks the 'Retain Reused BIE' context menu.

### Test Step Pre-condition:
1. The latest release branch, developer-owned CCs, reusable top-level BIEs, and business-context data needed for developer BIE reuse are available in connectCenter.
2. Assertions [#25.1.17](#test-assertion-25117) and [#25.1.19](#test-assertion-25119) are not currently automated in `TS_25`. The express-BIE assertion is present but disabled in the test class, and the global-schema assertion has a placeholder test method with no executable steps.

### Test Step:
1. A developer signs in to connectCenter, opens developer-owned top-level BIEs for editing, and navigates to reusable descendant ASBIE nodes.
2. Invoke `Reuse BIE` on valid descendant nodes, verify selection candidates and reused-node UI behavior, and confirm remove/copy/report flows where covered by the suite. (Assertions [#25.1.1](#test-assertion-2511), [#25.1.2](#test-assertion-2512), [#25.1.3](#test-assertion-2513), [#25.1.4](#test-assertion-2514), [#25.1.5](#test-assertion-2515), [#25.1.6](#test-assertion-2516), [#25.1.7](#test-assertion-2517), [#25.1.8](#test-assertion-2518), [#25.1.16](#test-assertion-25116), [#25.1.18](#test-assertion-25118), [#25.1.20](#test-assertion-25120))
3. Verify discard and state-transition behavior between reusing and reused developer BIEs across WIP, QA, and Production scenarios. (Assertions [#25.1.9](#test-assertion-2519), [#25.1.10](#test-assertion-25110), [#25.1.11](#test-assertion-25111), [#25.1.12](#test-assertion-25112), [#25.1.13](#test-assertion-25113), [#25.1.14](#test-assertion-25114), [#25.1.15](#test-assertion-25115))
4. Verify reused-node retain behavior on the reusing BIE so that reused references are removed while retained association details remain editable in the resulting node. (Assertion [#25.1.20](#test-assertion-25120))
## Test Case 25.2

**Create a Top-level BIE from a BIE node**

> This functionality allows the user to create a top-level BIE from a descendant BIE node within a top-level BIE. The created top-level BIE may be reused within another top-level BIE afterward.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #25.2.1
For any top-level BIE a developer can open to view the details, he can invoke ‘Create top-level BIE’. On any of the descendant ASBIE/ASBIEP/ABIE node that is not a reuse, Consequently:

##### Test Assertion #25.2.1.a
A top-level BIE is created based on the content of the selected ASBIEP. It shall be a copy of that ASBIEP.
##### Test Assertion #25.2.1.b
The newly created top-level BIE shall be in WIP state (after finish copying) and appear in the same release branch as the originating top-level BIE.
##### Test Assertion #25.2.1.c
The new top-level BIE is owned by the developer invoking the creation.

### Test Step Pre-condition:
1. Developer-owned top-level BIEs with descendant non-reuse ASBIE or ABIE nodes exist in the target release branch.
2. The originating BIE and descendant node can be opened by the developer in the BIE editor.


### Test Step:
1. A developer signs in, opens a top-level BIE in the BIE editor, and selects a descendant non-reuse ASBIE or ABIE node.
2. Invoke `Create top-level BIE` from that descendant node.
3. Open the newly created top-level BIE and verify that it exists in the same release branch, is created in `WIP`, and is owned by the invoking developer. (Assertion [#25.2.1](#test-assertion-2521))
## Test Case 25.3

**Warn before an "Used" un-check clears used descendants**

> In the BIE editor tree, the "Used" checkbox sits immediately next to the expand/collapse chevron, so a misclick can un-check a node and cascade-clear "Used" across its entire subtree — silently when the node is collapsed. A confirmation dialog guards this. The warning is deliberately generic (it names the node but does not enumerate the affected descendants) because the tree lazy-loads its children, so an enumerated count would vary with whether the subtree had been expanded.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #25.3.1
When the developer un-checks the "Used" checkbox of a BIE node that has descendants (an expandable node) while it is currently used, a confirmation dialog appears — titled "Unchecking will clear used descendants" and warning that unchecking the node will also clear "Used" on its used descendants — before the change is applied. The dialog names the un-checked node.

#### Test Assertion #25.3.2
If the developer confirms (via "Uncheck anyway"), the node is un-checked and its used descendants are cleared.

#### Test Assertion #25.3.3
If the developer cancels, no change is made and the node's "Used" checkbox is restored to its checked (used) state.

#### Test Assertion #25.3.4
Un-checking a leaf node (a node without descendants) does not trigger the confirmation dialog and is applied immediately.

#### Test Assertion #25.3.5
Checking a node's "Used" on (i.e., the check direction) never triggers the confirmation dialog and is applied immediately, regardless of whether the node has descendants.

### Test Step Pre-condition:
1. A developer-owned top-level BIE in `WIP` (editing) state exists in the latest release branch, with at least one expandable, optional descendant node (an ASBIE whose ACC has a child) and at least one optional leaf node (a BBIE whose data type has no supplementary components).
2. The top-level BIE can be opened by the developer in the BIE editor.


### Test Step:
1. A developer signs in to connectCenter and opens a developer-owned top-level BIE in the BIE editor.
2. On an expandable descendant node, ensure its "Used" checkbox is checked (checking it on must not raise any dialog), then un-check it and verify the confirmation dialog appears with the expected title and message naming the node. (Assertions [#25.3.1](#test-assertion-2531), [#25.3.5](#test-assertion-2535))
3. Cancel the dialog and verify the node's "Used" checkbox is restored to checked. (Assertion [#25.3.3](#test-assertion-2533))
4. Un-check the same node again, confirm the dialog via "Uncheck anyway", and verify the node is now un-checked (its used descendants cleared). (Assertion [#25.3.2](#test-assertion-2532))
5. Check the same node's "Used" on again and verify no confirmation dialog appears and it becomes checked immediately. (Assertion [#25.3.5](#test-assertion-2535))
6. On a leaf node whose "Used" is checked, un-check it and verify no confirmation dialog appears and the change is applied immediately. (Assertion [#25.3.4](#test-assertion-2534))
