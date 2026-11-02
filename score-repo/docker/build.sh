#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_dir=$(CDPATH= cd -- "$script_dir/.." && pwd)

# shellcheck source=../../docker/buildx.sh
. "$project_dir/../docker/buildx.sh"

project_version=
if [ -f "$project_dir/VERSION" ]; then
  project_version=$(tr -d '[:space:]' < "$project_dir/VERSION")
fi
if [ -z "$project_version" ] && [ $# -gt 0 ]; then
  project_version=$1
fi
if [ -z "$project_version" ] && [ -n "${IMAGE_VERSION:-}" ]; then
  project_version=$IMAGE_VERSION
fi
if [ -z "$project_version" ]; then
  echo "Error: app.version is not set. Add $project_dir/VERSION, pass it as the first argument, or define IMAGE_VERSION." >&2
  exit 1
fi

image_name="oagi1docker/srt-repo:$project_version"

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker is not installed. See https://docs.docker.com/install/ for details." >&2
  exit 1
fi

(
  cd "$script_dir"
  buildx_build "$image_name" Dockerfile . --no-cache
)

# Skip the base-image gosu helper because we intentionally inherit it from mariadb
# and do not manage patching that upstream binary in this image build.
buildx_scan "$image_name" --format table --scanners vuln --skip-files /usr/local/bin/gosu --severity HIGH,CRITICAL --ignore-unfixed

echo "Done."
