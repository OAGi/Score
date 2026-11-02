#!/bin/sh

if ! [[ -x "$(command -v npm)" ]]; then
  echo "Error: npm is not installed. See https://docs.npmjs.com/downloading-and-installing-node-js-and-npm for details." >&2
  exit 1
fi

if ! [[ -x "$(command -v ng)" ]]; then
  echo "Error: Angular CLI is not installed. Try 'npm install -g @angular/cli. See https://cli.angular.io/ for details.'" >&2
  exit 1
fi

if ! [[ -x "$(command -v docker)" ]]; then
  echo "Error: docker is not installed. See https://docs.docker.com/install/ for details.'" >&2
  exit 1
fi

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

# shellcheck source=../../docker/buildx.sh
. "$script_dir/../../docker/buildx.sh"

cd "$script_dir/.."

echo "Building project..."
rm -rf dist

image_name="oagi1docker/srt-external-api:3.4.2"
buildx_build "$image_name" docker/Dockerfile . --no-cache --target runner

if [ "${PUSH_IMAGE:-0}" = 1 ] || [ "${PUSH_IMAGE:-0}" = true ]; then
  echo "Info: pushed multi-arch image; scan it with: docker scout cves registry://$image_name"
else
  echo "Scanning vulnerabilities..."
  docker scout cves "$image_name"
fi

echo "Done."
