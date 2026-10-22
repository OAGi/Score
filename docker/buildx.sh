# shellcheck shell=sh
# Shared multi-architecture image build for the module docker/build.sh scripts.
# Sourced by score-http, score-web, score-repo, score-external-api and connect-center-api --
# do not run directly.
#
# Environment:
#   PUSH_IMAGE        1/true  build for every platform and push the multi-arch image to the
#                             registry (needs `docker login`). Default: build locally, no push.
#   IMAGE_PLATFORMS   comma-separated, e.g. linux/amd64,linux/arm64. Defaults:
#                               push  -> linux/amd64,linux/arm64
#                               local -> the host's own platform (fast; `docker run`-able here)
#                             DOCKER_DEFAULT_PLATFORM is honored when IMAGE_PLATFORMS is unset.
#   BUILDX_BUILDER    builder used for multi-platform builds (default: score-multiarch). It is
#                     created with the docker-container driver when missing, because the default
#                     `docker` driver cannot build more than one platform.
#   BUILDX_DRY_RUN    1 prints the docker command instead of running it.
#
# A local (non-push) build can only hold one platform in the classic Docker image store, so a
# multi-platform list without PUSH_IMAGE is rejected unless the containerd image store is on.
# Cross-platform builds run under QEMU: Docker Desktop ships it; on a Linux host install it once
# with `docker run --privileged --rm tonistiigi/binfmt --install all`.

buildx_default_platforms="linux/amd64,linux/arm64"
buildx_builder="${BUILDX_BUILDER:-score-multiarch}"

buildx_fail() {
  echo "Error: $*" >&2
  exit 1
}

buildx_resolve() {
  case "${PUSH_IMAGE:-0}" in
    1|true|True|TRUE|yes) buildx_push=1 ;;
    *) buildx_push=0 ;;
  esac

  if [ -n "${IMAGE_PLATFORMS:-}" ]; then
    buildx_platforms=$IMAGE_PLATFORMS
  elif [ -n "${DOCKER_DEFAULT_PLATFORM:-}" ]; then
    buildx_platforms=$DOCKER_DEFAULT_PLATFORM
  elif [ "$buildx_push" = 1 ]; then
    buildx_platforms=$buildx_default_platforms
  else
    buildx_platforms=
  fi

  case "$buildx_platforms" in
    *,*) buildx_multi=1 ;;
    *) buildx_multi=0 ;;
  esac

  if [ "$buildx_multi" = 1 ] && [ "$buildx_push" = 0 ] \
      && ! docker info 2>/dev/null | grep -q 'io.containerd.snapshotter'; then
    buildx_fail "multi-platform build ($buildx_platforms) cannot be loaded into the local Docker image store. Set PUSH_IMAGE=1 to publish it, or IMAGE_PLATFORMS=<one platform> for a local image."
  fi
}

buildx_ensure_builder() {
  docker buildx inspect "$buildx_builder" >/dev/null 2>&1 \
    || docker buildx create --name "$buildx_builder" --driver docker-container >/dev/null
}

# buildx_build <image> <dockerfile> <context> [extra docker build args...]
buildx_build() {
  buildx_image=$1
  buildx_dockerfile=$2
  buildx_context=$3
  shift 3

  buildx_resolve

  set -- "$@" -f "$buildx_dockerfile" -t "$buildx_image" "$buildx_context"
  if [ -n "$buildx_platforms" ]; then
    set -- --platform "$buildx_platforms" "$@"
  fi
  if [ "$buildx_push" = 1 ]; then
    set -- --push "$@"
  else
    set -- --load "$@"
  fi
  if [ "$buildx_multi" = 1 ]; then
    if [ "${BUILDX_DRY_RUN:-0}" != 1 ]; then
      buildx_ensure_builder
    fi
    set -- --builder "$buildx_builder" "$@"
  fi
  set -- docker buildx build "$@"

  echo "Building ${buildx_image} (platforms: ${buildx_platforms:-host}, push: ${buildx_push})..."
  if [ "${BUILDX_DRY_RUN:-0}" = 1 ]; then
    echo "$*"
    return 0
  fi
  "$@"
}

# buildx_scan <image> [trivy image args...]
# After a push the image only exists in the registry, so scan each published platform.
buildx_scan() {
  buildx_scan_image=$1
  shift

  if ! command -v trivy >/dev/null 2>&1; then
    echo "Info: trivy is not installed. Install Trivy to scan vulnerabilities for $buildx_scan_image."
    return 0
  fi
  if [ "${BUILDX_DRY_RUN:-0}" = 1 ]; then
    return 0
  fi

  echo "Scanning vulnerabilities with Trivy (HIGH/CRITICAL, fixed only)..."
  if [ "$buildx_push" = 1 ]; then
    buildx_old_ifs=$IFS
    IFS=,
    for buildx_platform in $buildx_platforms; do
      IFS=$buildx_old_ifs
      echo "-- $buildx_platform"
      trivy image --platform "$buildx_platform" "$@" "$buildx_scan_image"
      IFS=,
    done
    IFS=$buildx_old_ifs
  else
    trivy image "$@" "$buildx_scan_image"
  fi
}
