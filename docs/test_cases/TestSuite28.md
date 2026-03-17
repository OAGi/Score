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
1. Developer accounts and top-level BIE data exist across `WIP`, `QA`, and `Production` states for release `10.8.4`, and additional developer BIE data exists in another release (for example `10.8.3`) so `All`-branch aggregation checks can be exercised.
2. Additional developer and end-user BIE data exists so the `BIEs by users and states` table can show multiple users and totals.

### Test Step:
1. A developer signs in, opens the Home page, selects the `BIEs` tab, and uses the relevant branch filter (`10.8.4` for branch-specific panels and `All` where the suite checks cross-release multi-user totals).
2. Verify the `Total BIEs by state` panel counts and open `WIP`, `QA`, and `Production` state bars to confirm that the returned BIE list contains only records in the selected state. (Assertions [#28.1.1](#test-assertion-2811), [#28.1.2](#test-assertion-2812))
3. Verify the `My BIEs by state` panel counts and open its `WIP`, `QA`, and `Production` state bars to confirm that only the developer’s BIEs in the selected state are listed. (Assertions [#28.1.3](#test-assertion-2813), [#28.1.4](#test-assertion-2814))
4. Verify the `BIEs by users and states` panel totals, apply the user filter, and open the `WIP`, `QA`, `Production`, and `Total` table cells to confirm the linked BIE list results. (Assertions [#28.1.5](#test-assertion-2815), [#28.1.6](#test-assertion-2816), [#28.1.7](#test-assertion-2817))
5. Verify the `My recent BIEs` panel and open a recent BIE from that panel into the BIE editor. (Assertions [#28.1.8](#test-assertion-2818), [#28.1.9](#test-assertion-2819))

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
1. Developer and end-user accounts, release `10.8.4`, and user extension groups exist across `WIP`, `QA`, and `Production` states.
2. The current `TS_28` developer user-extension coverage automates only the `Total User Extensions` panel and the `User Extensions by users and states` panel for developers.

### Test Step:
1. A developer signs in, opens the Home page, selects the `User Extensions` tab, and sets branch `10.8.4` where required.
2. Verify the `Total User Extensions` panel counts and open the `WIP`, `QA`, and `Production` state bars to confirm the returned core component list contains only extensions in the selected state. (Assertions [#28.2.1](#test-assertion-2821), [#28.2.2](#test-assertion-2822))
3. Verify the `User Extensions by users and states` table, apply the user filter, and open a user-state table cell to confirm the linked core component list results. (Assertions [#28.2.3](#test-assertion-2823), [#28.2.4](#test-assertion-2824), [#28.2.5](#test-assertion-2825))

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
1. End-user accounts, release `10.8.4`, and end-user-owned user extension groups exist across `WIP`, `QA`, and `Production` states.
2. There are end-user BIEs with local extensions so the `My unused extensions in BIEs` panel can be populated.


### Test Step:
1. An end user signs in, opens the Home page, selects the `User Extensions` tab, and sets branch `10.8.4` where required.
2. Verify the `Total User Extensions` panel counts and open the `WIP`, `QA`, and `Production` state bars to confirm the returned core component list contains only extensions in the selected state. (Assertions [#28.3.1](#test-assertion-2831), [#28.3.2](#test-assertion-2832))
3. Verify the `My User Extensions by states` panel counts and open its state bars to confirm only the end user’s extensions in the selected state are listed. (Assertions [#28.3.3](#test-assertion-2833), [#28.3.4](#test-assertion-2834))
4. Verify the `User Extensions by users and states` panel, apply the user filter, and open a user-state table cell to confirm the linked core component list results. (Assertions [#28.3.5](#test-assertion-2835), [#28.3.6](#test-assertion-2836), [#28.3.7](#test-assertion-2837))
5. Verify the `My unused extensions in BIEs` panel entries and open an unused extension from that panel into its edit page. (Assertions [#28.3.8](#test-assertion-2838), [#28.3.9](#test-assertion-2839))
