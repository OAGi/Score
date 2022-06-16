# Test Suite 13

> Release Branch Core Component Management Behavior for OAGIS Developer


## Test Case 13.1

> Access to Core component viewing, editing and commenting

Pre-condition: A release branch, i.e., one of the release branches, is selected.
(As it is now) individual ASCC page is not provided. Generally, the developer cannot make add or change content of released developer CCs and end user CCs. He can only see the detail.


### Test Assertion:

#### Test Assertion #13.1.1
The developer can see in the CC View/Edit page all CCs owned by any user in any state in the selected release. The UEGACC shall be displayed in the list, but the UEGASCC and UEGASCCP shall not be displayed (hidden). But there shall not be any developer CC listed in a release branch that is not in the Published state (this is not a query condition, i.e., such situation shouldn’t exist in the database).

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



### Test Step: