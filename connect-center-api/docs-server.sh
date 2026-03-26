#!/usr/bin/env bash
set -euo pipefail

DOCS_HOST="${DOCS_HOST:-0.0.0.0}"
DOCS_PORT="${DOCS_PORT:-3001}"
DOCS_SERVER_DIR="${DOCS_SERVER_DIR:-/app/docs-server}"

cd "${DOCS_SERVER_DIR}"
exec env HOSTNAME="${DOCS_HOST}" PORT="${DOCS_PORT}" NODE_ENV=production node server.js
