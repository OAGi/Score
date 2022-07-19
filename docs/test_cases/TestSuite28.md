# Test Suite 28

**Home Page**


## Test Case 28.1

**BIEs Tab**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #28.1.1
In the “Total BIEs by state” panel, the developer can see the number of all BIEs per state.

#### Test Assertion #28.1.2
In the “Total BIEs by state” panel, the developer can click on a state to view the BIEs of that state in the BIE List page.

#### Test Assertion #28.1.3
In the “My BIEs by state” panel, the developer can see the number of BIEs owned by him per state.

#### Test Assertion #28.1.4
In the “My BIEs by state” panel, the developer can click on a state to view the BIEs of that state and owned by him in the BIE List page.

#### Test Assertion #28.1.5
In the “BIEs by users and states” panel, the developer can see the number of BIEs per user and per state.

#### Test Assertion #28.1.6
In the “BIEs by users and states” panel, the developer can select the user to narrow down the list and see only the number of his BIEs per state.

#### Test Assertion #28.1.7
In the “BIEs by users and states” panel, the developer can click on a table cell, which contains the number of BIEs for a user and a state, to view the relevant BIEs into the BIE List page.

#### Test Assertion #28.1.8
In the “My recent BIEs” panel, the developer can see the last 5 BIEs that he modified or created.

#### Test Assertion #28.1.9
In the “My recent BIEs” panel, the developer can click on a BIE to view it in the Edit BIE page.

### Test Step Pre-condition:

1. There are at least three BIEs created by the OAGi developer and they are in different states (Editing, Candidate, Published).
2. There are at least three BIEs created by an end user and developer (e.g., usera and devx) and they are in different states.

### Test Step:

1. An OAGi developer logins.
2. He visits the Home page and clicks on the BIEs tab.
3. Verify that the “Total BIEs by state” panel contains the number of the BIEs existing in Score per their state (e.g., by first navigating to the BIE List and count them – without using the Database) (Assertion [#1](#test-assertion-2811))
4. He clicks on the Editing state of the “Total BIEs by state” panel.
5. Verify that the BIE list is returned where all BIEs in Editing state are displayed. Also verify that their number is the same as the number displayed inside the Editing bar of the “Total BIEs by state” panel. Finally, verify that there are BIEs owned by different users (e.g., usera) but not BIEs in Candidate or Published state. (Assertion [#2](#test-assertion-2812))
6. The developer goes back to home page and clicks the BIEs tab.
7. He clicks on the Candidate state of the “Total BIEs by state” panel.
8. Verify that the BIE list is returned where all BIEs in Candidate state are displayed. Also verify that their number is the same as the number displayed inside the Candidate bar of the “Total BIEs by state” panel.  Finally, verify that there are BIEs owned by different users (e.g., usera) but not BIEs in Editing or Published state. (Assertion [#2](#test-assertion-2812))
9. The developer goes back to home page and clicks the BIEs tab.
10. He clicks on the Published state of the “Total BIEs by state” panel.
11. Verify that the BIE list is returned where all BIEs in Published state are displayed. Also verify that their number is the same as the number displayed inside the Published bar of the “Total BIEs by state” panel. Finally, verify that there are BIEs owned by different users (e.g., usera) but not BIEs in Editing or Candidate state. (Assertion [#2](#test-assertion-2812))
12. The developer goes back to home page and clicks the BIEs tab.
13. Verify that the “My BIEs by state” panel contains the number of his BIEs per their state (e.g., by first navigating to the BIE List and count them – without using the Database) (Assertion [#3](#test-assertion-2813))
14. He clicks on the Editing state of the “My BIEs by state” panel.
15. Verify that the BIE list is returned where the BIEs owned by the developer and are in Editing state are displayed. Also verify that their number is the same as the number displayed inside the Editing bar of the “My BIEs by state” panel. Finally, verify that there is no BIEs owned by another user or developer (e.g., usera or devx) as well as there is no BIEs in Candidate or in Published state. (Assertion [#4](#test-assertion-2814))
16. The developer goes back to home page and clicks the BIEs tab.
17. He clicks on the Candidate state of the “My BIEs by state” panel.
18. Verify that the BIE list is returned where the BIEs owned by the developer and are in Candidate state are displayed. Also verify that their number is the same as the number displayed inside the Candidate bar of the “My BIEs by state” panel. Finally, verify that there is no BIEs owned by another user or developer (e.g., usera or devx) as well as there is no BIEs in Editing or in Published state. (Assertion [#4](#test-assertion-2814))
19. The developer goes back to home page and clicks the BIEs tab.
20. He clicks on the Published state of the “My BIEs by state” panel.
21. Verify that the BIE list is returned where the BIEs owned by the developer and are in Published state are displayed. Also verify that their number is the same as the number displayed inside the Published bar of the “My BIEs by state” panel. Finally, verify that there is no BIEs owned by another user or developer (e.g., usera or devx) as well as there is no BIEs in Editing or in Candidate state. (Assertion [#4](#test-assertion-2814))
22. The developer goes back to home page and clicks the BIEs tab.
23. Verify that the “BIEs by users and states” panel displays the correct number of BIEs per state and per user (e.g., by first counting them using BIE List page and then compare their number to the ones displayed in the “BIEs by users and states” panel) (Assertion [#5](#test-assertion-2815))
24. The developer clicks on the “User” filter of the “BIEs by users and states” panel and chooses his name.
25. Verify that the “BIEs by users and states” panel displays only the number of the BIEs that he owns. Also, verify that the number of the BIEs per state is correct as well as the Total Number of the BIEs that he owns. (Assertion [#6](#test-assertion-2816))
26. The developer clicks on the “User” filter of the “BIEs by users and states” panel and chooses another username (e.g., usera)
27. Verify that the “BIEs by users and states” panel displays only the number of the BIEs that the usera owns. Also, verify that the number of the BIEs per state is correct as well as the Total Number of the BIEs that the usera owns. (Assertion [#6](#test-assertion-2816))
28. The developer clicks on the number of BIEs that exist under the Editing cell of a developer (e.g., “oagi” developer).
29. Verify that the BIE List page is returned where the BIEs onwed by the “oagi” developer are displayed. Also, verify that the number of them is the same as the number that was displayed in the “BIEs by users and states”. Finally, verify that there is no BIE owned by another user or developer (e.g., devx or usera) (Assertion [#7](#test-assertion-2817))
30. The developer visits the Home page and clicks on BIEs tab.
31. Verify that the “My recent BIEs” contains the last modified BIEs owned by him or the last created BIEs by him. (Assertion [#8](#test-assertion-2818))
32. The developer clicks on a BIE of the “My recent BIEs” panel.
33. Verify that the “Edit BIE” page is returned where he can view or edit the BIE clicked. (Assertion [#9](#test-assertion-2819))

## Test Case 28.2

**User Extensions Tab for Developers**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #28.2.1
In the “Total User Extensions” panel, the developer can see the number of all extensions per state.

#### Test Assertion #28.2.2
In the “Total User Extensions” panel, the developer can click on a state to view the extensions of that state in the Core Component List page.

#### Test Assertion #28.2.3
In the “User Extensions by users and states” panel, the developer can see the number of extensions per user and per state.

#### Test Assertion #28.2.4
In the “User Extensions by users and states” panel, the developer can select the user to narrow down the list and see only the number of his extensions per state.

#### Test Assertion #28.2.5
In the “User Extensions by users and states” panel, the developer can click on a table cell, which contains the number of UEGs for a user and a state, to view the relevant UEGs into the CC List page.

### Test Step Pre-condition:

1. There are at least three UEG created by the OAGi and they are in three editing states  (Editing, Candidate, Published).
2. There is at least one association of a UEG that is not used in any BIE.
3. There are at least three UEG created by an end user and they are in three editing states  (Editing, Candidate, Published).

### Test Step:

1. An OAGi developer logins.
2. He visits the User Extension Tab of the Home Page.
3. Verify that the “Total User Extensions by state” panel contains the number of the UEGs existing in Score per their state (Assertion [#1](#test-assertion-2821))
4. He clicks on the Editing state of the “Total User Extensions by state” panel.
5. Verify that the CC list page is returned where all UEGs in Editing state are displayed. Also verify that their number is the same as the number displayed inside the Editing bar of the “Total User Extensions by state” panel. Finally, verify that there are UEGs owned by different users (e.g., usera) but not UEGs in Candidate or Published state. (Assertion [#2](#test-assertion-2822))
6. The developer goes back to home page and clicks the User Extension tab.
7. He clicks on the Candidate state of the “Total User Extensions by state” panel.
8. Verify that the CC list page is returned where all UEGs in Candidate state are displayed. Also verify that their number is the same as the number displayed inside the Candidate bar of the “Total User Extensions by state” panel.  Finally, verify that there are UEGs owned by different users (e.g., usera) but not UEGs in Editing or Published state. (Assertion [#2](#test-assertion-2822))
9. The developer goes back to home page and clicks the User Extension tab.
10. He clicks on the Published state of the “Total User Extensions by state” panel.
11. Verify that the CC list page is returned where all UEGs in Published state are displayed. Also verify that their number is the same as the number displayed inside the Published bar of the “Total User Extensions by state” panel. Finally, verify that there are UEGs owned by different users (e.g., usera) but not CCs in Editing or Candidate state. (Assertion [#2](#test-assertion-2822))
12. The developer goes back to home page and clicks the User Extension tab.
13. Verify that the “My User Extensions by states” panel contains the number of his UEGs per their state (Assertion [#3](#test-assertion-2823))
14. He clicks on the Editing state of the “My User Extensions by states” panel.
15. Verify that the CC list page is returned where the UEGs owned by the developer and are in Editing state are displayed. Also verify that their number is the same as the number displayed inside the Editing bar of the “My User Extensions by states” panel. Finally, verify that there is no UEGs owned by another user or developer (e.g., usera or devx) as well as there is no CCs in Candidate or in Published state. (Assertion [#4](#test-assertion-2824))
16. The developer goes back to home page and clicks the User Extension tab.
17. He clicks on the Candidate state of the “My User Extensions by states” panel.
18. Verify that the CC list page is returned where the UEGs owned by the developer and are in Candidate state are displayed. Also verify that their number is the same as the number displayed inside the Candidate bar of the “My User Extensions by states” panel. Finally, verify that there is no UEGs owned by another user or developer (e.g., usera or devx) as well as there is no CCs in Editing or in Published state. (Assertion [#4](#test-assertion-2824))
19. The developer goes back to home page and clicks the User Extension tab.
20. He clicks on the Published state of the “My User Extensions by states” panel.
21. Verify that the CC list page is returned where the UEGs owned by the developer and are in Published state are displayed. Also verify that their number is the same as the number displayed inside the Published bar of the “My User Extensions by states” panel. Finally, verify that there is no CCs owned by another user or developer (e.g., usera or devx) as well as there is no CCs in Editing or in Candidate state. (Assertion [#4](#test-assertion-2824))
22. The developer goes back to home page and clicks the User Extension tab.
23. Verify that the “User Extensions by users and states” panel displays the correct number of UEGs per state and per user (Assertion [#5](#test-assertion-2825))
24. The developer clicks on the “User” filter of the “User Extensions by users and states” panel and chooses his name.
25. Verify that the “User Extensions by users and states” panel displays only the number of the UEGs that he owns. Also, verify that the number of the UEGs per state is correct as well as the Total Number of the UEGs that he owns. (Assertion [#6](#test-assertion-2826))
26. The developer clicks on the “User” filter of the “User Extensions by users and states” panel and chooses another username (e.g., usera)
27. Verify that the “User Extensions by users and states” panel displays only the number of the UEGs that the usera owns. Also, verify that the number of the UEGs per state is correct as well as the Total Number of the UEG that the usera owns. (Assertion [#6](#test-assertion-2826))
28. The developer clicks on the number of UEGs that exist under the Editing cell of a developer (e.g., “oagi” developer).
29. Verify that the CC List page is returned where the UEGs onwed by the “oagi” developer are displayed. Also, verify that the number of them is the same as the number that was displayed in the “User Extensions by users and states”. Finally, verify that there is no CC owned by another user or developer (e.g., devx or usera) (Assertion [#7](#test-assertion-2827))
30. The developer visits the Home page and clicks on User Extension tab.
31. Verify that the “My unused extensions in BIEs” contains the UEGs that he owns and not used in any BIE. (Assertion [#8](#test-assertion-2828))
32. The developer clicks on a UEG of the “My unused extensions in BIEs” panel.
33. Verify that the page where he can view or edit the UEG that he clicked is returned. (Assertion [#9](#test-assertion-2829))

## Test Case 28.3

**User Extensions Tab for End Users**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #28.3.1
In the “Total User Extensions” panel, the end user can see the number of all extensions per state.

#### Test Assertion #28.3.2
In the “Total User Extensions” panel, the end user can click on a state to view the extensions of that state in the Core Component List page.

#### Test Assertion #28.3.3
In the “My User Extensions by states” panel, the end user can see the number of extensions owned by him per state.

#### Test Assertion #28.3.4
In the “My User Extensions by states” panel, the end user can click on a state to view the extensions of that state and owned by him in the Core Component List page.

#### Test Assertion #28.3.5
In the “User Extensions by users and states” panel, the end user can see the number of extensions per user and per state.

#### Test Assertion #28.3.6
In the “User Extensions by users and states” panel, the end user can select the user to narrow down the list and see only the number of his extensions per state.

#### Test Assertion #28.3.7
In the “User Extensions by users and states” panel, the end user can click on a table cell, which contains the number of UEGs for a user and a state, to view the relevant UEGs into the CC List page.

#### Test Assertion #28.3.8
In the “My unused extensions in BIEs” panel, the end user can see the associations of the UEGs that he owns and they are not used in BIEs.

#### Test Assertion #28.3.9
In the “My unused extensions in BIEs” panel, the end user can click on a UEG to view it in the page where he can edit it.

### Test Step Pre-condition:



### Test Step: