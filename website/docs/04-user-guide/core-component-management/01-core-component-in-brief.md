---
title: "Core Component in Brief"
sidebar_position: 1
---

Core Components or CCs for short are canonical, context-independent data (exchange) models.
The meta-model of CCs in connectCenter follows the [UN/CEFACT Core Component Specification (CCS) standard](https://www.unece.org/fileadmin/DAM/cefact/codesfortrade/CCTS/CCTS-Version3.pdf), also known as ISO 15000-5.

connectCenter treats CCs as the canonical model of a data exchange standard such as connectSpec or MIMOSA CCOM.
In CCS, Business Information Entities (or BIEs) can be derived from CCs.
connectCenter treats BIEs like an implementation/usage guide of corresponding CCs for a particular business context.
See the [BIE Management](../bie-management/01-bie-in-brief.md) section for further explanation about BIEs.

In CCTS/CCS terms, the primary core component categories are ACC, ASCC, BCC, and Core Component Type (CCT).
For using connectCenter, however, it is also important to understand the related reusable properties and managed artifacts that appear in the UI.
connectCenter generally exposes Data Types and related artifacts in the UI instead of using raw CCT terminology.
The most important core-component-related artifacts are as follows:

1. Aggregate Core Component (ACC)

2. Association Core Component Property (ASCCP)

3. Association Core Component (ASCC)

4. Basic Core Component Property (BCCP)

5. Basic Core Component (BCC)

6. Business Data Type (BDT)

7. Code List (CL)

8. Agency ID List

Explanations about these artifacts, including connectCenter-specific implementation details, are given next.
The figure below illustrates the kinds of artifacts discussed in this section.

<div class="mermaid-fullwidth">

```mermaid
---
config:
  block:
    padding: 2
  themeVariables:
    fontSize: 16px
    edgeLabelBackground: transparent
---
block-beta
columns 56

space:7 Order["&lt;&lt;ACC&gt;&gt;<br/><b>Order. Details</b>"]:7 space:19 Identifier["&lt;&lt;BCCP&gt;&gt;<br/><b>Identifier. Identifier</b>"]:10 space:3 IdentifierType["&lt;&lt;BDT&gt;&gt;<br/><b>Identifier. Type</b>"]:8 space:2
space:56
space:33 DateTime["&lt;&lt;BCCP&gt;&gt;<br/><b>Date Time. Date Time</b>"]:10 space:3 DateTimeType["&lt;&lt;BDT&gt;&gt;<br/><b>Date Time. Type</b>"]:8 space:2
space:56
space:5 PurchaseOrder["&lt;&lt;ACC&gt;&gt;<br/><b>Purchase Order. Details</b>"]:11 space:17 TaxAmount["&lt;&lt;BCCP&gt;&gt;<br/><b>Tax Amount. Amount</b>"]:10 space:3 AmountType["&lt;&lt;BDT&gt;&gt;<br/><b>Amount. Type</b>"]:8 space:2
space:56
AsccLabel1["&lt;&lt;ASCC&gt;&gt;<br/>Purchase Order."]:11 space:45
AsccLabel2["Purchase Order Line.<br/>Purchase Order Line"]:11 space:33 CurrencyCode["&lt;&lt;SC&gt;&gt;<br/><b>Amount. Currency. Code</b>"]:12
space:56
POLineASCCP["&lt;&lt;ASCCP&gt;&gt;<br/><b>Purchase Order Line. Purchase Order Line</b>"]:21 space:10 POLineACC["&lt;&lt;ACC&gt;&gt;<br/><b>Purchase Order Line. Details</b>"]:14 space:1 ISO4217["&lt;&lt;Code List&gt;&gt;<br/><b>ISO 4217</b>"]:8 space:2

Order -- "&lt;&lt;BCC&gt;&gt;<br/>Order. Identifier. Identifier" --> Identifier
Identifier --> IdentifierType
Order -- "&lt;&lt;BCC&gt;&gt;<br/>Order. Date Time. Date Time" --> DateTime
DateTime --> DateTimeType
PurchaseOrder --> Order
PurchaseOrder -- "&lt;&lt;BCC&gt;&gt;<br/>Purchase Order. Tax Amount. Amount" --> TaxAmount
TaxAmount --> AmountType
AmountType --> CurrencyCode
CurrencyCode --> ISO4217
PurchaseOrder --> POLineASCCP
POLineASCCP --> POLineACC

classDef cc fill:#87CEEB,stroke:#2B6A8F,color:#000
class Order,PurchaseOrder,Identifier,IdentifierType,DateTime,DateTimeType,TaxAmount,AmountType,CurrencyCode,ISO4217,POLineASCCP,POLineACC cc
classDef lbl fill:transparent,stroke:transparent
class AsccLabel1,AsccLabel2 lbl
```

</div>

## Aggregate Core Component (ACC)

ACC represents reusable complex data structure, i.e., one that has one or more properties, some of which are also complex data structures.
Some ACCs are semantic data structure, some are architectural archetype.
An example of an ACC is "Purchase Order", which may have "Identifier", "Order Date Time", "Customer Party", and "Purchase Order Line" properties.
While "Identifier" and "Order Date Time" are simple properties, "Customer Party" and "Purchase Order Line" are complex properties and are represented by another ACC.
Hence, ACCs can be building blocks for other complex data structures.

There are two ways to use ACC as a building block, 1) as a based type inheritance for another ACC and 2) as a representation of a property.

In the first case, an "Order" ACC, which has "Identifier" and "Order Date Time" properties, may be a based type of the "Purchase Order" ACC.
That means, the "Purchase Order" ACC inherits these two properties from the based "Order" ACC.
While the ACCs' based type relationship is not supported in CCS, connectCenter extends the CCS model to support that.

As stated earlier some ACCs are semantic data structures and some are architecture archetypes.
Taking connectSpec as an example, it has an architectural archetype called "Base" to support its extension architecture.
For instance, connectSpec standard defines "Address Base" and "Address" ACCs for the "Address" semantic entity; all semantic properties are placed in the "Address Base" ACC while the "Address" ACC is based on the "Address Base" ACC and only adds to it an extension point.
connectCenter Core Component Management has specific features to address connectSpec architectural requirements and also XML schema features.
These will be described in specific sections about the management of these different types of CCs.

In the second case of ACC as building block, "Party" and "Purchase Order Line" ACCs may be used as the representations for the "Customer Party" and the "Purchase Order Line" properties.
To use an ACC as a representation of a property, an ASCCP that is described next has to be first created and then association, ASCC, from the "Purchase Order" to the ASCCP then can be made.
These are described next.

## Association Core Component Property (ASCCP)

Practically, ASCCP allows ACC to be reused as a property of another ACC.
All CCS entities have an important detail called Dictionary Entry Name (DEN).
While ACC DEN is made up of an Object Class Term and the fixed string "Details" such as "Purchase Order. Details" – "Purchase Order" is the Object Class Term; an ASCCP DEN is made up of its unique Property Term and the ACC's Object Class Term representing its data structure (notice that each term in DEN is separated by a dot and a space and each word is separated by a space).
Oftentimes, the Property Term is the same as Object Class Term such that an ASCCP DEN looks like "Party. Party" because a Party ASCCP is also represented by the Party ACC; however, this is not always the case.
For example, a Property Term may be "Customer Party" which may also be represented by the "Party. Details" ACC.
In this case, according to the CCS truncation rule, DEN of the ASCCP would be "Customer. Party".
However, connectCenter has NOT implemented the truncation rule; therefore, DEN is "Customer Party. Party" in connectCenter.

Property Term is the most important detail of the ASCCP.
If you would like to understand the detail to why connectCenter has not implemented the truncation rule in DEN, read on; otherwise, you can skip to the next section that describes ASCC.
ASCC is the way in which an ACC uses an ASCCP.

There are three reasons to why connectCenter has not implemented the truncation rule in DEN.
First, connectCenter was initially tested on an existing standard.
While the standard also adopted CCS, its normative form was in XML schema.
Therefore, connectCenter has to reverse engineered the standard from XML schema into the CCS meta-model.
Due to limited development resources, the reverse engineering has to be done by code.
Names in the XML schema may be resulting from the truncation rule; and in such case, it is not computationally deterministic to recognize what the full Property Term is.
Taking the component named "Bank Draft Check" that should be imported into connectCenter as an ASCCP.
It uses the component named "Check" that should be imported into connectCenter as an ACC.
It is not clear whether the Property Term should be just "Bank Draft" or "Bank Draft Check".

The second reason follows the first reason that connectCenter cannot rely on DEN to express the CCs into XML schemas such that the generated schemas are the same as the imported source.
It is more reliable to use the whole component name as the Property Term and then use the Property Term to generate schemas as well.

The third reason, as it turned out, when reading the untruncated DEN, it is clearer what the Property Term is and searching is simpler as well.
For example, if the user thinks about searching for the "Bank Draft Check" notion.
He does not have to worry about putting "Bank Draft. Check" or "Bank Draft Check" in the search field; he can just type in the DEN search field, "Bank Draft Check".

DEN is a generated field in connectCenter.
Note that generated DENs are capped at 200 characters; when a DEN would exceed that length, the property term is truncated to fit.
At some point in the future, the algorithm for DEN can change to use the truncation rule, if the user community wants so.

## Association Core Component (ASCC)

When an ACC has an ASCCP as a property, there is an association from the ACC to the ASCCP.
An ASCC represents that association.
An ASCC has a DEN which is composed of the Object Class Term of the ACC and the DEN of the ASCCP.

For example, the "Purchase Order. Details" ACC has the property "Customer Party. Party" ASCCP means that there is a "Purchase Order. Customer Party. Party" ASCC representing the association between the ACC and the ASCCP.
The ASCC carries details such as the cardinality of the property, the definition of the property when used under the ACC.

In connectCenter, ASCC is local to the ACC.
It is not reused.

## Basic Core Component Property (BCCP)

Just like ASCCP, BCCP is a reusable property that can be used by an ACC.
The difference is BCCP has a simpler structure or no structure.
BCCP itself can carry a value (in an instance data) and can have at most one level of children.
An example of a BCCP is "Tax Amount"; and it has a child "Currency Code".
An instance data of the "Tax Amount" BCCP includes the 3000 value of the tax amount itself and the "US Dollar" value of the "Currency Code".

Just like ASCCP, BCCP also has a Property Term representing its semantics.
DEN of the BCCP is made up of the Property Term and the Data Type Term of the BDT it uses.
BDT will be described below, but BDT indicates the values and the children the BCCP can have.
For example, the "Tax Amount" BCCP uses the "Amount. Type" BDT.
Since Property Term of the BCCP is "Tax Amount" and Data Type Term of the BDT is "Amount", the BCCP DEN is "Tax Amount. Amount" if the truncation rule is not used (See the ASCCP section about why connectCenter does not use the CCS DEN truncation rule).
When the BCCP uses a qualified BDT, the BDT's qualifier is included in the DEN as well; for example, a "Tax Amount" BCCP that uses the "Open_ Amount. Type" BDT has the DEN "Tax Amount. Open_ Amount".

## Basic Core Component (BCC)

Unfortunately, the name, BCC, is not very suggestive of what it is.
But it might be easier to remember BCC as a counterpart of the ASCC.
That is, think of it as an association from the ACC to the BCCP (just like ASCC as an association from ACC to the ASCCP).
Similar to the ASCC, BCC has DEN which is made up of the ACC's Object Class Term and the BCCP DEN.
For example, the "Order. Details" ACC has the property Identifier means that it uses the "Identifier. Identifier" BCCP.
Consequently, DEN of the respective BCC is "Order. Identifier. Identifier".

## Business Data Type (BDT)

BDT is a standard data type defined based on [Core Data Type Catalog](https://www.unece.org/fileadmin/DAM/cefact/codesfortrade/CCTS/CCTS-DTCatalogueVersion3p1.pdf), a companion specification to the CCS.
Practically, BDT is used to indicate values (or formally, a value domain) a simple property can take, and the child attributes it can have and their value domains.
For example, the "Amount. Type" BDT is defined to have the decimal value domain and one attribute whose DEN is "Amount. Currency. Code".
Attribute of a BDT is called Supplementary Component (SC) in CCS and the part of the BDT that holds the BDT's value is called Content Component.
Both Content Component and Supplementary Component may have multiple value domains, one of which is a default.
Value domains can be primitives (e.g., decimal, token), code lists, or agency ID lists; for instance, the "Currency Code" SC can be restricted with an ISO currency code list in a derived BDT.

The first part of the BDT DEN such as "Amount. Type" is called Data Type Term.
Data Type Term is used to make up the second part of the BCCP DEN as in "Tax Amount. Amount".
BDT DEN may include a qualifier in front of the Data Type Term separated by the under bar such as "Open\_ Amount. Type".
The qualifier communicates additional semantics and may also results in a restriction to the value domain.

**Optional Reading about how CDT, BDT, and BIE work in connectCenter**: In connectCenter, Data Types are managed in the context of a Library and a Release.
The Library selector determines which DT catalog you are working with, and the "Branch" selector next to the search bar picks the release within that library.
For example, the "CCTS Data Type Catalogue v3" library contains the CDT catalog used as the canonical base DT set for CCTS-oriented derivation.
If a repository also includes an "ISO 15000-5" library, its base DT entries play that same base-DT role for CCS-oriented content in that library.

Internally, connectCenter distinguishes a library base DT from a derived BDT by the inheritance relationship itself.
A DT without a based DT is treated as the library's base DT.
A DT with a based DT is treated as a BDT derived either from that base DT or from another BDT.
This is why connectCenter can keep the user-facing DT experience simpler while still preserving CDT-BDT relationships in the repository.

In the current UI, the "Data Type" page is library-scoped and the main list does not prominently separate CDT from BDT.
Creating a new DT always creates a BDT by copying the selected base DT's inherited content, supplementary components (SCs), and default value domains.
This allows a business library such as connectSpec to create BDTs that are based on DTs supplied by another library, such as "CCTS Data Type Catalogue v3".

On the BIE side, connectCenter does not expose a separate BDT layer under BBIEs.
Instead, BBIE value domains and supplementary components appear where they are applied in the BIE, even though the underlying repository still preserves the DT inheritance chain.

### Types of BDTs in connectCenter

By data convention, BDTs in connectCenter can be viewed in three groups: default BDTs, unqualified BDTs, and qualified BDTs.
This grouping is a convention of the shipped connectSpec data rather than an explicit field in the application.

1. Default BDTs are variations of connectSpec implementations of CDTs.
   These BDTs use various primitives for the content and supplementary components.
   They don't have semantic qualifiers.
   Each default BDT carries a random six-character identifier; in connectCenter this identifier is not part of the name or DEN but is stored separately and shown in the "Six Hexadecimal ID" column of the "Data Type" page.

2. An unqualified BDT can be viewed as a connectSpec selection of default BDTs for a particular CDT.
   Therefore, unqualified BDTs do not have a semantic qualifier.
   For example, for the Amount CDT, connectSpec selects the default BDT whose Six Hexadecimal ID is "0723C8", which uses the decimal primitive as the default for the content component and token for its "Currency Code" SC.
   Consequently, connectSpec defines an unqualified "Amount. Type" BDT based on that default BDT.
   These are good BDTs to use with BCCPs because they use the least restrictive primitives that can be further restricted in the BIE.
   Most connectSpec BCCPs use unqualified BDTs or qualified BDTs, although a few BCCPs use certain default BDTs directly.

3. Qualified BDTs are BDTs with a semantic qualifier.
   A lot of qualified BDTs are created so that code lists can be used with BCCPs.

connectCenter also employs another flag called Commonly Used.
This flag is generally for convenience.
In the connectSpec data shipped with connectCenter, BDTs used by BCCPs in connectSpec 10.6 are flagged with Commonly Used equal true.
The flag cannot be changed from the UI, and newly created DTs are not flagged as commonly used.
The "Select DT to create BCCP" dialog filters the DT list to "Commonly Used" = "True" by default, so BCCP authors see only commonly used BDTs unless they clear that filter (see [BCCP Management](./developer/03-bccp-management.md#create-a-new-bccp)).

## Code List (CL) and Agency ID List

Code list is a way of providing a value domain to the BDT and its SCs – other ways of providing value domains are via primitives (e.g., decimal, boolean).

Code List is a list of allowable values (however, sometimes standards intentionally leave the list open to any additional values for extensibility reason).

While the CCS meta-model does not define Code Lists as Core Components, connectCenter manages Code Lists together with CC-related artifacts to support standard publication and usage.

The Core Data Type Catalog specification specifies "Identifier Scheme" as another way of providing a value domain to the BDT and its SC.
connectCenter has not implemented full Identifier Scheme management.
Instead, it supports one related managed artifact, the "Agency ID List", as a list of values.
