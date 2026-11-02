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
Automatically matched nodes have checked mapping indicators in both trees. The source checkbox is read-only, including for system mappings. A target checkbox is enabled when the currently selected source can legally map to that target; a system mapping may be manually overridden. Root nodes and descendants covered by a selected reuse reference do not expose independent mapping checkboxes.
##### Test Assertion #29.1.5.b
Select the source node, then the target node and its mapping checkbox. Matching is allowed only between the same tree-node types: association nodes with association nodes, BBIE with BBIE, and BBIE_SC with BBIE_SC. BBIE attributes and elements cannot map to each other. If the referenced ACC, DT, or DT_SC differs, a Mapping confirmation dialog requires Continue; Cancel keeps the previous mapping. Mapped node details are transferred. Unmatched children must be mapped separately when no automatic match exists.
##### Test Assertion #29.1.5.c
When a mapped descendant requires otherwise unmapped target ancestors, those ancestors are enabled in the resulting BIE using target defaults; source-node profiling details are not copied to them. This does not mean the verification tree automatically creates source-to-target mapping pairs for those ancestors.
##### Test Assertion #29.1.5.d
If a node of the source BIE is a reuse node and it was not automatically mapped, the user can map it to a node in the target BIE. Once mapped, the user can select a BIE to reuse in the target BIE. This reused BIE shall belong to the newer release (this might also be a previously uplifted BIE). If the user does not select a BIE to reuse, the warning and inline-copy behavior in [#29.1.5.e](#test-assertion-2915e) applies.
##### Test Assertion #29.1.5.e
Reuse selection is occurrence-specific (OAGi/Score#1735). Used, unlocked source reuse nodes that are already mapped but have no selected target reuse reference are listed in the warning. An association that is itself unmatched remains an unmatched row under #29.1.7 and is not presented as an inline-copy warning. For mapped reuse nodes:
- If the user selects a BIE to reuse for the target release, the uplifted node shall keep a *reference* to that reused BIE — shown as a reuse icon on the node when the uplifted BIE is reopened — rather than an inline copy of the reused BIE's fields. Selecting the same reused BIE on two or more nodes shall not raise an error.
- If the user leaves a mapped reuse node without selecting a BIE to reuse, the system shall warn the user (`Proceed without selecting reuse BIEs?`) and list the affected node path(s) before proceeding. Cancel returns to verification without proceeding with uplift. If the user continues, the mapped reuse node's fields are inline-copied into the uplifted BIE and no reference to a reused BIE is kept (the uplifted node shows no reuse icon).

##### Test Assertion #29.1.5.f
Ordinary descendants must preserve structural parent pairing: an unmapped parent or a different mapped occurrence prevents the child mapping. Removing a parent mapping disables descendant mapping; restoring the parent makes compatible descendants mappable again. Extension descendants are an exception: they may relocate under target ancestry compatible with their source ancestry, allowing extension content to be moved into standard nodes. Descendants covered by a selected target reuse BIE expose no independent mapping checkbox.

##### Test Assertion #29.1.5.g
Repeated occurrences are evaluated independently. A source BIE may contain the same reused BIE more than once, and each occurrence may independently select a target-release reuse BIE, use a custom mapping, or remain unselected for inline copy. The result must retain occurrence-specific references without cross-occurrence duplication or loss.

##### Test Assertion #29.1.5.h
Base and inherited source BIEs can both be uplifted. The uplifted root is a new independent BIE; it does not retain a base link to the source release. Effective source profiling and reuse occurrences participate in uplift. Separately, an existing inherited BIE in the target release can be selected as a reuse candidate: the uplifted parent must reference that exact inherited BIE, whose own base relationship remains intact. The current flow does not automatically uplift a base/inherited pair into a new inheritance hierarchy.

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
It should be transferred to the target BBIE or BBIE_SC node in case of system map providing that a matching developer code list or agency ID list is found and allowed on the target node. Otherwise, the BBIE or BBIE_SC node is set to default primitive value.
##### Test Assertion #29.1.10.b
It should be transferred to the target BBIE or BBIE_SC node in case of manual map providing that a matching developer code list or agency ID list is found in the target release and allowed on the target BBIE or BBIE_SC node. Otherwise, the BBIE or BBIE_SC node is set to default primitive value.

#### Test Assertion #29.1.11
If a source BBIE or BBIE_SC node has a specific end user code list or agency ID list applied:

##### Test Assertion #29.1.11.a
It should be transferred to the target BBIE or BBIE_SC node in case of system map providing that a matching end-user code list or agency ID list is found and allowed on the target node. Otherwise, the BBIE or BBIE_SC node is set to default primitive value.
##### Test Assertion #29.1.11.b
It should be transferred to the target BBIE or BBIE_SC node in case of manual map providing that the end user code list or agency ID is found in the target release and the code list or agency ID is allowed in the target BBIE or BBIE_SC node. Otherwise, the BBIE or BBIE_SC node is set to default primitive value.

List matching in #29.1.10 and #29.1.11 first uses the component identity matcher (GUID), then falls back to name, list ID, and version ID; agency lists additionally require the agency value name. The selected manifest must be allowed by the target DT/DT_SC. Mere presence elsewhere in the target release is insufficient. Primitive identity in #29.1.9 is resolved by stable XBT ID across release-specific manifests.

#### Test Assertion #29.1.12
Before uplift, Next opens the Uplift BIE Report. Rows show source and target paths, component type, mapping status (System, Manual, or Unmatched), reuse status (Selected or Not selected), and value-domain validation issues. View Issues Only is enabled by default: it retains manual mappings, reuse rows, and validation issues, while hiding clean system mappings. Disabling it shows all report rows. Descendants covered by a selected reuse reference are omitted; descendants being inline-copied can appear. Download exports the currently visible rows as CSV; there is no automatic post-uplift log-file requirement.

#### Test Assertion #29.1.13
Tree expansion should reflect the nested reused BIE path in BIE uplift page if the source BIE has the nested reuse BIE. 

### Test Step Pre-condition:
1. Published releases in the `connectSpec` library are available for the uplift paths exercised by the suite, including older source releases (`10.8.6`, `10.8.7.1`, `10.8.8`) and newer target release `10.9`.
2. The suite can create the developer and end-user accounts, business contexts, namespaces, BIEs, reused BIE hierarchies, local extensions, and code or agency lists needed to exercise system-map, manual-map, unmatched-node, and nested-reuse uplift scenarios.
3. Nested reused-BIE uplift coverage in `TS_29` includes the `BOM -> BOM Item Data -> Party` shape, repeated use of the same child BIE, a base source parent, target-release base and inherited reuse candidates, and fresh test data for every test method. The inherited-BIE fixture uses the suite's established `10.8.7.1` to `10.9` uplift pair, which is also used by the existing TS_29 scenarios.


### Test Step:
1. A developer or end user signs in, prepares source-branch BIEs in the older release, and configures the source data needed for uplift validation, including business contexts, enabled and disabled BBIE or ASBIE or BBIE_SC nodes, local extensions, reused BIEs, primitive restrictions, and developer or end-user code or agency lists.
2. Open the `Uplift BIE` page and verify that uplift is allowed only from an older release to a newer release, that QA or Production BIEs can be uplifted by another user with ownership transferred to the uplifted result, and that a non-owner cannot uplift another user’s `WIP` BIE. (Assertions [#29.1.1](#test-assertion-2911), [#29.1.2](#test-assertion-2912), [#29.1.3](#test-assertion-2913))
3. Uplift system-mapped BIEs and verify that business contexts are transferred, source mapping indicators are checked and read-only, while compatible system-matched target nodes remain editable, enabled source nodes retain their details after uplift, and nodes left disabled do not become enabled in the uplifted BIE. (Assertions [#29.1.4](#test-assertion-2914), [#29.1.5.a](#test-assertion-2915a), [#29.1.6.a](#test-assertion-2916a), [#29.1.6.b](#test-assertion-2916b))
4. Perform manual mapping for unmatched nodes and verify the automated compatible mapping flows, ancestor enablement in the resulting BIE, unmatched-node omission, and the different-term mapping case where only association information is transferred. In the repeated nested-reuse flow, map `Party` to `Manufacturing Party` below the same `BOM Item Data` occurrence and verify the mapping completes, then attempt the same mapping below the other parent occurrence and verify that the target checkbox is disabled. (Assertions [#29.1.5.b](#test-assertion-2915b), [#29.1.5.c](#test-assertion-2915c), [#29.1.5.f](#test-assertion-2915f), [#29.1.7](#test-assertion-2917), [#29.1.8](#test-assertion-2918))
5. Uplift reused-node scenarios, map reuse nodes to target-release reuse candidates, and verify that reused-node association details and nested reused paths are handled correctly on the uplift verification tree and in the uplifted BIE. Verify that selecting a reuse BIE during uplift keeps the reference on the uplifted node (reuse icon retained when the uplifted BIE is reopened) and that leaving a mapped reuse node unselected raises the `Proceed without selecting reuse BIEs?` warning listing the affected node path and, on continue, inline-copies the fields with no reference kept (no reuse icon). (Assertions [#29.1.5.d](#test-assertion-2915d), [#29.1.5.e](#test-assertion-2915e), [#29.1.5.g](#test-assertion-2915g), [#29.1.5.h](#test-assertion-2915h), [#29.1.13](#test-assertion-29113))
6. Repeat the nested `BOM -> BOM Item Data -> Party` flow with a base BOM Item Data BIE and a target-release inherited BOM Item Data BIE based on it. Use the same source child in repeated BOM occurrences, uplift both a base BOM parent and an inherited BOM parent, select the target base reuse on one occurrence and the target inherited reuse on another, and mix an inline copy with the inherited target reference for the inherited source parent. Verify the inherited base panel before uplift, occurrence-specific reuse references, inline Party copies, and reuse-reference count. The separate repeated-occurrence scenario covers all four selected/unselected combinations. (Assertions [#29.1.5.e](#test-assertion-2915e), [#29.1.5.g](#test-assertion-2915g), [#29.1.5.h](#test-assertion-2915h), [#29.1.13](#test-assertion-29113))
7. Verify primitive-value transfer through both system mapping and manual mapping, including cases where the target primitive is allowed and cases where the target node falls back to its default primitive because the source value is not allowed. (Assertions [#29.1.9.a](#test-assertion-2919a), [#29.1.9.b](#test-assertion-2919b), [#29.1.9.c](#test-assertion-2919c))
8. Verify developer and end-user code-list or agency-list transfer through both system mapping and manual mapping, including uplifted target-release list reuse where available and defaulting behavior where the source list is not valid for the target node. (Assertions [#29.1.10.a](#test-assertion-29110a), [#29.1.10.b](#test-assertion-29110b), [#29.1.11.a](#test-assertion-29111a), [#29.1.11.b](#test-assertion-29111b))
9. Prepare an uplift with unmatched nodes and open the pre-uplift report and verify path-specific unmatched rows (including nodes with code/agency lists), reuse statuses, and the View Issues Only filter. (Assertion [#29.1.12](#test-assertion-29112))

### Automated coverage and assertion IDs

`TC_29_1_TA_5_a` means Test Case 29.1, Test Assertion 29.1.5.a. Each JUnit `@Test` has a `@DisplayName`; multiple IDs mean the method verifies several assertions. Parent headings (29.1.5, 29.1.6, etc.) group the lettered assertions rather than naming additional tests.

The implementation is [TC_29_1_BIEUplifting.java](../../score-e2e/src/test/java/org/oagi/score/e2e/TS_29_BIEUplifting/TC_29_1_BIEUplifting.java).

| JUnit method | Display name / assertions |
| --- | --- |
| `bie_can_be_uplifted_only_to_a_newer_release_than_the_one_the_bie` | `TC_29_1_TA_1` |
| `user_can_uplift_any_qa_or_production_bie_and_takes_ownership_of_the_uplifted_bie` | `TC_29_1_TA_2 (Production, developer)` |
| `end_user_can_uplift_another_users_qa_bie_and_takes_ownership` | `TC_29_1_TA_2 (QA, end user)` |
| `user_cannot_uplift_a_wip_bie_he_does_not_own` | `TC_29_1_TA_3` |
| `uplift_transfers_business_contexts_and_preserves_system_mapped_enabled_nodes` | `TC_29_1_TA_4, TC_29_1_TA_5_a, TC_29_1_TA_6_a` |
| `unused_bbie_and_supplementary_component_details_are_not_transferred` | `TC_29_1_TA_6_b` |
| `in_case_that_a_node_does_not_match_i_e_because_of_refactoring_a` | `TC_29_1_TA_5_b (type compatibility)` |
| `manual_mapping_checks_ancestor_nodes_and_skips_unmatched_node_information` | `TC_29_1_TA_5_b, TC_29_1_TA_5_c, TC_29_1_TA_7, TC_29_1_TA_8` |
| `if_a_node_of_the_source_bie_is_a_reuse_node_and_it_was` | `TC_29_1_TA_5_d` |
| `if_a_node_of_the_source_bie_is_a_reuse_node_left_unselected_warn_before_uplift` | `TC_29_1_TA_5_e (warning and inline copy)` |
| `all_selected_and_unselected_combinations_of_two_bom_item_data_reuses_are_uplifted` | `TC_29_1_TA_5_e, TC_29_1_TA_5_g (distinct children)` |
| `the_same_bom_item_data_reuse_with_a_nested_party_is_inlined_at_both_target_paths` | `TC_29_1_TA_5_g, TC_29_1_TA_13 (same child)` |
| `nested_reuse_uplift_supports_base_and_inherited_bies_at_repeated_occurrences` | `TC_29_1_TA_5_h, TC_29_1_TA_13` |
| `nested_reuse_uplift_supports_valid_custom_mapping_and_rejects_cross_parent_mapping` | `TC_29_1_TA_5_f, TC_29_1_TA_5_g` |
| `be_transferred_to_the_target_bbie_or_bbie_sc_node_in_case_of_system` | `TC_29_1_TA_9_a` |
| `manual_mapping_transfers_allowed_primitive_values_and_defaults_disallowed_values` | `TC_29_1_TA_9_b, TC_29_1_TA_9_c` |
| `it_should_be_transferred_to_the_target_bbie_or_bbie_sc_node_in_case` | `TC_29_1_TA_10_a` |
| `it_should_be_transferred_to_the_target_bbie_or_bbie_sc_node_in_case_scenario_2` | `TC_29_1_TA_10_b` |
| `it_should_be_transferred_to_the_target_bbie_or_bbie_sc_node_in_case_scenario_3` | `TC_29_1_TA_11_a, TC_29_1_TA_11_b` |
| `paths_of_unmapped_source_nodes_including_the_code_list_and_agency_id_list_nodes` | `TC_29_1_TA_12` |

The E2E cases use representative BBIE/BBIE_SC, list, reuse, and extension fixtures; this table does not imply every release/list/state combination is covered. Additional focused checks are in [BieUpliftingServiceTest](../../score-http/src/test/java/org/oagi/score/gateway/http/api/bie_management/service/BieUpliftingServiceTest.java) for primitive/list identity, identifier fallback and allowed target manifests, and [report-dialog.component.spec.ts](../../score-web/src/app/bie-management/bie-uplift/report-dialog/report-dialog.component.spec.ts) for report filtering and CSV escaping. The E2E report case verifies rendered rows, filter behavior, and Download availability; downloaded file contents are not currently checked by E2E.

Implementation references: [mapping eligibility and confirmation](../../score-web/src/app/bie-management/bie-uplift/bie-uplift.component.ts), [uplift generation and value-domain resolution](../../score-http/src/main/java/org/oagi/score/gateway/http/api/bie_management/service/BieUpliftingService.java), and [report rendering/export](../../score-web/src/app/bie-management/bie-uplift/report-dialog/report-dialog.component.ts).

The regression case `TC_29_1_BIEUplifting#if_a_node_of_the_source_bie_is_a_reuse_node_and_it_was` was rerun against MariaDB/Chrome after the path and reuse-candidate fixes (1 test, 0 failures). The complete `TS_29` suite remains environment-dependent and was not rerun here.

### Supplemental edge-case coverage

For `TC_29_1_TA_5_d`, a root `ABIE` entry may contain source/target paths without `targetManifestId`: the request's target ASCCP determines the root ACC. Both create and validation accept this metadata entry. Mapped `ASBIE`, `BBIE`, and `BBIE_SC` entries still require a target manifest ID. `BieUpliftingRequestValidationTest` covers the root-only payload and mixed root/association payloads.

The create endpoint also rechecks that the selected target ASCCP belongs to a newer release than the source and that a supplied target release matches the ASCCP release. Before creating a reuse reference it verifies that the referenced BIE exists in the target release and has the target ASCCP. Every mapped target path is checked against its manifest ID and BIE type. These checks cover direct API requests that bypass the analysis screen (`TC_29_1_TA_1`, `TC_29_1_TA_5_d`, `TC_29_1_TA_5_e`, `TC_29_1_TA_12`).

The web client recomputes target-only required-parent rows for every create attempt and marks descendant mappings cleared by a parent change as explicit negative overrides. This keeps retry requests consistent with the current verification tree (`TC_29_1_TA_5_c`, `TC_29_1_TA_5_f`, `TC_29_1_TA_7`).

The following focused tests pin the edge cases identified in `BIE_UPLIFT_FLOW_CASE_MATRIX.md`:

| Test | Matrix/document coverage |
| --- | --- |
| `org.oagi.score.gateway.http.api.bie_management.repository.jooq.JooqBieQueryRepositoryTest.discoversNestedReuseAfterEarlierLeaf` | `TC_29_1_TA_5_h (R43)`: a leaf reuse does not stop discovery of a later sibling's nested reuse |
| `org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingCustomMappingTableTest` (`explicitUnmatchedOccurrenceIsRetainedAsANegativeOverride`, `legacyUnmatchedEntryDoesNotSuppressAutomaticMapping`) | `TC_29_1_TA_7 (M33/V19)` and `TC_29_1_TA_7 (M32)`: explicit UI unmap is a negative override while legacy unmatched entries remain compatible |
| `org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingRequestValidationTest` | `TC_29_1_TA_1 (A02)` and `TC_29_1_TA_12 (A03–A08, G14/G15)`: malformed requests, null entries, mapped targets without manifest IDs, and missing analysis IDs fail explicitly |
| `org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingServiceTest` | `TC_29_1_TA_12 (A09)` and `TC_29_1_TA_5_d (P26)`: existing but unrelated target path components are rejected, while directly related inherited/group ACC chains are accepted |
| `org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingGenerationOwnershipTest` | `TC_29_1_TA_5_c (E01/E10/§11.4)`: deep target mappings use the target-path owner relation and suppress an available automatic candidate |
| `org.oagi.score.gateway.http.api.bie_management.service.BieUpliftingCodeListDomainTest` / `...BieUpliftingAgencyIdListDomainTest` | `TC_29_1_TA_10_b/TC_29_1_TA_11_b`: manual Code List and Agency ID List retention matches validation for BBIE and BBIE_SC |
| `org.oagi.score.gateway.http.api.code_list_management.service.CodeListQueryServiceTest` / `org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdListQueryServiceTest` | `TC_29_1_TA_10_b (CL09/CL11)` and `TC_29_1_TA_11_b (AG10/AG12)`: explicit relations with no visible candidates do not receive an unrestricted release fallback |
| `org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingCustomMappingTableTest` | `TC_29_1_TA_5 (P01/P02/P04/P05/P09/P10/P13/P15/P16)` and `TC_29_1_TA_12 (A03)`: exact path, duplicate, legacy/suffix rejection, incomplete, and null-entry policies |
| `score-web/.../bie-uplift.component.spec.ts` negative-override and canonical-path cases | `TC_29_1_TA_7 (M33/V19)`, `TC_29_1_TA_5_e (R17)`, and `TC_29_1_TA_5_d (P25)`: report/create state agrees, unmatched reuse is not advertised as inline copy, and reused target paths retain their outer occurrence prefix |
| `org.oagi.score.gateway.http.api.bie_management.service.BieAssociationPathsTest` | `TC_29_1_TA_5_g (R45–R47)`: stale inherited/group association paths are skipped or flattened without dropping valid siblings |

Malformed API inputs now fail with explicit `IllegalArgumentException` for missing custom mappings, null mapping entries, target release, mapping list, BIE type/ID, unsupported BIE types, mapped target manifest ID, or unrelated target path components (`A03–A09`). The service still treats validation as a report and does not block uplift for a value-domain issue (`V16`).
