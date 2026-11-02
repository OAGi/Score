---
title: What is connectCenter?
slug: /
sidebar_position: 1
sidebar_label: What is connectCenter?
description: connectCenter is an open-source tool for CCS-based library development, contextual profiling, and XML/JSON/OpenAPI schema generation.
---

# What is connectCenter?

**connectCenter** is an open-source application for developing and maintaining
[Core Component Specification (CCS)](https://www.iso.org/standard/61433.html)
libraries and for producing contextual data-exchange profiles and their schemas.

Many data-interoperability standards have been held back by inadequate tooling. One
of them is the
[Core Component Technical Specification (CCTS)](https://unece.org/trade/uncefact/ccts)
from the United Nations Centre for Trade Facilitation and Electronic Business
(UN/CEFACT), which has a corresponding ISO specification:
[ISO 15000-5, Core Component Specification (CCS)](https://www.iso.org/standard/61433.html)
(hereafter just **CCS**).

connectCenter grew out of a collaboration between the
[Open Applications Group (OAGi)](https://oagi.org) and the
[National Institute of Standards and Technology (NIST)](https://www.nist.gov/services-resources/software/score-standards-life-cycle-management-tool), part of
the United States Department of Commerce. NIST built a proof-of-concept application
for CCS-based library development and contextual profile development; OAGi
contributed a CCS-compliant library, design input, and real-world testing. The
result is **connectCenter**[^1] — now in production use across industries such as
agriculture, aerospace, payroll, and ERP, and available as an open-source project.

[^1]: connectCenter was formerly known as **Score**. You may still see the name
"Score" in the source repository, Docker image names, and historical documentation.

## What you can do with connectCenter

### Specify context

Clearly specified context is a prerequisite to effective data-model implementations.
Key context categories include geopolitical, process, and industry. connectCenter
supports unlimited context categories, unlimited possible values for each category,
and unlimited business-context definitions (sets of context category/value pairs).

### Select a core component

Once you have specified a context, select a core component (message). For example,
perhaps you specify a context of an order-to-cash process for crop-nutrition products
in New Zealand. You then choose *purchase order* as the first message.

### Profile the core component

Many information-model libraries are large — some so large that they may intimidate
potential or actual implementers. connectCenter makes it easy to select only the
components necessary to meet the data-exchange needs of the applicable message
interaction in the specified context. The result is a **Business Information Entity
(BIE)**.

### Produce XML Schema

XML Schemas specify the structures and data types of each type of XML message.
connectCenter generates XML schemas from profiled core components.

### Produce JSON Schema

For lighter-weight, "chatty" system interactions, JSON is often preferred.
connectCenter generates JSON schemas from profiled core components; class generators
exist for many languages to consume them.

### Produce an OpenAPI Specification

Message formats are only part of interoperability; delivering and retrieving messages
is the other part. connectCenter generates OAS 3.0-compliant API specifications that
include the schema object, giving developers the details they need to invoke or
expose the services behind an API.

## Using this guide

:::tip[Read along with the tool]
It is recommended that you read the user guide along with a running connectCenter
instance. Follow the instructions while interacting with the tool.
:::

### Documentation conventions

The pages in this guide use the following typographic conventions:

| Structural Format | Meaning |
|---|---|
| "Text in Double Quotes" | Double-quoted text is used for button labels, labels, table column names on a page, names of web pages, labels of tree nodes, and example text for a form field. |
| *Italicized Text* | Italicized text is used for form field labels or to emphasize some keywords. |

## Where to go next

- **New to the concepts?** Start with [Key Concepts](./key-concepts.md) to learn the
  CCS vocabulary (Core Components, BIEs, releases, namespaces).
- **Want to run it?** See [Installation with Docker](../02-getting-started/installation-docker.md).
- **Want to understand how it's built?** See the [Architecture Overview](../06-contributing/architecture.md).
- **Ready to use it?** Jump into the [User Guide](../04-user-guide/01-about-this-guide.md).
