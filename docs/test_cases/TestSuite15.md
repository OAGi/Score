# Test Suite 15

> Release Branch Core Component Management Behavior for End User


## Test Case 15.1

> Access to core component viewing, editing, and commenting

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #15.1.1
The end user can see in the CC page all CCs owned by any user in any state. But there shall not be any developer CC listed in a release branch that is not in the Published state (this is not a query condition, i.e., such situation shouldn’t exist in the database). UEGACC if exists in that branch shall be listed while UEGASCC and UEGASCCP shall NOT be listed.

#### Test Assertion #15.1.2
The end user can view and edit the details of a CC that is in WIP state and owned by him. He can also change state of the CC to QA state.

#### Test Assertion #15.1.3
The end user can view the detail of a CC that is in QA state owned by him. He cannot edit the detail. He can only add comments or change the state to Production.

#### Test Assertion #15.1.4
The end user can view the detail of a CC that is in Production state owned by him. He cannot edit the detail. He can only add comments or amend the CC.

#### Test Assertion #15.1.5
The end user CAN view but CANNOT edit the details of a CC that is in WIP state and owned by another user (in principle, only end user CCs can be in the WIP state in a release branch), and he CAN add comments.

#### Test Assertion #15.1.6
The end user can view the details of a CC that is in QA state and owned by another user but he cannot make any change except adding comments (in principle, only end user CCs can be in the QA state in a release branch).

#### Test Assertion #15.1.7
The end user can view the details of a Production CC owned by another user but he cannot make any change except adding comments. The end user can also amend the CC, in which case, he takes over the ownership.

#### Test Assertion #15.1.8
The end user can see the detail of developer CCs but cannot make any change. He can only make comments.

#### Test Assertion #15.1.9
The end user can move the state of multiple CCs at once.

#### Test Assertion #15.1.10
The end user can open a CC and find the usages of its nodes (i.e., ACC, ASCCP or BCCP) by invoking the function “Where Used” of the context menu of a particular node. In the returned dialog, he can view where the CC wherein the selected CC is used. Check the usages for ACC, ASCCP and BCCP nodes.

##### Test Assertion #15.1.10.a
If an ACC is selected to view wherein it is used, then the ACCs which are based on this ACC either directly or indirectly should be listed in the returned dialog.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.2

> Creating a brand-new end user ACC

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #15.2.1
The end user cannot create a brand-new end user ACC on the working branch.

#### Test Assertion #15.2.2
On the CC list page with a particular, release branch selected, the end user can create a brand-new end user ACC with the following default values – Object Class Term = “Object Class Term”; DEN= [Object Class Term] + “. Details” and locked, Component Type = “Semantics”; Abstract = false; Definition = blank; Definition Source = blank; Deprecated = false and locked; Namespace = null, Comments = empty. The brand-new ACC must have the selected release number assigned right away, i.e., it must not appear in any release except the release the user has selected at the time of creation. It has a revision number of 1.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.3

> Editing a brand-new end user ACC

Pre-condition: The brand-new ACC is created by the end user and is in the WIP state. The end user accesses these functionalities by opening the brand-new ACC from the CC list page or after creating a brand-new ACC.


### Test Assertion:

#### Test Assertion #15.3.1
Component Type can only be either Base, Semantics, or Semantic Group.

#### Test Assertion #15.3.2
If ACC’s Component Type is User Extension Group, the end user cannot change/update any detail of the ACC.

#### Test Assertion #15.3.3
If the ACC’s Component Type is NOT User Extension Group, the end user can change properties of the ACC and save changes with the following business rules.

##### Test Assertion #15.3.3.a
If the Component Type is base, Abstract shall be true.
##### Test Assertion #15.3.3.b
Abstract can only be true when the Component Type is base or Semantics.
##### Test Assertion #15.3.3.c
Object Class Term, Component Type, Namespace, and Abstract are required.  Namespace must be a non-standard namespace (there should be a drop-down list to select a namespace). The following Component Type shall be disabled or hidden – Extension, User Extension Group, Embedded, OAGIS 10 Nouns, OAGIS 10 BODs.
##### Test Assertion #15.3.3.d
Deprecated must be false and locked b/c revision is 1.
##### Test Assertion #15.3.3.e
A warning should be given when the Definition is empty.
##### Test Assertion #15.3.3.f
If Object Class Term of the ACC change and there is one or more ASCCP depending on it, DEN of those ASCCP shall be updated accordingly, (Note: This should affect ASCCPs that are in revision 1 only.)

#### Test Assertion #15.3.4
The end user can Exclude SCs or not from the Searching Field by checking or unchecking the “Exclude SCs” checkbox accordingly.

##### Test Assertion #15.3.4.a
If the “Exclude SCs” checkbox is enabled (i.e., checked) the SCs are excluding from the searching field
##### Test Assertion #15.3.4.b
If the “Exclude SCs” checkbox is disabled (i.e., unchecked) the SCs are excluding from the searching field

### Test Step Pre-condition:



### Test Step:

## Test Case 15.4

> Amend an end user ACC

Pre-condition: The end user has selected a particular release branch.
Generally, only backwardly compatible changes can be made.


### Test Assertion:

#### Test Assertion #15.4.1
On the CC Detail page of an end user ACC in Production state, the end user can amend the ACC. There should be a dialog asking the end user to confirm the intention to amend. [Note: In this case of amendment, the system simply advances the revision number, resets the revision tracking number to 1, and changes the state of the ACC back to WIP, i.e.,  detail of the previous revision of the ACC is not kept in the ACC table and it will not be possible to retrieve the whole structure of the previous revision of the ACC.]  Its attributes are initially the same as before the amendment.

#### Test Assertion #15.4.2
The end user cannot amend a released developer ACC.

#### Test Assertion #15.4.3
If ACC’s Component Type is User Extension Group, the end user cannot change/update any detail of the ACC.

#### Test Assertion #15.4.4
If ACC’s Component Type is NOT User Extension Group, the end user can change the details of the ACC and save the changes with the following business rules.

##### Test Assertion #15.4.4.a
Component Type cannot be changed.
##### Test Assertion #15.4.4.b
Abstract can only be changed from True to False, except when the Component Type is base where it should be locked as True.
##### Test Assertion #15.4.4.c
“Namespace” and “Object Class Term” cannot be changed.
##### Test Assertion #15.4.4.d
If the Deprecated was already True in the previous revision, the field along with the Replaced By field should be locked. If it was False in the previous revision the checkbox shall be enabled. When Deprecated is changed to True, the end user must be able to select a replacement ACC in the same release that is not already deprecated from a drop-down list or search dialog in the Replaced By field – but the field is optional. If the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.
##### Test Assertion #15.4.4.e
Definition and Definition Source can be changed. However, a warning should be given when the Definition is empty.

#### Test Assertion #15.4.5
Place holder for testing about undoing changes to the ACC in the future or about displaying history during the amendment.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.5

> End user ACC state management

Pre-condition: The end user is on the ACC detail page, which he owns.
All these state changes need a confirmation dialog box with slightly different messages. Changing to the QA state only need a confirmation. Retracting a QA ACC to WIP needs a confirmation. Amending a Production ACC which set its state to WIP needs a confirmation.


### Test Assertion:

#### Test Assertion #15.5.1
The end user can change the ACC state from WIP to QA. The system shall then change the state of its children associations that are also in the WIP state to the QA state. There is no need to check the state of the dependencies (we will ensure that everything is consistently in the Production state when a BIE Extension that has the end user CC is expanded). (Note that I change the rule about viewing details of CC in WIP state that the detail can be viewed by any user so the user should be able to expand the tree of the ACC).

#### Test Assertion #15.5.2
The end user can change the ACC state from QA back to WIP. State of the associations also go back to WIP.

#### Test Assertion #15.5.3
The end user can change the ACC state from QA to Production. State of the associations also go to Production.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.6

> Deleting an end user ACC

Pre-condition: N/A
Delete a CC means that it is marked as “Deleted” and it is still displayed in the CC list when the branch it belongs to is selected. If a CC is “Deleted” any other end user can restore it.


### Test Assertion:

#### Test Assertion #15.6.1
If an ACC revision number is 1, the end user owner can delete it when it is in WIP state. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the CC list page.

#### Test Assertion #15.6.2
Upon opening an ACC that has a descendant ACC that has a deleted ACC as a base, the system shall be able to flag that the descendant-based ACC is in deleted state. After the based ACC is replaced or resurrected, the end user should be able to see that reflected in the ACC tree (e.g., after clicking refresh).

#### Test Assertion #15.6.3
Upon opening an ACC that uses a deleted ACC as a base, the system should flag that the based ACC is in deleted state. The system shall allow the end user to select a new ACC in the same release as a base or do away with the base. The system shall also allow the end user to open the deleted end user ACC to restore it. Then, the end user shall be able to see in the ACC detail page that the based ACC is no longer in the deleted state, e.g., after refreshing the ACC detail page.

#### Test Assertion #15.6.4
Upon opening an ACC that has an association to an ASCCP that uses a deleted ACC, the system shall be able to flag that ACC as deleted. After the deleted ACC is resurrected or replaced, the end user shall be able to see that reflected in the ACC he has opened earlier, e.g., after refreshing the detail page.

#### Test Assertion #15.6.5
Upon opening an ACC that has a descendant ACC that was deleted earlier used in in one or more associations, the system shall be able to flag the deleted ACC on those associations (for example in many BODs the same ACC such as customer party is used in both the header and the line components). After the deleted ACC is resurrected or replaced, the end user shall be able to see that reflected in the ACC he has opened earlier, e.g., after refreshing the ACC detail page.

#### Test Assertion #15.6.6
Upon opening an ASCCP that uses the deleted ACC, the ASCCP shall be highlighted or flagged somehow indicating that the ASCCP is an invalid state. The system shall provide an option for the end user to choose another ACC, which can be an end user ACC or developer ACC. The system shall also allow the end user to open the deleted ACC in another tab where he can restore it. The end user may need to refresh the ASCCP detail page to clear the invalid flag.

#### Test Assertion #15.6.7
ACC whose revision number is more than 1 and is in any state cannot be deleted.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.7

> Restoring end user ACC

Pre-condition: The end user is on the CC View/Edit page with a release branch open. Deleted CCs are shown in the list.


### Test Assertion:

#### Test Assertion #15.7.1
The end user can open an ACC and restore it. All of its associations shall be restored as well.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.8

> Editing associations of a brand-new end user ACC

Pre-condition: The revision number of ACC under test is 1 and it is in WIP state.


### Test Assertion:

#### Test Assertion #15.8.1
The end user can append an ASCCP to an ACC that results in an association to the (developer or end user) ASCCP in the same release in any state. The default value of the new ASCC shall be as follows: Min = 0, Max = “unbounded”, Deprecated = false and disabled, Definition=empty, Definition Source=empty.

##### Test Assertion #15.8.1.a
The selected ASCCP can be in any state.
##### Test Assertion #15.8.1.b
A warning shall be given if the ASCCP is deprecated.
##### Test Assertion #15.8.1.c
The ACC shall not already contain an ASCCP or BCCP with the same property term whether in itself or within its based ACC. (maybe the ASCCP with the same property term should not appear in the list of the dialog used to append ASCCPs. Note that there may be multiple ASCCPs with the same property term even in the same release – this situation occurs with the Data Area ASCCP).
##### Test Assertion #15.8.1.d
The resulting ASCC shall be in the WIP state.
##### Test Assertion #15.8.1.e
If the ASCCP is not reusable, check that there is no ASCC already using it.

#### Test Assertion #15.8.2
The end user can click on any associations and insert an ASCCP before or after it. The rest of the behavior is the same as test assertion #1.

#### Test Assertion #15.8.3
The end user can update the following fields of its new ASCC (rev = 1); Min, Max, Definition, Definition Source. I.e., he cannot update the details of the ASCCP. The following business rules are applied:

##### Test Assertion #15.8.3.a
Min >= 0
##### Test Assertion #15.8.3.b
Max >= -1 and (Max >= Min when Max != -1)
##### Test Assertion #15.8.3.c
The user may type in “unbounded” in place of -1 for Max. If the user type in -1 for Max, the UI should display it as “unbounded”.
##### Test Assertion #15.8.3.d
Min, Max, and Deprecated are required.
##### Test Assertion #15.8.3.e
A warning should be given when the Definition is empty.
##### Test Assertion #15.8.3.f
Deprecated must be false and locked (because it is a new association, it shouldn’t be deprecated).

#### Test Assertion #15.8.4
The end user can append a BCCP to an ACC that results in an association to the BCCP in the same release in any state. The default value of the new BCC shall be as follows: Min=0, Max = “unbounded”, Deprecated = false and disabled, Entity Type = Element, Value Constraint= None (Default and Fixed value should be empty), Definition = empty, Definition Source = empty.

##### Test Assertion #15.8.4.a
The selected BCCP can be in any state.
##### Test Assertion #15.8.4.b
A warning shall be given if the BCCP is deprecated.
##### Test Assertion #15.8.4.c
The added BCCP shall not cause a property uniqueness violation to the ACC.
##### Test Assertion #15.8.4.d
The resulting BCC shall be in the WIP state.

#### Test Assertion #15.8.5
The end user can right-click on any associations and insert a BCCP before or after the BCCP. The rest of the behavior is the same as test assertion #4.

#### Test Assertion #15.8.6
The end user can edit the following detail of a BCC - Min, Max, Entity Type, Default or Fixed Value, Definition, and Definition Source. He cannot update the BCCP information. The following business rules apply:

##### Test Assertion #15.8.6.a
Min >= 0
##### Test Assertion #15.8.6.b
Max >= -1 and (Max >= Min when Max != -1)
##### Test Assertion #15.8.6.c
The user may type in “unbounded” in place of -1 for Max. If the user type in -1 for Max, the UI should display it as “unbounded”.
##### Test Assertion #15.8.6.d
All fields except Definition and Definition Source are required. However, a warning should be given when the Definition is empty.
##### Test Assertion #15.8.6.e
Deprecated must be false (because it is a new association, it shouldn’t be deprecated).
##### Test Assertion #15.8.6.f
The Default and Fixed value shall be disabled and cleared of value if the Entity Type is Element. The system shall warn the user that the Default and Fixed Value will be cleared if changing entity type to Element. If enabled, the Default and Fixed value shall be mutually exclusive.
##### Test Assertion #15.8.6.g
If the Entity Type is changed from Element to Attribute the Min Cardinality should be changed to 0 and the Max Cardinality to 1.
##### Test Assertion #15.8.6.h
The Entity Type can be changed to Attribute ONLY if the BCCP has no SC or all SCs have 0 max cardinality.

#### Test Assertion #15.8.7
The end user can change (move up or down) the sequence of an association to an ASCCP. It would be good to be able to drag-n-drop instead of moving up or down.

#### Test Assertion #15.8.8
The end user can change (move up or down) the sequence of an association to a BCCP. It would be good to be able to drag-n-drop instead of moving up or down.

#### Test Assertion #15.8.9
The end user can remove an association (ASCC or BCC) that has no reference, such as, as a replacement association to a deprecated one. There must be a confirmation dialog box though.

#### Test Assertion #15.8.10
The developer can add a based ACC to an ACC that is in the same release.

##### Test Assertion #15.8.10.a
The new based ACC can be in any state.
##### Test Assertion #15.8.10.b
If the chosen based ACC is deprecated, a warning shall be given.
##### Test Assertion #15.8.10.c
The based ACC can be only of Base or Semantics component type.
##### Test Assertion #15.8.10.d
The end user cannot change the fields of the base ACC.
##### Test Assertion #15.8.10.e
The based ACC should not contain an ASCCP or a BCCP with the same property term as those already in the ACC itself (property uniqueness).

#### Test Assertion #15.8.11
The end user can remove the based ACC when one exists.

#### Test Assertion #15.8.12
The end user can transfer the ownership of an ACC, which is in WIP states and he owns, to another end user but not developer. In that case the ownership of the associations (ASCC and BCC) are transferred as well.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.9

> Editing associations during an end-user ACC amendment

Pre-condition: An end-user ACC is opened and its revision is higher than 1.


### Test Assertion:

#### Test Assertion #15.9.1
The end user can append an ASCCP to add an ACC that results in a brand-new association to the (developer or end user) ASCCP in the same release in any state. The default value of the new ASCC shall be as follows: Min = 0, Max = “unbounded”, Deprecated = false and disabled, Definition=empty, Definition Source=empty.

##### Test Assertion #15.9.1.a
The selected ASCCP can be in any state.
##### Test Assertion #15.9.1.b
A warning shall be given if the ASCCP is deprecated.
##### Test Assertion #15.9.1.c
The ASCCP shall not violate the property uniqueness constraint of the ACC. In other words, the ACC shall not already contain an ASCCP or BCCP with the same property term whether in itself or within its based ACC. (maybe the ASCCP with the same property term should not appear in the list of the dialog used to append ASCCPs. Note that there may be multiple ASCCPs with the same property term even in the same release – this situation occurs with the Data Area ASCCP).
##### Test Assertion #15.9.1.d
The resulting ASCC shall be in the WIP state.
##### Test Assertion #15.9.1.e
If the ASCCP is not reusable, check that there is no ASCC already using it.

#### Test Assertion #15.9.2
The end user can right-click on any associations and insert an ASCCP before or after it. The rest of the behavior is the same as test assertion #1.

#### Test Assertion #15.9.3
The end user can update the following fields of its new ASCC (rev = 1); Min, Max, Definition, Definition Source. I.e., he cannot update the details of the ASCCP. The following business rules are applied:

##### Test Assertion #15.9.3.a
Min >= 0
##### Test Assertion #15.9.3.b
Max >= -1 and (Max >= Min when Max != -1)
##### Test Assertion #15.9.3.c
The user may type in “unbounded” in place of -1 for Max. If the user type in -1 for Max, the UI should display it as “unbounded”.
##### Test Assertion #15.9.3.d
Min, Max, and Deprecated are required.
##### Test Assertion #15.9.3.e
A warning should be given when the Definition is empty.
##### Test Assertion #15.9.3.f
Deprecated must be false and locked (because it is a new association, it shouldn’t be deprecated).

#### Test Assertion #15.9.4
The end user can append a BCCP to an ACC that results in an association to the BCCP in the same release in any state. The default value of the new BCC shall be as follows: Min=0, Max = “unbounded”, Deprecated = false and disabled, Entity Type = Element, Value Constraint= None (Default and Fixed value should be empty), Definition = empty, Definition Source = empty.

##### Test Assertion #15.9.4.a
The selected BCCP can be in any state.
##### Test Assertion #15.9.4.b
A warning shall be given if the BCCP is deprecated.
##### Test Assertion #15.9.4.c
The added BCCP shall not cause a property uniqueness violation to the ACC.
##### Test Assertion #15.9.4.d
The resulting BCC shall be in the WIP state.
##### Test Assertion #15.9.4.e
If the Entity Type is changed from Element to Attribute the Min Cardinality should be changed to 0 and the Max Cardinality to 1.
##### Test Assertion #15.9.4.f
Entity Type can be changed to Attribute only when the BCCP has no SC or all SCs have min and max cardinalities equal zeros.

#### Test Assertion #15.9.5
The end user can right-click on any associations and insert a BCCP before or after the BCCP. The rest of the behavior is the same as test assertion #4.

#### Test Assertion #15.9.6
The end user can edit the following detail of a new BCC (rev = 1) - Min, Max, Entity Type, Default or Fixed Value, Definition, and Definition Source. He cannot update the BCCP information. The following business rules apply:

##### Test Assertion #15.9.6.a
Min >= 0
##### Test Assertion #15.9.6.b
Max >= -1 and (Max >= Min when Max != -1)
##### Test Assertion #15.9.6.c
The user may type in “unbounded” in place of -1 for Max. If the user type in -1 for Max, the UI should display it as “unbounded”.
##### Test Assertion #15.9.6.d
All fields except Definition and Definition Source are required. However, a warning should be given when the Definition is empty.
##### Test Assertion #15.9.6.e
Deprecated must be false (because it is a new association, it shouldn’t be deprecated).
##### Test Assertion #15.9.6.f
The Default and Fixed value shall be disabled and cleared of value if the Entity Type is Element. The system shall warn the user that the Default and Fixed Value will be cleared if changing entity type to Element. If enabled, the Default and Fixed value shall be mutually exclusive.

#### Test Assertion #15.9.7
The end user can change (move up or down) position of the brand-new association to an ASCCP. It would be good to be able to drag-n-drop instead of moving up or down. The existing ASCC cannot be moved. If the user tries to move existing ASCC a dialog box should pop up indicating that “ASCC existed before the amendment cannot be moved.”

#### Test Assertion #15.9.8
The end user can change (move up or down) position of the brand-new association to a BCCP. It would be good to be able to drag-n-drop instead of moving up or down. Existing BCC cannot be moved. If the user tries to move existing BCC a dialog box should pop up indicating that “BCC existed before the amendment cannot be moved.”

#### Test Assertion #15.9.9
The end user can remove a brand-new association (ASCC or BCC) if the association has no reference, particularly as a replacement of a deprecated association. There must be a confirmation dialog box though.

#### Test Assertion #15.9.10
The end user can add a based ACC to an ACC that is in the same release if one does not already exist.

##### Test Assertion #15.9.10.a
The new based ACC can be in any state.
##### Test Assertion #15.9.10.b
If the chosen based ACC is deprecated, a warning shall be given.
##### Test Assertion #15.9.10.c
The based ACC can only be Base or Semantics component type.
##### Test Assertion #15.9.10.d
The end user cannot change the fields of the base ACC.
##### Test Assertion #15.9.10.e
The based ACC should not contain an ASCCP or a BCCP with the same property term as those already in the ACC itself (property uniqueness).

#### Test Assertion #15.9.11
The end user can remove the based ACC when one already exists before the amendment.

#### Test Assertion #15.9.12
The end user can transfer the ownership of an ACC, which is in WIP states and he owns, to another end user. In that case the ownership of the associations (ASCC and BCC) are transferred as well.

#### Test Assertion #15.9.13
The end user cannot remove ASSC or BCC existed before the amendment.

#### Test Assertion #15.9.14
The end user can make the following changes to ASCC existed before this amendment following these rules.

##### Test Assertion #15.9.14.a
0 <= Min <= PreviousMin
##### Test Assertion #15.9.14.b
If Previous-Max = -1, it cannot be changed; otherwise, Max = -1 or Max >= Previous-Max
##### Test Assertion #15.9.14.c
If the Deprecated was already True before the amendment, the field along with the Replaced By field should be lock. If it was False before the amendment the checkbox shall be enabled. When Deprecated is changed to True, the developer must be able to select a replacement ASCC or BCC (filtered out the deprecated ones) within the ACC from a drop-down list in the Replaced By field – but the field is optional. If the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.
##### Test Assertion #15.9.14.d
A warning should be given when the Definition is empty.

#### Test Assertion #15.9.15
The end user can cancel the amendment. In such case, all changes to the ACC during the amendment including changes to its associations and based ACC addition are rolled back. The changes made before the cancellation are still kept in the history record.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.10

> Creating a brand-new end user ASCCP

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #15.10.1
The end user cannot create a brand-new ASCCP when the working branch is selected.

#### Test Assertion #15.10.2
On the CC list page where a release branch is selected, the end user can create a brand-new ASCCP by selecting an (developer or end user) ACC in the same release, in any state, and owned by any user. The ASCCP should have the following default values – Property Term = [ACC Object Class Term]; DEN = [Property Term] + [ACC Object Class Term] and locked; Reusable = true; Definition = blank; Definition_Source=blank; Deprecated = false and disable; Nillable = false; Namespace = null; Comments = empty. The brand-new ASCCP must have the selected release number assigned right away, i.e., it must not appear in any other release except the release the user has selected at the time of creation. It has a revision number of 1. The ASCCP should have the associated ACC along with its properties but they cannot be changed. Only ACC whose Component Type is Semantics or Semantic Group can be selected.

#### Test Assertion #15.10.3
If the underlying ACC has changed, particularly the Object Class Term of the ACC, after the ASCCP was first created, the system should automatically pick up the change, e.g., when the ASCCP is opened or refreshed on the ASCCP details page.

#### Test Assertion #15.10.4
The end user can create an ASCCP from an ACC in WIP state using the function “Create ASCCP from this”. The Property Term of the created ASCCP is based on the Object Class Term of the ACC.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.11

> Editing a brand-new end user ASCCP

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #15.11.1
The end user can change the properties of the ASCCP and save the changes with the following business rules.

##### Test Assertion #15.11.1.a
The fields “Property Term”, “Reusable”, “Definition”, “Definition Source”, “Namespace”, and “Nillable” can be changed.
##### Test Assertion #15.11.1.b
Deprecated field is locked at the value false.
##### Test Assertion #15.11.1.c
The field “DEN” is automatically changed based on the changes of the “Property Term” field and the “Object Class Term” of the ACC.
##### Test Assertion #15.11.1.d
A warning should be given when the Definition is empty.
##### Test Assertion #15.11.1.e
The fields “GUID” and “DEN” cannot be changed.
##### Test Assertion #15.11.1.f
The fields of the Associated ACC and its children nodes cannot be changed.
##### Test Assertion #15.11.1.g
The developer can choose a new ACC in the same release for the ASCCP. ASCCP DEN shall be automatically updated.
##### Test Assertion #15.11.1.h
Only non-standard namespace shall be allowed for the “Namespace”.
##### Test Assertion #15.11.1.i
“Property Term”, “Reusable”, “Namespace”, and “Nillable” are required. [Note that Namespace is required for ASCCP, so the User Extension Group ASCCP perhaps should have a namespace too. In this case, once the user assign a namespace to the UEGACC, the system shall assign that namespace to the UEGASCCP too.]

#### Test Assertion #15.11.2
The end user can transfer the ownership of an ASCCP, which is in WIP states and he owns, to another end user but not developer.

#### Test Assertion #15.11.3
If Object Class Term of the ACC used by the ASCCP changes, DEN of the ASCCP shall get updated. This should occur only when both the ASCCP and ACC have revision #1.

#### Test Assertion #15.11.4
When the ASCCP DEN changes, all ASCCs which uses the ASCCP and whose revision numbers are 1 shall have their DEN updated accordingly.

#### Test Assertion #15.11.5
The end user can Exclude SCs or not from the Searching Field by checking or unchecking the “Exclude SCs” checkbox accordingly.

##### Test Assertion #15.11.5.a
If the “Exclude SCs” checkbox is enabled (i.e., checked) the SCs are excluding from the searching field
##### Test Assertion #15.11.5.b
If the “Exclude SCs” checkbox is disabled (i.e., unchecked) the SCs are excluding from the searching field

### Test Step Pre-condition:



### Test Step:

## Test Case 15.12

> Amend an end user ASCCP

Pre-condition: The end user has selected a particular release branch.
Generally, only backwardly compatible changes can be made.


### Test Assertion:

#### Test Assertion #15.12.1
On the CC Detail page of an end user ASCCP in Production state, the end user can amend the ASCCP. There should be a dialog asking the end user to confirm the intention to amend. [Note: In this case, the system simply advances the revision number, resets the revision tracking number to 1, and changes the state of the ASCCP back to WIP, i.e., there is no snapshot of the previous version kept in the ASCCP table.] Its attributes are initially the same as before the amendment.

#### Test Assertion #15.12.2
The end user cannot amend a released developer ASCCP.

#### Test Assertion #15.12.3
The end user cannot amend or see a User Extension Group ASCCP.

#### Test Assertion #15.12.4
If the ASCCP is not a User Extension Group ASCCP, the end user can change some details of the ASCCP and save changes compliant to the following business rules.

##### Test Assertion #15.12.4.a
The fields “Property Term” and “Namespace” cannot be changed.
##### Test Assertion #15.12.4.b
“Definition” and “Definition Source” can change.
##### Test Assertion #15.12.4.c
If “Reusable” is false in the previous revision, it can be changed to true; otherwise, it cannot be changed.
##### Test Assertion #15.12.4.d
If the Deprecated was already True in the previous revision, the field along with the Replaced By field should be lock. If it was False in the previous revision the checkbox shall be enabled. When Deprecated is changed to True, the developer must be able to select a replacement ASCCP that is not already deprecated from a drop-down list in the Replaced By field – but the field is optional. If the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.
##### Test Assertion #15.12.4.e
If “NIllable” was False, it can be changed to True but not vice versa.
##### Test Assertion #15.12.4.f
A warning should be given when the “Definition” is empty.
##### Test Assertion #15.12.4.g
The fields of the Associated ACC and its children nodes cannot be changed.
##### Test Assertion #15.12.4.h
“Property Term”, “Reusable”, “Namespace”, and “Nillable” are required.

#### Test Assertion #15.12.5
The end user cannot change the ACC to another one.

#### Test Assertion #15.12.6
The end user can cancel the amendment, in which case, the system rollbacks all changes during the amendment. History records throughout the course of the amendment are in the history record.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.13

> End user ASCCP state management

Pre-condition: The user is on the ASCCP detail page, which he owns.
All these state changes need a confirmation dialog box with slightly different messages.


### Test Assertion:

#### Test Assertion #15.13.1
The end user can change the state of ASCCP he owns from WIP to QA.

#### Test Assertion #15.13.2
The end user can change the state of ASCCP he owns from QA back to WIP.

#### Test Assertion #15.13.3
The end user can change the state of ASCCP he owns from QA to Production.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.14

> Creating a brand-new end user BCCP

Pre-condition: N/A


### Test Assertion:

#### Test Assertion #15.14.1
On the CC View/Edit page where a particular release branch is selected, the end user can create a brand-new BCCP by selecting a BDT which can be in any state and belongs to any user. The BDT choice shall, by default, be all BDT types and Commonly Used filter set to Empty-All. (see section 4.1.1.12.1 in the SRT Design Document version 2.4 to understand what is unqualified BDT), but the user has a choice to be able to select any BDT (type > 1). The BCCP should have the following default values – Property Term = “Property Term”; DEN = Property Term + “. “ + BDT_Data_Type_Term and locked; Nillable = false; Deprecated = false and disabled; Value Constraint= None (Default and Fixed value should be empty); Definition = blank; ; Definition Source = blank; Namespace = null, Comments = empty. The brand-new BCCP must have the selected release number assigned right away, i.e., it must not appear in any release except the release the user has selected at the time of creation. It has a revision number of 1. The BCCP should display the associated BDT details but cannot be changed. BDT supplementary components are displayed in the tree but their details cannot be changed.

#### Test Assertion #15.14.2
The end user cannot create a brand-new BCCP when the working branch is selected.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.15

> Editing a brand-new end user BCCP

Pre-condition: The brand-new BCCP is created by the end user and it is in the WIP state. The end user accesses these functionalities by opening the brand-new BCCP from the CC list page or after creating a brand-new BCCP.


### Test Assertion:

#### Test Assertion #15.15.1
The end user can change the properties of the BCCP and save the changes with the following business rules.

##### Test Assertion #15.15.1.a
The fields “Property Term”, “Nillable”, “Namespace”, “Value Constraint (Default or Fixed Value)”, “Definition”, “Definition Source” can be changed.
##### Test Assertion #15.15.1.b
The field “DEN” is automatically changed based on the changes of the “Property Term” field.
##### Test Assertion #15.15.1.c
A warning should be given when the Definition is empty.
##### Test Assertion #15.15.1.d
The fields “GUID” and “DEN”, and “Deprecated” cannot be changed.
##### Test Assertion #15.15.1.e
The fields of the BDT and its components cannot be changed.
##### Test Assertion #15.15.1.f
The fields “Default Value” and “Fixed Value” are mutually exclusive.
##### Test Assertion #15.15.1.g
The fields “Fixed Value” and “Nillable” are mutually exclusive.
##### Test Assertion #15.15.1.h
“Property Term”, “Nillable”, and “Namespace” are required. Only non-standard namespace shall be allowed.

#### Test Assertion #15.15.2
The end user can change the BDT to another one.

#### Test Assertion #15.15.3
When the BCCP DEN changes, all BCCs which uses the BCCP and whose revision numbers are 1 shall have their DEN update accordingly.

#### Test Assertion #15.15.4
Place holder for testing about history of BCCP when available.

#### Test Assertion #15.15.5
The end user can Exclude SCs or not from the Searching Field by checking or unchecking the “Exclude SCs” checkbox accordingly.

##### Test Assertion #15.15.5.a
If the “Exclude SCs” checkbox is enabled (i.e., checked) the SCs are excluding from the searching field
##### Test Assertion #15.15.5.b
If the “Exclude SCs” checkbox is disabled (i.e., unchecked) the SCs are excluding from the searching field

### Test Step Pre-condition:



### Test Step:

## Test Case 15.16

> Amend an end user BCCP

Pre-condition: N/A
Generally, only backwardly compatible changes can be made.


### Test Assertion:

#### Test Assertion #15.16.1
On the CC Detail page of an end user BCCP in Production state, the end user even when he is not the owner can amend the BCCP. There should be a dialog asking the end user to confirm the intention to amend. [Note: In this case, the system simply advances the revision number, resets the revision tracking number to 1, and changes the state of the BCCP back to WIP.]  Its attributes are initially the same as before the amendment. [Note that I use the word ‘Amend’ as opposed to ‘Create a new Revision’ or ‘Revise’ as in the case of the developer CC, because in the case of the end user CC, the previous revision won’t be kept.]

#### Test Assertion #15.16.2
The end user cannot amend a released developer BCCP.

#### Test Assertion #15.16.3
The end user can change the details of the BCCP and save the changes with the following business rules.

##### Test Assertion #15.16.3.a
The fields “GUID”, “DEN”, “Namespace”, and “Property Term” cannot be changed.
##### Test Assertion #15.16.3.b
If a fixed value is already applied, it cannot be changed (hence neither default nor nillable can change).
##### Test Assertion #15.16.3.c
If the Deprecated was already True in the previous revision, the field along with the Replaced By field should be locked. If it was False in the previous revision the checkbox shall be enabled. When Deprecated is changed to True, the end user must be able to select a replacement BCCP that is not already deprecated from a drop-down list in the Replaced By field – but the field is optional. If the Deprecated is changed to False, the Replaced By field shall be Null and optionally disappears from the UI.
##### Test Assertion #15.16.3.d
If nillable can change, it can only be changed from false to true.
##### Test Assertion #15.16.3.e
If the default value can change, it can change to any valid value w.r.t. the primitive.
##### Test Assertion #15.16.3.f
Definition and Definition Source can change.
##### Test Assertion #15.16.3.g
A warning should be given when the Definition is empty.
##### Test Assertion #15.16.3.h
The fields of the BDT and its supplementary components cannot be changed.

#### Test Assertion #15.16.4
The end user cannot change the BDT to another one.

#### Test Assertion #15.16.5
The end user can cancel the amendment, in which case, the system rollbacks all changes during the amendment. History changes are kept in the history record.

#### Test Assertion #15.16.6
Place holder for testing about undoing changes to the BCCP or cancel the amendment in the future or about displaying history during the amendment.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.17

> End user BCCP state management

Pre-condition: The user is on the BCCP detail page, which he owns.
All these state changes need a confirmation dialog box with slightly different messages.


### Test Assertion:

#### Test Assertion #15.17.1
The end user can change the BCCP state from WIP to QA, if he is the owner.

#### Test Assertion #15.17.2
The end user can change the BCCP state from QA back to WIP, only if he is the owner.

#### Test Assertion #15.17.3
The end user can change the BCCP state from QA to Production, only if he is the owner.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.18

> Deleting an ender user BCCP

Pre-condition: N/A
Delete a CC means that it is marked as “Deleted” and it is still displayed in the CC list when the associated release branch is selected. If a CC is “Deleted” another end user can restore it. A confirmation dialog is needed for all deleting actions. “Do you really want to delete the core component?”


### Test Assertion:

#### Test Assertion #15.18.1
If a BCCP revision number is 1, the end user who is the owner of the BCCP can delete it when it is in WIP state. A confirmation dialog box should appear to ask for a confirmation.  After successful deletion, the system takes the user back to the CC list page.

#### Test Assertion #15.18.2
Upon opening an ancestor ACC that has an ACC using a deleted BCCP, as the ancestor ACC is expanded down to the ACC that contains the deleted BCCP, the system shall be able to flag that the BCCP is in deleted state.

#### Test Assertion #15.18.3
Upon opening an ACC that uses the BCCP, the BCC that uses that BCCP shall be highlighted or flagged somehow indicating that the BCC has a deleted BCCP. The system shall provide an option for the end user to choose another BCCP for the BCC. The system shall also allow the end user to open the deleted BCCP in another tab or dialog where he can restore it, even if the BCCP was is owned by another end user. Then, the system shall be able to clear the flag, e.g., when the end user refreshes the ACC.

#### Test Assertion #15.18.4
BCCP whose revision number is more than 1 in any state cannot be deleted.

### Test Step Pre-condition:



### Test Step:

## Test Case 15.19

> Restoring end user BCCP

Pre-condition: The end user is on the CC View/Edit page with the release branch open. Deleted CCs are shown in the list (“Deleted” state is selected in the state filter box). The end user opens a deleted BCCP to view its detail.


### Test Assertion:

#### Test Assertion #15.19.1
The end user can restore the deleted BCCP whose BDT is still alive in both cases that he owns and does not own the BCCP.

#### Test Assertion #15.19.2
The end user can restore a deleted BCCP whose BDT is deleted. The UI display a flag in the BCCP detail page that its BDT is in deleted state.

### Test Step Pre-condition:



### Test Step: