#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_dir=$(CDPATH= cd -- "$script_dir/.." && pwd)

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

echo "Building docker image $image_name..."
(
  cd "$script_dir"
  docker build --no-cache -f Dockerfile -t "$image_name" .
)

if command -v trivy >/dev/null 2>&1; then
  echo "Scanning vulnerabilities with Trivy (HIGH/CRITICAL, fixed only)..."
  trivy image --format table --severity HIGH,CRITICAL --ignore-unfixed "$image_name"
else
  echo "Info: trivy is not installed. Install Trivy to scan vulnerabilities for $image_name."
fi

echo "Done."
