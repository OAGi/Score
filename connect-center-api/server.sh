#!/usr/bin/env bash
set -euo pipefail

HOST="${HOST:-0.0.0.0}"
PORT="${PORT:-5555}"
VENV_DIR="${VENV_DIR:-.venv}"
APP_MODULE="${APP_MODULE:-app.main:app}"
LOG_LEVEL="${LOG_LEVEL:-}"
ACCESS_LOG="${ACCESS_LOG:-true}"
RELOAD="${RELOAD:-true}"
UVICORN_WORKERS="${UVICORN_WORKERS:-${WEB_CONCURRENCY:-1}}"

if [[ -z "${LOG_LEVEL}" ]]; then
  if [[ "${DEBUG:-}" == "true" || "${DEBUG:-}" == "True" || "${DEBUG:-}" == "1" ]]; then
    LOG_LEVEL="debug"
  else
    LOG_LEVEL="info"
  fi
fi

UVICORN_ARGS=(
  --host "$HOST"
  --port "$PORT"
  --log-level "$LOG_LEVEL"
)

if [[ "${RELOAD}" == "true" || "${RELOAD}" == "True" || "${RELOAD}" == "1" ]]; then
  UVICORN_ARGS+=(--reload)
elif [[ "${UVICORN_WORKERS}" =~ ^[1-9][0-9]*$ ]] && [[ "${UVICORN_WORKERS}" != "1" ]]; then
  UVICORN_ARGS+=(--workers "${UVICORN_WORKERS}")
fi

if [[ "${ACCESS_LOG}" != "false" && "${ACCESS_LOG}" != "0" ]]; then
  UVICORN_ARGS+=(--access-log)
fi

if [[ -x "${VENV_DIR}/bin/python" ]]; then
  # Prefer the repo virtualenv so `uvicorn` and deps resolve consistently.
  exec "${VENV_DIR}/bin/python" -u -m uvicorn "${APP_MODULE}" "${UVICORN_ARGS[@]}" "$@"
elif command -v uvicorn >/dev/null 2>&1; then
  # Fallback to PATH uvicorn if the repo virtualenv is not available.
  exec uvicorn "${APP_MODULE}" "${UVICORN_ARGS[@]}" "$@"
else
  echo "Unable to locate uvicorn executable."
  exit 1
fi
