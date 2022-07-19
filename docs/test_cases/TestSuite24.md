# Test Suite 24

**End user BIE Management**

> Test Case 6.2 and Test Case 6.3 cover other end user’s BIE functions.

## Test Case 24.1

**Reuse a BIE**

> BIE reuse allows for top-level BIEs to be reused under another top-level BIE.

Pre-condition: In a latest release, there are existing top-level BIEs in Production, QA, and WIP state that reference the same CC as a BIE under another top-level BIE being edited (we will call these “BIEs to be reused”). There are also some other top-level BIEs in Production state that do not reference that same CC. There are BIEs with the same DENs as “BIEs to be reused” in an older release.


### Test Assertion:

#### Test Assertion #24.1.1
For a particular descendant node (reuse-target node) of a top-level BIE in Editing state, the end user can invoke the ‘Reuse BIE’ function.

##### Test Assertion #24.1.1.a
The reuse-target node can only be ASBIE/ASBIEP/ABIE node but not any BBIE/BBIEP nor SC node. The root top-level BIE node cannot be a reuse-target node.
##### Test Assertion #24.1.1.b
A confirmation dialog shall appear indicating that “Details of descendant BIEs will be lost. Do you want to continue?”
##### Test Assertion #24.1.1.c
Top-level BIEs in any state owned by any user (i.e., end user or developer) in the same Release as the top-level BIE that reference the same ASCCP as the target node can be chosen from. (Maybe from the UI the ‘reference the same ASCCP’ assertion can only be partially tested that only ASBIEPs with the same property terms are in the list). The pop-up dialog for choosing a top-level BIE must include the following details – State, Property Term, Business Term, Owner, Business Context, Remark, Version, Status, and Updated Date Time (Release is NOT needed, they must be in the same release as the reuse-target anyway). Filters shall be available for Business Context, Owner, Updater, Updated Date Time. Business Context and Remark may be visible only when hovering over.
##### Test Assertion #24.1.1.d
An icon indicating a reused BIE shall be present. Clicking on that node shall open the corresponding top-level BIE in another tab.
##### Test Assertion #24.1.1.e
On the detail pane of a reused BIE node, Business Term, Context Definition, Remark, Version, Owner, Business Contexts of the reused ASBIEP shall be display. The user can still customize cardinality, context definition, nillable details of the ASBIE (which belongs to the reusing top-level BIE). Details of the reused BIE and its descendant nodes cannot be changed. Nillable is editable ONLY if Nillable of the underlying ASCCP is true.

#### Test Assertion #24.1.2
The end user can reuse developer top-level BIE.

#### Test Assertion #24.1.3
The end user can reuse a BIE that has a nested BIE Reuse. All reuse information shall appear appropriately on the UI.

#### Test Assertion #24.1.4
The end user can remove the BIE Reuse. This menu option shall be available ONLY on the reused BIE node. After removal:

##### Test Assertion #24.1.4.a
Only the ASBIE details remain on the detail pane of the node.
##### Test Assertion #24.1.4.b
Other children nodes shall be unchecked.

#### Test Assertion #24.1.5
The end user cannot discard a reused BIE that he owns if it is used in another top-level BIE (the reusing BIE can be owned by a different user).

#### Test Assertion #24.1.6
The end user cannot move a reusing BIE from WIP state to QA state if the reused BIE is in WIP state.

#### Test Assertion #24.1.7
The end user cannot move a reusing BIE from QA state to Production state if the reused BIE is in QA state.

#### Test Assertion #24.1.8
The end user can move a reusing BIE from WIP state to QA state if the reused BIE is in QA or Production state

#### Test Assertion #24.1.9
The end user can move a reusing BIE from QA state to Production state if the reused BIE is in Production state.

#### Test Assertion #24.1.10
The end user cannot move a reused BIE from QA state to WIP state if the reusing BIE is in QA state.

#### Test Assertion #24.1.11
The end user can move a reused BIE from QA state to WIP state if the reusing BIE is in WIP state.

#### Test Assertion #24.1.12
The end user can see the details of a reused BIE node that he does not own, and it is in WIP or QA state. He can also edit the details of the association of the BIE Reuse node.

#### Test Assertion #24.1.13
The end user can express a reusing BIE that reuses a BIE in WIP state and owned by a different user.

### Test Step Pre-condition:



### Test Step:

## Test Case 24.2

**Create a Top-level BIE from a BIE node**

> This functionality allows the user to create a top-level BIE from a descendant BIE node within a top-level BIE. The created top-level BIE may be reused within another top-level BIE afterward.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #24.2.1
An end user opens a top-level BIE to view. The top-level BIE can be in any state and owned by anyone as long as it can be viewed by the end user. On any of the descendant ASBIE/ASBIEP/ABIE node that is not a reuse, the end user can invoke ‘Create top-level BIE’. Consequently:

##### Test Assertion #24.2.1.a
A top-level BIE is created based on the content of the selected ASBIEP. It shall be a copy of that ASBIEP.
##### Test Assertion #24.2.1.b
The newly created top-level BIE shall be in WIP state (after finish copying) and appear in the same release branch as the originating top-level BIE.
##### Test Assertion #24.2.1.c
The new top-level BIE is owned by the end user invoking the creation.

### Test Step Pre-condition:



### Test Step: