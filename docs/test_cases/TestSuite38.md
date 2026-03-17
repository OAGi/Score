# Test Suite 38

**Working Branch Data Type Management for Developer**


## Test Case 38.1

**DT access**

Pre-condition: The Working branch is selected.


### Test Assertion:

#### Test Assertion #38.1.1
The developer can see in the CC list page all DTs owned by any developer in any state.

#### Test Assertion #38.1.2
The developer can view and edit the details of a DT that is in WIP state and owned by him.

#### Test Assertion #38.1.3
The developer CAN view but CANNOT edit the details of a DT that is in WIP state and owned by another developer. However, he can add comments.

#### Test Assertion #38.1.4
The developer can view the details of a DT that is in Draft, Candidate, or Release Draft state not owned by him but he cannot make any change except adding comments.

#### Test Assertion #38.1.5
The developer can view the details of a Published DT owned by any developer, but he cannot make any change except adding comments or make a new revision of the DT.

#### Test Assertion #38.1.6
There must not be any end user DT in the Working branch.

#### Test Assertion #38.1.7
The developer can view details of a deleted DT owned by another developer.

#### Test Assertion #38.1.8
The developer cannot edit details of a deleted DT owned by him. He can add comments.

#### Test Assertion #38.1.9
The developer cannot edit details of a deleted DT owned by another developer. He can add comments.

#### Test Assertion #38.1.10
The developer can restore a deleted DT owned by him.

#### Test Assertion #38.1.11
The developer can restore a deleted DT owned by another developer.

#### Test Assertion #38.1.12
The developer can move states of several DTs owned by him in one shot on the view/edit CC page. Test for:

##### Test Assertion #38.1.12.a
Changing state from WIP to Draft
##### Test Assertion #38.1.12.b
Changing state from Draft to WIP
##### Test Assertion #38.1.12.c
Changing state from Draft to Candidate
##### Test Assertion #38.1.12.d
Transfer the ownership
##### Test Assertion #38.1.12.e
Deleting.

#### Test Assertion #38.1.13
The developer cannot move states of several DTs in one shot if all selected DTs are not owned by him.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The Working branch is selected.
2. The users, branches, releases, and records needed to exercise "DT access" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open **Core Component > View/Edit Core Component** with the Working branch selected.
2. Verify DT visibility in the Working branch, including developer-owned DTs in covered states and exclusion of end user DTs. (Assertions [#38.1.1](#test-assertion-3811), [#38.1.6](#test-assertion-3816))
3. Open own and other developers' DTs in `WIP`, `Draft`, `Candidate`, `Release Draft`, `Published`, and `Deleted` states and verify edit, comment, revise, and restore permissions by state and ownership. (Assertions [#38.1.2](#test-assertion-3812), [#38.1.3](#test-assertion-3813), [#38.1.4](#test-assertion-3814), [#38.1.5](#test-assertion-3815), [#38.1.7](#test-assertion-3817), [#38.1.8](#test-assertion-3818), [#38.1.9](#test-assertion-3819), [#38.1.10](#test-assertion-38110), [#38.1.11](#test-assertion-38111))
4. Use bulk actions on owned DTs to change state, transfer ownership, and delete records in the supported Working-branch flows. (Assertions [#38.1.12.a](#test-assertion-38112a), [#38.1.12.b](#test-assertion-38112b), [#38.1.12.c](#test-assertion-38112c), [#38.1.12.d](#test-assertion-38112d), [#38.1.12.e](#test-assertion-38112e))
5. Verify bulk state changes are rejected when the selected DTs are not all owned by the current developer. (Assertion [#38.1.13](#test-assertion-38113))
## Test Case 38.2

**Creating a brand-new DT**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #38.2.1
On the CC list page where the Working branch is selected, the developer can create a brand-new DT after selecting a based DT. The new DT has the following default values – Based Data Type= [DEN of Based DT] and disabled, Data Type Term = [Data Type Term of Based DT] and disabled, Representation Term = [Representation Term of Based DT] and disabled, Qualifier = [Qualifier of Based DT] (the system may tokenize qualifiers) if any, otherwise empty; DEN = [Qualifier] “ “ + [Data Type Term] + “. Type” and disabled; Six Digit Identifier= blank; Namespace = [Namespace of Based DT] if available, Definition Source=blank, Definition=blank, Content Component Definition=[Content Component Definition from Based DT], Comments = empty. There must be entries in the Value Domain that are inherited from based DT – those can’t be edited. The brand-new DT must not have any release assigned yet, i.e., it must not appear in any release branch except the Working branch. It has a revision number 1. All fields are required and cannot be blank except Qualifier, Six Digit Identifier, Definition, Definition Source, Content Component Definition and Comments.

#### Test Assertion #38.2.2
The developer cannot create a brand-new developer DT when a release branch is selected.

### Test Step Pre-condition:
1. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
2. Any additional data required by the assertions has been prepared before execution.

### Test Step:
1. Sign in as a developer and open **Core Component > View/Edit Core Component** with the Working branch selected.
2. Start the create-DT flow from a based DT and verify the documented defaults, inherited value domains, Working-branch scoping, and revision `1` result. (Assertion [#38.2.1](#test-assertion-3821))
3. Switch to a release branch and verify brand-new developer DT creation is unavailable there. (Assertion [#38.2.2](#test-assertion-3822))
## Test Case 38.3

**Editing a brand-new developer DT**

Pre-condition: The brand-new DT is created by the developer and is in the WIP state. The developer accesses these functionalities by opening the brand-new DT from the CC list page or after creating a brand-new DT according to the Test Case 38.2.


### Test Assertion:

#### Test Assertion #38.3.1
The developer can change the properties of the DT and save changes with the following business rules.

##### Test Assertion #38.3.1.a
Namespace and Domain Value are required.  There should be a drop-down list for Namespace. A new value domain entry can be added, discarded, or Edited according to Test Case 38.8. Inherited domain value entry cannot be edited. Only Namespace that is a standard namespace shall be allowed.
##### Test Assertion #38.3.1.b
Qualifier can be updated such that it contains zero or more qualifiers in front of the qualifiers inherited from the based DT.
##### Test Assertion #38.3.1.c
Six digit identifier is optional.
##### Test Assertion #38.3.1.d
Content Component Definition, Definition and Definition Source are optional. However, A warning should be given when the Definition is empty.

#### Test Assertion #38.3.2
A new SC can be added to the DT and edited according to Test Case 38.4 and Test Case 38.6, respectively.

#### Test Assertion #38.3.3
SC inherited from based DT can only be edited according to Test Case 38.7.

#### Test Assertion #38.3.4
Once the Update button is clicked, the changes, except Definition and Definition Source and Content Component Definition, must also be made to all DTs derived from this DT (test for at least 2 levels of derivations). Definition, Definition Source, and Content Component Definition shall be propagated only if they were the same before the change. In other word, if these fields have been altered in the derived DTs before the change in this DT, leave the one altered alone. New value domain added in the derived DT should not be lost/overwritten. Note that the Namespace should not be propagated to the derived DTs.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The brand-new DT is created by the developer and is in the WIP state. The developer accesses these functionalities by opening the brand-new DT from the CC list page or after creating a brand-new DT according to the Test Case 38.2.
2. The users, branches, releases, and records needed to exercise "Editing a brand-new developer DT" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open the brand-new `WIP` DT created in the Working branch.
2. Update DT properties and verify namespace and value-domain requirements, qualifier handling, optional fields, and the empty-definition warning. (Assertions [#38.3.1.a](#test-assertion-3831a), [#38.3.1.b](#test-assertion-3831b), [#38.3.1.c](#test-assertion-3831c), [#38.3.1.d](#test-assertion-3831d))
3. Add a new SC and verify that locally added and inherited SC behavior follows the dedicated SC test flows. (Assertions [#38.3.2](#test-assertion-3832), [#38.3.3](#test-assertion-3833))
4. Update the DT and verify supported changes propagate to derived DTs without overwriting local overrides or added value domains. (Assertion [#38.3.4](#test-assertion-3834))
## Test Case 38.4

**Add a brand-new SC**

Pre-condition: A DT in WIP state is open.


### Test Assertion:

#### Test Assertion #38.4.1
The developer can add an SC to the DT.

#### Test Assertion #38.4.2
Default values of the new SC shall be as follows.

##### Test Assertion #38.4.2.a
Object Class Term = [Data Type Term of the DT]. The field is not editable.
##### Test Assertion #38.4.2.b
Property Term = “Property Term” + [a number]. The number ensures the Property Term is unique. It shall be unique across all DT derived from this DT as well. This field is required.
##### Test Assertion #38.4.2.c
Representation Term = one of the values in the dropdown list. (The list contains Representation Terms from all CDTs). This field is required.
##### Test Assertion #38.4.2.d
Cardinality = Optional.  This field is required.
##### Test Assertion #38.4.2.e
Value Constraint = None.
##### Test Assertion #38.4.2.f
Value Domain is populated with entries based on the selected Representation Term. Since each Representation Term corresponds to a single CDT, value domains from the associated CDT are populated. These value domains are not editable.
##### Test Assertion #38.4.2.g
Default value domain is set to the one default one based on the Representation Term. This field is required.
##### Test Assertion #38.4.2.h
Definition and Definition Source = blank text. These two fields are optional but warning shall be given when the Definition is empty.

#### Test Assertion #38.4.3
The added SC is propagated to all DTs derived from the DT in which the SC is added.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A DT in WIP state is open.
2. The users, branches, releases, and records needed to exercise "Add a brand-new SC" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open a `WIP` DT in the Working branch.
2. Add a brand-new SC and verify the action succeeds. (Assertion [#38.4.1](#test-assertion-3841))
3. Verify the default SC field values, required fields, locked fields, and default value-domain behavior. (Assertions [#38.4.2.a](#test-assertion-3842a), [#38.4.2.b](#test-assertion-3842b), [#38.4.2.c](#test-assertion-3842c), [#38.4.2.d](#test-assertion-3842d), [#38.4.2.e](#test-assertion-3842e), [#38.4.2.f](#test-assertion-3842f), [#38.4.2.g](#test-assertion-3842g), [#38.4.2.h](#test-assertion-3842h))
4. Update the DT and verify the new SC propagates to derived DTs. (Assertion [#38.4.3](#test-assertion-3843))
## Test Case 38.5

**Remove a brand-new SC**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #38.5.1
The developer can remove a brand-new SC.

#### Test Assertion #38.5.2
The developer can remove a brand-new SC of a DT that is a base of another DT. In this case, the change should be propagated to the latter DT after clicking the Update button.

### Test Step Pre-condition:
1. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
2. Any additional data required by the assertions has been prepared before execution.

### Test Step:
1. Sign in as a developer and open a `WIP` DT with a locally added SC.
2. Remove a brand-new SC and verify it is discarded from the current DT. (Assertion [#38.5.1](#test-assertion-3851))
3. Remove a brand-new SC from a base DT and verify the removal propagates to derived DTs after update. (Assertion [#38.5.2](#test-assertion-3852))
## Test Case 38.6

**Editing a brand-new SC**

Pre-condition: A DT in WIP state is open and there exists a brand-new SC (locally/uninherited) added in the DT revision (first revision or a subsequent revision).


### Test Assertion:

#### Test Assertion #38.6.1
The following fields can be edited with business rule checked either when field is updated or the Update button is clicked:

##### Test Assertion #38.6.1.a
Property Term: Property term shall be unique across all DT derived from this DT as well as within the DT itself. Required.
##### Test Assertion #38.6.1.b
Representation Term can be updated. Required. When the Representation Term is changed the Value Constraint should be reset.
##### Test Assertion #38.6.1.c
Cardinality = Optional or Required. Required.
##### Test Assertion #38.6.1.d
Value Constraint can be updated. Optional.
##### Test Assertion #38.6.1.e
Value Domain: Value Domain based on the selected Representation Term cannot be updated. New value domain can be added or discarded. Code List or Agency ID List value domain can be added only if there is a Token CDT Primitive in the value domains. A value domain with Token CDT Primitive cannot be discarded, if there a Code List or Agency ID List in the value domain – dialog shall indicate that this is the reason the value domain cannot be discarded. A value domain which is a default value domain cannot be discarded.
##### Test Assertion #38.6.1.f
Default value domain can be changed and must be chosen from an existing value domain. Default value domain is required.
##### Test Assertion #38.6.1.g
Definition and Definition Source can be updated and they are optional. Warning shall be given when Definition is empty.

#### Test Assertion #38.6.2
Once the Update button is clicked, the changes, except Definition and Definition Source, must also be made to the corresponding SC in all DTs derived from this DT (test for at least 2 levels of derivations). Definition and Definition Source shall be propagated only if they were the same before the change. In other word, if the Definition and Definition Source have been altered in the derived DTs before the change in this DT, leave the one altered alone. New value domain added in the derived DT should not be lost/overwritten.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A DT in WIP state is open and there exists a brand-new SC (locally/uninherited) added in the DT revision (first revision or a subsequent revision).
2. The users, branches, releases, and records needed to exercise "Editing a brand-new SC" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open a `WIP` DT containing a locally added SC.
2. Edit the SC fields and verify the business rules for property term, representation term, cardinality, value constraint, value domains, default value domain, and definition fields. (Assertions [#38.6.1.a](#test-assertion-3861a), [#38.6.1.b](#test-assertion-3861b), [#38.6.1.c](#test-assertion-3861c), [#38.6.1.d](#test-assertion-3861d), [#38.6.1.e](#test-assertion-3861e), [#38.6.1.f](#test-assertion-3861f), [#38.6.1.g](#test-assertion-3861g))
3. Update the DT and verify supported SC changes propagate to derived DTs without overwriting local overrides. (Assertion [#38.6.2](#test-assertion-3862))
## Test Case 38.7

**Editing an inherited SC in a brand-new DT or revised DT**

Pre-condition: A DT in WIP state is open and there exists some SCs inherited from its based DT.


### Test Assertion:

#### Test Assertion #38.7.1
Property Term cannot be changed.

#### Test Assertion #38.7.2
Representation Term cannot be changed.

#### Test Assertion #38.7.3
Value domains inherited from the based SC remain available on the inherited SC. They can be selected for inspection, but cannot be discarded until a local value-domain change is introduced.

#### Test Assertion #38.7.4
A new Code List or Agency ID List value domain can be added when there is a Token CDT Primitive in the existing value domain. And such locally-added value domain can be discarded.

#### Test Assertion #38.7.5
Default value domain can be changed.

#### Test Assertion #38.7.6
Cardinality can be only be changed only if the inherited value is optional. If the value was changed to required, it can be changed back to optional.

#### Test Assertion #38.7.7
Value Constraint, Definition, and Definition Source can be edited.

#### Test Assertion #38.7.8
Once the Update button is clicked, the changes, except Definition and Definition Source, must also be made to the corresponding SC in all DTs derived from this DT (test for at least 2 levels of derivations). Definition and Definition Source shall be propagated only if they were the same before the change. In other word, if the Definition and Definition Source have been altered in the derived DTs before the change in this DT, leave the one altered alone. New value domain added in the derived DT should not be lost/overwritten.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: A DT in WIP state is open and there exists some SCs inherited from its based DT.
2. The users, branches, releases, and records needed to exercise "Editing an inherited SC in a brand-new DT or revised DT" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open a `WIP` DT containing inherited SCs from its based DT.
2. Verify inherited property term and representation term remain locked, and confirm inherited value domains are still available for selection while discard remains unavailable until a local value-domain change is made. (Assertions [#38.7.1](#test-assertion-3871), [#38.7.2](#test-assertion-3872), [#38.7.3](#test-assertion-3873))
3. Add and discard local code-list or Agency ID List value domains, change the default value domain and cardinality where allowed, and edit value constraint and definition fields. (Assertions [#38.7.4](#test-assertion-3874), [#38.7.5](#test-assertion-3875), [#38.7.6](#test-assertion-3876), [#38.7.7](#test-assertion-3877))
4. Update the DT and verify supported inherited-SC changes propagate to derived DTs while preserving local overrides. (Assertion [#38.7.8](#test-assertion-3878))
## Test Case 38.8

**Editing Value Domains**

Pre-condition: The DT is in WIP state. This Test Case applies to value domain editing of both the DT content component and supplementary components, so each assertion needs to be tested for both the cases of content component and supplementary component.


### Test Assertion:

#### Test Assertion #38.8.1
The user can add a value domain that whose value domain type is a Code List or Agency ID List only if there is already a value domain whose type is Primitive and the CDT Primitive is Token.

##### Test Assertion #38.8.1.a
When a value domain is added and there is a DT that is based on this DT, the added value domain must be copied to derived DT.

#### Test Assertion #38.8.2
The user can discard a value domain, that is not inherited from based DT and that is currently not used as the default value domain.

##### Test Assertion #38.8.2.a
When the value domain is discarded, it has to also be discarded from all derived BDTs (test for when there is more than one derivation levels).

#### Test Assertion #38.8.3
The user cannot add a duplicate value domain.

#### Test Assertion #38.8.4
The user can change Default value domain. (Changing default value domain does effect derived BDTs).

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The DT is in WIP state. This Test Case applies to value domain editing of both the DT content component and supplementary components, so each assertion needs to be tested for both the cases of content component and supplementary component.
2. The users, branches, releases, and records needed to exercise "Editing Value Domains" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open a `WIP` DT in the Working branch.
2. Exercise value-domain editing on both the content component and a supplementary component.
3. Add Code List and Agency ID List value domains only when a Token primitive exists and verify the addition propagates to derived DTs. (Assertions [#38.8.1](#test-assertion-3881), [#38.8.1.a](#test-assertion-3881a))
4. Discard eligible non-inherited, non-default value domains and verify the removal propagates to derived DTs. (Assertions [#38.8.2](#test-assertion-3882), [#38.8.2.a](#test-assertion-3882a))
5. Verify duplicate value domains are rejected and the default value domain can be changed. (Assertions [#38.8.3](#test-assertion-3883), [#38.8.4](#test-assertion-3884))
## Test Case 38.9

**Creating a new revision of a developer DT**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #38.9.1
On the CC Detail page of the Working branch, the developer can create a new revision of an DT that is in Published state regardless of the current owner. The result is that the Working branch has that DT with an incremental revision number that is in the WIP state.  Its detail attributes are initially the same as those of the previous revision.

#### Test Assertion #38.9.2
A new revision CANNOT be made on an DT in non-Published state.

### Test Step Pre-condition:
1. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
2. Any additional data required by the assertions has been prepared before execution.

### Test Step:
1. Sign in as a developer and open a published DT in the Working branch.
2. Create a new revision and verify the DT moves to a new `WIP` revision with copied detail attributes. (Assertion [#38.9.1](#test-assertion-3891))
3. Verify the revise action is unavailable for DTs in non-published states. (Assertion [#38.9.2](#test-assertion-3892))
## Test Case 38.10

**Editing a revision of a developer DT**

Pre-condition: The DT under test has revision number greater than 1 and is in WIP state.


### Test Assertion:

#### Test Assertion #38.10.1
Only the following can change.

##### Test Assertion #38.10.1.a
Definition, Definition Source, and Content Component Definition can be changed. A warning should be given when the Definition is empty.
##### Test Assertion #38.10.1.b
A value domain can be added per Test Case 38.8. And such newly added value domain can be discarded.
##### Test Assertion #38.10.1.c
A new SC can be added per Test Case 38.4, discarded per Test Case 38.5, and edited per Test Case 38.6.
##### Test Assertion #38.10.1.d
Existing SC can be edited per Test Case 38.12. It cannot be discarded.

#### Test Assertion #38.10.2
Once the Update button is clicked, all changes, except Definition, Definition Source, and Content Component Definition, shall be propagated to DTs derived from this DT. New value domain and SC added in the derived DT shouldn’t be lost. Once the Update button is clicked, the changes, except Definition and Definition Source, must also be made to all DTs derived from this DT (test for at least 2 levels of derivations). Definition and Definition Source shall be propagated only if they were the same before the change. In other word, if these fields have been altered in the derived DTs before the change in this DT, leave the one altered alone. New value domain added in the derived DT should not be lost/overwritten.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The DT under test has revision number greater than 1 and is in WIP state.
2. The users, branches, releases, and records needed to exercise "Editing a revision of a developer DT" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open a `WIP` DT revision whose revision number is greater than `1`.
2. Verify only the documented fields remain editable: definition fields, new value domains, new SC add or remove flows, and edit-only behavior for existing SCs. (Assertions [#38.10.1.a](#test-assertion-38101a), [#38.10.1.b](#test-assertion-38101b), [#38.10.1.c](#test-assertion-38101c), [#38.10.1.d](#test-assertion-38101d))
3. Update the DT revision and verify supported changes propagate to derived DTs without removing local additions or overridden definition fields. (Assertion [#38.10.2](#test-assertion-38102))
## Test Case 38.11

**Editing existing supplementary components of a revision of a developer DT**

Pre-condition: The DT revision (> 1) is created by the developer and is in the WIP state. The developer accesses these functionalities by opening an DT revision (> 1) from the CC list page.


### Test Assertion:

#### Test Assertion #38.11.1
Property Term cannot be changed.

#### Test Assertion #38.11.2
Representation Term cannot be changed.

#### Test Assertion #38.11.3
Value domains inherited from the based SC remain available on the revised SC. They can be selected for inspection, but cannot be discarded until a local value-domain change is introduced.

#### Test Assertion #38.11.4
A new Code List or Agency ID List value domain can be added when there is a Token CDT Primitive in the existing value domain. And such locally-added value domain can be discarded.

#### Test Assertion #38.11.5
Default value domain can be changed.

#### Test Assertion #38.11.6
Cardinality can be only be changed only if the original value is optional. If the value was changed to required, it can be changed back to optional.

#### Test Assertion #38.11.7
Definition, and Definition Source can be edited.

#### Test Assertion #38.11.8
Once the Update button is clicked, the changes, except Definition and Definition Source, must also be made to the corresponding SC in all DTs derived from this DT (test for at least 2 levels of derivations). Definition and Definition Source shall be propagated only if they were the same before the change. In other word, if the Definition and Definition Source have been altered in the derived DTs before the change in this DT, leave the one altered alone. New value domain added in the derived DT should not be lost/overwritten.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The DT revision (> 1) is created by the developer and is in the WIP state. The developer accesses these functionalities by opening an DT revision (> 1) from the CC list page.
2. The users, branches, releases, and records needed to exercise "Editing existing supplementary components of a revision of a developer DT" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open a `WIP` DT revision whose revision number is greater than `1`.
2. Open an existing SC from that revision and verify locked property term, locked representation term, and that inherited value domains are still available for selection while discard remains unavailable until a local value-domain change is made. (Assertions [#38.11.1](#test-assertion-38111), [#38.11.2](#test-assertion-38112), [#38.11.3](#test-assertion-38113))
3. Add and discard local Code List or Agency ID List value domains, change the default value domain and cardinality where allowed, and edit definition fields. (Assertions [#38.11.4](#test-assertion-38114), [#38.11.5](#test-assertion-38115), [#38.11.6](#test-assertion-38116), [#38.11.7](#test-assertion-38117))
4. Update the DT revision and verify supported SC changes propagate to derived DTs while preserving local overrides. (Assertion [#38.11.8](#test-assertion-38118))
## Test Case 38.12

**Developer DT state management**

> All these state changes need a confirmation dialog box with slightly different messages.

Pre-condition: The user is on the DT detail page, which he owns.


### Test Assertion:

#### Test Assertion #38.12.1
The developer can change the DT state from WIP to Draft state.

#### Test Assertion #38.12.2
The developer can change the DT state from Draft to Candidate.

#### Test Assertion #38.12.3
The developer can retract a candidate DT (i.e., to move the DT from the Candidate State back to WIP).

#### Test Assertion #38.12.4
The developer can change the DT state from Draft back to WIP.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The user is on the DT detail page, which he owns.
2. The users, branches, releases, and records needed to exercise "Developer DT state management" are available in connectCenter.

### Test Step:
1. Sign in as a developer and open an owned DT detail page in the Working branch.
2. Change state from `WIP` to `Draft` and verify the confirmation and resulting state. (Assertion [#38.12.1](#test-assertion-38121))
3. Change state from `Draft` to `Candidate`, from `Candidate` back to `WIP`, and from `Draft` back to `WIP`, verifying each resulting state transition. (Assertions [#38.12.2](#test-assertion-38122), [#38.12.3](#test-assertion-38123), [#38.12.4](#test-assertion-38124))
## Test Case 38.13

**Deleting developer DT**

> Delete a CC means that it is marked as “Deleted” and it is still displayed in the CC list when the Working branch is selected. If a CC is “Deleted” any other developer can restore it. Delete also means the deleted information is kept in the DT table. Generally, when an entity is opened that has a relationship (whether in association or in base relationship) to a deleted entity, the opened entity shall be flagged as in an invalid state. And once the user has expanded the tree down to the deleted entity, the deleted entity should be flagged as deleted. This is applied to all test cases related to deletions.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #38.13.1
If an DT revision number is 1, the developer owner can delete it when it is in WIP state. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the CC list page. The deleted DT shall appear in the Working branch CC list (even without the Deleted state filter).

#### Test Assertion #38.13.2
Upon opening a DT that uses a deleted DT as a base, the system shall be able to flag that the opening DT is in an invalid state and flag that the descendant-based DT is in the deleted state. After the based DT is replaced or restored, the developer should be able to see that reflected in the DT tree (e.g., after clicking refresh).

#### Test Assertion #38.13.3
DT whose revision number is more than 1 and is in any state cannot be deleted.

### Test Step Pre-condition:
1. The users, branches, releases, and records needed to exercise this test case are available in connectCenter.
2. Any additional data required by the assertions has been prepared before execution.

### Test Step:
1. Sign in as a developer and open a `WIP` DT whose revision number is `1`.
2. Delete the DT, confirm the dialog, and verify the user returns to the CC list with the deleted DT still visible in the Working branch. (Assertion [#38.13.1](#test-assertion-38131))
3. Open a DT that uses a deleted DT as its base and verify invalid-state and deleted-state indicators update correctly after base replacement or restore. (Assertion [#38.13.2](#test-assertion-38132))
4. Open a DT whose revision number is greater than `1` and verify deletion is unavailable in every state. (Assertion [#38.13.3](#test-assertion-38133))
## Test Case 38.14

**Restoring developer DT**

Pre-condition: The developer is on the CC View page with the Working branch open. Deleted DTs are shown in the list.


### Test Assertion:

#### Test Assertion #38.14.1
The developer user can open a DT and restore it.

### Test Step Pre-condition:
1. The stated test-case pre-condition is satisfied: The developer is on the CC View page with the Working branch open. Deleted DTs are shown in the list.
2. The users, branches, releases, and records needed to exercise "Restoring developer DT" are available in connectCenter.


### Test Step:
1. Sign in as a developer and open **Core Component > View/Edit Core Component** with the Working branch selected and deleted DTs visible.
2. Open a deleted DT and restore it. (Assertion [#38.14.1](#test-assertion-38141))
