---
title: Upgrading connectCenter
sidebar_position: 2
---

Upgrading a Docker-based connectCenter deployment is simple: **back up, change the image tag,
restart.** The backend applies any database schema changes automatically on startup via
**Flyway** — you do not apply anything to the database yourself.

:::warning[Always back up first]
An upgrade changes your database schema in place. **Take a full backup before you start** and
verify it can be restored — it is your only way back if something goes wrong. See
[Backup and Restore](./backup-restore.md).
:::

## Upgrade steps

1. **Back up** your database — see [Backup and Restore](./backup-restore.md).

2. **Change the image tags** in your `docker-compose.yml` to the target version. The three
   `oagi1docker/srt-*` images are released together, so set all three to the **same** tag:

   ```yaml
   services:
     frontend:
       image: oagi1docker/srt-web:3.6.0
     backend:
       image: oagi1docker/srt-http-gateway:3.6.0
     db:
       image: oagi1docker/srt-repo:3.6.0
   ```

   Leave the `redis` image and your named volumes unchanged so the new containers attach to your
   existing data.

3. **Pull and restart** (use the Docker Compose v2 plugin — `docker compose`, two words):

   ```bash
   docker compose pull
   docker compose up -d
   ```

   On startup the backend runs its bundled Flyway migrations and brings the schema up to date.
   There is nothing else to run.

4. **Verify.** Tail the backend logs for a clean start, then sign in and confirm your releases,
   code lists, and BIE packages are present and that a representative export (XML / JSON / OpenAPI)
   still generates.

   ```bash
   docker compose logs -f backend
   ```

## Upgrading to 3.6.x

Version 3.6.0 is a platform release, so read these notes before upgrading from 3.5.x:

- **Database.** The `V3_6_0` Flyway migration is applied automatically on first start. It adds the
  passkey tables, lookup indexes on the default-primitive tables, a faster `levenshtein`
  function, and some missing column comments. It needs no manual step.
- **Runtime.** The backend now runs on Java 21, Spring Boot 4.1 and Tomcat 11 inside the
  `srt-http-gateway` image, and the front end is built on Angular 22. Docker deployments need no
  change; a deployment that runs the WAR on its own Tomcat must move to **Tomcat 11 / Java 21**.
- **Everyone is signed out.** The backend clears Redis on startup, so after any deployment users
  have to sign in again.
- **CORS.** Credentialed cross-origin access is now an allow-list. Same-host deployments need
  nothing; otherwise set `SCORE_CORS_ALLOWED_ORIGINS` — see
  [Cross-origin requests](../02-getting-started/deployment.md#cross-origin-requests-cors).
- **Record cache.** The core-component record cache now defaults to an in-JVM Caffeine cache
  instead of Redis (`spring.cache.type`). Leave it unless you have a reason to share it through
  Redis.
- **Optional.** [Passkey sign-in](../02-getting-started/passkey-setup.md) stays off until you set
  its environment variables.

## Rollback

If the upgrade fails, restore the database from the backup you took in step 1, revert the image
tags in `docker-compose.yml` to the previous version, and run `docker compose up -d`.

## Related

- [Backup and Restore](./backup-restore.md)
- [Deployment](../02-getting-started/deployment.md)
