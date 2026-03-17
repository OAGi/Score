# Test Suite 13

**Release Branch Core Component Management Behavior for OAGIS Developer**


## Test Case 13.1

**Access to Core component viewing, editing and commenting**

> (As it is now) individual ASCC page is not provided. Generally, the developer cannot make add or change content of released developer CCs and end user CCs. He can only see the detail.

Pre-condition: A release branch, i.e., one of the release branches, is selected.


### Test Assertion:

#### Test Assertion #13.1.1
The developer can see in the CC View/Edit page the list-supported CCs in the selected release branch: published developer-owned ACCs, ASCCPs, and BCCPs, plus end-user-owned ACCs, ASCCPs, and BCCPs in WIP, QA, Production, and Deleted states. The UEGACC shall be displayed in the list, but the UEGASCC and UEGASCCP shall not be displayed. Developer CCs that are not in the Published state shall not be listed in the selected release branch.

#### Test Assertion #13.1.2
The developer CAN view but CANNOT edit the details of a EUCC in the selected release that is in WIP state and owned by another user. He can also add comments. This includes the UEGACC.

#### Test Assertion #13.1.3
The developer can view the details of an EUCC that is in QA or Deleted state and owned by any user but he cannot make any change except adding comments; this includes UEGACC. (However, only end user CCs in the QA or Deleted state can show up in the list.)

#### Test Assertion #13.1.4
The developer can view the details of an EUCC which is in Production state owned by any user but he cannot make any change; this includes the UEGACC. He also cannot make an amendment on the CC either, but he can add comments.

#### Test Assertion #13.1.5
The developer can view details of any Published CC but cannot make any change except adding comments.

#### Test Assertion #13.1.6
The developer cannot make a new revision on any CC.

#### Test Assertion #13.1.7
The developer shall not be able to create any new CC.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A release branch, i.e., one of the release branches, is selected.
2. There is a developer account to execute the scenario.
3. There are published developer-owned ACCs, ASCCPs, and BCCPs in the selected release branch.
4. There are end-user-owned ACCs, ASCCPs, and BCCPs in the selected release branch in WIP, QA, Production, and Deleted states.
5. There are non-published developer-owned CCs in the Working branch to verify that they do not appear in the selected release branch.
6. There is at least one UEGACC in the selected release branch to verify its visibility behavior.


### Test Step:
1. The developer signs in to connectCenter, opens the View/Edit Core Component page, and selects the target release branch.
2. Search for the prepared published developer CCs and end-user CCs and verify that the list-supported records appear, while non-published developer CCs from the Working branch do not appear. Also verify that UEGACC is listed but UEGASCC and UEGASCCP are not listed. (Assertions [#13.1.1](#test-assertion-1311))
3. Open end-user CCs in WIP, QA, and Deleted states owned by another user and verify that their details are read-only and comments can be added. (Assertions [#13.1.2](#test-assertion-1312), [#13.1.3](#test-assertion-1313))
4. Open an end-user CC in Production state and verify that it is read-only, comments are allowed, and amendment is not available. (Assertion [#13.1.4](#test-assertion-1314))
5. Open published CCs and verify that their details are view-only, comments are allowed, and revising is not available. (Assertions [#13.1.5](#test-assertion-1315), [#13.1.6](#test-assertion-1316))
6. Verify that creating any new CC is not available from the release-branch CC page. (Assertion [#13.1.7](#test-assertion-1317))
