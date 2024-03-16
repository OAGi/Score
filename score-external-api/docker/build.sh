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

cd ..

echo "Building project..."
rm -rf dist

echo "Building docker image..."
docker build --no-cache -f docker/Dockerfile --target runner -t oagi1docker/srt-external-api:3.3.0 .

echo "Scanning vulnerabilities..."
docker scout cves oagi1docker/srt-external-api:3.3.0

echo "Done."
