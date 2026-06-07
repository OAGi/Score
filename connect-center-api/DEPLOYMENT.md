# Deploying connect-center-api into an existing connectCenter

This guide covers adding **connect-center-api** to a connectCenter platform that already runs with
Docker Compose, and exposing it behind a reverse proxy (nginx).

For a from-scratch Docker quickstart and the full list of configuration variables, see the
[README](README.md).

## Add to an existing Docker Compose stack

If you already run connectCenter with Docker Compose, add connect-center-api as one more service on
the same Compose network and point it at the existing database.

Append the service below to your existing `docker-compose.yml` (which already defines the
connectCenter database — typically a `db` service running the `srt-repo` image — alongside the
backend/frontend). The API container talks to the database over the Compose network, so `DB_HOST` is
just that service's name:

```yaml
services:
  # ... your existing connectCenter services (db, backend, frontend, redis) ...

  connect-center-api:
    image: oagi1docker/connect-center-api:0.1
    depends_on:
      - db
    ports:
      - "5555:5555"   # REST API + MCP  (bind to 127.0.0.1:5555:5555 if it sits behind a proxy)
      - "3001:3001"   # docs site       (bind to 127.0.0.1:3001:3001 if it sits behind a proxy)
    environment:
      - HOST=0.0.0.0
      - PORT=5555
      - DB_VENDOR=mariadb
      - DB_HOST=db            # the Compose service name of the connectCenter database
      - DB_PORT=3306
      - DB_USER=oagi
      - DB_PASSWORD=oagi
      - DB_NAME=oagi
      # Public base URLs — match how you expose it (see the reverse-proxy section):
      - PUBLIC_DOCS_BASE_URL=https://<your-host>/services/docs
      - PUBLIC_API_BASE_URL=https://<your-host>/services
      - CORS_ALLOW_ORIGINS=https://<your-host>
      # OIDC (optional) — enables auth and the interactive MCP OAuth proxy:
      - OAUTH2_PROVIDER_NAME=
      - OAUTH2_ISSUER_URI=
      - OAUTH2_CLIENT_ID=
      - OAUTH2_CLIENT_SECRET=
      - OAUTH2_AUDIENCE=
      # - OAUTH2_CONFIGURATION_URL=   # optional; derived from OAUTH2_ISSUER_URI if omitted
```

Then start just the new service (the database keeps running):

```bash
docker compose up -d connect-center-api
```

The connectCenter database image already contains the schema this API reads, so keep
`DB_AUTO_CREATE=false` and point `DB_*` at your existing database service.

## Behind a reverse proxy (nginx)

In a typical deployment the docs, API, and MCP endpoint are published under one HTTPS domain and the
container ports (`5555`, `3001`) are not exposed publicly. The example below mounts everything under
a `/services` prefix — matching `PUBLIC_DOCS_BASE_URL` / `PUBLIC_API_BASE_URL` above — next to the
existing connectCenter web app:

```nginx
server {
    listen 443 ssl;
    server_name <your-host>;
    # ssl_certificate / ssl_certificate_key ...        # e.g. managed by Certbot

    # connectCenter web app (existing)
    location / {
        proxy_pass http://127.0.0.1:4200;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }

    # OAuth/OIDC + MCP protected-resource metadata (served by the API)
    location ^~ /.well-known/ {
        proxy_pass http://127.0.0.1:5555;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto https;
    }

    # Documentation site  ->  PUBLIC_DOCS_BASE_URL=https://<your-host>/services/docs
    location = /services       { return 302 https://$host/services/docs/; }
    location = /services/      { return 302 https://$host/services/docs/; }
    location = /services/docs  { return 302 https://$host/services/docs/; }
    location /services/docs/ {
        proxy_pass http://127.0.0.1:3001;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port 443;
    }

    # REST API  ->  PUBLIC_API_BASE_URL=https://<your-host>/services  (i.e. /services/api -> /api)
    location /services/api/ {
        rewrite ^/services/api/(.*)$ /api/$1 break;
        proxy_pass http://127.0.0.1:5555;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }

    # MCP (streamable HTTP / SSE)  ->  https://<your-host>/services/mcp
    location /services/mcp {
        rewrite ^/services/mcp/?(.*)$ /mcp/$1 break;
        proxy_pass http://127.0.0.1:5555;
        proxy_http_version 1.1;
        proxy_set_header Connection "";          # keep-alive to upstream (needed for streaming)
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_buffering off;                     # flush SSE events immediately
        proxy_read_timeout 3600s;                # don't drop long-lived MCP streams
        proxy_send_timeout 3600s;
    }
}
```

With this proxy the public endpoints are:

| Endpoint | URL |
| --- | --- |
| Docs & playground | `https://<your-host>/services/docs/` |
| REST API (base) | `https://<your-host>/services/api` (e.g. health: `/services/api/health`) |
| MCP | `https://<your-host>/services/mcp` |

Set the container's `PUBLIC_DOCS_BASE_URL` / `PUBLIC_API_BASE_URL` to match these paths.

Notes:

- The `/.well-known/` passthrough is what lets MCP clients discover the OAuth flow — keep it.
- `Authorization` and `Mcp-Session-Id` headers pass through nginx unchanged; just don't introduce
  underscore-named headers (nginx drops those by default).

### Using a different domain or paths

You choose the domain and the public paths; two things bind them:

- **`PUBLIC_API_BASE_URL` / `PUBLIC_DOCS_BASE_URL`** are runtime env — set them to whatever public
  URLs you expose. The docs read `PUBLIC_API_BASE_URL` at runtime and call it + `/api`.
- The **docs base path** is baked into the image at build time (`PROD_DOCS_BASE_PATH` in
  `docs/next.config.mjs`, default `/services/docs`). Next.js embeds it in every asset and link URL,
  so a proxy rewrite alone cannot move it — **the docs base path must equal the public docs path**.

The REST API and MCP have no baked-in prefix (they live at `/api` and `/mcp`), so they can be proxied
to any host/path freely.

**Example — one host, short paths** (`https://api.example.com/docs`, `/api`, `/mcp`):

1. Build the docs at the matching base path: set `PROD_DOCS_BASE_PATH = '/docs'` in
   `docs/next.config.mjs` (keep the last path segment named `docs` — the runtime path helpers match
   `…/docs`), then rebuild the image (`docker/build.sh`).
2. Configure nginx:

   ```nginx
   server {
       listen 443 ssl;
       server_name api.example.com;
       # ssl_certificate / ssl_certificate_key ...

       # MCP (streamable HTTP / SSE)
       location /mcp {
           proxy_pass http://127.0.0.1:5555;
           proxy_http_version 1.1;
           proxy_set_header Connection "";
           proxy_set_header Host $host;
           proxy_set_header X-Forwarded-Proto https;
           proxy_buffering off;
           proxy_read_timeout 3600s;
           proxy_send_timeout 3600s;
       }

       # Docs (image built with PROD_DOCS_BASE_PATH=/docs)
       location = /docs { return 302 https://$host/docs/; }
       location /docs/ {
           proxy_pass http://127.0.0.1:3001;
           proxy_http_version 1.1;
           proxy_set_header Host $host;
           proxy_set_header X-Forwarded-Proto https;
           proxy_set_header X-Forwarded-Host $host;
           proxy_set_header X-Forwarded-Port 443;
       }

       # REST API (/api), Swagger UI (/swagger-ui), OAuth metadata (/.well-known/)
       location / {
           proxy_pass http://127.0.0.1:5555;
           proxy_http_version 1.1;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto https;
       }
   }
   ```

3. Set the env: `PUBLIC_API_BASE_URL=https://api.example.com`,
   `PUBLIC_DOCS_BASE_URL=https://api.example.com/docs`, `CORS_ALLOW_ORIGINS=https://api.example.com`.

**Variations:**

- **Keep the default `/services/docs` base path** (no rebuild) — just change the `server_name`, point
  `PUBLIC_*_BASE_URL` at the new host, and reuse the `/services/*` nginx blocks above.
- **Split docs and API onto separate hosts** (e.g. `docs.example.com` + `api.example.com`) — same
  idea, but since they are now different origins, add the docs origin to the API's
  `CORS_ALLOW_ORIGINS`.
