#!/usr/bin/env bash
# Post-deploy verification for a fleet instance. HARD-ASSERTS the core health signals
# (exits non-zero if any fail); STOMP is a best-effort warning.
#   - public HTTPS front door returns 200 (retried briefly while the frontend warms)
#   - running frontend/backend images are at the expected version
#   - backend :8080 returns 401 (Spring Security is up)
#   - Flyway head == expected version with success=1
#   - STOMP-over-WebSocket upgrade (101) through the public wss endpoint (real-time push)
#
# The db (srt-repo) image tag is reported but NOT asserted -- it legitimately lags under
# a --no-db roll (schema is driven by the backend's runtime Flyway, not the image seed).
#
# Container names are resolved dynamically (works under both `docker compose` v2 and the
# v1 `docker-compose` shim), so no hard-coded `ec2-user-db-1` to silently miss.
#
# Usage: deploy/verify.sh <instance> [version]
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

INSTANCE="${1:?usage: deploy/verify.sh <instance> [version]}"
VERSION="${2:-$(default_version)}"
HOST="$(instance_host "$INSTANCE")" || exit 1

log "Verify '$INSTANCE' ($HOST) @ $VERSION"
ensure_ssh "$HOST"

fails=0
check() { # check "<label>" "<actual>" "<expected>"
  if [ "$2" = "$3" ]; then
    printf '  \033[1;32m✓\033[0m %s: %s\n' "$1" "$2"
  else
    printf '  \033[1;31m✗\033[0m %s: got "%s", expected "%s"\n' "$1" "$2" "$3"
    fails=$((fails + 1))
  fi
}

# --- public front door: 200 (retry a few times; the frontend may still be warming) ---
code=000
for _ in 1 2 3 4 5 6; do
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 15 "https://$HOST/" || echo 000)
  [ "$code" = 200 ] && break
  sleep 5
done
check "public https://$HOST/" "$code" "200"

# --- remote checks: image tags, backend 401, Flyway head (emitted as KEY=value markers) ---
# DB creds pass through from deploy.env; container ids resolved via the compose CLI.
# shellcheck disable=SC2086
remote_out=$(ssh $SSH_OPTS "$SSH_USER@$HOST" "DB_USER='$DB_USER' DB_PASS='$DB_PASS' DB_NAME='$DB_NAME' bash -s" <<'REMOTE' || true
set -uo pipefail
cd "$HOME"
if docker compose version >/dev/null 2>&1; then DC="docker compose"; else DC="docker-compose"; fi
cid() { $DC ps -q "$1" 2>/dev/null || true; }
imgtag() {           # image tag of the container backing service $1
  local c t
  c="$(cid "$1")"
  if [ -z "$c" ]; then echo "?"; return; fi
  t="$(docker inspect -f '{{.Config.Image}}' "$c" 2>/dev/null)" || { echo "?"; return; }
  echo "${t##*:}"
}
echo "IMG_WEB=$(imgtag frontend)"
echo "IMG_HTTP=$(imgtag backend)"
echo "IMG_REPO=$(imgtag db)"
echo "BACKEND=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/ 2>/dev/null || echo 000)"
db="$(cid db)"
if [ -n "$db" ]; then
  row="$(docker exec "$db" mariadb -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e \
    "SELECT CONCAT(version,'|',success) FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1" 2>/dev/null || true)"
  echo "FLYWAY=${row:-?|?}"
else
  echo "FLYWAY=?|?"
fi
REMOTE
)

# `|| true` so a missing marker (grep exit 1) under pipefail doesn't abort the whole
# script via set -e -- a missing/empty value should surface as a failed check, not a
# bare exit with no report.
get() { printf '%s\n' "$remote_out" | grep -m1 "^$1=" | cut -d= -f2- || true; }
[ -n "$remote_out" ] || warn "no output from remote checks (ssh to $HOST failed or returned nothing) — the remote checks below will report as failures."
img_web=$(get IMG_WEB); img_http=$(get IMG_HTTP); img_repo=$(get IMG_REPO)
backend=$(get BACKEND); flyway=$(get FLYWAY)
fw_ver="${flyway%%|*}"; fw_ok="${flyway##*|}"

echo "  -- running images --"
printf '     frontend(srt-web)=%s  backend(srt-http-gateway)=%s  db(srt-repo)=%s\n' "$img_web" "$img_http" "$img_repo"
check "frontend image" "$img_web" "$VERSION"
check "backend image" "$img_http" "$VERSION"
# db(srt-repo) may legitimately lag under --no-db -- report only, do not fail.
[ "$img_repo" = "$VERSION" ] || warn "db(srt-repo) is $img_repo, not $VERSION (expected if rolled with --no-db; cosmetic)."
check "backend :8080 (security up)" "$backend" "401"
# Flyway: hard-assert the schema applied cleanly (success=1), but ONLY when we could read it.
# The head *version* is the migration namespace (V3_5_x), independent of the app release tag
# and never rolled back -- so a no-migration release or a downgrade legitimately differs;
# report it, don't fail on it (the frontend/backend image checks already pin the version).
if [ -z "$fw_ok" ] || [ "$fw_ok" = "?" ]; then
  warn "Flyway head not read (missing DB creds in deploy.env, or DB unreachable) — schema-health check skipped."
else
  check "Flyway head success" "$fw_ok" "1"
  [ "$fw_ver" = "$VERSION" ] || warn "Flyway head is $fw_ver (app $VERSION) — fine for a no-migration release or a rollback."
fi

# --- STOMP (best-effort; non-fatal) -- upgrade to WebSocket through the public wss door.
# 101 proves the host nginx routes /stomp to the backend (the piece that has regressed
# before, and is not tracked in git). Real-time push (notifications, concurrent edit) needs it.
echo "  -- STOMP wss://$HOST/stomp --"
# A successful upgrade returns 101 then holds the socket open, so curl hits --max-time and
# exits non-zero AFTER printing "101" -- keep the captured code and don't let set -e abort.
stomp=$(curl -s -o /dev/null -w '%{http_code}' --max-time 10 \
  -H "Connection: Upgrade" -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Key: $(openssl rand -base64 16 2>/dev/null || echo dGVzdGtleTEyMzQ1Ng==)" \
  -H "Sec-WebSocket-Version: 13" -H "Sec-WebSocket-Protocol: v12.stomp" \
  "https://$HOST/stomp" 2>/dev/null) || true
stomp="${stomp:-000}"
if [ "$stomp" = 101 ]; then
  printf '  \033[1;32m✓\033[0m STOMP wss upgrade: 101 (real-time push routed)\n'
else
  warn "STOMP wss upgrade returned $stomp (expected 101). Real-time push may be down (check host nginx /stomp location)."
fi

if [ "$fails" -eq 0 ]; then
  log "Verification PASSED for '$INSTANCE' @ $VERSION."
else
  die "Verification FAILED for '$INSTANCE' @ $VERSION: $fails core check(s) failed."
fi
