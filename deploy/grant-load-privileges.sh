#!/usr/bin/env bash
# Let the database user of the application ('oagi') make, use and drop the schemas that loading a release package goes
# through: oagi_load_<id>, oagi_backup_<id>, oagi_discarded_<id> (and oagi_probe_<id>, which the application makes and drops to
# see whether it may). Without this the application says that release packages cannot be loaded, and refuses to.
#
# Runs ON the instance through SSH, inside the db container as the database's root user (its password is in the container's own
# environment, so none is asked for here). Every host the 'oagi' user is known by is granted.
#   GRANT ALL PRIVILEGES ON `oagi\_%`.* TO 'oagi'@'<host>'      -- '_' is escaped, so only oagi_* is named, not oagiXYZ
#
# Usage: deploy/grant-load-privileges.sh <domain> [--check-only]
#   --check-only   only shows what the user may do now; nothing is granted.
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"
DOMAIN="${1:-}"; MODE="${2:-}"
[ -n "$DOMAIN" ] || die "usage: grant-load-privileges.sh <domain> [--check-only]"
case "$DOMAIN" in *[!A-Za-z0-9.-]*) die "invalid domain: $DOMAIN" ;; esac
case "$MODE" in ""|--check-only) ;; *) die "unknown flag: $MODE" ;; esac

# shellcheck disable=SC2086  # SSH_OPTS is a word list
ssh $SSH_OPTS "$SSH_USER@$DOMAIN" "MODE='$MODE' bash -s" <<'REMOTE'
set -euo pipefail
cd "$HOME"
if docker compose version >/dev/null 2>&1; then DC="docker compose"; else DC="docker-compose"; fi
db="$($DC ps -q db)"
[ -n "$db" ] || { echo "no db container" >&2; exit 1; }
sql() { docker exec -i "$db" sh -c 'exec mariadb -uroot -p"$MYSQL_ROOT_PASSWORD" "$@"' sh "$@"; }
hosts="$(sql -N -B -e "SELECT Host FROM mysql.user WHERE User='oagi'")"
[ -n "$hosts" ] || { echo "no 'oagi' database user" >&2; exit 1; }
while IFS= read -r host; do
  if [ -z "$MODE" ]; then
    sql -e "GRANT ALL PRIVILEGES ON \`oagi\\_%\`.* TO 'oagi'@'$host'"
  fi
  echo "-- oagi@$host"; sql -N -B -e "SHOW GRANTS FOR 'oagi'@'$host'"
done <<< "$hosts"
REMOTE
