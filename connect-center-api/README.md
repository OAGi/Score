# connectCenter API

[![Python](https://img.shields.io/badge/python-3.11%2B-blue.svg)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.136-009688.svg)](https://fastapi.tiangolo.com/)
[![MCP](https://img.shields.io/badge/MCP-FastMCP-7c3aed.svg)](https://gofastmcp.com/)
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](../LICENSE.txt)

The REST + [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) service layer for
**connectCenter** (formerly *Score*) — OAGi/NIST's tooling for the UN/CEFACT Core Components
methodology (CCTS/CCS).

It exposes a connectCenter repository — libraries, releases, core components, business information
entities (BIEs), data types, code lists, agency ID lists, contexts, namespaces, and tags — over a
clean HTTP API **and** an MCP server, and ships an interactive documentation + API playground so
people and AI agents can explore the same data through the same vocabulary.

```
                     +---------------------------------+
                     | connect-center-api              |
Browsers / apps  --> | FastAPI  ->  REST API + OpenAPI |
                     | FastMCP  ->  MCP tools          | -->  MariaDB
AI / MCP clients --> | OAuth2 / OIDC bearer auth       |      (connectCenter repository)
                     | Next.js  ->  docs + playground  |
                     +---------------------------------+
```

> Part of the [OAGi/Score](https://github.com/OAGi/Score) platform. The API reads the same
> connectCenter repository database used by the rest of the platform.

---

## Features

- **REST API** (`/api`) over connectCenter resources, with a generated **OpenAPI** schema
  (`/api/openapi.json`) and **Swagger UI** (`/swagger-ui`).
- **Built-in MCP server** (`/mcp`, powered by [FastMCP](https://gofastmcp.com/)) that exposes the
  same data domains as MCP tools, so AI agents can query the repository directly.
- **OAuth2 / OIDC bearer authentication**, including an OIDC proxy for interactive MCP clients and
  JWT verification for trusted external issuers.
- **Interactive documentation & API playground** — a Next.js app under [`docs/`](docs/).
- **MariaDB** data access through async SQLAlchemy (`asyncmy`), behind a pluggable vendor layer.
- Consistent, user-friendly error responses and structured logging.

## Requirements

- **Python 3.11+**
- A reachable **connectCenter repository database** (MariaDB). The schema is *not* created by
  default (`DB_AUTO_CREATE=false`); point the API at an existing connectCenter database.
- **Node.js 20+** — only if you want to run the documentation site locally (see
  [Run from source](#run-from-source)).

---

## Quick start (Docker)

A ready-to-run image is published on Docker Hub. It bundles the API and the documentation site,
started together:

```bash
docker pull oagi1docker/connect-center-api:0.1

docker run -d --name connect-center-api \
  -p 5555:5555 -p 3001:3001 \
  -e DB_HOST=<db-host> -e DB_PORT=3306 \
  -e DB_USER=<user> -e DB_PASSWORD=<password> -e DB_NAME=<schema> \
  oagi1docker/connect-center-api:0.1
```

| Service | URL |
| --- | --- |
| Health check | http://localhost:5555/api/health |
| REST API (base) | http://localhost:5555/api |
| Swagger UI | http://localhost:5555/swagger-ui |
| OpenAPI schema | http://localhost:5555/api/openapi.json |
| MCP endpoint | http://localhost:5555/mcp |
| Docs & playground | http://localhost:3001/services/docs/ |

The image needs a reachable connectCenter database; the [`docker/`](docker/) Compose file brings up
the API together with a database and the rest of the platform.

> **Already running connectCenter?** See **[DEPLOYMENT.md](DEPLOYMENT.md)** to add the API to an
> existing Docker Compose stack and put it behind a reverse proxy (nginx).

## Run from source

Requires Python 3.11+ (and Node.js 20+ for the docs site).

```bash
cp .env.example .env        # then set DB_HOST / DB_PORT / DB_USER / DB_PASSWORD / DB_NAME
pip install -e .            # API dependencies (MariaDB stack)
./server.sh                 # API on http://127.0.0.1:5555  (or: uvicorn app.main:app --reload)
```

The documentation site runs as a separate process:

```bash
cd docs && npm install && npm run dev   # docs on http://localhost:3001
```

## Configuration

Configuration is read from environment variables (and `.env`). The most common settings:

| Variable | Default | Description |
| --- | --- | --- |
| `DB_VENDOR` | `mariadb` | Database vendor (currently `mariadb`). |
| `DB_HOST` / `DB_PORT` | `127.0.0.1` / `3306` | Database host and port. |
| `DB_USER` / `DB_PASSWORD` / `DB_NAME` | — | Database credentials and schema. |
| `DB_AUTO_CREATE` | `false` | Create ORM tables on startup (dev/testing only). |
| `HOST` / `PORT` | `127.0.0.1` / `5555` | API bind address. |
| `DOCS_HOST` / `DOCS_PORT` | `127.0.0.1` / `3001` | Docs server bind address. |
| `PUBLIC_API_BASE_URL` / `PUBLIC_DOCS_BASE_URL` | — | Public base URLs used in links, CORS, and MCP metadata. |
| `CORS_ALLOW_ORIGINS` | — | Comma-separated origins allowed to call the API / read OpenAPI. |
| `OAUTH2_*` | — | OIDC discovery + bearer-token validation, and the interactive MCP OAuth proxy. |
| `MCP_JWT_*` | — | JWT verification for trusted external MCP issuers. |

**Authentication.** When `OAUTH2_*` is configured, the API validates bearer tokens against the
configured issuer (using `OAUTH2_AUDIENCE`, falling back to `OAUTH2_CLIENT_ID`) and maps the verified
identity to a connectCenter user in the database. Leaving these blank disables OIDC auth for local
development. See [`.env.example`](.env.example) for an annotated reference.

---

## Using the MCP server

The API process exposes a [FastMCP](https://gofastmcp.com/) server over streamable HTTP at the
`/mcp` path. Point an MCP client at the base URL where the API is reachable:

- Local: `http://127.0.0.1:5555/mcp`
- Deployed: `https://<your-host>/mcp` — behind a reverse proxy the public path may differ (e.g.
  `/services/mcp`); see [DEPLOYMENT.md](DEPLOYMENT.md).

**Authentication is decided by the server, not by the client** — by how this deployment is
configured (see [Configuration](#configuration)):

- **OIDC (`OAUTH2_*`)** — the server runs an OAuth proxy. Clients perform an **interactive browser
  login** through connectCenter's identity provider and a consent screen on first connect; no token
  is entered by hand. This is the usual setup.
- **External JWT (`MCP_JWT_*`)** — clients send a **bearer token** minted by a trusted issuer
  (machine-to-machine).
- **Neither configured** — the MCP endpoint is unauthenticated (local development).

In the OIDC and JWT cases the verified identity must map to an existing **connectCenter user** — the
server resolves the token's `login_id` / `sub` to an account in the database — so the account has to
be provisioned first; a valid token by itself is not enough.

In the usual (OIDC) deployment you just register the URL and let the client run the login flow:

**Claude Code**

```bash
claude mcp add --transport http connectCenter https://<your-host>/mcp
```

Then run `/mcp` inside Claude Code to complete the browser login.

**OpenAI Codex CLI** — add to `~/.codex/config.toml` (native remote MCP in Codex ≥ 0.45):

```toml
[mcp_servers.connectCenter]
url = "https://<your-host>/mcp"
```

For an OAuth-protected server, register it (adding `--oauth-client-id <id>` if your provider needs a
pre-registered client) and run `codex mcp login connectCenter`.

**VS Code** (Copilot agent mode) — add to `.vscode/mcp.json` (the top-level key is `servers`):

```json
{
  "servers": {
    "connectCenter": { "type": "http", "url": "https://<your-host>/mcp" }
  }
}
```

VS Code detects the OAuth challenge and opens the browser on first connect.

> **Passing a token by hand** applies only to the external-JWT deployment (`MCP_JWT_*`): supply a
> token from the trusted issuer — it must verify against the server's issuer/audience and carry the
> `login_id` claim of a provisioned connectCenter user (e.g. Claude Code
> `--header "Authorization: Bearer <token>"`, or Codex `bearer_token_env_var = "..."`). Against an
> OIDC (`OAUTH2_*`) deployment, use the interactive login above instead.

Each REST domain has a matching set of MCP tools (agency ID lists, BIEs, code lists, contexts, core
components, data types, libraries, namespaces, releases, tags, XBTs, …).

## Testing

```bash
pip install -e ".[dev]"
pytest -q                                  # run the suite
pytest -n auto -q                          # parallel (pytest-xdist)
pytest -q tests/routes tests/service       # a subset
```

Linting and docstring checks:

```bash
ruff check .
pydoclint .
```

## License

[MIT](../LICENSE.txt) © The Open Applications Group Inc. (OAGi)
