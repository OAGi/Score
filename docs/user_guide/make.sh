#!/bin/bash

set -euo pipefail

if [ -d ".venv" ]; then
  VENV_DIR=".venv"
elif [ -d "venv" ]; then
  VENV_DIR="venv"
else
  echo "No virtualenv found. Expected .venv or venv in $(pwd)." >&2
  exit 1
fi

source "${VENV_DIR}/bin/activate"
sphinx-build -M clean . _build
sphinx-build -M html . _build
deactivate
