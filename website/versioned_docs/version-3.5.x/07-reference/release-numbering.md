---
title: Release Numbering
sidebar_position: 2
---

connectCenter follows [Semantic Versioning](https://semver.org/): versions are
`MAJOR.MINOR.PATCH` (for example `3.5.0`), published as Git tags (`v3.5.0`).

- **MAJOR** — backward-incompatible changes.
- **MINOR** — backward-compatible new features.
- **PATCH** — backward-compatible fixes.

:::note[Any release may change the database]
A release at **any** level — including a PATCH — may include database schema changes. The
`score-http` backend applies them automatically on startup via Flyway, so always run the matching
backend image when you upgrade. See [Database Migration](../05-operations/database-migration.md).
:::

The version is declared in `score-web/package.json` and supplied to the backend build through the
Maven `revision` property in `score-http/pom.xml`.

## See also

- [What's New](./whats-new.md) — highlights of recent versions.
- [GitHub releases](https://github.com/OAGi/Score/releases) — full release notes.
