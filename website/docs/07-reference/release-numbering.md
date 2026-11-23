---
title: Release Numbering
sidebar_position: 2
---

connectCenter releases are numbered with up to four fields separated by dots: `MAJOR.MINOR.PATCH`
(for example `3.6.1`), and `MAJOR.MINOR.PATCH.HOTFIX` for a hotfix. Each release is published as a
Git tag (`v3.6.1`). This page explains what the fields mean and what a release contains; the same
scheme is described on the
[Score Release Numbering Scheme wiki page](https://github.com/OAGi/Score/wiki/Score-Release-Numbering-Scheme).

## The fields

The fields describe the **impact of the functional change**. Whether a release changes the
database does not decide which field increases: any release can include a database change (see
[Database changes](#database-changes)).

| Field | Name | Changes when |
|---|---|---|
| 1st | Major | A change that fundamentally alters the structure of the application, in its logic or its database schema. It may contain backward incompatibility (that is, some data may be lost). |
| 2nd | Minor | A new capability is introduced, rather than an improvement of an existing one. |
| 3rd | Patch | Bug fixes and improvements of existing features. If this field is not present, assume `0`. |
| 4th | Hotfix | An urgent fix of a high-priority bug that cannot wait for the next release. Hotfixes are created only against the latest release while the next one is being developed. |

## What a release provides

Every release is published with the following Docker images, all tagged with the release number:

| Image | Content |
|---|---|
| `oagi1docker/srt-web` | The frontend. |
| `oagi1docker/srt-http-gateway` | The backend. |
| `oagi1docker/srt-repo` | The MariaDB database with the initial data, used for new installations. |

The `redis` image is not built by OAGi. The release notes state which `redis` tag to use.

The [`external-api` module](https://github.com/OAGi/Score/wiki/How-to-set-up-the-external%E2%80%90api-module-in-docker%E2%80%90compose.yaml) has its own version number,
which does not follow the release number.

Which tags to change in your `docker-compose.yml` is listed in the **Docker environment** section
of each release in the [Release Detail wiki](https://github.com/OAGi/Score/wiki/Release-Detail)
and in [What's New](./whats-new.md). A release may mark the `srt-repo` tag as optional when its
database content and schema are unchanged.

## Database changes

| Releases | How database changes are applied |
|---|---|
| **3.5.0 and later** | A database change can be part of any release, including a patch release. Database version management is handled by [Flyway](https://flywaydb.org/). On first startup, the backend automatically applies the migrations it needs, so you do not need to run a SQL script. An existing installation keeps using its database; the `srt-repo` image matters for new installations. Back up your data before upgrading. See [Database Migration](../05-operations/database-migration.md). |
| **Before 3.5.0** | The release notes provide a database migration script or data migration tool when a release changes the database. Run it as described there before starting the new release. |

## Older releases

Releases before 3.5.0 followed a stricter rule, where the kind of change decided which images were
published:

- A change of the first or second number always came with a new database image, a new application
  image, and a data migration script.
- A change of the third number provided only a new application image, and no database image or
  data migration script.
- A hotfix (fourth number) that changed the database was suffixed with `d` and came with a data
  migration script.

The version is declared in `score-web/package.json` and supplied to the backend build through the
Maven `revision` property in `score-http/pom.xml`.

## See also

- [What's New](./whats-new.md) — highlights of recent versions.
- [GitHub releases](https://github.com/OAGi/Score/releases) — full release notes.
