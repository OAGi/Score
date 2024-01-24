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

echo "Running 'npm install'..."
npm install

echo "Building project..."
rm -rf dist
ng build --configuration production --optimization --aot --build-optimizer --common-chunk --vendor-chunk

echo "Preparing files..."
rm -rf docker/score-web
cp -rf dist/score-web docker/score-web

rm -rf docker/docs
cp -rf ../docs/user_guide/_build/html docker/docs

echo "Building docker image..."
cd docker
docker build --no-cache -f Dockerfile -t oagi1docker/srt-web:3.2.2 .

echo "Scanning vulnerabilities..."
docker scout cves oagi1docker/srt-web:3.2.2

echo "Done."
