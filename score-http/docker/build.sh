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

# Guard against drift between the VERSION file and the pom's <revision> default.
# build.sh passes -Drevision below so the build itself would still succeed, but a mismatch
# means the committed sources drifted (one bumped without the other) -- fail fast so it gets fixed.
pom_revision=$(sed -n 's|^[[:space:]]*<revision>\(.*\)</revision>[[:space:]]*$|\1|p' "$project_dir/pom.xml" | head -n 1)
if [ -z "$pom_revision" ]; then
  echo "Error: <revision> not found in $project_dir/pom.xml. Add it under <properties> and keep it in sync with VERSION." >&2
  exit 1
fi
if [ -f "$project_dir/VERSION" ]; then
  version_file_value=$(tr -d '[:space:]' < "$project_dir/VERSION")
  if [ -n "$version_file_value" ] && [ "$version_file_value" != "$pom_revision" ]; then
    echo "Error: version mismatch between VERSION and pom.xml <revision>." >&2
    echo "  VERSION            = $version_file_value" >&2
    echo "  pom.xml <revision> = $pom_revision" >&2
    echo "Set both to the same value (VERSION is the single source of truth), then re-run." >&2
    exit 1
  fi
fi

# Keep the score-e2e module in lockstep with the same version (it has no VERSION file of its own).
# Skipped if the module is not checked out alongside score-http.
score_e2e_pom="$project_dir/../score-e2e/pom.xml"
if [ -f "$score_e2e_pom" ]; then
  e2e_revision=$(sed -n 's|^[[:space:]]*<revision>\(.*\)</revision>[[:space:]]*$|\1|p' "$score_e2e_pom" | head -n 1)
  if [ -z "$e2e_revision" ]; then
    echo "Error: <revision> not found in $score_e2e_pom. Add it under <properties> and keep it in sync with VERSION." >&2
    exit 1
  fi
  if [ "$e2e_revision" != "$pom_revision" ]; then
    echo "Error: version mismatch between score-http and score-e2e <revision>." >&2
    echo "  score-http pom.xml <revision> = $pom_revision" >&2
    echo "  score-e2e  pom.xml <revision> = $e2e_revision" >&2
    echo "Set both to the same value (VERSION is the single source of truth), then re-run." >&2
    exit 1
  fi
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
