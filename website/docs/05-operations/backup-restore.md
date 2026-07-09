---
title: Backup and Restore
sidebar_position: 1
---

connectCenter stores all of its persistent application data — libraries, core components, BIEs, code lists, accounts, releases, and so on — in a single relational database. Backing up and restoring connectCenter therefore comes down to backing up and restoring that database. This page describes how to do that for the standard Docker Compose deployment.

## What to back up

| Data | Where it lives | Back up? |
|---|---|---|
| Application database (`oagi` schema) | `db` service, named volume `db-volume` at `/var/lib/mysql` | **Yes — this is the source of truth** |
| Redis cache / session store | `redis` service, named volume `redis-volume` at `/data` | Usually not — see below |

The `db` service runs a MariaDB-based image (`oagi1docker/srt-repo:3.5.2`, built on `mariadb:11.8.6`). The connectCenter backend connects to it over JDBC using a MariaDB driver, against a database named `oagi` by default.

:::tip[Redis is a cache, not the source of truth]
The `redis` service (`redis:7.4`) is used for caching and session state. It can be rebuilt from the application database and from user re-authentication, so it normally does **not** need to be backed up. Restoring only the `db` data is sufficient to recover a connectCenter instance. Expect users to be logged out after a restore.
:::

## Credentials are environment-specific

The commands below use the database name `oagi` and the application user `oagi`. The `db` service takes its credentials from environment variables:

```text
MYSQL_DATABASE=oagi
MYSQL_USER=oagi
MYSQL_PASSWORD=<your-db-password>         # placeholder — set your own value
MYSQL_ROOT_PASSWORD=<your-root-password>  # placeholder — set your own value
```

:::warning[Do not assume the default password]
The image ships with weak built-in **example** credentials. Production and shared deployments should override these (for example via environment variables or a secrets mechanism), and backups **must** use the credentials that the running database actually uses. Substitute your real database name, user, and password in every command below.
:::

## A note on `docker compose`

Use the modern Docker Compose v2 plugin syntax — `docker compose` (two words), not the legacy `docker-compose` binary. All examples assume you run them from the directory that contains the production `docker-compose.yml`.

:::note[Obsolete `version:` key]
The bundled `docker-compose.yml` still carries a top-level `version: "3.0"` key. Under the current Compose Specification this key is obsolete and ignored; the file still works, and Compose v2 may print a deprecation warning. You can safely leave it or remove it.
:::

See [Docker Deployment](../02-getting-started/deployment.md) and [Install with Docker](../02-getting-started/installation-docker.md) for the full stack layout and host port mappings.

## Take a consistent backup

The cleanest backup is one taken while nothing is writing to the database. The safest approach is to keep the `db` service running but stop the application tiers so no new writes arrive, then dump the database.

### 1. Stop writes to the database

Stop the frontend and backend so users cannot modify data while the dump runs, but leave `db` up so you can dump it:

```bash
docker compose stop frontend backend
```

:::warning[Consistent dumps]
Taking a dump while connectCenter is actively writing can capture a half-finished transaction (for example, a BIE save in progress) and produce an inconsistent backup. The simplest guarantee of consistency is to stop the application tiers first, as shown above. If you cannot afford downtime, use a transaction-consistent dump instead (see [Online / hot backups](#online--hot-backups)).
:::

### 2. Dump the database with `mysqldump`

The `db` service publishes MariaDB on host port `3306`, so you can run `mysqldump` directly from the host with any MySQL/MariaDB client and write the dump to a local file:

```bash
mysqldump --host=localhost --port=3306 --protocol=tcp \
  --default-character-set=utf8 \
  --user=oagi --password \
  --routines --column-statistics=0 --skip-triggers \
  "oagi" > score_backup.sql
```

Notes on this command:

- `--user=oagi` is the database user; `--password` **without a value** makes the tool prompt for the password interactively. Do not put the password on the command line (`--password=...` or `-p<value>`) — it lands in your shell history and can appear in process listings.
- `--routines` includes stored procedures and functions in the dump; `--skip-triggers` leaves trigger definitions out.
- `--column-statistics=0` is **important when the client is MySQL 8's `mysqldump` and the server is MariaDB** — which the `db` service is. Without it the dump aborts, because MariaDB has no `information_schema.COLUMN_STATISTICS` table. MariaDB's own client (`mariadb-dump`) does not have — or need — this option; drop the flag there.
- The final `"oagi"` is the **database name** to dump. Replace it with your real schema name.

:::tip[Compress large dumps]
A full connectCenter database can be large. Pipe through `gzip` to save space:

```bash
mysqldump --host=localhost --port=3306 --protocol=tcp \
  --default-character-set=utf8 \
  --user=oagi --password \
  --routines --column-statistics=0 --skip-triggers \
  "oagi" | gzip > score_backup.sql.gz
```
:::

If no MySQL/MariaDB client is installed on the host, run MariaDB's dump tool *inside* the `db` container instead. The container already knows its own credentials through its environment, so no secret appears on your command line:

```bash
docker compose exec -T db sh -c \
  'mariadb-dump --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" \
     --default-character-set=utf8 --routines --skip-triggers \
     "$MYSQL_DATABASE"' > score_backup.sql
```

`-T` disables pseudo-TTY allocation so the redirected output is not corrupted, and the single quotes ensure the `$MYSQL_*` variables are expanded inside the container, not by your host shell. For unattended (cron) host-side backups, keep the password in a client option file readable only by the backup user (`--defaults-extra-file=...`, mode `600`) rather than on the command line.

### 3. Restart connectCenter

Bring the application tiers back up once the dump completes:

```bash
docker compose start frontend backend
```

## Restore from a backup

Restoring loads a previously created dump back into the running `db` service.

:::warning[Restoring overwrites existing data]
Importing a dump replaces the contents of the matching tables. Restore into a clean/empty database, or into one you intend to overwrite. Take a fresh backup before restoring over a database you might still need.
:::

### 1. Stop the application tiers

As with backup, stop the frontend and backend so nothing reads or writes while the import runs:

```bash
docker compose stop frontend backend
```

### 2. Load the dump

Feed the dump file into the `mysql` client against the published port. The redirection (`<`) reads the file on the host; the interactive password prompt still works because it reads from your terminal, not from standard input:

```bash
mysql --host=localhost --port=3306 --protocol=tcp \
  --default-character-set=utf8 \
  --user=oagi --password \
  "oagi" < score_backup.sql
```

The trailing `"oagi"` is the target database name.

If your backup was gzip-compressed, decompress it on the way in:

```bash
gunzip -c score_backup.sql.gz | \
  mysql --host=localhost --port=3306 --protocol=tcp \
    --default-character-set=utf8 --user=oagi --password "oagi"
```

Without a host client, stream the file into the container the same way as the backup:

```bash
docker compose exec -T db sh -c \
  'mariadb --user="$MYSQL_USER" --password="$MYSQL_PASSWORD" "$MYSQL_DATABASE"' \
  < score_backup.sql
```

The `-T` flag is important here: it disables the pseudo-TTY so the file is streamed correctly to the client's standard input.

### 3. Restart connectCenter

```bash
docker compose start frontend backend
```

After the backend reconnects, the application is available again. Because Redis was not restored, cached/session state is rebuilt on demand and users will need to sign in again.

:::tip[Match the schema version]
A dump is tied to the database schema version it was taken from. Restore it into a `db` image of the matching version whenever you can. If the dump is older than the application, there is normally nothing to run by hand: since **3.5.0** the backend applies its bundled **Flyway** migrations automatically on startup and brings the restored schema up to date. See [Database Migration](./database-migration.md) and [Upgrading connectCenter](./upgrade.md).
:::

## Online / hot backups

If you cannot stop the application tiers, take a transaction-consistent dump instead. For the InnoDB tables that connectCenter uses, `--single-transaction` produces a consistent snapshot without locking out writers:

```bash
mysqldump --host=localhost --port=3306 --protocol=tcp \
  --default-character-set=utf8 \
  --user=oagi --password \
  --single-transaction --routines --column-statistics=0 --skip-triggers \
  "oagi" > score_backup.sql
```

:::warning[Hot backups are best-effort]
`--single-transaction` gives a consistent snapshot only for transactional (InnoDB) tables, and any DDL (schema-changing) operations performed during the dump can still break consistency. For the most reliable backup — for example before an upgrade — prefer the **stop-the-application-tiers** procedure above so the database is quiescent.
:::

## Backing up the raw volume (alternative)

Instead of a logical SQL dump, you can archive the database files directly from the `db-volume` named volume. The database **must be stopped** for a file-level copy to be consistent:

```bash
# Stop the database so its files are at rest
docker compose stop db

# Archive the db-volume contents to a tar file on the host
docker run --rm \
  -v "$(docker volume inspect --format '{{ .Name }}' "$(basename "$PWD")_db-volume")":/var/lib/mysql \
  -v "$PWD":/backup \
  alpine tar czf /backup/db-volume.tar.gz -C /var/lib/mysql .

# Restart
docker compose start db
```

:::note[Volume naming]
Compose prefixes named volumes with the project name (by default the directory name), so the volume is typically `<project>_db-volume`. Confirm the exact name with `docker volume ls`. A logical `mysqldump` (above) is generally more portable across image/schema versions than a raw file copy, which must be restored into the same database engine version.
:::

## Verifying a backup

After taking a backup, confirm the dump is non-empty and looks well-formed before relying on it:

```bash
ls -lh score_backup.sql            # non-zero size
tail -n 1 score_backup.sql         # mysqldump ends with a "Dump completed" comment
```

For critical data, periodically perform a **test restore** into a throwaway database or a separate Compose project and verify connectCenter starts and the data is present. A backup is only as good as your last successful restore.

## Related pages

- [Docker Deployment](../02-getting-started/deployment.md) — services, volumes, and host port mappings
- [Install with Docker](../02-getting-started/installation-docker.md) — bringing the stack up
- [Database Migration](./database-migration.md) — migration scripts and the upgrade chain
- [Upgrading connectCenter](./upgrade.md) — version upgrade procedure
