# Test Suite 29

**BIE Uplifting**


## Test Case 29.1

Pre-condition: There should exist at least two published releases. There should also be some BIEs in a non-latest release. Some of them should reuse a BIE and have an extension (UEGACC).


### Test Assertion:

#### Test Assertion #29.1.1
A BIE can be uplifted only to a newer release than the one the BIE belongs to.

#### Test Assertion #29.1.2
A user can uplift any QA or Production BIE. If he does not own the BIE, then he takes the ownership of the uplifted BIE.

#### Test Assertion #29.1.3
A user cannot uplift a WIP BIE he does not own.

#### Test Assertion #29.1.4
The assigned business contexts shall be transferred to the uplifted BIE.

#### Test Assertion #29.1.5
During the uplifting process, there should be two panels; one showing the tree of the BIE to be uplifted (source BIE) and one showing the uplifted BIE (target BIE). The actions of the system and the user can be:

##### Test Assertion #29.1.5.a
If all nodes between two BIEs match (i.e., there is no CC refactoring between two releases), there should be no checkbox in the source BIE and all checkboxes in the target BIE should be disabled.
##### Test Assertion #29.1.5.b
In case that a node does not match (i.e., because of refactoring a CC or extensions), there should be a checkbox in front of that node in the source BIE. The user can click on that node and then click on the node of the target BIE that wants to match with.  Matching is allowed only between nodes of the same type, i.e., ASBIE/ASBIEP/ABIE node with ASBIE/ASBIEP/ABIE node, BBIE node with BBIE node, and BBIE_SC node with BBIE_SC node. The details of the source node are transferred to the target node. However, its child nodes have to be matched separately in case they do not automatically match, i.e., the user has to perform this process for all unmatched nodes.
##### Test Assertion #29.1.5.c
If a map is performed on a descendant node on the target BIE, the ancestor nodes shall be automatically checked, but no BIE information is copied to any of those ancestor nodes.
##### Test Assertion #29.1.5.d
If a node of the source BIE is a reuse node and it was not automatically mapped, the user can map it to a node in the target BIE. Once mapped, the user can select a BIE to reuse in the target BIE. This reused BIE shall belong to the newer release (this might also be a previously uplifted BIE). If the user does not select a BIE to reuse, then only the ASBIE information (i.e., the association information and not the profiled details) are transferred from the source reused node to the target node.

#### Test Assertion #29.1.6
The uplifted BIE should include the nodes along with their details with the following rules.

##### Test Assertion #29.1.6.a
If a node was enabled, it should be also enabled in the uplifted BIE. All its details should be transferred as well.
##### Test Assertion #29.1.6.b
If a node is not enabled, it should not be enabled in the uplifted BIE. In addition to that, its details should not be transferred.

#### Test Assertion #29.1.7
The user can uplift a BIE without matching all nodes, i.e., he can leave some nodes unmatched. In that case, the uplifted BIE should not contain the information of the node left unmatched.

#### Test Assertion #29.1.8
The user can uplift a BIE by matching a node to another node with different term (e.g., the “Sender” to the “Document Identifier Set”). In that case, the uplifted BIE should contain only the association information (i.e., those of the “Sender”).

#### Test Assertion #29.1.9
The selected Primitive Value of a source BBIE or BBIE_SC node should:

##### Test Assertion #29.1.9.a
Be transferred to the target BBIE or BBIE_SC node in case of system map.
##### Test Assertion #29.1.9.b
Be transferred to the target BBIE or BBIE_SC node in case of manual map considering that the Primitive value is allowed in the target BBIE or BBIE_SC node.
##### Test Assertion #29.1.9.c
Not be transferred to the target BBIE or BBIE_SC node in case of manual map if the value is not allowed. In this case the value of the set target BBIE or BBIE_SC node is set to the default one.

#### Test Assertion #29.1.10
If a source BBIE or BBIE_SC node has a specific developer code list or agency ID list applied:

##### Test Assertion #29.1.10.a
It should be transferred to the target BBIE or BBIE_SC node in case of system map providing that the developer code list or agency ID is found in the target release. Otherwise, the BBIE or BBIE_SC node is set to default primitive value.
##### Test Assertion #29.1.10.b
It should be transferred to the target BBIE or BBIE_SC node in case of manual map providing that the developer code list or agency ID is found in the target release (using primary key) and the code list or agency ID is allowed in the target BBIE or BBIE_SC node. Otherwise, the BBIE or BBIE_SC node is set to default primitive value.

#### Test Assertion #29.1.11
If a source BBIE or BBIE_SC node has a specific end user code list or agency ID list applied:

##### Test Assertion #29.1.11.a
It should be transferred to the target BBIE or BBIE_SC node in case of system map providing that the end user code list or agency ID is found in the target release. Otherwise, the BBIE or BBIE_SC node is set to default primitive value.
##### Test Assertion #29.1.11.b
It should be transferred to the target BBIE or BBIE_SC node in case of manual map providing that the end user code list or agency ID is found in the target release and the code list or agency ID is allowed in the target BBIE or BBIE_SC node. Otherwise, the BBIE or BBIE_SC node is set to default primitive value.

#### Test Assertion #29.1.12
Paths of unmapped source nodes including the code list and agency ID list nodes shall be expressed in an uplift output/log file. If the unmapped node is a reused node, the path shall be indicated as Reused. There is no need to include paths of the descendant nodes of the reused BIE node.

#### Test Assertion #29.1.13
Tree expansion should reflect the nested reused BIE path in BIE uplift page if the source BIE has the nested reuse BIE. 

### Test Step Pre-condition:
1. Published releases in the `connectSpec` library are available for the uplift paths exercised by the suite, including older source releases (`10.8.6`, `10.8.7.1`, `10.8.8`) and newer target release `10.9`.
2. The suite can create the developer and end-user accounts, business contexts, namespaces, BIEs, reused BIE hierarchies, local extensions, and code or agency lists needed to exercise system-map, manual-map, unmatched-node, and nested-reuse uplift scenarios.
3. Nested reused-BIE uplift coverage in `TS_29` is exercised through the same reuse and uplift flows used for assertion [#29.1.5.d](#test-assertion-2915d) rather than through a separate standalone test.


### Test Step:
1. A developer or end user signs in, prepares source-branch BIEs in the older release, and configures the source data needed for uplift validation, including business contexts, enabled and disabled BBIE or ASBIE or BBIE_SC nodes, local extensions, reused BIEs, primitive restrictions, and developer or end-user code or agency lists.
2. Open the `Uplift BIE` page and verify that uplift is allowed only from an older release to a newer release, that QA or Production BIEs can be uplifted by another user with ownership transferred to the uplifted result, and that a non-owner cannot uplift another user’s `WIP` BIE. (Assertions [#29.1.1](#test-assertion-2911), [#29.1.2](#test-assertion-2912), [#29.1.3](#test-assertion-2913))
3. Uplift system-mapped BIEs and verify that business contexts are transferred, automatically matched target nodes are checked and disabled in the verification page, enabled source nodes retain their details after uplift, and nodes left disabled do not become enabled in the uplifted BIE. (Assertions [#29.1.4](#test-assertion-2914), [#29.1.5.a](#test-assertion-2915a), [#29.1.6.a](#test-assertion-2916a), [#29.1.6.b](#test-assertion-2916b))
4. Perform manual mapping for unmatched nodes and verify the automated compatible mapping flows, ancestor auto-check behavior, unmatched-node omission, and the different-term mapping case where only association information is transferred. The current automation does not explicitly prove that incompatible cross-type mappings are rejected. (Assertions [#29.1.5.b](#test-assertion-2915b), [#29.1.5.c](#test-assertion-2915c), [#29.1.7](#test-assertion-2917), [#29.1.8](#test-assertion-2918))
5. Uplift reused-node scenarios, map reuse nodes to target-release reuse candidates, and verify that reused-node association details and nested reused paths are handled correctly on the uplift verification tree and in the uplifted BIE. The current automation covers explicit reuse selection, but not the fallback path where no reused BIE is selected after mapping. (Assertions [#29.1.5.d](#test-assertion-2915d), [#29.1.13](#test-assertion-29113))
6. Verify primitive-value transfer through both system mapping and manual mapping, including cases where the target primitive is allowed and cases where the target node falls back to its default primitive because the source value is not allowed. (Assertions [#29.1.9.a](#test-assertion-2919a), [#29.1.9.b](#test-assertion-2919b), [#29.1.9.c](#test-assertion-2919c))
7. Verify developer and end-user code-list or agency-list transfer through both system mapping and manual mapping, including uplifted target-release list reuse where available and defaulting behavior where the source list is not valid for the target node. (Assertions [#29.1.10.a](#test-assertion-29110a), [#29.1.10.b](#test-assertion-29110b), [#29.1.11.a](#test-assertion-29111a), [#29.1.11.b](#test-assertion-29111b))
8. Complete an uplift with unmatched nodes and verify that the resulting uplift output or log lists the unmatched source-node paths, including code-list or agency-list nodes and reused-node entries. (Assertion [#29.1.12](#test-assertion-29112))
