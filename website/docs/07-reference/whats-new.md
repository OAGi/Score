---
title: What's New
sidebar_position: 1
toc_max_heading_level: 2
---

What's new in the **3.5** line — the current published release is **3.5.2**. connectCenter
follows [Semantic Versioning](./release-numbering.md); each change below links to its GitHub
issue. The same history — including releases before 3.5.0 — is maintained on the
[Release Detail wiki](https://github.com/OAGi/Score/wiki/Release-Detail) and the
[GitHub releases](https://github.com/OAGi/Score/releases).

## 3.5.2

This release centers on a substantial OpenAPI Document upgrade — RFC 9457 error responses,
request bodies on DELETE operations, an OpenAPI 3.0/3.1 minor-version selector, and the ability
to edit a BIE's OpenAPI options directly from its root — together with a broad Business Term
overhaul that adds a drag-and-drop import dialog, in-place term management in the BIE editor,
and a large set of correctness and integrity fixes. It also introduces developer-controlled
sibling sort order for model browsing, a safety warning in the BIE editor, and richer database
schema documentation. On the platform side, the score-web front end moves to Angular 21.

### Enhancements

#### OpenAPI / OAS

- [Issue #1347 - OpenAPI Doc: Add error HTTP response codes, using RFC 9457 ProblemDetails schema in an OpenAPI context](https://github.com/OAGi/Score/issues/1347)
- [Issue #1610 - DELETE verb expression has no request body or responses](https://github.com/OAGi/Score/issues/1610)
- [Issue #1760 - OpenAPI Document: show minor version (3.0/3.1) in the UI and emit the canonical patch on generate](https://github.com/OAGi/Score/issues/1760)
- [Issue #1519 - Update BIE root view to include OpenAPI options](https://github.com/OAGi/Score/issues/1519)

  > **Note on OpenAPI version and DELETE bodies (#1610 / #1760):** The OpenAPI Version selector
  > now offers just the minor version (3.0 or 3.1); new documents default to the 3.1 family, and
  > generated documents automatically declare the latest patch of the selected family (3.0.4 or
  > 3.1.2). A DELETE **Request** message body is expressed only for 3.1 documents — if a document
  > targets 3.0 the body is intentionally dropped, and the editor shows an amber banner to warn
  > you before generation.

#### Business Term

- [Issue #1754 - Business Term UX: import dialog and in-place management in the BIE editor](https://github.com/OAGi/Score/issues/1754)
- [Issue #1753 - Business Term: UX improvements and assignment-list scoping](https://github.com/OAGi/Score/issues/1753)

#### BIE editor and model browsing

- [Issue #1638 - Add support for sibling sort order for model browsing and BIE editing](https://github.com/OAGi/Score/issues/1638)
- [Issue #1755 - BIE editor: warn before an 'Used' un-check clears descendants](https://github.com/OAGi/Score/issues/1755)

  > **Note on sibling sort order (#1638):** Reordering is a developer capability available in the
  > Model Browser (the BIE editor reflects the order but is view-only), attributes remain grouped
  > above elements, and the ordering is release-specific. Custom order carries forward
  > automatically into newly drafted releases; exporting it in the migration script is opt-in via
  > an "Include sibling view order" option.

#### Database schema

- [Issue #1759 - Augment database schema tables and comments](https://github.com/OAGi/Score/issues/1759)

  > This is a documentation-only change — table-level descriptions were added for 60
  > previously-undocumented tables and column comments were corrected or filled in. There are no
  > structural, column, index, or constraint changes.

### Bug fixes

#### OpenAPI / OAS

- [Issue #1757 - OpenAPI Doc: false "Operation ID must be unique" error when saving a request/response operation pair](https://github.com/OAGi/Score/issues/1757)

#### Business Term

- [Issue #1752 - Business Term: correctness, authorization, and integrity defects](https://github.com/OAGi/Score/issues/1752)

  > A broad set of Business Term catalog and assignment defects were fixed, including assignment
  > Discard removing the wrong record, missing server-side authorization on endpoints, a
  > non-working in-use deletion guard, unpersisted list filters, duplicate assignments, and
  > External Reference URI truncation, along with numerous validation and UI fixes.

### Platform updates

- score-web front end upgraded from Angular 20.3.25 to Angular 21.2.17
  ([Issue #1748](https://github.com/OAGi/Score/issues/1748)), including TypeScript 5.9 and the
  Angular Material/CDK 21 toolchain
- Security and library updates, including jackson-bom 2.21.1 → 2.21.4 (patching jackson-databind
  CVEs) and jOOQ 3.20.12 → 3.20.17
- Spring Boot remains at 3.5.15

### Upgrade note

Before proceeding with the upgrade, we strongly recommend **backing up your data**. As with
versions 3.5.0 and 3.5.1, database version management is handled by Flyway. On first startup the
backend automatically applies the `V3_5_2` migration — which adds the sibling sort-order table,
the OpenAPI error-response columns, and the schema table/column comments — so you do not need to
manually apply a database migration SQL script during the upgrade.

### Docker environment

If you are running connectCenter in a Docker environment, update the following image tags:

- `srt-web` to `3.5.2`
- `srt-http-gateway` to `3.5.2`
- `srt-repo` to `3.5.2`
- `redis` to `7.4.8`

See [Installation with Docker](../02-getting-started/installation-docker.md) for an example
`docker-compose.yml`.

## 3.5.1

This release focuses on OpenAPI Doc improvements, including the ability to define endpoints that
do not reference a BIE, a simplified operationId format, and additional security scheme options.
It also enhances the BIE Package manifest with backward-compatibility and revision reason
information (delivered as a Draft 0.3 manifest — see the note below), introduces GitHub issue
tracking for component changes, and resolves several bug fixes. Key platform dependencies are
updated as well, including Spring Boot 3.5.15 and Angular 20.3.25.

### Enhancements

#### OpenAPI / OAS

- [Issue #1730 - OpenAPI Doc: Provide ability to define endpoints that do NOT reference a BIE](https://github.com/OAGi/Score/issues/1730)
- [Issue #1732 - OpenAPI Doc: Modify the generation of the OperationId to exclude the business context in the name](https://github.com/OAGi/Score/issues/1732)
- [Issue #1729 - OpenAPI Doc: Enable the API Key and JWT bearer options as additional security scheme object](https://github.com/OAGi/Score/issues/1729)
- [Issue #1731 - OpenAPI Doc: move the Discard button away from the Update Button](https://github.com/OAGi/Score/issues/1731)

#### BIE package

- [Issue #1733 - Populate Backward Compatibility Indicator and Revision Reason Text in BIE Package Manifest](https://github.com/OAGi/Score/issues/1733)

  > **Partially delivered — emitted only under the new Draft 0.3 manifest** (selectable via the
  > **Manifest Version** dropdown next to **Generate**; 0.2 stays the stable default). The
  > package-level **Revision Reason** is complete, but 0.3 is marked **Draft** because the
  > **Backward Compatibility Indicator** design is not yet final: the confusing
  > `syntaxIndependent` indicator will likely be dropped in favor of the XML/JSON indicators, the
  > JSON scalar→array cardinality-flip break is still unresolved, and the rule set needs further
  > review. See the issue's comments.

#### Component change tracking

- [Issue #1533 - Score to track github issues for component changes](https://github.com/OAGi/Score/issues/1533)

### Bug fixes

#### OpenAPI / OAS

- [Issue #1728 - OpenAPI Doc: Two separate schema objects are created for the same BIE](https://github.com/OAGi/Score/issues/1728)

#### Additional fixes

- [Issue #1744 - Cannot update an existing Context Scheme — uniqueness check treats the scheme as a duplicate of itself](https://github.com/OAGi/Score/issues/1744)
- [Issue #1738 - Remove duplicate 'Move to WIP' toolbar button from CC/DT/BIE list pages](https://github.com/OAGi/Score/issues/1738)
- [Issue #1737 - createAscc rejects the first ASCC against a non-reusable ASCCP](https://github.com/OAGi/Score/issues/1737)
- [Issue #1735 - Unexpected Results When Uplifting a BIE with Reuse BIEs](https://github.com/OAGi/Score/issues/1735)
- [Issue #1723 - Codelist value domain value not showing in UI](https://github.com/OAGi/Score/issues/1723)

### Platform updates

- Spring Boot upgraded to 3.5.15
- Angular upgraded to 20.3.25
- Routine library and security updates, including jOOQ, Netty, Lombok, MariaDB Java client,
  Log4j, springdoc, Spring AI, AspectJ, PlantUML, json-schema-validator, and XSOM

### Upgrade note

Before proceeding with the upgrade, we strongly recommend **backing up your data**. As with
version 3.5.0, database version management in 3.5.1 is handled by Flyway. As a result, users do
not need to manually apply a database migration SQL script during upgrade.

### Docker environment

If you are running connectCenter in a Docker environment, update the following image tags:

- `srt-web` to `3.5.1`
- `srt-http-gateway` to `3.5.1`
- `srt-repo` to `3.5.1`
- `redis` to `7.4.8`

See [Installation with Docker](../02-getting-started/installation-docker.md) for an example
`docker-compose.yml`.

## 3.5.0

This release includes significant enhancements to BIE expression and packaging, expanded JSON
Schema and OpenAPI support, and several fixes for OpenAPI document generation and BIE behavior.
It also updates key platform dependencies, including Spring Boot 3.5.13, Angular 20.3.18,
MariaDB 11.8.6, and Redis 7.4.8.

### Enhancements

#### BIE expression and packaging

- [Issue #1703 - Add Expression Format Options to Generate BIE Package](https://github.com/OAGi/Score/issues/1703)
- [Issue #1711 - Ability to Express BIEs with File Names Having a Preferred Pattern](https://github.com/OAGi/Score/issues/1711)
- [Issue #1712 - BIE Expression Option with Separated Reuse BIE and Reuse BIE Types In Separate Files](https://github.com/OAGi/Score/issues/1712)
- [Issue #1713 - Reusable BIE Type Expression - JSON Schema](https://github.com/OAGi/Score/issues/1713)
- [Issue #1714 - Reusable BIE Type Expression - XML Schema](https://github.com/OAGi/Score/issues/1714)
- [Issue #1311 - Add capability to for stand-alone export in JSON schema format](https://github.com/OAGi/Score/issues/1311)
- [Issue #1701 - Json Schema Expression of BIE lacks Descriptions](https://github.com/OAGi/Score/issues/1701)
- [Issue #1691 - Pagination Response and Meta Header no longer expressing with BIE](https://github.com/OAGi/Score/issues/1691)

#### OpenAPI / OAS

- [Issue #1704 - OAS Doc HTTP 500](https://github.com/OAGi/Score/issues/1704)
- [Issue #1709 - OpenAPI Doc - Orphaned reference to a Reused BIE that was in a BIE is still being expressed](https://github.com/OAGi/Score/issues/1709)
- [Issue #1710 - OpenAPI Doc: Minor Enhancement to generate all path parameters when specified in the Resource name](https://github.com/OAGi/Score/issues/1710)
- [Issue #1715 - OpenAPI Doc: Minor Enhancement to add `{version}` to the resource name and pass in the Document Version into `{version}` during generation](https://github.com/OAGi/Score/issues/1715)

#### Browser view and end-user experience

- [Issue #1700 - Enable a modified Core Component Menu item and modified Core Component screen for end user accounts for the all instances including multi-tenant cloud](https://github.com/OAGi/Score/issues/1700)
- [Issue #1699 - Allow for BCCP visibility during Browser View for Dev/Admin accounts](https://github.com/OAGi/Score/issues/1699)

#### BIE state transition

- [Issue #1708 - Moving Reuse BIE to Production state causes BIEs to also automatically move to Production](https://github.com/OAGi/Score/issues/1708)

#### Additional fixes

- [Issue #1628 - Can't cancel recent changes made to a BDT](https://github.com/OAGi/Score/issues/1628)

### Platform updates

- Spring Boot upgraded to 3.5.13
- Angular upgraded to 20.3.18
- MariaDB upgraded to 11.8.6
- Redis upgraded to 7.4.8

### Upgrade note

Before proceeding with the upgrade, we strongly recommend **backing up your data**. Starting
with version 3.5.0, database version management is handled by Flyway. As a result, users no
longer need to manually apply a database migration SQL script during upgrade.

### Docker environment

If you are running connectCenter in a Docker environment, update the following image tags:

- `srt-web` to `3.5.0`
- `srt-http-gateway` to `3.5.0`
- `srt-repo` to `3.5.0`
- `redis` to `7.4.8`

See [Installation with Docker](../02-getting-started/installation-docker.md) for an example
`docker-compose.yml`.

:::note[Database changes]
Any release — including a PATCH — may include database schema changes, applied automatically on
startup via Flyway. See [Release Numbering](./release-numbering.md) and
[Database Migration](../05-operations/database-migration.md).
:::
