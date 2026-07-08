---
title: Key Concepts
sidebar_position: 2
---

connectCenter implements the **Core Component Specification (CCS)** — standardized as
[ISO 15000-5](https://www.iso.org/standard/61433.html) and historically the UN/CEFACT
[Core Component Technical Specification (CCTS) v3.0](https://www.unece.org/fileadmin/DAM/cefact/codesfortrade/CCTS/CCTS-Version3.pdf).
This page is a short primer on the vocabulary you will meet throughout the application and the
rest of this documentation. Read it once before diving into the
[User Guide](../04-user-guide/01-about-this-guide.md); each concept here links to the deeper guide page
where it is covered in full.

:::note[Reference specifications]
- [CCTS v3.0](https://www.unece.org/fileadmin/DAM/cefact/codesfortrade/CCTS/CCTS-Version3.pdf) — the core conceptual model (ACC, ASCCP, BCCP, DT, …).
- [CCTS Data Type Catalogue v3.0](https://unece.org/DAM/cefact/codesfortrade/CCTS/CCTS-DataTypeCatalogueVersion3p0.pdf) — the standard primitive and core data types.
- [ISO 15000-5](https://www.iso.org/standard/61433.html) — the ISO publication of CCS.
:::

:::note
This is an overview, not the full reference. Definitions are deliberately brief.
For the authoritative treatment of Core Components (CCs), see
[Core Component — In Brief](../04-user-guide/core-component-management/01-core-component-in-brief.md).
For Business Information Entities (BIEs), see
[BIE — In Brief](../04-user-guide/bie-management/01-bie-in-brief.md).
:::

## Core Component (CC)

A **Core Component (CC)** is a canonical, context-independent data model — a
reusable building block of a data-exchange standard. connectCenter treats CCs as
the canonical model of a standard (such as connectSpec), and a
**Business Information Entity (BIE)** — see below — as a usage/implementation
profile of those CCs for a specific business context.

CCs come in three kinds, each with a precise role in the meta-model:

- **ACC — Aggregate Core Component.** A reusable complex data structure: a
  collection of related properties that together convey a distinct business
  meaning, e.g. a *Purchase Order* with *Identifier*, *Order Date Time*, and
  *Customer Party* properties. An ACC can serve as a building block either as
  a *based type* (inheritance) for another ACC or as the representation of a
  property of another ACC.
- **ASCC / ASCCP — Association Core Component / Association Core Component
  Property.** An **ASCCP** is a reusable property whose content is complex and is
  therefore represented by an ACC — it lets one ACC appear as a property of
  another, and it carries the **Property Term**. An **ASCC** is the use of an
  ASCCP within a specific ACC: it constitutes a complex business characteristic
  of that ACC and always lives inside it. In connectCenter an ASCC is local to
  its ACC and is not reused.
- **BCC / BCCP — Basic Core Component / Basic Core Component Property.** A
  **BCCP** is a reusable property whose content is simple and can be directly
  represented by a value, e.g. *Tax Amount*; its value domain is given by a Data
  Type. A **BCC** is the use of a BCCP within a specific ACC: it constitutes a
  singular business characteristic of that ACC and always lives inside it.

In connectCenter, BCCs and ASCCs are managed as part of their ACC's *unit of
control* — their states are not managed independently of the ACC.

### Data Type (DT) and Supplementary Component (DT_SC)

A **Data Type (DT)** defines the value domain a simple property may take (for
example *decimal*, *boolean*, a code list, or an agency ID list) together
with any child attributes. The standard primitive and core data types are defined in the
[CCTS Data Type Catalogue v3.0](https://unece.org/DAM/cefact/codesfortrade/CCTS/CCTS-DataTypeCatalogueVersion3p0.pdf).
The CCS term for a derived business data type is **BDT** (Business Data Type). A DT attribute is a **Supplementary Component (SC)** — in the
repository this is the `DT_SC`. For example, an *Amount* DT has a content value
plus a *Currency Code* supplementary component.

connectCenter manages Data Types on a dedicated, library-scoped page and
distinguishes a library's base DT from a derived BDT by the inheritance
relationship itself: a DT with no based DT is the library base; a DT with a based
DT is a derived BDT. See
[DT Management](../04-user-guide/core-component-management/developer/06-dt-management.md).

### Dictionary Entry Name (DEN)

Every CCS entity has a **Dictionary Entry Name (DEN)** — a generated, human-readable
name built from the entity's terms, with each term separated by a dot-and-space and
each word by a space. For example, an ACC DEN is an *Object Class Term* plus the
fixed word `Details`, as in `Purchase Order. Details`; the BCC DEN
`Order. Identifier. Identifier` combines the ACC's object class term with the BCCP's
DEN.

:::note
connectCenter does **not** implement the CCS DEN truncation rule. It uses the full
Property Term in the DEN (e.g. `Customer Party. Party`, not `Customer. Party`),
which makes names easier to read and search. DEN is a generated field.
:::

## Business Information Entity (BIE)

A **BIE (Business Information Entity)** is a derivation — a *profile* — of a shared
CC for a particular usage context. Modelling constructs that exist in the CC realm
(subtyping, restriction, abstract, groups) are removed or flattened in the BIE
realm, because a BIE is about *using* a model rather than *defining* one.

Mirroring the three kinds of CC, BIEs come in three kinds:

- **ABIE — Aggregate Business Information Entity.** An ACC employed in a specific
  business context: a complex structure whose properties are ASBIEs and BBIEs.
- **ASBIE / ASBIEP — Association Business Information Entity / Association
  Business Information Entity Property.** An **ASBIEP** represents an ASCCP used
  in a business context; its content is an ABIE. An **ASBIE** is the use of an
  ASBIEP within a specific ABIE — it profiles the corresponding ASCC.
- **BBIE / BBIEP — Basic Business Information Entity / Basic Business Information
  Entity Property.** A **BBIEP** represents a BCCP used in a business context. A
  **BBIE** is the use of a BBIEP within a specific ABIE — it profiles the
  corresponding BCC, and it carries a value whose domain is given by a data type.

A BIE is always associated with a **Business Context** (see below) that describes
where it applies. The **top-level BIE** is the root of the tree and is created from
an **ASCCP**; the word "BIE" may refer to that root or to any descendant node of
the data-structure tree.

Put simply: **CCs are for the data modeler and data architect; BIEs are for the
business analyst and integration developer.**

## Library

A **library** is the canonical, context-independent vocabulary of a standard —
the Core Components themselves, together with the Data Types, code lists, and
other artifacts they depend on. Every CC belongs to a library, and a library is
published in versions called **Releases** (see below). connectCenter can manage
more than one library side by side (connectSpec is one such library).

## Release

A **Release** is a published version of a library/standard, just like publishing a
standard or a software version. connectCenter keeps releases in the database as
deltas, and a single database is intended to hold backwardly compatible releases so
users can work with more than one at a time (older projects on an older release,
new projects on the latest).

Components **belong to a release**: every component is specified with respect to a
particular release. Developers build a future release on the **Working branch**;
when a set of candidate components is ready, a developer assembles a release draft
that, once approved, is published. End-user components and BIEs are always specific
to a release — only components in the *same* release as a BIE can be used to extend
it. See
[Release Management](../04-user-guide/core-component-management/developer/11-release-management.md).

:::note
A **branch** is a snapshot of a set of CC revisions and is used to represent a
release. The *Working* branch represents revisions being developed for a future
release.
:::

## Namespace

Every CC requires a **Namespace**. A namespace serves two purposes: it designates
the universe to which a CC belongs, and it can be used in the XML Schema (or other
syntactic) expression of that CC. Namespaces are designated as either **Standard**
(developer) or **Non-standard** (end user). See
[Namespace Management](../04-user-guide/core-component-management/developer/02-namespace-management.md).

## Code List and Agency ID List

A **Code List (CL)** is a list of allowable values used to provide a value domain
to a Data Type or its supplementary components (an alternative to primitive value
domains such as *decimal* or *boolean*). Some code lists are intentionally left open
to additional values for extensibility. See
[Code List Management](../04-user-guide/core-component-management/developer/09-code-list-management.md).

An **Agency ID List** is a related managed artifact: a list of values used
to identify organizations (agencies). It is connectCenter's supported form of the
CCS *Identifier Scheme* concept. See
[Agency ID List Management](../04-user-guide/core-component-management/developer/10-agency-id-list-management.md).

:::note
The CCS meta-model does not define Code Lists or Agency ID Lists as Core
Components, but connectCenter manages them alongside CC-related artifacts to support
standard publication and usage.
:::

## Context

A BIE's applicability is described by **context**. connectCenter organizes context
into three related concepts:

- **Context Category** — what a context dimension is *about* (for example *Industry*,
  *Application*, or *Business Process*).
- **Context Scheme** — a set of allowable values for a category (for example a
  standard industry classification scheme). A scheme belongs to a category.
- **Business Context** — a combination of context-scheme values that defines the
  situation in which a BIE should be used. A Business Context is required to create
  a BIE.

Within a Business Context, all values are interpreted **conjunctively** (AND). See
[Manage Context](../04-user-guide/bie-management/05-manage-context.md).

## Developer vs. End User roles

Core Components can be created and managed by two kinds of users, and the
distinction drives both intent and lifecycle:

- **Developer** users create **Developer CCs (DCCs)**, also called *Standard CCs* —
  components intended to *become* the standard. DCCs progress through development
  states (WIP, Draft, Candidate) and release states (Release Draft, Published).
- **End User** users create **End User CCs (EUCCs)** — components used to *extend a
  BIE* when there is an urgent or unique requirement to add to the standard. An EUCC
  always lives in a specific release and uses DCCs from that same release; EUCCs
  progress through WIP, QA, and Production states.

All users have read access to CCs in any state; only the current owner has write
access while a component is in an editable state. See
[Core Component — Key Concepts](../04-user-guide/core-component-management/02-key-concepts.md)
for the full lifecycle, ownership, and state-transition rules.

## Where to go next

- **Core Components in depth:** [Core Component — In Brief](../04-user-guide/core-component-management/01-core-component-in-brief.md).
- **Profiling:** [BIE — In Brief](../04-user-guide/bie-management/01-bie-in-brief.md).
- **How it's built:** [Architecture Overview](../06-contributing/architecture.md).
