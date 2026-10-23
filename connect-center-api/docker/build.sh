#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# shellcheck source=../../docker/buildx.sh
source "${PROJECT_ROOT}/../docker/buildx.sh"

IMAGE_NAME="${IMAGE_NAME:-oagi1docker/connect-center-api}"
IMAGE_TAG="${IMAGE_TAG:-0.1}"
DOCKERFILE_PATH="${SCRIPT_DIR}/Dockerfile"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required but was not found in PATH."
  exit 1
fi

buildx_build "${IMAGE_NAME}:${IMAGE_TAG}" "${DOCKERFILE_PATH}" "${PROJECT_ROOT}"

echo "Built ${IMAGE_NAME}:${IMAGE_TAG} (platforms: ${buildx_platforms:-host}, push: ${buildx_push})"
