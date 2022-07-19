# Test Suite 41

**Release Branch Data Type Management for End User**


## Test Case 41.1

**DT access**

Pre-condition: A release branch is selected.


### Test Assertion:

#### Test Assertion #41.1.1
The end user can see in the CC list page all DTs owned by any end user in any state.

#### Test Assertion #41.1.2
The end user can view and edit the details of a DT that is in WIP state and owned by him.

#### Test Assertion #41.1.3
The end user CAN view but CANNOT edit the details of a DT that is in WIP state and owned by another end user. However, he can add comments.

#### Test Assertion #41.1.4
The end user can view the details of a DT that is in QA, or Production not owned by him but he cannot make any change except adding comments.

#### Test Assertion #41.1.5
The end user can view the details of a Published DT owned by any developer, but he cannot make any change except adding comments or make a new revision of the DT.

#### Test Assertion #41.1.6
There must not be any developer DT in the Release branch not in Published state.

#### Test Assertion #41.1.7
The end user can view details of a deleted DT owned by another end user.

#### Test Assertion #41.1.8
The end user cannot edit details of a deleted DT owned by him. He can add comments.

#### Test Assertion #41.1.9
The end user cannot edit details of a deleted DT owned by another end user. He can add comments.

#### Test Assertion #41.1.10
The end user can restore a deleted DT owned by him.

#### Test Assertion #41.1.11
The end user can restore a deleted DT owned by another end user.

#### Test Assertion #41.1.12
The end user can move states of several DTs owned by him in one shot on the view/edit CC page. Test for:

##### Test Assertion #41.1.12.a
Changing state from WIP to QA
##### Test Assertion #41.1.12.b
Changing state from QA to WIP
##### Test Assertion #41.1.12.c
Changing state from QA to Production
##### Test Assertion #41.1.12.d
Transfer the ownership
##### Test Assertion #41.1.12.e
Deleting.

#### Test Assertion #41.1.13
The end user cannot move states of several DTs in one shot if all selected DTs are not owned by him.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.2

**Creating a brand-new DT**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #41.2.1
On the CC list page where a release branch is selected, the end user can create a brand-new DT after selecting a base DT. The new DT has the following default values – Based Data Type= [DEN of Base DT] and disabled, Data Type Term = [Data Type Term of Base DT] and disabled, Qualifier = [Qualifier of Base DT]; DEN = [Qualifier] + [Data Type Term] + “. Type” and disabled; Six Digit Identifier= blank; Namespace = null, Definition Source=blank, Definition=blank, Content Component Definition=blank, Comments = empty, Primitive=[Base DT Primitive], Primitive XML Expression=[ Base DT Primitive XML Expression]. The brand-new DT belong to a specific release, i.e., it must not appear in the working branch. It has a revision number 1. All fields are required and cannot be blank except Qualifier, Six Digit Identifier, Definition, Definition Source, Content Component Definition and Comments.

#### Test Assertion #41.2.2
The end user cannot create a brand-new end user DT when the working branch is selected.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.3

**Editing a brand-new end user DT**

Pre-condition: The brand-new DT is created by the end user and is in the WIP state. The end user accesses these functionalities by opening the brand-new DT from the CC list page or after creating a brand-new DT according to the Test Case 39.1.


### Test Assertion:

#### Test Assertion #41.3.1
The end user can change the properties of the DT and save changes with the following business rules.

##### Test Assertion #41.3.1.a
Namespace and Domain Value are required.  There should be a drop-down list for Namespace. A new value domain entry can be added, discarded, or Edited according to Test Case 41.8. Inherited domain value entry cannot be edited. Only Namespace that is a standard namespace shall be allowed.
##### Test Assertion #41.3.1.b
Qualifier can be updated such that it contains zero or more qualifiers in front of the qualifiers inherited from the based DT.
##### Test Assertion #41.3.1.c
Six digit identifier is optional.
##### Test Assertion #41.3.1.d
Content Component Definition, Definition and Definition Source are optional. However, A warning should be given when the Definition is empty.

#### Test Assertion #41.3.2
A new SC can be added to the DT and edited according to Test Case 41.4 and Test Case 41.5, respectively.

#### Test Assertion #41.3.3
SC inherited from based DT can only be edited according to Test Case 38.7.

#### Test Assertion #41.3.4
Once the Update button is clicked, the changes, except Definition and Definition Source and Content Component Definition, must also be made to all DTs derived from this DT (test for at least 2 levels of derivations). Note that any restriction put upon this DT in the derived DT would be lost. There should be a confirmation dialog indicating this when the Update button was clicked – “Restrictions applied in all DT derived from this DT will be lost”. Definition, Definition Source, and Content Component Definition shall be propagated only if they were the same before the change. In other word, if these fields have been altered in the derived DTs before the change in this DT, leave the one altered alone. Note the loss is associated with only restrictions, i.e., new value domain added in the derived DT should not be loss/overwritten. Note that the Namespace should not be propagated to the derived DTs.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.4

**Add a brand-new SC**

Pre-condition: A DT in WIP state is open.


### Test Assertion:

#### Test Assertion #41.4.1
The end user can add an SC to the DT.

#### Test Assertion #41.4.2
Default values of the new SC shall be as follows.

##### Test Assertion #41.4.2.a
Object Class Term = [Data Type Term of the DT]. The field is not editable.
##### Test Assertion #41.4.2.b
Property Term = “Property Term” + [a number]. The number ensures the Property Term is unique. It shall be unique across all DT derived from this DT as well. This field is required.
##### Test Assertion #41.4.2.c
Representation Term = one of the values in the dropdown list. (The list contains Representation Terms from all CDTs). This field is required.
##### Test Assertion #41.4.2.d
Cardinality = Optional.  This field is required.
##### Test Assertion #41.4.2.e
Value Constraint = None.
##### Test Assertion #41.4.2.f
Value Domain is populated with entries based on the selected Representation Term. Since each Representation Term corresponds to a single CDT, value domains from the associated CDT are populated. These value domains are not editable.
##### Test Assertion #41.4.2.g
Default value domain is set to the one default one based on the Representation Term. This field is required.
##### Test Assertion #41.4.2.h
Definition and Definition Source = blank text. These two fields are optional but warning shall be given when the Definition is empty.

#### Test Assertion #41.4.3
The added SC is propagated to all DTs derived from the DT in which the SC is added.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.5

**Remove a brand-new SC**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #41.5.1
The end user can remove a brand-new SC.

#### Test Assertion #41.5.2
The end user can remove a brand-new SC of a DT that is a base of another DT. In this case, the change should be propagated to the latter DT after clicking the Update button.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.6

**Editing a brand-new SC**

Pre-condition: A DT in WIP state is open and there exists a brand-new SC (locally/uninherited) added in the DT revision (first revision or a subsequent revision).


### Test Assertion:

#### Test Assertion #41.6.1
The following fields can be edited with business rule checked either when field is updated or the Update button is clicked:

##### Test Assertion #41.6.1.a
Property Term: Property term shall be unique across all DT derived from this DT as well as within the DT itself. Required.
##### Test Assertion #41.6.1.b
Representation Term can be updated. Required. When the Representation Term is changed the Value Constraint should be reset.
##### Test Assertion #41.6.1.c
Cardinality = Optional or Required. Required.
##### Test Assertion #41.6.1.d
Value Constraint can be updated. Optional.
##### Test Assertion #41.6.1.e
Value Domain: Value Domain based on the selected Representation Term cannot be updated. New value domain can be added or discarded. Code List or Agency ID List value domain can be added only if there is a Token CDT Primitive in the value domains. A value domain with Token CDT Primitive cannot be discarded, if there a Code List or Agency ID List in the value domain – dialog shall indicate that this is the reason the value domain cannot be discarded. A value domain which is a default value domain cannot be discarded.
##### Test Assertion #41.6.1.f
Default value domain can be changed and must be chosen from an existing value domain. Default value domain is required.
##### Test Assertion #41.6.1.g
Definition and Definition Source can be updated and they are optional. Warning shall be given when Definition is empty.

#### Test Assertion #41.6.2
Once the Update button is clicked, the changes, except Definition and Definition Source, must also be made to the corresponding SC in all DTs derived from this DT (test for at least 2 levels of derivations). Note that any restriction put upon this SC in the derived DT would be lost. There should be a confirmation dialog indicating this when the Update button was clicked – “Restrictions applied to this SC in all DT derived from this DT will be lost”. Definition and Definition Source shall be propagated only if they were the same before the change. In other word, if the Definition and Definition Source have been altered in the derived DTs before the change in this DT, leave the one altered alone. Note the loss is associated with only restrictions, i.e., new value domain added in the derived DT should not be loss/overwritten.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.7

**Editing an inherited SC in a brand-new DT or revised DT**

Pre-condition: A DT in WIP state is open and there exists some SCs inherited from its based DT.


### Test Assertion:

#### Test Assertion #41.7.1
Property Term cannot be changed.

#### Test Assertion #41.7.2
Representation Term cannot be changed.

#### Test Assertion #41.7.3
Value domains inherited from the based SC cannot be changed.

#### Test Assertion #41.7.4
A new Code List or Agency ID List value domain can be added when there is a Token CDT Primitive in the existing value domain. And such locally-added value domain can be discarded.

#### Test Assertion #41.7.5
Default value domain can be changed.

#### Test Assertion #41.7.6
Cardinality can be only be changed only if the inherited value is optional. If the value was changed to required, it can be changed back to optional.

#### Test Assertion #41.7.7
Value Constraint, Definition, and Definition Source can be edited.

#### Test Assertion #41.7.8
Once the Update button is clicked, the changes, except Definition and Definition Source, must also be made to the corresponding SC in all DTs derived from this DT (test for at least 2 levels of derivations). Note that any restriction put upon this SC in the derived DT would be lost. There should be a confirmation dialog indicating this when the Update button was clicked – “Extensions and restrictions applied to this SC in all DT derived from this DT will be lost”.  Definition and Definition Source shall be propagated only if they were the same before the change. In other word, if the Definition and Definition Source have been altered in the derived DTs before the change in this DT, leave the one altered alone. Note the loss is associated with only restrictions, i.e., new value domain added in the derived DT should not be loss/overwritten.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.8

**Editing Value Domains**

Pre-condition: The DT is in WIP state. This Test Case applies to value domain editing of both the DT content component and supplementary components, so each assertion needs to be tested for both the cases of content component and supplementary component.


### Test Assertion:

#### Test Assertion #41.8.1
The user can add a value domain that whose value domain type is a Code List or Agency ID List only if there is already a value domain whose type is Primitive and the CDT Primitive is Token.

##### Test Assertion #41.8.1.a
When a value domain is added and there is a DT that is based on this DT, the added value domain must be copied to derived DT.

#### Test Assertion #41.8.2
The user can discard a value domain, that is not inherited from based DT and that is currently not used as the default value domain.

##### Test Assertion #41.8.2.a
When the value domain is discarded, it has to also be discarded from all derived BDTs (test for when there is more than one derivation levels).

#### Test Assertion #41.8.3
The user cannot add a duplicate value domain.

#### Test Assertion #41.8.4
The user can change Default value domain. (Changing default value domain does effect derived BDTs).

### Test Step Pre-condition:



### Test Step:

## Test Case 41.9

**Creating a new revision of an end user DT**

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #41.9.1
On the CC Detail page of a release branch, the end user can create a new revision of an DT (i.e., amend) that is in Production state regardless of the current owner. The result is that this branch has that DT with an incremental revision number that is in the WIP state.  Its detail attributes are initially the same as those of the previous revision.

#### Test Assertion #41.9.2
A new revision CANNOT be made on an DT in non-Production state.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.10

**Editing a revision of an end user DT**

Pre-condition: The DT under test has revision number greater than 1 and is in WIP state.


### Test Assertion:

#### Test Assertion #41.10.1
Only the following can change.

##### Test Assertion #41.10.1.a
Definition, Definition Source, and Content Component Definition can be changed. A warning should be given when the Definition is empty.
##### Test Assertion #41.10.1.b
A value domain can be added per Test Case 41.9. And such newly added value domain can be discarded.
##### Test Assertion #41.10.1.c
A new SC can be added per Test Case 41.4, discarded per Test Case 41.5, and edited per Test Case 41.6
##### Test Assertion #41.10.1.d
Existing SC can be edited but it cannot be discarded.

#### Test Assertion #41.10.2
Once the Update button is clicked, all changes, except Definition, Definition Source, and Content Component Definition, shall be propagated to DTs derived from this DT. There should be a confirmation dialog indicating – “Restrictions applied to this SC in all DT derived from this DT will be lost”. Note the loss is associated with only restrictions, i.e., new value domain and SC added in the derived DT shouldn’t be lost. Once the Update button is clicked, the changes, except Definition and Definition Source, must also be made to all DTs derived from this DT (test for at least 2 levels of derivations). Note that any restriction put upon this DT in the derived DT would be lost. There should be a confirmation dialog indicating this when the Update button was clicked – “Restrictions applied in all DT derived from this DT will be lost”. Definition and Definition Source shall be propagated only if they were the same before the change. In other word, if these fields have been altered in the derived DTs before the change in this DT, leave the one altered alone. Note the loss is associated with only restrictions, i.e., new value domain added in the derived DT should not be loss/overwritten.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.11

**Editing existing supplementary components of a revision of an end user DT**

Pre-condition: The DT revision (> 1) is created by the developer and is in the WIP state. The end user accesses these functionalities by opening an DT revision (> 1) from the CC list page.


### Test Assertion:

#### Test Assertion #41.11.1
Property Term cannot be changed.

#### Test Assertion #41.11.2
Representation Term cannot be changed.

#### Test Assertion #41.11.3
Value domains inherited from the based SC cannot be changed.

#### Test Assertion #41.11.4
A new Code List or Agency ID List value domain can be added when there is a Token CDT Primitive in the existing value domain. And such locally-added value domain can be discarded.

#### Test Assertion #41.11.5
Default value domain can be changed.

#### Test Assertion #41.11.6
Cardinality can be only be changed only if the original value is optional. If the value was changed to required, it can be changed back to optional.

#### Test Assertion #41.11.7
Definition, and Definition Source can be edited.

#### Test Assertion #41.11.8
Once the Update button is clicked, the changes, except Definition and Definition Source, must also be made to the corresponding SC in all DTs derived from this DT (test for at least 2 levels of derivations). Note that any restriction put upon this SC in the derived DT would be lost. There should be a confirmation dialog indicating this when the Update button was clicked – “Extensions and restrictions applied to this SC in all DT derived from this DT will be lost”.  Definition and Definition Source shall be propagated only if they were the same before the change. In other word, if the Definition and Definition Source have been altered in the derived DTs before the change in this DT, leave the one altered alone. Note the loss is associated with only restrictions, i.e., new value domain added in the derived DT should not be loss/overwritten.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.12

**End User DT state management**

> All these state changes need a confirmation dialog box with slightly different messages.

Pre-condition: The user is on the DT detail page, which he owns.


### Test Assertion:

#### Test Assertion #41.12.1
The end user can change the DT state from WIP to QA state.

#### Test Assertion #41.12.2
The end user can change the DT state from QA to Production.

#### Test Assertion #41.12.3
The end user can change the DT state from QA back to WIP.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.13

**Deleting end user DT**

> Delete a CC means that it is marked as “Deleted” and it is still displayed in the CC list when a non-working branch is selected. If a CC is “Deleted” any other end user can restore it. Delete also means the deleted information is kept in the DT table. Generally, when an entity is opened that has a relationship (whether in association or in base relationship) to a deleted entity, the opened entity shall be flagged as in an invalid state. And once the user has expanded the tree down to the deleted entity, the deleted entity should be flagged as deleted. This is applied to all test cases related to deletions.

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #41.13.1
If an DT revision number is 1, the end user owner can delete it when it is in WIP state. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the CC list page. The deleted DT shall appear in the non-working branch CC list (even without the Deleted state filter).

#### Test Assertion #41.13.2
Upon opening a DT that uses a deleted DT as a base, the system shall be able to flag that the opening DT is in an invalid state and flag that the descendant-based DT is in the deleted state. After the based DT is replaced or restored, the end user should be able to see that reflected in the DT tree (e.g., after clicking refresh).

#### Test Assertion #41.13.3
DT whose revision number is more than 1 and is in any state cannot be deleted.

### Test Step Pre-condition:



### Test Step:

## Test Case 41.14

**Restoring end user DT**

Pre-condition: The end user is on the CC View page with a non-working branch open. Deleted DTs are shown in the list.


### Test Assertion:

#### Test Assertion #41.14.1
The end user can open a deleted DT and restore it.

### Test Step Pre-condition:



### Test Step: