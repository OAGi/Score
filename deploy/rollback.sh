#!/usr/bin/env bash
# Roll an instance back to the previous compose file (the most recent .bak-*),
# or to an explicit version tag.
#
# NOTE: every roll writes a fresh .bak, so after a failed+retried roll the NEWEST
# backup may already hold the new version. `--list` shows each backup's srt-* tags so
# you can pick the right one; the default restore prints the tags it is about to apply.
#
# Usage:
#   deploy/rollback.sh <instance>            # restore the newest ~/docker-compose.yml.bak-*
#   deploy/rollback.sh <instance> <version>  # or re-point tags to <version> and recreate
#   deploy/rollback.sh <instance> --list     # list available compose backups (with their tags)
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

INSTANCE="${1:?usage: deploy/rollback.sh <instance> [version|--list]}"
VERSION="${2:-}"
HOST="$(instance_host "$INSTANCE")" || exit 1

if [ "$VERSION" = "--list" ] || [ "$VERSION" = "-l" ]; then
  log "Compose backups on '$INSTANCE' ($HOST) (newest first):"
  ensure_ssh "$HOST"
  # shellcheck disable=SC2086
  ssh $SSH_OPTS "$SSH_USER@$HOST" 'bash -s' <<'REMOTE'
set -uo pipefail
cd "$HOME"
found=0
for f in $(ls -1t docker-compose.yml.bak-* 2>/dev/null); do
  found=1
  echo "== $f =="
  grep -E "oagi1docker/srt-" "$f" | sed 's/^/     /'
done
[ "$found" = 1 ] || echo "  (no docker-compose.yml.bak-* found)"
REMOTE
  exit 0
fi

if [ -n "$VERSION" ]; then
  require_release_version "$VERSION"
  warn "Rolling '$INSTANCE' to explicit version $VERSION"
  WITH_DB="${WITH_DB:-0}" "$DEPLOY_DIR/remote-deploy.sh" "$INSTANCE" "$VERSION"
  exit 0
fi

log "Restoring newest compose backup on '$INSTANCE' ($HOST)"
ensure_ssh "$HOST"
# shellcheck disable=SC2086
ssh $SSH_OPTS "$SSH_USER@$HOST" 'bash -s' <<'REMOTE'
set -euo pipefail
cd "$HOME"
if docker compose version >/dev/null 2>&1; then DC="docker compose"; else DC="docker-compose"; fi
# Pick the newest backup whose contents DIFFER from the current compose, so a failed+retried
# roll (which backed up the already-broken file) doesn't restore the very version you're
# escaping. If you need a specific one, use: rollback.sh <inst> --list  then  <inst> <version>.
bak=""
for f in $(ls -1t docker-compose.yml.bak-* 2>/dev/null); do
  if ! diff -q "$f" docker-compose.yml >/dev/null 2>&1; then bak="$f"; break; fi
done
[ -n "$bak" ] || { echo "ERROR: no backup differing from the current compose (all identical, or none found). Try: rollback.sh <inst> --list" >&2; exit 1; }
echo "-- restoring from $bak; it pins:"
grep -nE "oagi1docker/srt-" "$bak" | sed 's/^/     /' || true
cp -p "$bak" docker-compose.yml
$DC up -d
$DC ps
REMOTE
log "Rollback done. Verify with: deploy/verify.sh $INSTANCE <version>"
