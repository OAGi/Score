#!/usr/bin/env bash
# Build the three connectCenter images at the given version (default: score-http/VERSION).
# Wraps each module's own docker/build.sh, which already tag oagi1docker/srt-*:<version>.
#
# Prereqs (checked by the underlying build.sh scripts):
#   - docker running; the fleet is linux/amd64, so a local build defaults to that platform
#     (emulated, slow but correct, on Apple Silicon)
#   - score-web: Node 22 (via nvm) + prebuilt docs at docs/user_guide/_build/html
#   - score-http: Maven wrapper + mariadb JDBC driver in ~/.m2
#
# Usage: deploy/build-images.sh [version]
#
# Multi-architecture: PUSH_IMAGE=1 builds linux/amd64 + linux/arm64 and pushes the manifest list
# straight to Docker Hub (a local image holds one platform only); push-images.sh then sees the tags
# are already published. IMAGE_PLATFORMS overrides the platform list (see docker/buildx.sh).
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

VERSION="${1:-$(default_version)}"
if [ "${PUSH_IMAGE:-0}" = 1 ] || [ "${PUSH_IMAGE:-0}" = true ]; then
  export IMAGE_PLATFORMS="${IMAGE_PLATFORMS:-linux/amd64,linux/arm64}"
else
  export IMAGE_PLATFORMS="${IMAGE_PLATFORMS:-${DOCKER_DEFAULT_PLATFORM:-linux/amd64}}"
fi

command -v docker >/dev/null 2>&1 || die "docker not found."
docker info >/dev/null 2>&1 || die "docker daemon not reachable (is Docker Desktop running?)."

log "Building images for $VERSION (platforms $IMAGE_PLATFORMS)"

# repo first (fast), then http (Maven), then web (Angular).
log "[1/3] srt-repo"
sh "$REPO_ROOT/score-repo/docker/build.sh" "$VERSION"
log "[2/3] srt-http-gateway"
sh "$REPO_ROOT/score-http/docker/build.sh" "$VERSION"
log "[3/3] srt-web"
sh "$REPO_ROOT/score-web/docker/build.sh" "$VERSION"

log "Built:"
docker images --format '  {{.Repository}}:{{.Tag}}\t{{.ID}}\t{{.Size}}' \
  | grep -E "oagi1docker/srt-(web|http-gateway|repo):${VERSION}$" || true
