Key Concepts of connectCenter CC Management
-------------------------------------------

In addition to employing the CCS meta-model, connectCenter aims to improve the state of art in **collaborative** development of a standard data model with enhanced **traceability**.

In addition to being a web-based application that allows multiple users to simultaneously access a single source of truth, life cycle states each CC have also contributed to enhancing the collaborative experience in connectCenter.
CCs can take various states.
These states allow for the CC developers to let their collaborators know the readiness of the CCs to be reviewed or put into a release.
CCs can be viewed and used by others even while it is still being developed.
A mechanism was designed so that references between CCs cannot become invalid.
That is deleted CCs can be restored by another user.
More details about CC life-cycle states are described in the `Developer vs. End User Core Components <#developer-vs-end-user-core-components>`__ section and other specific CC management sections.

connectCenter also supports the release concept, just like the traditional way of publishing a standard or software.
At present, a single connectCenter database intends to support only backwardly compatible releases.
It should be noted that backward compatibility depends on syntactical expression of the CCs.
Therefore, connectCenter backward compatibility may be more liberal than that of a specific syntax expected by a particular standard user.
Release also has states that support typical standard publication and review process.
This is further described in the `Release Management <#release-management>`__ section.

Once CCs are published/released, they can be revised. connectCenter keeps revisions of CCs for each of its release (as part of the whole standard).
For traceability enhancement, every change to CCs is also kept in connectCenter database.
These are called history.

Releases of a standard are kept in the connectCenter database (as delta).
The user can work with multiple backwardly compatible releases in the tool.
For example, some ongoing integration projects may still use CC definitions from an older release, while new integration projects can embark on CC definitions from the latest release

CC ownership
~~~~~~~~~~~~

The user who creates the CC is the first owner of the CC.
Ownership of the CC can be transferred only between the same types of users – developer user or end user.

There are three situations when the ownership of a CC is automatically transferred.
First is when a user *restores* a deleted CC.
Second is when the developer user *revises* the CC, and lastly when the end user *amends* the CC.
In those cases, the ownership is automatically transferred to the user who performed the respective actions.
See also the `cc <#cc-states>`__ section.

CC details are always visible to other users (in any state).
It is just that only the owner can make changes to the CC.
The only exception is when CCs are put into a release.
Any developer can put any CC in the `Candidate state <#cc-states>`__ into a release and then manage their release states.

Developer vs. End User Core Components
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Core components (CCs) can be created and managed by either connectCenter developer user or end user.
The intention for CCs created by the developer is for them to become a standard.
The intention for CCs created by the end user are for used in the BIE extension.
BIE extension is typically used when there is an urgent need to add to the standard or when there is unique requirement to add to the standard to meet the integration need, see the `Extend a BIE <#extend-a-bie>`__ section.

CCs created by the connectCenter developer role are called *Developer CCs* (DCCs) or *Standard CCs*.
CCs created by connectCenter end user role are called *End User CCs* (EUCCs).

The most important difference between DCCs and EUCCs is their life cycles.
The way EUCCs interact with BIEs is also different, but this will be explained in the `Extend a BIE <#extend-a-bie>`__ section.
The next section below describes CC states.

CC states
~~~~~~~~~

Developer CC states
^^^^^^^^^^^^^^^^^^^

The figure below shows life cycle states of DCCs.
When a CC is created for the first time, it has a WIP state with revision number 1.
The creator is the owner.
If a developer revises a Published CC, it also has a WIP state with an incremental revision number and has a new owner.

.. image:: media/image2.jpg
   :alt: Diagram Description automatically generated
   :width: 6.5in
   :height: 2.20833in

All users (not just developers) have read access to CCs in all states.
Only the current owner has the write access when a CC is in the WIP state.
The ownership of a CC is relinquished when the CC is in the Deleted or Published state.
In other words, another developer can take over the ownership by restoring a deleted CC or revising a published CC.
Transitions from Candidate to Release Draft and from Release Draft to Published of a CC is occurred by the state transitions of the Release, to which the CC is assigned.
See more about this in the `Release Management <#release-management>`__ section.
The table below summarizes the action and authorization in each state.

+---------------+---------------------------------------------+--------------------------------------------+-------------------------+
| Role State    | Developer Owner                             | Other Developers                           | End Users               |
+===============+=============================================+============================================+=========================+
| WIP           | Edit.                                       | View details.                              | View details            |
|               | Change state to Draft.                      | Use in other DCCs.                         |                         |
|               | Delete and the state is changed to Deleted. |                                            |                         |
|               | Use in other DCCs.                          |                                            |                         |
+---------------+---------------------------------------------+--------------------------------------------+-------------------------+
| Draft         | Change state to WIP or Candidate.           | View details.                              | View details            |
|               | Use in other DCCs.                          | Use in other DCCs.                         |                         |
+---------------+---------------------------------------------+--------------------------------------------+-------------------------+
| Candidate     | Change state to WIP.                        | View details.                              | View details            |
|               | Use in other DCCs.                          | Use in other DCCs.                         |                         |
|               | Assign to a new release and                 | Assign to a new release and                |                         |
|               | change state to Release Draft via           | change state to Release Draft via          |                         |
|               | Release Management                          | Release Management                         |                         |
+---------------+---------------------------------------------+--------------------------------------------+-------------------------+
| Release Draft | View Details.                               | View Details.                              | View details            |
|               | Use in other DCCs.                          | Use it other DCCs.                         |                         |
|               | Change state to Published or                | Change state to Published or               |                         |
|               | back to Candidate via                       | back to Candidate via                      |                         |
|               | Release Management.                         | Release Management.                        |                         |
+---------------+---------------------------------------------+--------------------------------------------+-------------------------+
| Published     | Revise – a new revision is created in WIP.  | Revise – a new revision is created in WIP. | View details.           |
|               | Use it in other DCCs.                       | Use it in other DCCs.                      | Create BIE from the CC. |
|               |                                             |                                            | Use in EUCCs.           |
+---------------+---------------------------------------------+--------------------------------------------+-------------------------+
| Deleted       | Restore back to WIP state.                  | Restore back to WIP state.                 | View details            |
|               | Use in other DCCs.                          | Use in other DCCs.                         |                         |
+---------------+---------------------------------------------+--------------------------------------------+-------------------------+

The intention of the Draft, Candidate, and Deleted states is to help with the development collaborations where multiple developers may work on different parts of the release changes and uses each other’s CCs.

The Deleted state in particular ensures that references across CCs do not become invalid if the owner does not want that CC anymore.
If a CC is deleted (accept BCC and ASCC), it can be restored by any other developer.

The Draft and Candidate states along with the Release Draft and Published state are for development collaboration.
Exactly how these states are used may subject to a practice designed by a specific standard governing body.
The following is an example, a CC developer transitions a set of related CCs to the Draft state to indicate that they are ready to be reviewed.
That set of CCs and others advanced to the Draft state are reviewed and received comments by a group of CC developers.
Some that need changes are put back to WIP state, changed, and put back to Draft again for the next review cycle; some that need no further changes are moved to Candidate.
CCs may be cycled through WIP and Draft a few times.
In addition, CCs in the Candidate state may need to be moved back to WIP for some changes due to dependencies to other CCs.
Next the group of developers (e.g., the standard architecture committee) decides that a set of Candidate CCs is sufficient to make a release, a developer (e.g., the chief architect) create a release draft from those Candidate CCs.
The release draft may be opened to public reviews and comments.
At this state, CCs in the release draft cannot be changed by anyone.
If changes are needed, the whole release draft is cancelled and all CCs in it are transitioned back to the Candidate state, at which point further changes can be made.
If no changes are needed, a developer can move the Release into the Published state and all CCs in the release draft are also moved correspondingly.
Using these states with issue management system, such as GitHub Issue or Jira, an SDO can establish an agile standard development process.

End user CC states
^^^^^^^^^^^^^^^^^^

Next, the state diagram below shows the life cycle of EUCCs.
EUCCs can have four states.
When an EUCC is first created, it has the WIP state.
An EUCC is always created and living in a specific release.
When an EUCC uses DCCs, those DCCs have to be in the same release and DCCs’ specifications are with respect to the release.

.. image:: media/image3.jpg
   :alt: Diagram Description automatically generated
   :width: 5.77083in
   :height: 2.21875in

WIP state means that the CC is still being changed or in fluid condition.
QA generally means that the CC is ready to be reviewed or tested.
Finally, Production means that the CC is already in used in a deployment.

All users (not just end users) have read access to EUCCs.
Only the current owner has write access when an EUCC is in the WIP state.
The ownership of an EUCC is relinquished when the CC is in the Deleted or Production state.
In other words, another end user can take over the ownership by restoring a deleted CC or amending a production EUCC.
The table below summarizes the action and authorization in each state.

+------------+---------------------------------------------+----------------------+-------------------+
| Role State | End User Owners                             | Other End Users      | Developers        |
+============+=============================================+======================+===================+
| WIP        | Edit.                                       | View details.        | View details      |
|            | Change state to QA.                         | Use in other EUCCS.  |                   |
|            | Delete and the state is changed to Deleted. |                      |                   |
|            | Use in other EUCCs.                         |                      |                   |
+------------+---------------------------------------------+----------------------+-------------------+
| QA         | Change state to WIP or Production.          | View details.        | View details      |
|            | Use in other EUCCs.                         | Use in other EUCCs.  |                   |
+------------+---------------------------------------------+----------------------+-------------------+
| Production | Use in other EUCCs.                         | View details.        | View details      |
|            | Amend and the state is back to WIP.         | Use in other EUCCs.  |                   |
|            | Profile in respective BIE extension.        |                      |                   |
+------------+---------------------------------------------+----------------------+-------------------+
| Deleted    | Use in other EUCCs.                         | Use in other EUCCs.  | View details      |
|            | Restore back to WIP.                        | Restore back to WIP. |                   |
+------------+---------------------------------------------+----------------------+-------------------+

The intention of the QA, Production, and Deleted states is to help with the development collaborations where multiple end users may work on different parts of the release changes.

The Deleted state allows the CCs to be managed independently while still encouraging collaboration and reuses.
Even though a user may no longer want a CC, another user may still use the CC.
The user who owned and deleted the CC can document the reason he/she deleted and what CC may have replaced it.
Other users who have CCs depending on that CC can determine whether to continue using that CC or switch to a replacement CC.
If a user decides to continue using it, he/she can restore the CC back to the WIP state again.

The QA and Production states allow the end user community to manage EUCC development collaboratively and along with integration project development life cycle.
Exactly how these two states are used may subject to a practice designed by a specific end user community.

Since connectCenter 2.0, the flexibility between the EUCC life cycle and the BIE life cycle was provided, while also minimizing the performance impact.
That is EUCCs can be modified even when there is active (non-Production) BIEs relying on them, albeit with some limitations.
That is, when the EUCC is not in the Production state, it cannot be profiled in BIE.
In other words, the EUCC is also locked in BIE during its amendment until it is in the Production state again.
In addition, only backwardly compatible changes can be made during the amendment.
Further details about EUCC and BIE interactions are described in the `Extend a BIE <#extend-a-bie>`__ section.

CC unit of control
~~~~~~~~~~~~~~~~~~

connectCenter treats ACC, ASCCP, BCCP, BDT, Code List, and Agency Identifier List as separate unit of controls.
In other words, their states are independently managed; and the following entities and their states are managed as part of one of those unit of controls.
1) BCCs and ASCCs are part of an ACC unit of control.
2) SCs is part of a BDT unit of control.
3) Code Values are part of a Code List unit of control.
And 4) Agency Identifiers are part of an Agency Identifier List unit of control.

Component types
~~~~~~~~~~~~~~~

Component types are connectCenter feature that supports connectSpec standard architecture.
It only applies to ACC.

While there are eight component types, connectCenter users usually use only 2 that are *Base* and *Semantics*.
They may seldomly use another one, which is *Semantic Group*.
They will also frequently see *Extension* and *User Extension Group*, but these two are largely handled by the system.
The table below summarizes these and other component types.
Standards other than connectSpec may use only *Semantics* and *Semantic Group* component types.

.. list-table::
   :header-rows: 1
   :widths: 25 75

   * - Component Type
     - Usage Description
   * - Semantics
     - Use this component type unless the component is supposed to be serialized as an XML schema group, which is signified with the Semantic Group component type. For connectSpec, use this component type when the ACC should have an extension point.
   * - Base
     - For connectSpec, use this component type for an ACC that should have an ACC counterpart that contains the extension point. For example, connectSpec design pattern is as follows. "Party Base. Details" ACC with the Base component type contains all the property the Party should have. A "Party. Details" ACC with the Semantics component type should be based on (derived from) the "Party Base. Details" ACC with only one additional property, which is Extension. connectCenter has a macro that automatically creates the Extension for a given Semantic ACC. connectSpec design pattern also necessitates that an ACC with Base component should have the object class term ending with the word "Base".
   * - Semantic Group
     - Use this component type for an ACC intended to reflect an XML Schema group definition. connectSpec design pattern also necessitates that an ACC with Semantic Group component type should have the object class term ending with the word "Group".
   * - Extension
     - This component type designates an ACC that is a connectSpec extension point of another ACC. For example, "Party Extension. Details" is an Extension ACC for the "Party. Details". connectCenter developers can create this type of ACC manually, but they usually do not need to do that. It is better that they invoke the "Create OAGi Extension" macro on a Semantics ACC to ensure consistent naming convention and design pattern.
   * - User Extension Group
     - connectCenter users never create this type of ACC manually. It is automatically created (or revised if one already exists) when the end user invokes an extension in the BIE. The end user then edits this ACC to add properties to the Extension component of an associated ACC.
   * - Embedded
     - This component type represents the notion of XML Schema any (`xsd:any`), which may also be representable in other syntaxes. There is only one ACC, "Any Structured Content. Details", that has this component type. At this time, connectCenter does not allow users to create an ACC with this component type. An ASCCP for this ACC is "Any Property. Any Structured Content". connectCenter users may use this property in an ACC to reflect the notion of `xsd:any`.
   * - OAGIS10 Nouns
     - This component type signifies an ACC that contains all connectSpec Noun definitions when serialized. connectCenter users most likely would never use this component type, and connectCenter does not allow users to create an ACC with this component type.
   * - OAGIS10 BODs
     - This component type signifies an ACC that contains all connectSpec BOD definitions when serialized. connectCenter users most likely would never use this component type, and connectCenter does not allow users to create an ACC with this component type.

Branch
~~~~~~

Branch is a snapshot of a set of revisions of CCs.
Branch is used to represent a release.
There is also a *Working* branch that represents the revisions of CCs being worked on for a future release.

connectCenter developers work on the Working branch to create a future release.
End users only create and maintain EUCCs in a specific release branch.
In other words, EUCCs are specific to a release.
Inherently, BIEs are also specific to a release.
Therefore, only EUCCs in the same release as a BIE can be used for extending the BIE.

When there is an active draft release, there is a draft release branch as well. connectCenter users can only view details of CCs in the draft release branch.

The user selects a branch after opening the "Core Component" page from "Core Component" -> "View/Edit Core Component".
Data Types are managed on the separate "Data Type" page opened from "Core Component" -> "View/Edit Data Type".
When Browse Standard mode is enabled for end users, the branch is selected from the "Browse Standard" page instead of the Core Component menu.
