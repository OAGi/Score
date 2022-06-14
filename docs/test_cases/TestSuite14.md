# Test Suite 14


## Test Case 14.1

> Access to Core component viewing, editing, and commenting

Pre-condition: A working release is selected.


### Test Assertion:

#### Test Assertion #14.1.1
The end user can see in the CC View/Edit page all CCs owned by any user in any state. Be sure to test for all states including the Deleted state.

#### Test Assertion #14.1.2
There shall not be any CC listed that is owned by any end user. Particularly, there shall not be any UEGACC listed in the working branch. In other words, all working release CCs shall be owned by developers.

#### Test Assertion #14.1.3
The end user CAN view but CANNOT edit the details of a CC that is in WIP state and owned by a developer. He can add comments.

#### Test Assertion #14.1.4
The end user can view the details of a CC that is in Draft state and owned by a developer but he cannot make any change except adding comments.

#### Test Assertion #14.1.5
The end user can view the details of a Candidate CC owned by a developer, but he cannot make any change. However, he can add comments.

#### Test Assertion #14.1.6
The end user can view the details of a Release Draft CC owned by a developer, but he cannot make any change. However, he can add comments.

#### Test Assertion #14.1.7
The end user can view the details of a Published CC owned by a developer, but he cannot make any change. However, he can add comments.

#### Test Assertion #14.1.8
The end user cannot create a brand-new ACC, ASCCP or a BCCP.

#### Test Assertion #14.1.9
The end user cannot revise an existing ACC, ASCCP or a BCCP.

### Test Step Pre-condition:

1. There are the ACC1devx, ASCCP1devx and BCCP1devx CCs created by a developer and they are in WIP state.
2. There are the ACC2devx, ASCCP2devx and BCCP2devx CCs created by a developer and they are in Draft state.
3. There are the ACC3devx, ASCCP3devx and BCCP3devx CCs created by a developer and they are in Candidate state.

### Test Step:

1. The end user, say usera, logs into the system.
2. He visits the CC list page and selects a working release.
3. Verify that he can view all CCs mentioned in the precondition. (Assertion [#1](#test-assertion-1411))
4. He expands the results to 50.
5. Verify that there is no CCs created by an end user. (Assertion [#2](#test-assertion-1412))
6. He opens the ACC1devx.
7. Verify that he can view the ACC1devx but he cannot make any change neither add comments (Assertion [#3](#test-assertion-1413))
8. He opens the ASCCP1devx.
9. Verify that he can view the ASCCP1devx but he cannot make any change neither add comments. (Assertion [#3](#test-assertion-1413))
10. He opens the BCCP1devx.
11. Verify that he can view the BCCP1devx but he cannot make any change neither add comments. (Assertion [#3](#test-assertion-1413))
12. He opens the ACC2devx.
13. Verify that he can view the ACC2devx, he cannot make any change, but he can add comments. (Assertion [#4](#test-assertion-1414))
14. He opens the ASCCP2devx.
15. Verify that he can view the ASCCP2devx, he cannot make any change, but he can add comments. (Assertion [#4](#test-assertion-1414))
16. He opens the BCCP2devx.
17. Verify that he can view the BCCP2devx, he cannot make any change, but he can add comments. (Assertion [#4](#test-assertion-1414))
18. He opens the ACC3devx.
19. Verify that he can view the ACC3devx, he cannot make any change, but he can add comments. Also, verify that there is no revise button. (Assertion [#5](#test-assertion-1415), [#7](#test-assertion-1417))
20. He opens the ASCCP3devx.
21. Verify that he can view the ASCCP3devx, he cannot make any change, but he can add comments. Also, verify that there is no revise button.  (Assertion [#5](#test-assertion-1415), [#7](#test-assertion-1417))
22. He opens the BCCP3devx.
23. Verify that he can view the BCCP3devx, he cannot make any change, but he can add comments. Also, verify that there is no revise button. (Assertion [#5](#test-assertion-1415), [#7](#test-assertion-1417))
24. The usera visits the CC list page and he selects a working release.
25. Verify that there are no Create ACC, Create ASCCP and Create BCCP buttons. (Assertion [#6](#test-assertion-1416))