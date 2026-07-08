---
title: Contributor Prerequisites
sidebar_position: 1
---

connectCenter (formerly **Score**) is a multi-module platform for Core Component
Specification (CCS / UN/CEFACT CCTS) library development, BIE profiling, and
schema generation. Contributing effectively means being comfortable with both
the **technology stack** of the module you're touching and the **standards
domain** the product implements.

:::note[connectCenter was formerly "Score"]
connectCenter was previously named **Score**. The module directories
(`score-http`, `score-web`, …), Java packages, and the published container
images (`oagi1docker/srt-web`, `oagi1docker/srt-http-gateway`,
`oagi1docker/srt-repo`) still carry the legacy naming; only the product name
changed.
:::

This page outlines what helps to know before diving in. You don't need mastery
of everything — most contributors specialize in one or two modules — but a
working familiarity with the relevant items below will make the codebase far
easier to navigate.

:::tip
For setting up a working build, see
[Development Setup](./development-setup.md). For how
the modules fit together, see the [Architecture Overview](./architecture.md).
:::

## Technology stack by module

connectCenter is split into several independently built modules. Pick the rows
that match what you intend to work on.

### `score-http` — backend REST API

The core backend, built as a **WAR** with packages under
`org.oagi.score.gateway.http.api.*`.

- **Java 17+** — the project compiles with `maven.compiler.source`/`target` set
  to `17` (see `score-http/pom.xml`). Spring Boot 3.x requires Java 17 as a
  minimum.
- **Spring Boot 3.5.x** — the Maven parent is
  `spring-boot-starter-parent:3.5.15`. Familiarity with Spring MVC, Spring
  Security (OAuth2 client + resource server), Spring Session, and Spring
  WebSocket is useful.
- **jOOQ 3.20.x** — the data-access layer uses jOOQ (not JPA/Hibernate).
  Generated entity classes live under
  `org.oagi.score.gateway.http.common.repository.jooq.entity`. Understanding
  type-safe SQL with jOOQ is essential for backend work.
- **MySQL / MariaDB** — the JDBC URL targets MariaDB
  (`org.mariadb.jdbc.Driver`), and schema migrations are managed with
  **Flyway**. Comfort with relational schema design and SQL administration
  pays off.
- **Redis** — used for session storage and distributed coordination (via
  Lettuce and Redisson, plus Spring Session).
- **Maven** — the build tool for this module.

:::note[Spring AI / MCP]
`score-http` also pulls in the Spring AI BOM and an MCP (Model Context Protocol)
client starter. You only need to understand these if you're working in the
AI-related areas of the backend.
:::

### `score-web` — frontend SPA

The single-page web application.

- **Angular ~21.2** — `score-web/package.json` pins `@angular/core` to
  `~21.2.17`. (The published `score-web` version is `3.5.2`.) Component
  architecture, RxJS, and Angular Material are core to this module.
- **TypeScript ~5.9** — all frontend code is TypeScript.
- **Node.js 22.x** — the `engines` field requires Node `22.x`.
- **RxJS, Angular Material, Bootstrap** — the UI relies on reactive streams and
  the Material component set.
- **Tooling** — ESLint for linting, Vitest for unit tests, and Playwright for
  browser tests.

### `score-external-api` — external REST API

A standalone, externally facing API layer.

- **NestJS 11** — built on `@nestjs/core` `^11`, running on **Node.js** with
  **TypeScript**. Decorators, dependency injection, and NestJS modules are the
  key concepts.
- Tested with **Jest**.

### `connect-center-api` — Python companion service

A FastAPI + FastMCP service.

- **Python 3.11+** — `pyproject.toml` declares `requires-python = ">=3.11"`.
- **FastAPI** and **FastMCP** — the HTTP and MCP surfaces, respectively.
- **SQLAlchemy (async)** with an async MariaDB driver (`asyncmy`), plus
  Pydantic for settings/validation.
- Tested with **pytest**.

### Supporting modules

- **`score-repo`** — SQL schema (`score-schema.sql`) and the `mig_*.sql` migration
  scripts in `score-repo/scripts`. Knowing SQL and the MySQL/MariaDB dialect helps here.
- **`score-e2e`** — Selenium-based end-to-end tests (Java).

## General development skills

Regardless of module, you'll want:

- **Git** — branch from `develop`; `master` is the release branch.
- **Docker / `docker compose`** — the stack ships as a multi-container
  composition (frontend, backend, MySQL/MariaDB, Redis). Use the modern
  `docker compose` (v2 plugin), not the legacy `docker-compose` binary.
- **REST / HTTP, JSON, OAuth2** — the modules communicate over REST and
  authenticate via OAuth2.

## Domain background (CCS / CCTS)

connectCenter implements UN/CEFACT's **Core Component Technical Specification
(CCTS)**, standardized as **ISO 15000-5**. A solid grasp of the domain makes the
data model and UI vocabulary far more intelligible:

- **Core Component Specification / CCTS (ISO 15000-5)** — the conceptual model
  behind ACCs, BCCs, ASCCs, BDTs, the Core Component library, and **Business
  Information Entities (BIEs)** — context-specific profiles derived from core
  components (the "BIE profiling" workflow). This is the single most important
  domain concept for understanding the product. Read the
  [CCTS v3.0](https://www.unece.org/fileadmin/DAM/cefact/codesfortrade/CCTS/CCTS-Version3.pdf)
  specification and the
  [CCTS Data Type Catalogue v3.0](https://unece.org/DAM/cefact/codesfortrade/CCTS/CCTS-DataTypeCatalogueVersion3p0.pdf).

A contributor working on schema generation should additionally understand the
output formats the product produces:

- **XML Schema (XSD)** — XML schema generation is a primary output.
- **JSON Schema** — JSON schema generation, including features like `const`.
- **OpenAPI 3.x** — OpenAPI document generation (operations, security schemes,
  operation IDs).

:::tip[Where to focus]
If you're contributing to the **library/BIE** features, prioritize the CCTS
domain background. If you're contributing to **export/schema generation**,
prioritize XML Schema, JSON Schema, and OpenAPI. If you're contributing to
**infrastructure or APIs**, prioritize the technology stack of the relevant
module.
:::

## Next steps

- [Development Setup](./development-setup.md) — set
  up a local build, run the stack, and follow the contributor workflow.
- [Architecture Overview](./architecture.md) — how `score-http`, `score-web`,
  `score-external-api`, `connect-center-api`, and the supporting modules relate.
