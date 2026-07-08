---
title: Database Migration
sidebar_position: 3
---

:::warning[Most installations can skip this page]
As of **3.5.0** the `score-http` backend runs schema migrations **automatically on
startup** via bundled **Flyway** scripts
(`score-http/src/main/resources/db/migration/V*.sql`; `spring.flyway.enabled`
defaults to `true`, baselined at `3.4.0`). If your installation is already on
**3.5.0 or later**, upgrading is just pulling the new image tags — you do **not**
need anything on this page.

This page only applies if you are migrating a **pre-3.5.0** schema up to 3.5.0,
applying a migration step manually, or running with `FLYWAY_ENABLED=false`.
:::

connectCenter ships its database schema and seed data inside the
`oagi1docker/srt-repo` image (the `db` service in the
[Docker installation](../02-getting-started/installation-docker.md) compose
file). The image initializes the schema **only the first time the data volume
is empty** — changing the image tag on an existing volume does **not**
retroactively change the schema. To move an existing installation from one
release to the next, you apply a **migration script**: a hand-written SQL file
of DDL/DML statements that brings the schema and seed data up to the new
release. Everything below describes that manual path.

:::note[Former branding]
The `srt-*` image names (`srt-repo`, `srt-web`, `srt-http-gateway`) and the
"Score" name are the former branding of connectCenter. The migration script
headers still read `Migration script for Score v<x.y.z>`.
:::

## Where the scripts live

The migration scripts are versioned in the repository under
`score-repo/scripts`. That directory also holds a `score-schema.sql` dump, but
note that the seed actually baked into the image is the large `oagis.sql`
dump under `score-repo/docker/` (see [How and when initialization
runs](#how-and-when-initialization-runs)), not `score-schema.sql`.

```bash
ls score-repo/scripts/
```

The migration chain (one file per release boundary) is:

| File | Brings schema to |
| --- | --- |
| `mig_2.4.0_to_3.0.0.sql` | 3.0.0 |
| `mig_3.0.4_to_3.1.0.sql` | 3.1.0 |
| `mig_3.1.0_to_3.2.0.sql` | 3.2.0 |
| `mig_3.2.0_to_3.3.0.sql` | 3.3.0 |
| `mig_3.3.0_to_3.4.0.sql` | 3.4.0 |
| `mig_3.4.0_to_3.4.1.sql` | 3.4.1 |
| `mig_3.4.1_to_3.4.2.sql` | 3.4.2 |
| `mig_3.4.2_to_3.5.0.sql` | 3.5.0 |

There is also a variant, `mig_3.3.0_to_3.4.0_for_adapt.sql`, used when the
target installation carries the AgGateway **ADAPT Standard** library in
addition to the default content. Apply the `_for_adapt` variant *instead of*
the plain `mig_3.3.0_to_3.4.0.sql` for those installations — not both.

:::warning[Chain gaps are intentional, but mind the boundaries]
The version on the left of a filename is the *minimum* schema the script
expects. For example `mig_3.0.4_to_3.1.0.sql` assumes the schema is already at
the 3.0.x line before you run it. Apply scripts strictly in order; do not skip
a release.
:::

The same directory also contains large scripts that are **not** part of the
schema-migration chain and should not be run as upgrades:

- `score-schema.sql` — a schema dump kept in `score-repo/scripts`. It is **not**
  the seed used to build the image, and it does not always carry the very
  latest columns/tables (for example, as of 3.5.0 it lacks `xbt.jbt_202012_map`,
  `xbt.openapi31_map`, and the `asbiep_support_doc` table). The current,
  fully-migrated seed is `oagis.sql`; see below.
- `migrate_adapt_standard.sql`, `migrate_cheme_standards.sql` — bulk loads of
  the ADAPT and ChemE standard libraries.

## How and when initialization runs

The `srt-repo` image is built from `score-repo/docker/Dockerfile`, which is
based on `mariadb:11.8.6` (MariaDB, MySQL-compatible) and copies the
`oagis.sql` seed dump (the current, fully-migrated schema plus seed content)
into the MariaDB init hook (password values shown as placeholders):

```dockerfile
FROM --platform=linux/amd64 mariadb:11.8.6
ENV MYSQL_DATABASE=oagi
ENV MYSQL_USER=oagi
ENV MYSQL_PASSWORD=<your-db-password>
ENV MYSQL_ROOT_PASSWORD=<your-root-password>
ADD oagis.sql /docker-entrypoint-initdb.d/oagis.sql
```

Because `oagis.sql` is placed in `/docker-entrypoint-initdb.d/`, the standard
MariaDB entrypoint runs it **once, only when the data directory
(`/var/lib/mysql`) is empty** — i.e. on the very first start of a fresh
`db-volume`. The database name is `oagi` and the application user is `oagi`;
the passwords are whatever your deployment sets (do not rely on the image's
built-in example values).

This is exactly why migration scripts exist: on an upgrade you keep your
existing `db-volume` (so your data is preserved), which means the init hook
does **not** re-run, so the new schema changes are never applied
automatically. You apply them by hand with the migration script for that
release boundary.

:::tip[Two ways to land a new schema]
- **Fresh install** — start with an empty `db-volume`; the image's
  `oagis.sql` seeds the current schema for you. No migration script needed.
- **In-place upgrade** — keep the existing `db-volume`; pull the new image tag,
  then run the migration script(s) for the boundary you are crossing.
:::

## Applying a script against the running container

The `db` service in the production compose file publishes the container's
MariaDB port `3306` on the same host port and stores data in the `db-volume`:

```yaml
db:
  image: oagi1docker/srt-repo:3.5.2
  ports:
    - 3306:3306
  volumes:
    - db-volume:/var/lib/mysql
```

With the stack running, the most reliable way to apply a script is to pipe it
into the client *inside* the container so you do not depend on a host-installed
MySQL/MariaDB client. From the repository root (use the modern `docker compose`
v2 plugin):

```bash
# Identify the db container (service name is "db")
docker compose ps db

# Apply a single migration script to the `oagi` database
docker compose exec -T db sh -c \
  'mariadb --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" "$MYSQL_DATABASE"' \
  < score-repo/scripts/mig_3.4.2_to_3.5.0.sql
```

The single quotes matter: they make the `$MYSQL_*` variables expand *inside*
the container — which already knows its own credentials — so no password
appears on your command line or in your shell history.

If you prefer `docker` directly, resolve the container id first:

```bash
docker exec -i $(docker compose ps -q db) sh -c \
  'mariadb --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" "$MYSQL_DATABASE"' \
  < score-repo/scripts/mig_3.4.2_to_3.5.0.sql
```

To connect interactively from the host (a MariaDB/MySQL client must be
installed locally), use the mapped port. `--password` without a value prompts
for the password:

```bash
mariadb --host=127.0.0.1 --port=3306 --user=oagi --password "oagi"
```

:::warning[Back up before you migrate]
Migration scripts run DDL such as `ALTER TABLE`, `DROP TABLE IF EXISTS`, and
data-rewriting `UPDATE`s, several of them wrapped in
`SET FOREIGN_KEY_CHECKS = 0; ... SET FOREIGN_KEY_CHECKS = 1;`. Take a backup
of the `db` container before running any script. See
[Backup & Restore](backup-restore.md).
:::

## What a migration script actually does

The scripts are plain, vendor-specific SQL — readable and reviewable before
you run them. As a representative example, `mig_3.4.2_to_3.5.0.sql` (the
3.5.0 boundary) performs changes such as:

- Tagging the `Data Area` ASCCP with `type = 'DataArea'` (issue #1700).
- Inserting `INSERT IGNORE` rows into the `configuration` table for new
  feature toggles and default BIE schema filename expressions
  (issues #1700, #1711).
- Adding JSON Schema 2020-12 and OpenAPI 3.1.1 mapping columns
  (`jbt_202012_map`, `openapi31_map`) to the `xbt` table and back-filling
  their values (issue #1703).

Smaller boundary scripts are correspondingly smaller. For instance
`mig_3.4.0_to_3.4.1.sql` only adds `library.is_default` and a `name` column
(plus a unique key) to `bie_package`, while `mig_3.4.1_to_3.4.2.sql` adds the
`asbiep_support_doc` table and package-versioning columns.

:::note[`INSERT IGNORE` and idempotency]
Several seed-data inserts use `INSERT IGNORE`, and some column additions in
the 3.5.0 script use `ADD COLUMN IF NOT EXISTS`, so re-running those specific
statements is generally safe. Plain `ALTER TABLE ... ADD COLUMN` and
`DROP TABLE IF EXISTS` statements are **not** guarded the same way — do not
assume a whole script is idempotent. Apply each boundary script exactly once.
:::

## Related

- [Docker installation](../02-getting-started/installation-docker.md) — the
  compose stack the `db` service lives in.
- [Backup & Restore](backup-restore.md) — take a backup before migrating.
- [Upgrade](upgrade.md) — the end-to-end image-tag upgrade procedure.
- [Database structure](../06-contributing/architecture.md#database-structure) —
  schema and table overview.
