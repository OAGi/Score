# Test Suite 25

> Developer BIE Management

Test Case 5.5 and Test Case 5.6 covers developer BIE functions.

## Test Case 25.1

> Reuse a BIE

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
The developer cannot move a reusing BIE from WIP state to QA state if the reused BIE is in WIP state.

#### Test Assertion #25.1.11
The developer cannot move a reusing BIE from QA state to Production state if the reused BIE is in QA state.

#### Test Assertion #25.1.12
The developer can move a reusing BIE from WIP state to QA state if the reused BIE is in QA or Production state

#### Test Assertion #25.1.13
The developer can move a reusing BIE from QA state to Production state if the reused BIE is in Production state.

#### Test Assertion #25.1.14
The developer cannot move a reused BIE from QA state to WIP state if the reusing BIE is in QA state.

#### Test Assertion #25.1.15
The developer can move a reused BIE from QA state to WIP state if the reusing BIE is in WIP state.

#### Test Assertion #25.1.16
The developer can see the details of a reused BIE node that he does not own, and it is in WIP, QA or Production state. He can also edit the details of the association of the BIE Reuse node.

#### Test Assertion #25.1.17
The developer can express a reusing BIE that reuses a BIE in WIP state and owned by a different developer.

### Test Step Pre-condition:



### Test Step:

## Test Case 25.2

> Create a Top-level BIE from a BIE node

Pre-condition: N/A
This functionality allows the user to create a top-level BIE from a descendant BIE node within a top-level BIE. The created top-level BIE may be reused within another top-level BIE afterward.


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



### Test Step: