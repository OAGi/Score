---
title: Deployment
sidebar_position: 4
---

[Installation with Docker](./installation-docker.md) covers a basic first run on `localhost`.
This page covers deploying connectCenter for real use: persisting data, sizing the host, putting
it behind TLS, and adding the optional companion services.

## Persist your data

The stack keeps all state in two named Docker volumes. Do not delete them unless you intend to
wipe the installation.

| Volume | Service | Container path | Holds |
|---|---|---|---|
| `db-volume` | `db` | `/var/lib/mysql` | All repository data (the source of truth) |
| `redis-volume` | `redis` | `/data` | Redis cache / session state |

:::warning[`down` keeps volumes, `down -v` destroys them]
`docker compose down` stops the stack but keeps the volumes. `docker compose down -v` **deletes**
them and all repository data. Take a backup of `db-volume` before destroying a stack — see
[Backup & Restore](../05-operations/backup-restore.md).
:::

## Size the host

The backend is the memory-hungry service: the Compose file caps its JVM heap (`JAVA_OPTS=-Xmx…`)
and both **limits and reserves** memory for the container. The values in the
[example Compose file](./installation-docker.md#the-compose-file) are working defaults, not
requirements — on a host with more RAM, raise the heap (and the container limit/reservation with
it) for larger libraries and more concurrent users. Whatever values you choose, make sure the host
has enough free RAM for the backend's reservation plus the database and Redis, or the backend may
fail to start or be OOM-killed under load. Disk needs are modest — mostly the images plus the
database volume.

### Database connection pool and logging

Since 3.5.3 the backend's HikariCP connection pool and its log levels are configurable through
environment variables on the `backend` service, so you can size them for your user count without
rebuilding the image. The defaults suit most installations; raise the pool when many people work
concurrently.

| Variable | Default | Purpose |
|---|---|---|
| `DB_POOL_MAXIMUM_POOL_SIZE` | `50` | Maximum number of database connections |
| `DB_POOL_MINIMUM_IDLE` | `10` | Idle connections kept ready |
| `DB_POOL_CONNECTION_TIMEOUT` | `30000` | Milliseconds to wait for a free connection before failing |
| `DB_POOL_MAX_LIFETIME` | `1800000` | Milliseconds after which a connection is retired and replaced |
| `LOG_LEVEL_SPRING` | `INFO` | Level for Spring, WebSocket and messaging loggers |
| `LOG_LEVEL_SPRING_SECURITY` | `INFO` | Level for Spring Security |
| `LOG_LEVEL_JOOQ` | `INFO` | Level for jOOQ (set `DEBUG` to log every SQL statement) |
| `LOG_LEVEL_SCORE` | `INFO` | Level for connectCenter's own `org.oagi.score` loggers |

Keep `DB_POOL_MAXIMUM_POOL_SIZE` below the database server's `max_connections`.

## Put it behind a reverse proxy (TLS)

In production the public entry point is the reverse proxy listening on ports **80/443** (for
example nginx terminating TLS) — users never connect to the container ports directly. Do **not**
publish those ports to the internet; bind them to the loopback interface so only the proxy on the
same host can reach them:

```yaml
# frontend: publish only to localhost; the proxy reaches it, the internet does not
ports:
  - "127.0.0.1:4200:4200"   # web UI — the proxy's only upstream
```

The web UI container's own nginx already proxies `/api/` and `/stomp` (WebSocket) to the backend
over the Compose network, so the reverse proxy needs just this one upstream. The other published
ports (`8080`, `3306`, `6379`) exist only for host-side convenience — bind them to loopback the
same way, or drop their `ports:` mappings entirely; the services reach each other over the Compose
network regardless.

A minimal nginx server block:

```nginx
server {
    listen 80;
    server_name connectcenter.example.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name connectcenter.example.com;
    # ssl_certificate / ssl_certificate_key ...

    client_max_body_size 0;   # large library / module-set uploads

    location / {
        proxy_pass http://127.0.0.1:4200;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-Host $host;
    }

    # WebSocket (STOMP) — the Upgrade handshake must pass through,
    # or real-time features silently break
    location /stomp {
        proxy_pass http://127.0.0.1:4200;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 600;
    }
}
```

Forward `X-Forwarded-Proto https` (and `X-Forwarded-Host`) so generated links use the correct
scheme and host.

### Cross-origin requests (CORS)

From 3.6.0 the API no longer answers credentialed cross-origin requests from arbitrary origins.
The origin a request is *addressed to* — the same host name, over `http` or `https` — is always
accepted, so a deployment served from one host needs no configuration. Any other origin gets no
CORS headers.

Set `SCORE_CORS_ALLOWED_ORIGINS` on the `backend` service (comma-separated origins, empty by
default) only when the `Host` header the backend sees differs from the address users open in the
browser: a web app on another domain, a proxy that rewrites `Host`, or a non-standard port behind
nginx `proxy_set_header Host $host` (which drops the port — `$http_host` keeps it).

```yaml
environment:
  - SCORE_CORS_ALLOWED_ORIGINS=https://score.example.org,https://score-alt.example.org
```

State-changing requests from other origins are rejected, and sign-out is accepted only as
`POST`. The session cookie is `SameSite=Lax`.

## Database

The `srt-repo` image bakes in a fully-migrated schema, so a **fresh** stack comes up already
populated — no import step is needed. When you **upgrade** to a newer backend image on an existing
`db-volume`, the `score-http` backend applies any pending schema changes automatically on startup
via **Flyway** (bundled `V*.sql` scripts under `score-http/src/main/resources/db/migration`,
baselined at `3.4.0`; `spring.flyway.enabled` defaults to `true`). You normally do not run SQL by
hand.

### Using your own MariaDB

Since 3.6.0 the backend can also initialize an **empty** database by itself: the bundled
`V3_4_0__baseline.sql` creates the 3.4.0 schema with the two initial accounts and the built-in primitive and XSD types, and the later
migrations bring it to the current version. You can therefore point the backend at any MariaDB
(11.x, 12.x or 13.x are verified) through `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME` and
`DB_PASSWORD`, without the `srt-repo` image. Note that a database initialized this way contains
the schema and that initial data only — not the OAGIS library content that the `srt-repo` image
ships with.

Requirements for your own database:

- The database must already exist.
- The database user needs DDL privileges and `CREATE ROUTINE` on it (the schema defines a
  `levenshtein` function).
- If binary logging is enabled, set `log_bin_trust_function_creators=1`. The manual procedure in [Database Migration](../05-operations/database-migration.md) is only
needed when you upgrade a **pre-3.5.0** installation or run with Flyway disabled.

## Optional companion services

Neither companion service is part of the core `docker-compose.yml`; add them as extra services on
the same Compose network when you need them.

### score-external-api

A NestJS 11 external REST API layer. It ships an image build
(`score-external-api/docker/Dockerfile`) but no committed Compose file, so wire its ports and
environment into your own Compose stack. It requires the backend to be connected to an
[OIDC provider](./oidc-setup.md) that publishes a JWK Set; see
[Installation → Adding the external API](./installation-docker.md#optional-adding-the-external-api).

### connect-center-api

A Python **FastAPI + FastMCP** service that exposes the connectCenter repository as a REST API and
an MCP server, plus a documentation + API playground. Its Compose file
(`connect-center-api/docker/docker-compose.yml`) bundles a full stack and adds:

```yaml
connect-center-api:
  image: oagi1docker/connect-center-api:0.1
  ports:
    - 5555:5555   # REST API + MCP (/mcp)
    - 3001:3001   # docs site + API playground
  environment:
    - DB_VENDOR=mariadb
    - DB_HOST=db
    - DB_PORT=3306
    # OIDC, CORS, and PUBLIC_*_BASE_URL settings omitted for brevity
  depends_on:
    - db
```

It talks to the **same** connectCenter database over the Compose network, so no extra migration is
needed. A typical reverse-proxy layout publishes everything under a `/services` prefix next to the
web app:

| Public path | Upstream | Notes |
|---|---|---|
| `/` | web UI (`127.0.0.1:4200`) | |
| `/services/docs/` | docs + playground (`127.0.0.1:3001`) | base path is baked into the image at build time (`PROD_DOCS_BASE_PATH`) — the public path must match it |
| `/services/api/` → `/api/` | REST API (`127.0.0.1:5555`) | |
| `/services/mcp` → `/mcp` | MCP (`127.0.0.1:5555`) | disable proxy buffering and raise timeouts for long-lived SSE; pass `Authorization` and `Mcp-Session-Id` through |

Set the container's `PUBLIC_API_BASE_URL` / `PUBLIC_DOCS_BASE_URL` and `CORS_ALLOW_ORIGINS` to
match the public HTTPS URLs you expose. See `connect-center-api/DEPLOYMENT.md` for the full guide.
