FROM node:20-bookworm-slim AS docs-builder

ENV NEXT_TELEMETRY_DISABLED=1

WORKDIR /build/docs

COPY docs/package.json docs/package-lock.json ./
RUN npm ci

COPY docs ./
RUN npm run build


FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    NEXT_TELEMETRY_DISABLED=1 \
    HOST=0.0.0.0 \
    PORT=5555 \
    DOCS_HOST=0.0.0.0 \
    DOCS_PORT=3001 \
    ACCESS_LOG=true \
    RELOAD=false \
    UVICORN_WORKERS=1 \
    DEBUG=false \
    APP_MODULE=app.main:app \
    APP_NAME=connect-center-api \
    SQL_ECHO=false \
    DB_AUTO_CREATE=false \
    DB_POOL_PRE_PING=true \
    DB_VENDOR=mariadb \
    DB_HOST=127.0.0.1 \
    DB_PORT=3306 \
    DB_USER=oagi \
    DB_NAME=oagi \
    DATABASE_URL= \
    OAUTH2_ISSUER_URI= \
    OAUTH2_PROVIDER_NAME= \
    OAUTH2_CLIENT_ID= \
    OAUTH2_CONFIGURATION_URL= \
    OAUTH2_AUDIENCE= \
    PUBLIC_DOCS_BASE_URL=http://127.0.0.1:3001 \
    CORS_ALLOW_ORIGINS=http://127.0.0.1:3001,http://localhost:3001,http://127.0.0.1:5555,http://localhost:5555 \
    PUBLIC_API_BASE_URL=http://127.0.0.1:5555

WORKDIR /app

RUN apt-get update \
    && apt-get install --yes --no-install-recommends build-essential pkg-config default-libmysqlclient-dev supervisor \
    && rm -rf /var/lib/apt/lists/*

COPY --from=docs-builder /usr/local/bin/node /usr/local/bin/node

COPY pyproject.toml README.md server.sh docs-server.sh ./
COPY docker/supervisord.conf /etc/supervisor/conf.d/connect-center.conf
COPY app ./app
COPY --from=docs-builder /build/docs/.next/standalone ./docs-server
COPY --from=docs-builder /build/docs/.next/static ./docs-server/.next/static
COPY --from=docs-builder /build/docs/public ./docs-server/public

RUN pip install ".[all]"

EXPOSE 5555 3001

CMD ["supervisord", "-c", "/etc/supervisor/conf.d/connect-center.conf"]
