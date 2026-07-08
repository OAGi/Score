---
title: Installation with Docker
sidebar_position: 1
---

This is the production install guide for connectCenter using Docker Compose. It uses the
standard Compose file for deploying the published connectCenter images.

:::note[connectCenter was formerly "Score"]
connectCenter was previously named **Score**. The published container images and several
internal artifacts still carry the legacy `srt-*` prefix (for example
`oagi1docker/srt-web`). The image names below are correct as-is; only the product name changed.
:::

## System requirements

The backend image bundles its own Java runtime, and the database image bundles its own
MySQL/MariaDB-compatible server. The host machine therefore only needs **Docker** with the
**Compose v2** plugin — you do not install Java, a servlet container, or a database on the host.

The following are **recommendations**, not hard requirements. Sizing depends on library size
and concurrent users.

| Resource | Recommendation |
|---|---|
| CPU | 4 vCPU (2 minimum) |
| Memory | Enough RAM for the backend's container reservation plus the database and Redis. The [Compose file below](#the-compose-file) sets a JVM heap cap (`JAVA_OPTS`) and a matching container limit/reservation as working defaults — raise them for larger libraries and more concurrent users. |
| Disk | A few GB for images plus room for the database volume. The dataset itself is small; most space is images and OS. |
| Docker | A recent Docker Engine with the Compose v2 plugin (`docker compose`). |

:::tip[Use `docker compose` (v2), not `docker-compose`]
All commands on this page use the modern Compose v2 plugin syntax, `docker compose` (a space,
not a hyphen). The standalone `docker-compose` v1 binary is deprecated.
:::

## Prerequisites

- A host with [Docker](https://docs.docker.com/get-docker/) installed, including the
  [Compose v2 plugin](https://docs.docker.com/compose/install/).
- Network access to [Docker Hub](https://hub.docker.com/u/oagi1docker) to pull the
  `oagi1docker/*` images, which are hosted under the `oagi1docker` organization. You may
  need to `docker login` and have access to those repositories to pull them — if you cannot
  see them, request access from OAGi
  ([OAGi Score repositories](https://hub.docker.com/u/oagi1docker)).

## The four core services

A standard connectCenter deployment runs four containers that the Compose file wires together:

| Service | Image | Role | Host → container port |
|---|---|---|---|
| `frontend` | `oagi1docker/srt-web:3.5.2` | Angular web UI (`score-web`) | `4200` → `4200` |
| `backend` | `oagi1docker/srt-http-gateway:3.5.2` | REST API gateway (`score-http`, Spring Boot) | `8080` → `8080` |
| `db` | `oagi1docker/srt-repo:3.5.2` | MySQL/MariaDB-compatible database with the schema preloaded (`score-repo`) | `3306` → `3306` |
| `redis` | `redis:7.4` | Session store and cache | `6379` → `6379` |

The `frontend` reaches the API through `GATEWAY_HOST`/`GATEWAY_PORT`, and the `backend`
reaches the database and Redis through `DB_HOST`/`DB_PORT` and `REDIS_HOST`/`REDIS_PORT`.

## The Compose file

Create a file named `docker-compose.yml` with the following content:

```yaml
version: "3.0"
services:
  frontend:
    image: oagi1docker/srt-web:3.5.2
    ports:
      - 4200:4200
    links:
      - backend
    environment:
      - GATEWAY_HOST=backend
      - GATEWAY_PORT=8080
    depends_on:
      - backend

  backend:
    image: oagi1docker/srt-http-gateway:3.5.2
    ports:
      - 8080:8080
    links:
      - db
      - redis
    environment:
      - DB_HOST=db
      - DB_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - JAVA_OPTS="-Xmx4096m"
    depends_on:
      - db
      - redis
    deploy:
      resources:
        limits:
          memory: 8192M
        reservations:
          memory: 8192M

  db:
    image: oagi1docker/srt-repo:3.5.2
    ports:
      - 3306:3306
    volumes:
      - db-volume:/var/lib/mysql

  redis:
    image: redis:7.4
    ports:
      - 6379:6379
    volumes:
      - redis-volume:/data

volumes:
  db-volume:
  redis-volume:
```

:::note[The top-level `version:` key is obsolete]
Under the current Compose specification the top-level `version:` field is obsolete and ignored.
It is kept in the example and does no harm; Compose v2 may print a warning about it.
:::

Named volumes (`db-volume`, `redis-volume`) persist the database and Redis data across
container restarts, so do not delete them unless you intend to wipe your data.

## Start connectCenter

From the directory containing `docker-compose.yml`:

```bash
docker compose up -d
```

The first run pulls the `oagi1docker/*` and `redis:7.4` images, which can take a while. Check
status and logs with:

```bash
docker compose ps
docker compose logs -f backend
```

When the backend has finished starting, open the web UI in your browser at the **frontend** host
port:

```
http://localhost:4200
```

To stop the stack (keeping data volumes):

```bash
docker compose down
```

## Optional: OIDC setup (SSO)

Out of the box, users sign in with local accounts, so connecting an Identity Provider is
optional. connectCenter can additionally sign users in through an external **OpenID Connect
(OIDC)** provider, configured at install time by inserting the provider's details into the
`oauth2_app` and `oauth2_app_scope` tables in the database. It can become a requirement,
however, when you use external features: the
[external API below](#optional-adding-the-external-api) relies on JWT validation against an
OIDC provider. The full column reference, a worked Google example, and the resource-server JWT
settings are on a dedicated page: [OIDC Setup](./oidc-setup.md).

## Optional: Adding the external API

`score-external-api` (a **NestJS 11** service) exposes additional programming interfaces for
accessing refined core component information. It is an optional add-on to the four core services.

This service relies on JWT validation, so it requires that your deployment is already connected to
an [OIDC provider](./oidc-setup.md) that publishes a JWK Set endpoint. You must also provide the `RS_JWK_SET_URI`
property to the `backend` (gateway) service. You can usually discover the `jwks_uri` from your
provider's OIDC discovery document:

```
https://auth.<mycompany>.com/.well-known/openid-configuration
```

To enable it, add an `external-api` service and the `RS_JWK_SET_URI` variable to the backend.
The additions to the core Compose file are shown below:

```yaml
  backend:
    image: oagi1docker/srt-http-gateway:3.5.2
    # ...existing backend configuration...
    environment:
      - DB_HOST=db
      - DB_PORT=3306
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - JAVA_OPTS="-Xmx4096m"
      - RS_JWK_SET_URI=https://auth.<mycompany>.com/pf/JWKS   # <- your provider's jwks_uri

  external-api:
    image: oagi1docker/srt-external-api:3.4.2
    ports:
      - 3000:3000
    environment:
      - backend_server=http://backend:8080
    depends_on:
      - backend
```

:::warning[Verify the external-api image tag before deploying]
`score-external-api` is versioned independently of the rest of the platform: its
`package.json` reports `3.4.1`, and the image build script
(`score-external-api/docker/build.sh`) tags `oagi1docker/srt-external-api:3.4.2`, which is
what the example above pins. Confirm the exact published tag on
[Docker Hub](https://hub.docker.com/u/oagi1docker) before deploying, and pin to whatever the
release notes specify rather than guessing.
:::

Like the frontend, the `external-api` container listens on port `3000` by default; expose it
behind an HTTPS URL in your environment so clients can reach it. Depending on your network you
may also need proxy-related environment variables.

## Next steps

- [OIDC Setup](./oidc-setup.md) — optionally connect an OpenID Connect provider (required for
  the external API).
- [First steps](./first-steps.md) — sign in and orient yourself in the UI.
- [Architecture Overview](../06-contributing/architecture.md) — how the modules fit together.
- [Operations](../05-operations/backup-restore.md) — backup/restore, migration, and upgrades.
