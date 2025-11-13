#!/bin/bash
set -e

# Load .env file if it exists
# This loads ALL variables from .env file and exports them to the environment
# Environment variables passed to docker run (-e) take precedence over .env file values
if [ -f .env ]; then
    # set -a makes all variables automatically exported
    set -a
    # Source the .env file - this will export all KEY=VALUE pairs
    # shellcheck source=/dev/null
    source .env
    set +a
fi

# Get environment variables with defaults
# If variable was set in .env or via -e flag, use that value
# Otherwise use the default
HOST=${HOST:-0.0.0.0}
PORT=${PORT:-8000}
WORKERS=${WORKERS:-4}

# Start uvicorn
# All environment variables from .env are now available to the Python process
exec .venv/bin/uvicorn "main:app" --host "$HOST" --port "$PORT" --workers "$WORKERS"

