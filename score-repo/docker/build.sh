#!/bin/sh

if ! [[ -x "$(command -v docker)" ]]; then
  echo "Error: docker is not installed. See https://docs.docker.com/install/ for details.'" >&2
  exit 1
fi

echo "Building docker image..."
docker build --no-cache -f Dockerfile -t oagi1docker/srt-repo:3.1.0-alpha .

echo "Scanning vulnerabilities..."
docker scout cves oagi1docker/srt-repo:3.1.0-alpha

echo "Done."
