# Test Suite 19

**Release Management**

> Only developers can access release management. There isn’t a release owner, but only developers manage a release. End users can see what is going on.

## Test Case 19.1

**Release management**

Pre-condition: N/A

> Current E2E coverage in this suite focuses on release creation and discard, draft creation from candidate content, selected reference and extension validations, draft visibility in branch selectors, publishing, published-release immutability, and end-user read-only access. Assertions about replaced-by reference validation, non-reusable ASCCP validation, candidate locking during draft creation, and migration-script generation are not automated here.


### Test Assertion:

#### Test Assertion #19.1.1
The developer can create a release. The following information can be specified. Release Number, Release License, Release Note, Release Namespace. Release Number and Namespace are required. Once the release is created, it is in the Initialized state – we can call it Initialized Release. There can be multiple Initialized Releases at a particular point in time.

#### Test Assertion #19.1.2
The developer can discard a release that is in the Initialized state if there is no reference to it (e.g., modules).

#### Test Assertion #19.1.3
From the Initialized Release, a Release Draft can be created by a developer – Create a Release Draft function. There can be only one Release Draft at a particular point in time. I.e., when a Release Draft is being created, another developer MUST NOT be able to invoke the Create a Release Draft function; and when there is a Release Draft, another Release Draft cannot be created. The process of creating a release draft has the following steps.

##### Test Assertion #19.1.3.a
The developer can add CCs in the Working branch that are in the Candidate state (no CCs in other states are allowed) to the release draft (this can include new and revised CCs).
##### Test Assertion #19.1.3.b
The developer can invoke Validate to:
##### Test Assertion #19.1.3.c
Validate References. This is an Error validation. The system ensures that those new and revised CCs reference CCs that are included in the release and that are in Candidate or Published state. The following references should be validated 1) ASCCP to ACC, 2) ACC to ASCCP in its ASCC, 3) ACC to BCCP in its BCC, 4) BDT to Code List, 5) BCCP to BDT, 6) BDT to Agency ID List, 7) ACC to based ACC, 8) ACC to replaced-by ACC, 9) ASCCP to replaced-by ASCCP, 10) BCCP to replaced-by BCCP. If the target entity is not in the prior release or the release assignment, the validation fails. If the validation fail, the system shall list CCs that have invalid references and list those CCs that are needed to make the release draft valid. Missing CCs must be listed along with the current state and owner (needed CCs may be in Draft, WIP, or Deleted state or in Candidate state but not included in the release assignment). Optionally, the developer can notify (via email) to the owners of those needed CCs that they need to act upon those CCs so that the release draft can be created. The developer can open the detail page of a needed CC (in another tab), in which actions can be performed based on his ownership and state of the CC. The system shall also give warnings for ACC, ASCCP, BCCP, Code List, Agency ID List and DT revised and used by the CCs in the release assignment, but not included in the release assignment.
##### Test Assertion #19.1.3.d
Validate Extension. This is a Warning validation. Give a warning:
##### Test Assertion #19.1.3.e
“Warning! This ACC with “ + [ACC Identity] ” should have an Extension component but it does not.”, if an ACC whose Component Type is Semantics and does not have an association to an ASCCP with DEN = “Extension. ” + [ACC Object Class Term] + “ Extension”. [ACC Identity] is “DEN” + DEN + “ and GUID ” + GUID.
##### Test Assertion #19.1.3.f
“Warning! This ACC with “ + [ACC Identity] “ has an Extension component but it is not in the last association.”, if an ACC whose Component Type is Semantics, has an association to an ASCCP with DEN = “Extension. ” + [ACC Object Class Term] + “ Extension”, but the ASCCP is not in the last association.
##### Test Assertion #19.1.3.g
“Warning! This ACC with “ + [ACC Identity] + “ should not have an Extension component.”, if an ACC whose Component Type is not Semantics and has an association to an ASCCP whose Property Term is “Extension”.
##### Test Assertion #19.1.3.h
Validate Deprecation. This is an Error Validation. New entities that are added to the release and that have its deprecated flag equal to True, need to be checked for references associated with the replacements. The replacement entities must be included in the release. If not, the validation shall report an error.
##### Test Assertion #19.1.3.i
Validate non-reusable ASCCP. It is an error if a non-reusable ASCCP is used in an ASCC more than one time. (note: this validation is needed because the user may change the reusable to false after already using the ASCCP multiple times.)
##### Test Assertion #19.1.3.j
If the reference validation fails, the developer can cancel the release draft creation. He can include addition CCs, excludes CC in the release draft, or run the reference validation again (maybe he owns all of the needed CCs and can advance them to the Candidate state).
##### Test Assertion #19.1.3.k
If the reference validation is successful, the user can hit the Finish button. The system creates a release manifest. The system takes the developer back to the Release page.

#### Test Assertion #19.1.4
Initialized Releases shall not show up in the Branch drop down box in the Core Component page or when selecting an ASCCP while creating a BIE.

#### Test Assertion #19.1.5
While a release draft is being created, all Working-branch CCs in the Candidate State that are included in the release draft shall be locked. This is needed so that no one can go in and change states of CCs included in the release draft. So, if anyone has a candidate developer CC already open, they cannot make a commit/change the state, for example, back to WIP.

#### Test Assertion #19.1.6
The created Release Draft shall show up in the Branch drop down box in the Core Component page, so that any users can view the CCs. All CCs shall display the Release Draft state. No actions can be performed in this branch except viewing details of those release draft CCs. The Working branch can still show. Among others, it would include CCs in Release Draft state where no actions can be performed except viewing.

#### Test Assertion #19.1.7
Release draft shall also not show up when selecting a Release for an ASCCP when creating a BIE.

#### Test Assertion #19.1.8
A developer can discard a release draft, i.e., take the release back to the Initialized state. There must be a confirmation dialog. All CCs that were in the Release Draft state shall be set back to the Candidate state. The release manifest of that release shall be cleared.

#### Test Assertion #19.1.9
A developer can move a release draft into the published state. There must be a confirmation dialog. All the CCs in the Release Draft state shall be moved the Published state. Afterward, the release shall appear in the branch drop down box on the CC View/Editing page. At this point the history records related to new and changed CCs in that release should be tagged with the release ID (I think based on this information, the system can list the new and changed CCs in the release. This page can be brought up after clicking the Release Name in the Manage Release page. On that page, a link can also be provided to pull up history records related to a particular CC. This can be a future feature).

#### Test Assertion #19.1.10
While a release in the draft state, a developer can still update its details including Release Number, Release License, Release Note, Release Namespace. Release Number and Release Namespace are required.

#### Test Assertion #19.1.11
After the release is published no further action can be done to the release.

#### Test Assertion #19.1.12
End users cannot manage releases, i.e., cannot initialize a release and move it forward through the process.

#### Test Assertion #19.1.13
End users can view the list of releases and individual details.

#### Test Assertion #19.1.14
A developer can generate a migration script only for the most recent published release. The migration script can be used 
in the external data loader to update other instances to the recent published release. 

### Test Step Pre-condition:
1. Developer and end-user accounts needed for release-management scenarios are available in connectCenter.
2. The suite prepares initialized releases, candidate Working-branch CCs, and selected dependency scenarios needed for draft creation and validation checks.
3. Where publish is verified, the acting developer has the permissions required by the current application configuration.


### Test Step:
1. A developer signs in to connectCenter and opens the View/Edit Release page.
2. Create an initialized release with the required release number and namespace, verify that it can be discarded when unused, and verify that initialized releases do not appear in the Core Component or BIE branch selectors. (Assertions [#19.1.1](#test-assertion-1911), [#19.1.2](#test-assertion-1912), [#19.1.4](#test-assertion-1914))
3. Create a draft release from an initialized release, assign all candidate content, and validate the implemented dependency scenarios: ACC to ASCCP, ACC to BCCP, ASCCP to ACC, ACC to based ACC, and extension-related missing-reference cases. (Assertion [#19.1.3](#test-assertion-1913))
4. Verify the successful and cancelled draft-release flows: when validation succeeds, create the draft release, verify that Release Draft content appears read-only in the branch selector for Core Components but is excluded from BIE branch selection, and verify that moving the draft back to Initialized restores the CC state. (Assertions [#19.1.3](#test-assertion-1913), [#19.1.6](#test-assertion-1916), [#19.1.7](#test-assertion-1917), [#19.1.8](#test-assertion-1918))
5. Publish a draft release, verify that draft release details can still be updated before publishing, verify that assigned CCs move to Published, and verify that the published release becomes read-only. (Assertions [#19.1.9](#test-assertion-1919), [#19.1.10](#test-assertion-19110), [#19.1.11](#test-assertion-19111))
6. Sign in as an end user and verify that release-management actions are unavailable while release details remain viewable. (Assertions [#19.1.12](#test-assertion-19112), [#19.1.13](#test-assertion-19113))

## Test Case 19.2

**View-order forwarding across releases**

Pre-condition: N/A

> Sibling view-order weights (the browse / BIE-edit sort order, issue #1638) are authored on the 'Working' release. This case verifies that those weights forward onto the corresponding manifests of a newly drafted release, and that they are removed again — and only for that release — when the draft is rolled back to Initialized.

### Test Assertion:

#### Test Assertion #19.2.1
When a Release Draft is created from an Initialized Release, the view-order weights applied on the 'Working' release are copied onto the corresponding manifests of the new release. Each copied weight is re-anchored so that both its view-parent ACC and its reordered ASCC/BCC child point to the new release's copied manifests (not the 'Working' ones), and the weight value is preserved. The 'Working' weights are left unchanged.

#### Test Assertion #19.2.2
When the Release Draft is rolled back to the Initialized state, the view-order weights that were forwarded onto that release are removed, and only those; the weights on the 'Working' release survive.

### Test Step Pre-condition:
1. A developer account and a developer namespace are available in connectCenter.
2. A self-consistent set of Candidate Working-branch CCs exists so that "Assign All" and "Validate" succeed: a view-parent ACC that owns one ASCC child (an ASCCP whose role is a second ACC) and one BCC child.
3. Distinct view-order weights are stored on the 'Working' release for the ACC child and the BCC child of the view-parent ACC.

### Test Step:
1. A developer signs in to connectCenter after view-order weights have been authored on the two 'Working' children of the view-parent ACC.
2. Create an initialized release, create a draft from it, assign all candidate content, validate, and create the draft. After the release reaches the Draft state, verify that both weights were forwarded onto the new release: the forwarded rows are keyed on the new release's copied view-parent ACC, the ASCC/BCC child manifests are the new release's copies (not the 'Working' ones), and the weights are preserved. Verify the 'Working' weights are unchanged. (Assertion [#19.2.1](#test-assertion-1921))
3. Move the draft release back to the Initialized state and verify that the release's forwarded weights are removed while the 'Working' weights remain. (Assertion [#19.2.2](#test-assertion-1922))

## Test Case 19.3

**Release work is all or nothing**

Pre-condition: N/A

> Turning a release into a draft, taking a draft back to Initialized, and publishing a draft do their work in the background (issue #1895). This case verifies that each of them is all or nothing, that the user who asked is told when one fails, and that a library cannot be changed while one of its releases is being worked on. The work is made to fail the way a lost connection to the database would: the database refuses the last step of it, so that everything done before it has to be undone.

### Test Assertion:

#### Test Assertion #19.3.1
When a release is turned into a draft, the list of releases shows it as a Draft when the work is done, without the user searching again. The assigned components are in the Release Draft state.

#### Test Assertion #19.3.2
When the work of turning a release into a draft fails, the release is Initialized again, the assigned components are Candidates as they were, and nothing has been copied into the release. The user who asked is told in a message that the release could not be made a draft and that it is Initialized again, whatever page they are on when the work ends. The message does not give a reference number, because the user cannot look one up.

#### Test Assertion #19.3.3
When the work of taking a draft back to Initialized fails, the release is still a Draft, everything it holds is still there, and the assigned components are still in the Release Draft state. The user who asked is told in a message that the release could not be taken back to Initialized and that it is a Draft again.

#### Test Assertion #19.3.4
When the work of publishing a draft fails, the release is still a Draft and the assigned components are still in the Release Draft state. The user who asked is told in a message that the release could not be published and that it is a Draft again.

#### Test Assertion #19.3.5
A release that is left in the middle of its work (for example because the server was stopped) is opened from the list, and its page says that it is being worked on. While it is, the components of its library cannot be changed, and the user is told that a release of the library is being drafted or published; the components of another library can be changed. When a developer gives the release back, it is Initialized again and the library can be changed.

### Test Step Pre-condition:
1. A developer account (and, for publishing, an administrator account) and a developer namespace are available in connectCenter.
2. A Candidate Working-branch ACC and an Initialized release of the same library exist.
3. For the failures, the database is made to refuse the last step of the work of the release.
4. For the library that cannot be changed, a Working-branch ACC of the library and a Working-branch ACC of another library exist, and the release is put in the state a stopped server leaves it in.

### Test Step:
1. A developer signs in to connectCenter, opens the Initialized release, creates a draft, assigns all candidate content, validates and creates the draft. With the list of releases open and not searched again, verify that the release becomes a Draft and that the ACC is in the Release Draft state. (Assertion [#19.3.1](#test-assertion-1931))
2. Make the work fail and create the draft. Verify that the release is Initialized, that the ACC is a Candidate, that no manifest was copied into the release, and that the message of the user says that nothing was changed and has no reference number. (Assertion [#19.3.2](#test-assertion-1932))
3. Create a draft, make the work fail, and move the draft back to Initialized. Verify that the message of the user says so, that the release is still a Draft, that the number of manifests it holds has not changed, and that the ACC is in the Release Draft state. (Assertion [#19.3.3](#test-assertion-1933))
4. As an administrator, create a draft, make the work fail, and publish it. Verify that the message of the user says so, that the release is still a Draft, and that the ACC is in the Release Draft state. (Assertion [#19.3.4](#test-assertion-1934))
5. Put an Initialized release in the middle of its work. Open it and verify the notice. Try to move an ACC of its library to Draft and verify the message and that the ACC is unchanged; move an ACC of another library to Draft and verify that it is changed. Give the release back and verify that it is Initialized and that the ACC of the library can be moved to Draft. (Assertion [#19.3.5](#test-assertion-1935))
