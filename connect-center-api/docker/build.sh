#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

IMAGE_NAME="${IMAGE_NAME:-oagi1docker/connect-center-api}"
IMAGE_TAG="${IMAGE_TAG:-0.1}"
DOCKERFILE_PATH="${SCRIPT_DIR}/Dockerfile"
DOCKER_DEFAULT_PLATFORM="${DOCKER_DEFAULT_PLATFORM:-linux/amd64}"
PUSH_IMAGE="${PUSH_IMAGE:-false}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required but was not found in PATH."
  exit 1
fi

echo "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG} ..."
BUILD_CMD=(
  docker buildx build
  --platform "${DOCKER_DEFAULT_PLATFORM}"
  -f "${DOCKERFILE_PATH}"
  -t "${IMAGE_NAME}:${IMAGE_TAG}"
)

if [[ "${PUSH_IMAGE}" == "true" || "${PUSH_IMAGE}" == "True" || "${PUSH_IMAGE}" == "1" ]]; then
  BUILD_CMD+=(--push)
else
  BUILD_CMD+=(--load)
fi

BUILD_CMD+=("${PROJECT_ROOT}")

"${BUILD_CMD[@]}"

echo "Built ${IMAGE_NAME}:${IMAGE_TAG} for ${DOCKER_DEFAULT_PLATFORM}"
