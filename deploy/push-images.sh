#!/usr/bin/env bash
# Push the three connectCenter images at <version> to Docker Hub (oagi1docker).
# Requires: docker login (Docker Hub account with push rights to oagi1docker/*).
#
# Usage: [IMAGES=web,http,repo] deploy/push-images.sh [version]
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

VERSION="${1:-$(default_version)}"

command -v docker >/dev/null 2>&1 || die "docker not found."
if ! docker system info 2>/dev/null | grep -qi 'Username:'; then
  warn "Docker Hub login not detected. If push fails with 'denied', run: docker login"
fi

normalize_images
tags=()
has_image web  && tags+=("$IMAGE_WEB:$(tag_for web "$VERSION")")
has_image http && tags+=("$IMAGE_HTTP:$(tag_for http "$VERSION")")
has_image repo && tags+=("$IMAGE_REPO:$(tag_for repo "$VERSION")")
for tag in "${tags[@]}"; do
  if ! docker image inspect "$tag" >/dev/null 2>&1; then
    # A PUSH_IMAGE=1 build publishes the multi-arch manifest itself and leaves nothing locally.
    docker buildx imagetools inspect "$tag" >/dev/null 2>&1 \
      || die "image $tag not found locally or in the registry -- run deploy/build-images.sh $VERSION first."
    log "$tag already published (multi-arch build) -- skipping push"
    continue
  fi
  log "docker push $tag"
  docker push "$tag"
done
log "Pushed: ${tags[*]}"
