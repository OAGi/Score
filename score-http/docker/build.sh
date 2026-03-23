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

mariadb_client_version=$(sed -n 's|^[[:space:]]*<mariadb-client.version>\(.*\)</mariadb-client.version>[[:space:]]*$|\1|p' "$project_dir/pom.xml" | head -n 1)
if [ -z "$mariadb_client_version" ]; then
  echo "Error: unable to resolve mariadb-client.version from $project_dir/pom.xml." >&2
  exit 1
fi

docker_platform=${DOCKER_DEFAULT_PLATFORM:-linux/amd64}
war_name="score-http-$project_version.war"
war_path="$project_dir/target/$war_name"
jdbc_jar_path="$HOME/.m2/repository/org/mariadb/jdbc/mariadb-java-client/$mariadb_client_version/mariadb-java-client-$mariadb_client_version.jar"
image_name="oagi1docker/srt-http-gateway:$project_version"

cleanup() {
  rm -f "$script_dir/score-http.war" "$script_dir/mariadb-java-client-$mariadb_client_version.jar"
}
trap cleanup EXIT

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker is not installed. See https://docs.docker.com/install/ for details." >&2
  exit 1
fi

if [ ! -x "$project_dir/mvnw" ]; then
  echo "Error: Maven wrapper not found at $project_dir/mvnw." >&2
  exit 1
fi

echo "Packaging project with the version $project_version..."
(
  cd "$project_dir"
  ./mvnw clean package -DskipTests=true -Drevision="$project_version"
)

if [ ! -f "$war_path" ]; then
  echo "Error: expected WAR not found: $war_path" >&2
  exit 1
fi

if [ ! -f "$jdbc_jar_path" ]; then
  echo "Error: expected MariaDB JDBC driver not found: $jdbc_jar_path" >&2
  exit 1
fi

echo "Preparing files..."
cp "$war_path" "$script_dir/score-http.war"
cp "$jdbc_jar_path" "$script_dir/mariadb-java-client-$mariadb_client_version.jar"

echo "Building docker image $image_name..."
(
  cd "$script_dir"
  docker build --no-cache --platform "$docker_platform" --build-arg "MARIADB_CLIENT_VERSION=$mariadb_client_version" -f Dockerfile -t "$image_name" .
)

if command -v trivy >/dev/null 2>&1; then
  echo "Scanning vulnerabilities with Trivy (HIGH/CRITICAL, fixed only)..."
  trivy image --format table --scanners vuln --pkg-types os --severity HIGH,CRITICAL --ignore-unfixed "$image_name"
else
  echo "Info: trivy is not installed. Install Trivy to scan vulnerabilities for $image_name."
fi

echo "Done."
