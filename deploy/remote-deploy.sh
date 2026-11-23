#!/usr/bin/env bash
# Roll a fleet instance to <version>: on the remote host, prune disk, VALIDATE+PULL the
# target images, back up the compose file, bump the oagi1docker/srt-* image tags, recreate,
# and wait for a clean backend startup. Only services whose image tag changed are recreated
# (db/redis/connect-center-api are left running unless their tag moves).
#
# The target images are pulled BEFORE the compose file is edited, so a bad or missing tag
# aborts with the compose file left UNCHANGED (no "bumped-but-not-recreated" window).
#
# Env:
#   IMAGES=web,http,repo  which images to roll (default all; e.g. IMAGES=http for backend only).
#   TAG_WEB/TAG_HTTP/TAG_REPO  per-image tag overrides (default: <version>).
#   WITH_DB=1  also bump/recreate db (srt-repo). Safe: same MariaDB engine on a
#              persistent volume; schema is driven by the backend's runtime Flyway.
#   WITH_DB=0  leave db untouched (frontend + backend only).
#   ALLOW_DEV_VERSION=1  permit a -dev/SNAPSHOT version (normally refused).
#
# Usage: deploy/remote-deploy.sh <instance> [version]
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

INSTANCE="${1:?usage: deploy/remote-deploy.sh <instance> [version]}"
VERSION="${2:-$(default_version)}"
WITH_DB="${WITH_DB:-1}"
HOST="$(instance_host "$INSTANCE")" || exit 1
normalize_images
T_WEB="$(tag_for web "$VERSION")"; T_HTTP="$(tag_for http "$VERSION")"; T_REPO="$(tag_for repo "$VERSION")"
for t in "$T_WEB" "$T_HTTP" "$T_REPO"; do require_release_version "$t"; done

require_release_version "$VERSION"

log "Deploying $VERSION to '$INSTANCE' ($HOST); images=$IMAGES"
ensure_ssh "$HOST"

# shellcheck disable=SC2086
ssh $SSH_OPTS "$SSH_USER@$HOST" \
  "VERSION='$VERSION' IMAGES='$IMAGES' T_WEB='$T_WEB' T_HTTP='$T_HTTP' T_REPO='$T_REPO' IMAGE_WEB='$IMAGE_WEB' IMAGE_HTTP='$IMAGE_HTTP' IMAGE_REPO='$IMAGE_REPO' bash -s" <<'REMOTE'
set -euo pipefail
cd "$HOME"
COMPOSE="$HOME/docker-compose.yml"
[ -f "$COMPOSE" ] || { echo "ERROR: $COMPOSE not found" >&2; exit 1; }
ts=$(date +%Y%m%d%H%M%S)

# Use whichever Compose CLI this host actually has (v2 plugin preferred, v1 fallback).
if docker compose version >/dev/null 2>&1; then DC="docker compose"; else DC="docker-compose"; fi

echo "-- disk before --"; df -h / | tail -1
echo "-- prune unreferenced images/build cache (never touches running/tagged images) --"
docker image prune -f    >/dev/null 2>&1 || true
docker builder prune -af >/dev/null 2>&1 || true
echo "-- disk after prune --"; df -h / | tail -1

# Validate + fetch the target images BEFORE touching the compose file, so a bad/missing
# tag aborts cleanly (compose left unchanged) instead of pinning it to a phantom tag.
echo "-- pre-pull target images @ $VERSION --"
has() { case ",$IMAGES," in *",$1,"*) return 0 ;; *) return 1 ;; esac; }
imgs=""
has web  && imgs="$imgs $IMAGE_WEB:$T_WEB"
has http && imgs="$imgs $IMAGE_HTTP:$T_HTTP"
has repo && imgs="$imgs $IMAGE_REPO:$T_REPO"
for im in $imgs; do
  echo "   pull $im"
  docker pull "$im" >/dev/null || { echo "ERROR: cannot pull $im -- compose file left UNCHANGED." >&2; exit 2; }
done

echo "-- backup: $COMPOSE.bak-${VERSION}-${ts}"
cp -p "$COMPOSE" "$COMPOSE.bak-${VERSION}-${ts}"

echo "-- bump image tags -> $VERSION"
has web  && sed -i -E "s#(${IMAGE_WEB}:)[A-Za-z0-9._-]+#\1${T_WEB}#"   "$COMPOSE"
has http && sed -i -E "s#(${IMAGE_HTTP}:)[A-Za-z0-9._-]+#\1${T_HTTP}#" "$COMPOSE"
has repo && sed -i -E "s#(${IMAGE_REPO}:)[A-Za-z0-9._-]+#\1${T_REPO}#" "$COMPOSE"
grep -nE "(${IMAGE_WEB}|${IMAGE_HTTP}|${IMAGE_REPO}):" "$COMPOSE" || true

echo "-- up -d (recreates only changed services) --"
$DC up -d

back=$($DC ps -q backend)
ok=0
if ! has http; then
  echo "-- backend image not rolled; skipping startup wait --"; ok=1
else
echo "-- waiting for backend clean startup --"
for _ in $(seq 1 72); do
  # Stream to grep (short-circuits on match) instead of slurping the whole log into a
  # variable -- the backend can log verbose DEBUG jOOQ, so slurping is huge and slow.
  # `{ ... || true; }` so grep -q closing the pipe early (SIGPIPE 141 on docker logs)
  # doesn't make pipefail report the match as a failure.
  if { docker logs "$back" 2>&1 || true; } | grep -q "Started ScoreHttpApplication"; then ok=1; break; fi
  # A startup failure, if any, is in the most recent lines -- only scan the tail.
  if { docker logs --tail 500 "$back" 2>&1 || true; } | grep -Eq "FlywayValidateException|APPLICATION FAILED TO START"; then
    echo "!! backend failed to start:"; docker logs --tail 500 "$back" 2>&1 | grep -iE "flyway|error|caused by" | tail -25 || true
    exit 3
  fi
  sleep 5
done
[ "$ok" = "1" ] || { echo "!! backend did not report 'Started ScoreHttpApplication' in ~6min"; docker logs "$back" 2>&1 | tail -40; exit 4; }
fi
[ "$ok" = "1" ] && has http && echo "backend: Started ScoreHttpApplication"

echo "-- container status --"
$DC ps
REMOTE

log "Remote roll complete. Verifying..."
IMAGES="$IMAGES" TAG_WEB="$T_WEB" TAG_HTTP="$T_HTTP" TAG_REPO="$T_REPO" "$DEPLOY_DIR/verify.sh" "$INSTANCE" "$VERSION"
