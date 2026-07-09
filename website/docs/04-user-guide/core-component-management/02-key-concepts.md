---
title: "Key Concepts of connectCenter CC Management"
sidebar_position: 2
---

In addition to employing the CCS meta-model, connectCenter aims to improve the state of art in **collaborative** development of a standard data model with enhanced **traceability**.

In addition to being a web-based application that allows multiple users to simultaneously access a single source of truth, life cycle states each CC have also contributed to enhancing the collaborative experience in connectCenter.
CCs can take various states.
These states allow for the CC developers to let their collaborators know the readiness of the CCs to be reviewed or put into a release.
CCs can be viewed and used by others even while it is still being developed.
A mechanism was designed so that references between CCs cannot become invalid.
That is deleted CCs can be restored by another user.
More details about CC life-cycle states are described in the [Developer vs. End User Core Components](#developer-vs-end-user-core-components) section and other specific CC management sections.

connectCenter also supports the release concept, just like the traditional way of publishing a standard or software.
At present, a single connectCenter database intends to support only backwardly compatible releases.
It should be noted that backward compatibility depends on syntactical expression of the CCs.
Therefore, connectCenter backward compatibility may be more liberal than that of a specific syntax expected by a particular standard user.
Release also has states that support typical standard publication and review process.
This is further described in the [Release Management](./developer/12-release-management.md) section.

Once CCs are published/released, they can be revised.
connectCenter keeps revisions of CCs for each of its release (as part of the whole standard).
For traceability enhancement, every change to CCs is also kept in connectCenter database.
These are called history.

Releases of a standard are kept in the connectCenter database (as delta).
The user can work with multiple backwardly compatible releases in the tool.
For example, some ongoing integration projects may still use CC definitions from an older release, while new integration projects can embark on CC definitions from the latest release.

## CC ownership

The user who creates the CC is the first owner of the CC.
Ownership of the CC can be transferred only between the same types of users – developer user or end user.
The current owner can transfer a CC in the WIP state to another user of the same type with the "Transfer Ownership" action on the "Core Component" page (see [Transfer ownership of a CC](./developer/07-common-developer-cc-management-functions.md#transfer-ownership-of-a-cc)).

There are three situations when the ownership of a CC is automatically transferred.
First is when a user *restores* a deleted CC.
Second is when the developer user *revises* the CC, and lastly when the end user *amends* the CC.
In those cases, the ownership is automatically transferred to the user who performed the respective actions.
See also the [CC states](#cc-states) section.

CC details are always visible to other users (in any state).
It is just that only the owner can make changes to the CC.
Users with the Admin right are an exception: they can change the state of a CC and transfer its ownership without being the owner.
The other exception is when CCs are put into a release.
Any developer can put any CC in the [Candidate state](#cc-states) into a release draft; publishing the release, however, requires a user with the Admin right (see [Release Management](./developer/12-release-management.md)).

## Developer vs. End User Core Components

Core components (CCs) can be created and managed by either connectCenter developer user or end user.
The intention for CCs created by the developer is for them to become a standard.
The intention for CCs created by the end user are for used in the BIE extension.
BIE extension is typically used when there is an urgent need to add to the standard or when there is unique requirement to add to the standard to meet the integration need, see the [Extend a BIE](../bie-management/06-manage-bie.md#extend-a-bie) section.

CCs created by the connectCenter developer role are called *Developer CCs* (DCCs) or *Standard CCs*.
CCs created by connectCenter end user role are called *End User CCs* (EUCCs).

The most important difference between DCCs and EUCCs is their life cycles.
The way EUCCs interact with BIEs is also different, but this will be explained in the [Extend a BIE](../bie-management/06-manage-bie.md#extend-a-bie) section.
The next section below describes CC states.

## CC states

### Developer CC states

The figure below shows life cycle states of DCCs.
When a CC is created for the first time, it has a WIP state with revision number 1.
The creator is the owner.
If a developer revises a Published CC, it also has a WIP state with an incremental revision number and has a new owner.

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 300" width="900" height="300" class="uml-figure" role="img" aria-label="Developer CC life-cycle state diagram: WIP, Draft, Candidate, Release Draft, Published, and Deleted" font-size="12">
  <defs>
    <marker id="dev-cc-arrow" viewBox="0 0 8 8" markerWidth="8" markerHeight="8" refX="7.5" refY="4" orient="auto">
      <path class="uml-arrowhead" d="M0.5,0.5 L7.5,4 L0.5,7.5 Z"/>
    </marker>
  </defs>

  <!-- initial state -->
  <circle class="uml-arrowhead" cx="22" cy="80" r="8"/>
  <path class="uml-edge" d="M30,80 H97" marker-end="url(#dev-cc-arrow)"/>
  <text class="uml-label" x="63" y="74" text-anchor="middle" font-size="11">Create</text>

  <!-- top lane: Published -> Wip -->
  <path class="uml-edge" d="M858,68 V14 H115 V65" marker-end="url(#dev-cc-arrow)"/>
  <text class="uml-label" x="486" y="10" text-anchor="middle" font-size="11">Make a new revision</text>

  <!-- top lane: Candidate -> Wip -->
  <path class="uml-edge" d="M443,68 V40 H130 V65" marker-end="url(#dev-cc-arrow)"/>

  <!-- Wip <-> Draft -->
  <path class="uml-edge" d="M256,78 H180" marker-end="url(#dev-cc-arrow)"/>
  <path class="uml-edge" d="M177,94 H253" marker-end="url(#dev-cc-arrow)"/>

  <!-- Draft -> Candidate -->
  <path class="uml-edge" d="M333,86 H399" marker-end="url(#dev-cc-arrow)"/>

  <!-- Candidate -> Release Draft -->
  <path class="uml-edge" d="M484,86 H607" marker-end="url(#dev-cc-arrow)"/>
  <text class="uml-label" x="547" y="81" text-anchor="middle" font-size="11">Create Release Draft</text>

  <!-- Release Draft -> Published -->
  <path class="uml-edge" d="M688,86 H817" marker-end="url(#dev-cc-arrow)"/>
  <text class="uml-label" x="754" y="81" text-anchor="middle" font-size="11">Publish Release Draft</text>

  <!-- Release Draft -> Candidate (put back) -->
  <path class="uml-edge" d="M649,104 V152 H443 V107" marker-end="url(#dev-cc-arrow)"/>
  <text class="uml-label" x="546" y="168" text-anchor="middle" font-size="11">Put Release Draft back to Initialized state</text>

  <!-- Wip -> Deleted -->
  <path class="uml-edge" d="M138,104 V177" marker-end="url(#dev-cc-arrow)"/>

  <!-- Deleted -> Wip (restore) -->
  <path class="uml-edge" d="M100,198 H70 V96 H97" marker-end="url(#dev-cc-arrow)"/>
  <text class="uml-label" x="36" y="150" text-anchor="middle" font-size="11">Restore</text>

  <!-- note connectors -->
  <path class="uml-edge" d="M172,104 L240,150" stroke-dasharray="5 3"/>
  <path class="uml-edge" d="M180,210 L385,253" stroke-dasharray="5 3"/>
  <path class="uml-edge" d="M525,247 L700,225 L850,106" stroke-dasharray="5 3"/>

  <!-- states -->
  <rect x="100" y="68" width="77" height="36" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="138.5" y="90" text-anchor="middle" fill="#000">WIP</text>
  <rect x="256" y="68" width="77" height="36" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="294.5" y="90" text-anchor="middle" fill="#000">Draft</text>
  <rect x="402" y="68" width="82" height="36" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="443" y="90" text-anchor="middle" fill="#000">Candidate</text>
  <rect x="610" y="68" width="78" height="36" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="649" y="90" text-anchor="middle" fill="#000" font-size="11">Release Draft</text>
  <rect x="820" y="68" width="77" height="36" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="858.5" y="90" text-anchor="middle" fill="#000">Published</text>
  <rect x="100" y="180" width="80" height="36" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="140" y="202" text-anchor="middle" fill="#000">Deleted</text>

  <!-- note: ownership can be transferred -->
  <path d="M235,147 H347 L361,161 V183 H235 Z" fill="#FFFFCC" stroke="#808080"/>
  <path d="M347,147 V161 H361" fill="none" stroke="#808080"/>
  <text x="241" y="162" fill="#000" font-size="11">Ownership can be</text>
  <text x="241" y="177" fill="#000" font-size="11">transferred</text>

  <!-- note: ownership can be overtaken -->
  <path d="M385,235 H511 L525,249 V271 H385 Z" fill="#FFFFCC" stroke="#808080"/>
  <path d="M511,235 V249 H525" fill="none" stroke="#808080"/>
  <text x="391" y="251" fill="#000" font-size="11">Ownership can be</text>
  <text x="391" y="266" fill="#000" font-size="11">overtaken</text>
</svg>

All users (not just developers) have read access to CCs in all states.
Only the current owner has the write access when a CC is in the WIP state.
The ownership of a CC is relinquished when the CC is in the Deleted or Published state.
In other words, another developer can take over the ownership by restoring a deleted CC or revising a published CC.
Transitions from Candidate to Release Draft and from Release Draft to Published of a CC is occurred by the state transitions of the Release, to which the CC is assigned.
See more about this in the [Release Management](./developer/12-release-management.md) section.
The table below summarizes the action and authorization in each state.

| Role State | Developer Owner | Other Developers | End Users |
|---|---|---|---|
| WIP | Edit. Change state to Draft. Delete (initial revision only) and the state is changed to Deleted; a WIP revision of a Published CC offers "Cancel" instead, which reverts to the previous revision. Use in other DCCs. | View details. Use in other DCCs. | View details |
| Draft | Change state to WIP or Candidate. Use in other DCCs. | View details. Use in other DCCs. | View details |
| Candidate | Change state to WIP. Use in other DCCs. Assign to a new release and change state to Release Draft via Release Management | View details. Use in other DCCs. Assign to a new release and change state to Release Draft via Release Management | View details |
| Release Draft | View details. Use in other DCCs. Move back to Candidate via Release Management. The transition to Published happens when a user with the Admin right publishes the release. | View details. Use in other DCCs. Move back to Candidate via Release Management. | View details |
| Published | Revise – a new revision is created in WIP. Use it in other DCCs. | Revise – a new revision is created in WIP. Use it in other DCCs. | View details. Create BIE from the CC. Use in EUCCs. |
| Deleted | Restore back to WIP state. Purge (permanently remove). Use in other DCCs. | Restore back to WIP state. Purge (permanently remove). Use in other DCCs. | View details |

The intention of the Draft, Candidate, and Deleted states is to help with the development collaborations where multiple developers may work on different parts of the release changes and uses each other's CCs.

The Deleted state in particular ensures that references across CCs do not become invalid if the owner does not want that CC anymore.
If a CC is deleted (except BCC and ASCC), it can be restored by any other developer.
A CC in the Deleted state can also be purged, which permanently removes it from connectCenter; see [Purge a CC](./developer/07-common-developer-cc-management-functions.md#purge-a-cc).

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
If no changes are needed, a user with the Admin right can move the Release into the Published state and all CCs in the release draft are also moved correspondingly.
Using these states with issue management system, such as GitHub Issue or Jira, an SDO can establish an agile standard development process.

:::note
A CC must have its "Namespace" field set before it can leave the WIP state.
Moving a CC without a namespace to Draft (or QA for EUCCs) is rejected; the bulk action on the "Core Component" page reports "Namespace is required for all selected components before moving to Draft/QA.", and the detail page reports "Namespace is required".
:::

### End user CC states

Next, the state diagram below shows the life cycle of EUCCs.
EUCCs can have four states.
When an EUCC is first created, it has the WIP state.
An EUCC is always created and living in a specific release.
When an EUCC uses DCCs, those DCCs have to be in the same release and DCCs' specifications are with respect to the release.

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 38 540 217" width="540" height="217" class="uml-figure" role="img" aria-label="End-user CC life-cycle state diagram: WIP, QA, Production, and Deleted" font-size="12">
  <defs>
    <marker id="eu-cc-arrow" viewBox="0 0 8 8" markerWidth="8" markerHeight="8" refX="7.5" refY="4" orient="auto">
      <path class="uml-arrowhead" d="M0.5,0.5 L7.5,4 L0.5,7.5 Z"/>
    </marker>
  </defs>

  <!-- initial state -->
  <circle class="uml-arrowhead" cx="22" cy="112" r="8"/>
  <path class="uml-edge" d="M30,112 H102" marker-end="url(#eu-cc-arrow)"/>
  <text class="uml-label" x="65" y="106" text-anchor="middle" font-size="11">Create</text>

  <!-- top lane: Production -> WIP (amend) -->
  <path class="uml-edge" d="M497,100 V55 H145 V97" marker-end="url(#eu-cc-arrow)"/>
  <text class="uml-label" x="321" y="51" text-anchor="middle" font-size="11">Amend</text>

  <!-- WIP <-> QA -->
  <path class="uml-edge" d="M290,110 H188" marker-end="url(#eu-cc-arrow)"/>
  <path class="uml-edge" d="M185,127 H287" marker-end="url(#eu-cc-arrow)"/>

  <!-- QA -> Production -->
  <path class="uml-edge" d="M370,118 H457" marker-end="url(#eu-cc-arrow)"/>

  <!-- WIP -> Deleted -->
  <path class="uml-edge" d="M145,137 V182" marker-end="url(#eu-cc-arrow)"/>

  <!-- Deleted -> WIP (restore) -->
  <path class="uml-edge" d="M105,203 H70 V128 H102" marker-end="url(#eu-cc-arrow)"/>
  <text class="uml-label" x="38" y="169" text-anchor="middle" font-size="11">Restore</text>

  <!-- note connectors -->
  <path class="uml-edge" d="M185,137 L265,165" stroke-dasharray="5 3"/>
  <path class="uml-edge" d="M185,207 L305,227" stroke-dasharray="5 3"/>
  <path class="uml-edge" d="M482,210 L497,139" stroke-dasharray="5 3"/>

  <!-- states -->
  <rect x="105" y="100" width="80" height="37" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="145" y="122" text-anchor="middle" fill="#000">WIP</text>
  <rect x="290" y="100" width="80" height="37" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="330" y="122" text-anchor="middle" fill="#000">QA</text>
  <rect x="460" y="100" width="74" height="37" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="497" y="122" text-anchor="middle" fill="#000" font-size="11">Production</text>
  <rect x="105" y="185" width="80" height="36" rx="10" fill="#87CEEB" stroke="#2B6A8F"/>
  <text x="145" y="207" text-anchor="middle" fill="#000">Deleted</text>

  <!-- note: ownership can be transferred -->
  <path d="M265,157 H355 L369,171 V197 H265 Z" fill="#FFFFCC" stroke="#808080"/>
  <path d="M355,157 V171 H369" fill="none" stroke="#808080"/>
  <text x="271" y="172" fill="#000" font-size="11">Ownership can</text>
  <text x="271" y="187" fill="#000" font-size="11">be transferred</text>

  <!-- note: ownership can be overtaken -->
  <path d="M305,210 H496 L510,224 V244 H305 Z" fill="#FFFFCC" stroke="#808080"/>
  <path d="M496,210 V224 H510" fill="none" stroke="#808080"/>
  <text x="311" y="231" fill="#000" font-size="11">Ownership can be overtaken</text>
</svg>

WIP state means that the CC is still being changed or in fluid condition.
QA generally means that the CC is ready to be reviewed or tested.
Finally, Production means that the CC is already in used in a deployment.

All users (not just end users) have read access to EUCCs.
Only the current owner has write access when an EUCC is in the WIP state.
The ownership of an EUCC is relinquished when the CC is in the Deleted or Production state.
In other words, another end user can take over the ownership by restoring a deleted CC or amending a production EUCC.
The table below summarizes the action and authorization in each state.

| Role State | End User Owners | Other End Users | Developers |
|---|---|---|---|
| WIP | Edit. Change state to QA. Delete (initial revision only) and the state is changed to Deleted; a WIP amendment of a Production EUCC offers "Cancel" instead. Use in other EUCCs. | View details. Use in other EUCCs. | View details |
| QA | Change state to WIP or Production. Use in other EUCCs. | View details. Use in other EUCCs. | View details |
| Production | Use in other EUCCs. Amend and the state is back to WIP. Profile in respective BIE extension. | View details. Use in other EUCCs. Amend – a new revision is created in WIP and the amender becomes the owner. | View details |
| Deleted | Use in other EUCCs. Restore back to WIP. Purge (permanently remove). | Use in other EUCCs. Restore back to WIP. Purge (permanently remove). | View details |

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
In addition, the amendment is intended for backwardly compatible changes, and the tool enforces several guards toward that goal; for example, associations carried over from the previous revision cannot be removed (only deprecated), and the component type cannot be changed.
Further details about EUCC and BIE interactions are described in the [Extend a BIE](../bie-management/06-manage-bie.md#extend-a-bie) section.

## CC unit of control

connectCenter treats ACC, ASCCP, BCCP, BDT, Code List, and Agency ID List as separate unit of controls.
In other words, their states are independently managed; and the following entities and their states are managed as part of one of those unit of controls.
1\) BCCs and ASCCs are part of an ACC unit of control.
2\) SCs is part of a BDT unit of control.
3\) Code Values are part of a Code List unit of control.
And 4) Agency ID List values are part of an Agency ID List unit of control.

## Component types

Component types are connectCenter feature that supports connectSpec standard architecture.
It only applies to ACC.

connectCenter users usually use only two component types, which are "Base (Abstract)" and "Semantics".
They may seldomly use "Semantic Group", "Choice", or "Attribute Group", the other user-selectable options in the "Component Type" drop-down.
They will also frequently see "Extension" and "User Extension Group" on existing ACCs, but these two are handled by the system and cannot be selected manually.
The table below summarizes these and other component types.
Standards other than connectSpec may use only "Semantics" and "Semantic Group" component types.

| Component Type | Usage Description |
|---|---|
| Semantics | Use this component type unless the component is supposed to be serialized as an XML schema group, which is signified with the Semantic Group component type. For connectSpec, use this component type when the ACC should have an extension point. New ACCs are created with this component type. |
| Base (Abstract) | For connectSpec, use this component type for an ACC that should have an ACC counterpart that contains the extension point. For example, connectSpec design pattern is as follows. "Party Base. Details" ACC with the Base component type contains all the property the Party should have. A "Party. Details" ACC with the Semantics component type should be based on (derived from) the "Party Base. Details" ACC with only one additional property, which is Extension. connectCenter has a macro that automatically creates the Extension for a given Semantics ACC (the "Create OAGi Extension Component" context-menu item). connectSpec design pattern also necessitates that an ACC with Base component type should have the object class term ending with the word "Base". An ACC with this component type is always abstract. |
| Semantic Group | Use this component type for an ACC intended to reflect an XML Schema group definition. connectSpec design pattern also necessitates that an ACC with Semantic Group component type should have the object class term ending with the word "Group". |
| Choice | Use this component type for an ACC intended to reflect an XML Schema choice group, where only one of the properties appears in an instance. |
| Attribute Group | Use this component type for an ACC intended to reflect an XML Schema attribute group. It can be selected only when every property of the ACC is a BCC whose entity type is Attribute. |
| Extension | This component type designates an ACC that is a connectSpec extension point of another ACC. For example, "Party Extension. Details" is an Extension ACC for the "Party. Details". Users cannot select this component type manually; Extension ACCs are created by invoking the "Create OAGi Extension Component" macro on a Semantics ACC, which ensures consistent naming convention and design pattern. |
| User Extension Group | connectCenter users never create this type of ACC manually. It is automatically created (or revised if one already exists) when the end user invokes an extension in the BIE. The end user then edits this ACC to add properties to the Extension component of an associated ACC. |
| Embedded | This component type represents the notion of XML Schema any (*xsd:any*), which may also be representable in other syntaxes. There is only one ACC, "Any Structured Content. Details", that has this component type. connectCenter does not allow users to create an ACC with this component type. An ASCCP for this ACC is "Any Property. Any Structured Content". connectCenter users may use this property in an ACC to reflect the notion of *xsd:any*. |
| OAGIS10 Nouns | This component type signifies an ACC that contains all connectSpec Noun definitions when serialized. connectCenter users most likely would never use this component type, and connectCenter does not allow users to create an ACC with this component type. |
| OAGIS10 BODs | This component type signifies an ACC that contains all connectSpec BOD definitions when serialized. connectCenter users most likely would never use this component type, and connectCenter does not allow users to create an ACC with this component type. |

In addition to the types above, the "Create OAGi BOD Component" and "Create OAGi Verb Component" macros on the "Core Component" page create ACCs with the internal BOD, Verb, and Noun component types (see [Create a connectSpec BOD](./developer/08-create-an-connectspec-bod.md)).

## Branch

Branch is a snapshot of a set of revisions of CCs.
Branch is used to represent a release.
There is also a *Working* branch that represents the revisions of CCs being worked on for a future release.

connectCenter developers work on the Working branch to create a future release.
End users only create and maintain EUCCs in a specific release branch.
In other words, EUCCs are specific to a release.
Inherently, BIEs are also specific to a release.
Therefore, only EUCCs in the same release as a BIE can be used for extending the BIE.

When there is an active draft release, there is a draft release branch as well.
connectCenter users can only view details of CCs in the draft release branch.

The user selects a branch with the "Branch" selector after opening the "Core Component" page from "Core Component" > "View/Edit Core Component".
Data Types are managed on the separate "Data Type" page opened from "Core Component" > "View/Edit Data Type".
When the Browse Standard mode is enabled in the application settings, end users see a "Browse Standard" top-menu button instead of the "Core Component" menu; it opens the "Standard" page, which has the same "Branch" selector (see [Search and Browse CC Library](./03-search-and-browse-cc-library.md#browser-view-mode)).
