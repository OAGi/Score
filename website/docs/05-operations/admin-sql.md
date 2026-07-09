---
title: Administrative SQL
sidebar_position: 4
---

This page collects low-level SQL recipes for administering a connectCenter database directly. connectCenter was formerly named **Score**, and the database image (`oagi1docker/srt-repo`) and table names still carry the legacy `srt-`/Score branding.

These recipes were verified against the **effective 3.5.2 schema** — the base schema (`score-repo/scripts/score-schema.sql`) *plus* the Flyway migrations the backend applies automatically at startup (`score-http/src/main/resources/db/migration/`, up to `V3_5_2`). The base schema file alone does not include the 3.5.x additions, so always think in terms of both. The recipes were also cross-checked against what the application itself deletes when it discards a BIE, so they match — and in a few places exceed — the application's own cleanup.

:::danger[Advanced, irreversible, and unsupported]
Everything on this page bypasses the application layer and its integrity rules.

- **There is no undo.** A `DELETE` against the database is permanent.
- **Take a full backup first.** See [Backup &amp; Restore](./backup-restore.md) before running anything here.
- **Foreign-key checks may be disabled** by these scripts. That removes the database's last safety net against orphaned or inconsistent rows.
- **Schema drifts across releases.** Table and column names below are correct for 3.5.2. Confirm them against the schema for *your* release before running anything, especially if you are not on 3.5.x.
- Prefer doing the work through the connectCenter web UI whenever an equivalent operation exists there. Use raw SQL only as a last resort.

Run these only on a database you can afford to lose, or after a verified backup that you have tested restoring.
:::

## Before you start

1. **Back up.** Take and verify a dump (see [Backup &amp; Restore](./backup-restore.md)).
2. **Work off-hours.** Stop or quiesce the backend (`oagi1docker/srt-http-gateway`) so no requests mutate rows mid-operation.
3. **Wrap in a transaction where possible** so you can `ROLLBACK` if a row count looks wrong. Note that `SET FOREIGN_KEY_CHECKS` is a session setting and that some MySQL/MariaDB DDL is non-transactional, so a transaction is a safety aid, not a guarantee.
4. **Test the `WHERE` clause with a `SELECT` first.** Always confirm exactly which rows you are about to delete.

For how to open a SQL shell against the running database container, see [Backup &amp; Restore](./backup-restore.md).

## Purge a top-level BIE

### When you actually need this

A top-level BIE moves through the states **WIP → QA → Production** (there is no "Published" state). Before reaching for SQL, know what the application can already do:

- The **owner** can discard their own BIE while it is in **WIP**.
- An **administrator** can discard a BIE in **any state — including QA and Production** — regardless of who owns it. State alone is never a reason to use SQL.
- The application refuses to discard a BIE that is **used as a request/response message body in an OpenAPI document**. Remove it from the OpenAPI document first (or see [OpenAPI bindings](#3-clear-openapi-bindings-if-any) below).
- If the BIE is **reused by** other BIEs, or is the **base of** an inherited BIE, the discard dialog asks you to include those dependent BIEs in the discard.

Raw SQL is therefore a last resort — for when no administrator account is available, the application is down, or a guard cannot be satisfied any other way.

### 1. Find the top-level BIE id

Every top-level BIE is a row in `top_level_asbiep`, identified by `top_level_asbiep_id`. When you open a top-level BIE in the connectCenter web UI, this id is the path segment immediately after `/profile_bie/` in the URL.

Confirm the row before deleting:

```sql
SELECT top_level_asbiep_id, release_id, version, state, status, owner_user_id
FROM `top_level_asbiep`
WHERE top_level_asbiep_id IN (?);
```

Replace `?` with the id (or a comma-separated list of ids) you intend to purge, and verify the `version`/`state` match the BIE you mean to remove.

### 2. Pre-flight: check what else points at this BIE

A top-level BIE can be referenced from outside its own tree. Some of these references make a purge unsafe; all of them should be inspected first.

```sql
-- (a) Is this BIE REUSED by other BIEs? Their ASBIE rows point directly
--     into this BIE's ASBIEP rows.
SELECT DISTINCT `owner_top_level_asbiep_id` AS reusing_bie
FROM `asbie`
WHERE `to_asbiep_id` IN
      (SELECT `asbiep_id` FROM `asbiep` WHERE `owner_top_level_asbiep_id` IN (?))
  AND `owner_top_level_asbiep_id` NOT IN (?);

-- (b) Is this BIE the source (Copy/Uplift) or base (inheritance) of other BIEs?
SELECT `top_level_asbiep_id`, `source_top_level_asbiep_id`, `based_top_level_asbiep_id`
FROM `top_level_asbiep`
WHERE `source_top_level_asbiep_id` IN (?) OR `based_top_level_asbiep_id` IN (?);

-- (c) Is this BIE bound to any OpenAPI document?
SELECT * FROM `oas_message_body` WHERE `top_level_asbiep_id` IN (?);
SELECT * FROM `oas_request`
  WHERE `meta_header_top_level_asbiep_id` IN (?) OR `pagination_top_level_asbiep_id` IN (?);
SELECT * FROM `oas_response`
  WHERE `meta_header_top_level_asbiep_id` IN (?) OR `pagination_top_level_asbiep_id` IN (?);
-- New in 3.5.2: an operation's error-response ConfirmMessage body (issue #1347)
SELECT * FROM `oas_operation` WHERE `error_confirm_top_level_asbiep_id` IN (?);

-- (d) Is this BIE a member of a BIE package, or the previous version of one?
SELECT * FROM `bie_package_top_level_asbiep`
  WHERE `top_level_asbiep_id` IN (?) OR `prev_top_level_asbiep_id` IN (?);
```

How to act on the results:

- **(a) Reuse rows found — stop.** Purging this BIE would leave the reusing BIEs' `asbie.to_asbiep_id` pointing at deleted rows, silently corrupting them (the deletes below run with `FOREIGN_KEY_CHECKS = 0`). Remove the reuse in the UI, or purge the reusing BIEs in the same operation by adding their ids to every statement.
- **(b) Copies and inherited BIEs** are handled by the `UPDATE` statements in [step 4](#detach-surviving-rows-that-still-point-at-the-purged-id) — the surviving BIEs keep working, they just lose their lineage link. Note that `based_top_level_asbiep_id` has **no enforced foreign key and no index** in the 3.5.2 schema, so nothing in the database will catch it if you skip step 4.
- **(c) OpenAPI bindings** should be removed through the OpenAPI document screens if at all possible. If not, see [step 3](#3-clear-openapi-bindings-if-any).
- **(d) BIE package rows** are deleted in step 4; the `prev_top_level_asbiep_id` version-chain column is detached in the step-4 detach block.

### 3. Clear OpenAPI bindings (if any)

Skip this step if the pre-flight query **(c)** returned nothing. Otherwise, prefer removing the BIE from the OpenAPI document in the UI. As a last resort, delete the binding rows bottom-up (each table here has an enforced foreign key to the next):

```sql
-- Request side
DELETE FROM `oas_request_parameter` WHERE `oas_request_id` IN
  (SELECT `oas_request_id` FROM `oas_request` WHERE `oas_message_body_id` IN
    (SELECT `oas_message_body_id` FROM `oas_message_body` WHERE `top_level_asbiep_id` IN (?)));
DELETE FROM `oas_request` WHERE `oas_message_body_id` IN
  (SELECT `oas_message_body_id` FROM `oas_message_body` WHERE `top_level_asbiep_id` IN (?));

-- Response side (oas_parameter_link and oas_response_headers hang off oas_response)
DELETE FROM `oas_parameter_link` WHERE `oas_response_id` IN
  (SELECT `oas_response_id` FROM `oas_response` WHERE `oas_message_body_id` IN
    (SELECT `oas_message_body_id` FROM `oas_message_body` WHERE `top_level_asbiep_id` IN (?)));
DELETE FROM `oas_response_headers` WHERE `oas_response_id` IN
  (SELECT `oas_response_id` FROM `oas_response` WHERE `oas_message_body_id` IN
    (SELECT `oas_message_body_id` FROM `oas_message_body` WHERE `top_level_asbiep_id` IN (?)));
DELETE FROM `oas_response` WHERE `oas_message_body_id` IN
  (SELECT `oas_message_body_id` FROM `oas_message_body` WHERE `top_level_asbiep_id` IN (?));

-- The message-body rows themselves
DELETE FROM `oas_message_body` WHERE `top_level_asbiep_id` IN (?);

-- Detach meta-header / pagination / error-ConfirmMessage references
UPDATE `oas_request`  SET `meta_header_top_level_asbiep_id` = NULL WHERE `meta_header_top_level_asbiep_id` IN (?);
UPDATE `oas_request`  SET `pagination_top_level_asbiep_id`  = NULL WHERE `pagination_top_level_asbiep_id`  IN (?);
UPDATE `oas_response` SET `meta_header_top_level_asbiep_id` = NULL WHERE `meta_header_top_level_asbiep_id` IN (?);
UPDATE `oas_response` SET `pagination_top_level_asbiep_id`  = NULL WHERE `pagination_top_level_asbiep_id`  IN (?);
UPDATE `oas_operation`
   SET `error_confirm_top_level_asbiep_id` = NULL, `error_response_body_type` = 'NONE'
 WHERE `error_confirm_top_level_asbiep_id` IN (?);
```

This removes the affected request/response definitions from their OpenAPI documents. Only do it if you understand the effect on those documents.

### 4. Delete the rows owned by the BIE

The statements below delete everything the purged BIE owns. **Order matters**: the first block resolves node ids through the BIE tree tables (`asbie`, `bbie`, `asbiep`, …), so it must run *before* those tables are emptied — disabling foreign-key checks does not change that.

```sql
SET FOREIGN_KEY_CHECKS = 0;

-- Rows hanging off this BIE's tree nodes (must run first — the subqueries
-- need the tree tables to still be populated)
DELETE FROM `asbie_bizterm`      WHERE `asbie_id`  IN (SELECT `asbie_id`  FROM `asbie`  WHERE `owner_top_level_asbiep_id` IN (?));
DELETE FROM `bbie_bizterm`       WHERE `bbie_id`   IN (SELECT `bbie_id`   FROM `bbie`   WHERE `owner_top_level_asbiep_id` IN (?));
DELETE FROM `asbiep_support_doc` WHERE `asbiep_id` IN (SELECT `asbiep_id` FROM `asbiep` WHERE `owner_top_level_asbiep_id` IN (?));
DELETE FROM `bie_usage_rule`
 WHERE `target_abie_id`   IN (SELECT `abie_id`   FROM `abie`   WHERE `owner_top_level_asbiep_id` IN (?))
    OR `target_asbie_id`  IN (SELECT `asbie_id`  FROM `asbie`  WHERE `owner_top_level_asbiep_id` IN (?))
    OR `target_asbiep_id` IN (SELECT `asbiep_id` FROM `asbiep` WHERE `owner_top_level_asbiep_id` IN (?))
    OR `target_bbie_id`   IN (SELECT `bbie_id`   FROM `bbie`   WHERE `owner_top_level_asbiep_id` IN (?))
    OR `target_bbiep_id`  IN (SELECT `bbiep_id`  FROM `bbiep`  WHERE `owner_top_level_asbiep_id` IN (?));

-- Rows keyed directly by the top-level BIE
DELETE FROM `biz_ctx_assignment`           WHERE `top_level_asbiep_id` IN (?);
DELETE FROM `bie_package_top_level_asbiep` WHERE `top_level_asbiep_id` IN (?);
DELETE FROM `bie_user_ext_revision`        WHERE `top_level_asbiep_id` IN (?);

-- The BIE tree itself
DELETE FROM `abie`    WHERE `owner_top_level_asbiep_id` IN (?);
DELETE FROM `asbie`   WHERE `owner_top_level_asbiep_id` IN (?);
DELETE FROM `asbiep`  WHERE `owner_top_level_asbiep_id` IN (?);
DELETE FROM `bbie`    WHERE `owner_top_level_asbiep_id` IN (?);
DELETE FROM `bbiep`   WHERE `owner_top_level_asbiep_id` IN (?);
DELETE FROM `bbie_sc` WHERE `owner_top_level_asbiep_id` IN (?);

-- The anchor row
DELETE FROM `top_level_asbiep` WHERE `top_level_asbiep_id` IN (?);

SET FOREIGN_KEY_CHECKS = 1;
```

Replace every `?` with the same id (or list of ids) from step 1.

#### Detach surviving rows that still point at the purged id

Other BIEs that were copied/uplifted from, or inherit from, the purged BIE still carry its id. The application's own discard performs the first two of these updates; the third — detaching `bie_package_top_level_asbiep.prev_top_level_asbiep_id` — goes beyond what the application does (the application leaves that column dangling), so running all three leaves the database cleaner than an in-app discard:

```sql
-- Copies / uplifts of the purged BIE lose their source lineage
UPDATE `top_level_asbiep`
   SET `source_top_level_asbiep_id` = NULL,
       `source_action`              = NULL,
       `source_timestamp`           = NULL
 WHERE `source_top_level_asbiep_id` IN (?);

-- Inherited BIEs lose their base reference. There is NO enforced foreign key
-- on this column, so nothing else will catch a dangling value.
UPDATE `top_level_asbiep`
   SET `based_top_level_asbiep_id` = NULL
 WHERE `based_top_level_asbiep_id` IN (?);

-- BIE-package version chains that recorded the purged BIE as a previous version
UPDATE `bie_package_top_level_asbiep`
   SET `prev_top_level_asbiep_id` = NULL
 WHERE `prev_top_level_asbiep_id` IN (?);
```

### 5. Verify

After the purge, spot-check that no rows for the purged id remain:

```sql
SELECT 'top_level_asbiep' AS t, COUNT(*) AS remaining FROM `top_level_asbiep` WHERE `top_level_asbiep_id` IN (?)
UNION ALL SELECT 'abie',    COUNT(*) FROM `abie`    WHERE `owner_top_level_asbiep_id` IN (?)
UNION ALL SELECT 'asbie',   COUNT(*) FROM `asbie`   WHERE `owner_top_level_asbiep_id` IN (?)
UNION ALL SELECT 'asbiep',  COUNT(*) FROM `asbiep`  WHERE `owner_top_level_asbiep_id` IN (?)
UNION ALL SELECT 'bbie',    COUNT(*) FROM `bbie`    WHERE `owner_top_level_asbiep_id` IN (?)
UNION ALL SELECT 'bbiep',   COUNT(*) FROM `bbiep`   WHERE `owner_top_level_asbiep_id` IN (?)
UNION ALL SELECT 'bbie_sc', COUNT(*) FROM `bbie_sc` WHERE `owner_top_level_asbiep_id` IN (?)
UNION ALL SELECT 'reuse (asbie.to_asbiep_id)', COUNT(*) FROM `asbie`
          WHERE `to_asbiep_id` NOT IN (SELECT `asbiep_id` FROM `asbiep`);
```

All counts should be `0`. The last line is a global dangling-reuse check: any non-zero count there means some surviving BIE still points at a deleted ASBIEP.

:::note[Verified against 3.5.2]
Every table and column above exists in the effective 3.5.2 schema, and the recipe is a superset of what the application itself deletes on discard (the application does not clean `asbie_bizterm`, `bbie_bizterm`, or `bie_user_ext_revision`; this recipe does). Tables new in 3.5.x need no cleanup here: `bie_view_order` (sibling ordering, added in 3.5.2) is keyed by Core Component manifests, not by BIEs, and the `oas_operation_security` / `oas_operation_security_scope` tables (added in 3.5.1) hang off OpenAPI operations and security schemes — nothing in them points at a BIE. Confirm the details against your release's schema before running anything.
:::

:::tip[Prefer the UI]
Discarding a top-level BIE through the connectCenter web UI keeps every related table consistent automatically, and an **administrator can discard a BIE in any state** — including Production. Reach for raw SQL only when the UI genuinely cannot perform the action.
:::

## See also

- [Backup &amp; Restore](./backup-restore.md) — take a verified backup before any SQL surgery, and learn how to open a database shell.
- [Database Migration](./database-migration.md) — schema migration scripts (`mig_*` in `score-repo/scripts`) and the Flyway migrations applied since 3.5.0.
- [Upgrade](./upgrade.md) — upgrading a running connectCenter deployment.
