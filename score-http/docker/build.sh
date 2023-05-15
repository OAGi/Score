#!/bin/sh

if ! [[ -x "$(command -v docker)" ]]; then
  echo "Error: docker is not installed. See https://docs.docker.com/install/ for details.'" >&2
  exit 1
fi

echo "Packaging project..."
cd ..
./mvnw clean package -DskipTests=true

echo "Preparing files..."
cp score-http/target/score-http-3.0.3.war docker
cp ~/.m2/repository/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar docker

echo "Building docker image..."
cd docker
docker build --no-cache -f Dockerfile -t oagi1docker/srt-http-gateway:3.0.3 .

echo "Scanning vulnerabilities..."
docker scout cves oagi1docker/srt-http-gateway:3.0.3

echo "Done."
