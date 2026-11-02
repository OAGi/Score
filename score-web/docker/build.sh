#!/bin/sh

set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
project_dir=$(CDPATH= cd -- "$script_dir/.." && pwd)
repo_root=$(CDPATH= cd -- "$project_dir/.." && pwd)

# shellcheck source=../../docker/buildx.sh
. "$repo_root/docker/buildx.sh"

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

node_version=22
if [ -f "$project_dir/.nvmrc" ]; then
  node_version=$(tr -d '[:space:]' < "$project_dir/.nvmrc")
fi
if [ -z "$node_version" ]; then
  node_version=22
fi

image_name="oagi1docker/srt-web:$app_version"
version_file="$project_dir/src/environments/version.ts"
package_json="$project_dir/package.json"
package_lock_json="$project_dir/package-lock.json"

current_node_major=
if command -v node >/dev/null 2>&1; then
  current_node_major=$(node -p 'process.versions.node.split(".")[0]')
fi

use_nvm=false
if [ -n "${current_node_major:-}" ] && [ "$current_node_major" = "$node_version" ] && command -v npm >/dev/null 2>&1; then
  :
elif [ -s "$HOME/.nvm/nvm.sh" ]; then
  use_nvm=true
else
  echo "Error: Node.js $node_version is required for score-web. Current version: ${current_node_major:-not installed}. Install it or activate it with nvm." >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker is not installed. See https://docs.docker.com/install/ for details." >&2
  exit 1
fi

run_project_command() {
  project_command=$1

  if [ "$use_nvm" = "true" ]; then
    bash -lc ". \"$HOME/.nvm/nvm.sh\" && nvm use --silent \"$node_version\" >/dev/null 2>&1 || { echo \"Error: Node.js $node_version is not installed in nvm. Run: nvm install $node_version\" >&2; exit 1; } && cd \"$project_dir\" && $project_command"
  else
    (
      cd "$project_dir"
      sh -c "$project_command"
    )
  fi
}

if [ "$use_nvm" = "true" ]; then
  echo "Using Node.js $node_version via nvm..."
else
  echo "Using Node.js $(node -v)..."
fi

echo "Running 'npm install'..."
echo "Updating package metadata version to $app_version..."
run_project_command "node -e '
const fs = require(\"fs\");
const version = process.argv[1];
const files = process.argv.slice(2);
for (const file of files) {
  if (!fs.existsSync(file)) continue;
  const json = JSON.parse(fs.readFileSync(file, \"utf8\"));
  json.version = version;
  if (json.packages && json.packages[\"\"]) {
    json.packages[\"\"].version = version;
  }
  fs.writeFileSync(file, JSON.stringify(json, null, 2) + \"\n\");
}
' \"$app_version\" \"$package_json\" \"$package_lock_json\""
run_project_command "npm install"

echo "Generating Angular version file..."
cat > "$version_file" <<EOF
export const projectVersion = '$app_version';
EOF

echo "Building project..."
rm -rf "$project_dir/dist"
run_project_command "npx ng build --configuration production --optimization --aot"

echo "Preparing files..."
rm -rf "$script_dir/score-web"
cp -R "$project_dir/dist/score-web" "$script_dir/score-web"

rm -rf "$script_dir/docs"
cp -R "$repo_root/docs/user_guide/_build/html" "$script_dir/docs"

(
  cd "$script_dir"
  buildx_build "$image_name" Dockerfile . --no-cache
)

buildx_scan "$image_name" --format table --scanners vuln --severity HIGH,CRITICAL --ignore-unfixed

echo "Done."
