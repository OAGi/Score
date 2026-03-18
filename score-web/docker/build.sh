#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_dir=$(CDPATH= cd -- "$script_dir/.." && pwd)
repo_root=$(CDPATH= cd -- "$project_dir/.." && pwd)

app_version=
if [ -f "$project_dir/VERSION" ]; then
  app_version=$(tr -d '[:space:]' < "$project_dir/VERSION")
fi
if [ -z "$app_version" ] && [ $# -gt 0 ]; then
  app_version=$1
fi
if [ -z "$app_version" ] && [ -n "${IMAGE_VERSION:-}" ]; then
  app_version=$IMAGE_VERSION
fi
if [ -z "$app_version" ]; then
  echo "Error: app.version is not set. Add $project_dir/VERSION, pass it as the first argument, or define IMAGE_VERSION." >&2
  exit 1
fi

image_name="oagi1docker/srt-web:$app_version"
version_file="$project_dir/src/environments/version.ts"

if ! command -v npm >/dev/null 2>&1; then
  echo "Error: npm is not installed. See https://docs.npmjs.com/downloading-and-installing-node-js-and-npm for details." >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker is not installed. See https://docs.docker.com/install/ for details." >&2
  exit 1
fi

echo "Running 'npm install'..."
(
  cd "$project_dir"
  npm install
)

echo "Generating Angular version file..."
cat > "$version_file" <<EOF
export const projectVersion = '$app_version';
EOF

echo "Building project..."
rm -rf "$project_dir/dist"
(
  cd "$project_dir"
  npx ng build --configuration production --optimization --aot
)

echo "Preparing files..."
rm -rf "$script_dir/score-web"
cp -R "$project_dir/dist/score-web" "$script_dir/score-web"

rm -rf "$script_dir/docs"
cp -R "$repo_root/docs/user_guide/_build/html" "$script_dir/docs"

echo "Building docker image $image_name..."
(
  cd "$script_dir"
  docker build --no-cache -f Dockerfile -t "$image_name" .
)

if command -v trivy >/dev/null 2>&1; then
  echo "Scanning vulnerabilities with Trivy (HIGH/CRITICAL, fixed only)..."
  trivy image --format table --scanners vuln --severity HIGH,CRITICAL --ignore-unfixed "$image_name"
else
  echo "Info: trivy is not installed. Install Trivy to scan vulnerabilities for $image_name."
fi

echo "Done."
