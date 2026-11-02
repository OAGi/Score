#!/usr/bin/env bash
# Regenerate the srt-repo seed (score-repo/docker/oagis.sql) and image so the baked
# schema matches the target version -- automating the manual DB-refresh workflow:
#
#   1. run a fresh <from> srt-repo container         -> clean baseline data on a throwaway volume
#   2. run the <to> backend against it               -> Flyway migrates the schema to <to>
#   3. mysqldump the migrated 'oagi' DB              -> new oagis.sql (via Workbench's MySQL 8 dump)
#   4. score-repo/docker/build.sh <to>               -> bake the new seed into srt-repo:<to>
#   (--push) push the refreshed image
#
# The <from> image seeds clean data; the <to> backend image performs the migration; nothing
# touches your normal local containers (everything runs under a private srtregen-* namespace
# on a throwaway volume and is torn down at the end).
#
# Usage:
#   deploy/regen-repo-image.sh <from> [to] [flags]
#     from   baseline srt-repo tag to seed clean data from (e.g. 3.5.1)   [required]
#     to     target version                                (default: score-repo/VERSION)
#   flags:
#     --push          docker push oagi1docker/srt-repo:<to> after building
#     --db-port N      host port to publish the temp DB on for the dump (default 3306)
#     --backend-image IMG   backend image to migrate with (default oagi1docker/srt-http-gateway:<to>)
#     --keep          do not tear down the temp containers/volume (for debugging)
#     -y, --yes       do not prompt
#
# Requires: docker; Workbench mysqldump (MySQL 8, for --column-statistics=0 vs MariaDB).
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

FROM_VERSION=""; TO_VERSION=""; DO_PUSH=0; KEEP=0; ASSUME_YES=0
BACKEND_IMAGE=""
# DB_PORT / MYSQLDUMP / DB_USER / DB_PASS / DB_NAME come from config.sh (deploy.env).
# --db-port overrides the host port the temp DB is published on for the dump.

while [ $# -gt 0 ]; do
  case "$1" in
    --push)          DO_PUSH=1 ;;
    --keep)          KEEP=1 ;;
    -y|--yes)        ASSUME_YES=1 ;;
    --db-port)       DB_PORT="$2"; shift ;;
    --backend-image) BACKEND_IMAGE="$2"; shift ;;
    -h|--help)       grep -E '^#( |$)' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    --*)             die "unknown flag: $1" ;;
    *)               if [ -z "$FROM_VERSION" ]; then FROM_VERSION="$1"; else TO_VERSION="$1"; fi ;;
  esac
  shift
done

TO_VERSION="${TO_VERSION:-$(default_version)}"
BACKEND_IMAGE="${BACKEND_IMAGE:-$IMAGE_HTTP:$TO_VERSION}"
[ -n "$FROM_VERSION" ] || die "missing <from> baseline version.
  usage: deploy/regen-repo-image.sh <from> [to]
  local srt-repo tags: $(docker images "$IMAGE_REPO" --format '{{.Tag}}' | tr '\n' ' ')"

command -v docker >/dev/null 2>&1 || die "docker not found."
docker info >/dev/null 2>&1 || die "docker daemon not reachable."
require_vars MYSQLDUMP DB_USER DB_PASS DB_NAME DB_PORT
[ -x "$MYSQLDUMP" ] || die "mysqldump not found/executable at $MYSQLDUMP (set MYSQLDUMP in deploy.env)."

SEED="$REPO_ROOT/score-repo/docker/oagis.sql"
NET="srtregen-net"; DBC="srtregen-db"; RC="srtregen-redis"; BC="srtregen-backend"

echo "-------------------------------------------------------------"
echo " regenerate srt-repo seed + image"
echo "   from (seed)  : $IMAGE_REPO:$FROM_VERSION"
echo "   to  (target) : $IMAGE_REPO:$TO_VERSION"
echo "   migrate with : $BACKEND_IMAGE"
echo "   seed file    : $SEED"
echo "   temp DB port : $DB_PORT   push: $DO_PUSH   keep: $KEEP"
echo "-------------------------------------------------------------"
if [ "$ASSUME_YES" != 1 ]; then
  printf 'Proceed? [y/N] '; read -r ans; case "$ans" in y|Y|yes) ;; *) die "aborted." ;; esac
fi

# Ensure the base images are available locally.
docker image inspect "$IMAGE_REPO:$FROM_VERSION" >/dev/null 2>&1 || { log "pulling $IMAGE_REPO:$FROM_VERSION"; docker pull "$IMAGE_REPO:$FROM_VERSION"; }
docker image inspect "$BACKEND_IMAGE" >/dev/null 2>&1 || { log "pulling $BACKEND_IMAGE"; docker pull "$BACKEND_IMAGE"; }

teardown() {
  [ "$KEEP" = 1 ] && { warn "--keep set: leaving $DBC/$RC/$BC and network $NET running"; return; }
  log "tearing down temp containers/volume/network"
  docker rm -f "$BC" "$RC" >/dev/null 2>&1 || true
  docker rm -f -v "$DBC" >/dev/null 2>&1 || true   # -v drops the throwaway data volume
  docker network rm "$NET" >/dev/null 2>&1 || true
}
trap teardown EXIT

# Clean any leftovers from a previous run, then a fresh isolated network.
docker rm -f "$BC" "$RC" "$DBC" >/dev/null 2>&1 || true
docker network rm "$NET" >/dev/null 2>&1 || true
docker network create "$NET" >/dev/null

log "[1/4] starting clean $IMAGE_REPO:$FROM_VERSION (seeds oagis.sql on a throwaway volume)"
docker run -d --name "$DBC" --network "$NET" -p "${DB_PORT}:3306" "$IMAGE_REPO:$FROM_VERSION" >/dev/null

log "waiting for DB init to finish (loading the ~500MB seed can take several minutes)"
db_ready=0
for i in $(seq 1 180); do
  docker inspect -f '{{.State.Running}}' "$DBC" 2>/dev/null | grep -q true || die "db container exited during init:
$(docker logs "$DBC" 2>&1 | tail -30)"
  # -h127.0.0.1 --protocol=tcp only succeeds once the REAL (networked) server is up,
  # i.e. after the entrypoint's temp init server has loaded the seed and shut down.
  if docker exec "$DBC" mariadb -u"$DB_USER" -p"$DB_PASS" -h127.0.0.1 --protocol=tcp "$DB_NAME" -N -e "SELECT 1" >/dev/null 2>&1; then
    db_ready=1; break
  fi
  [ $((i % 6)) -eq 0 ] && printf '   ...%ds\n' $((i*10))
  sleep 10
done
[ "$db_ready" = 1 ] || die "DB did not become ready in ~30min:
$(docker logs "$DBC" 2>&1 | tail -30)"
seed_head=$(docker exec "$DBC" mariadb -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1" 2>/dev/null || echo "?")
log "DB ready; seed Flyway head = $seed_head (expected $FROM_VERSION)"

log "[2/4] starting redis + backend ($BACKEND_IMAGE) to apply Flyway migrations"
docker run -d --name "$RC" --network "$NET" redis:7.4 >/dev/null
docker run -d --name "$BC" --network "$NET" \
  -e DB_HOST="$DBC" -e DB_PORT=3306 -e REDIS_HOST="$RC" -e REDIS_PORT=6379 \
  -e "JAVA_OPTS=-Xmx2048m" "$BACKEND_IMAGE" >/dev/null

log "waiting for backend to migrate schema to $TO_VERSION"
migrated=0
for i in $(seq 1 84); do
  docker inspect -f '{{.State.Running}}' "$BC" 2>/dev/null | grep -q true || die "backend container exited:
$(docker logs "$BC" 2>&1 | tail -40)"
  if docker logs "$BC" 2>&1 | grep -Eq "FlywayValidateException|APPLICATION FAILED TO START"; then
    die "backend failed (Flyway):
$(docker logs "$BC" 2>&1 | grep -iE 'flyway|error|caused by' | tail -25)"
  fi
  head=$(docker exec "$DBC" mariadb -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e "SELECT version FROM flyway_schema_history WHERE success=1 ORDER BY installed_rank DESC LIMIT 1" 2>/dev/null || echo "")
  if [ "$head" = "$TO_VERSION" ]; then migrated=1; break; fi
  [ $((i % 6)) -eq 0 ] && printf '   ...%ds (head=%s)\n' $((i*5)) "${head:-?}"
  sleep 5
done
[ "$migrated" = 1 ] || die "schema did not reach $TO_VERSION in ~7min (head=$(docker exec "$DBC" mariadb -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e 'SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1' 2>/dev/null)):
$(docker logs "$BC" 2>&1 | tail -30)"
log "Flyway head now = $TO_VERSION. Stopping backend (no longer needed)."
docker rm -f "$BC" "$RC" >/dev/null 2>&1 || true

log "[3/4] dumping migrated '$DB_NAME' DB -> new seed (Workbench mysqldump)"
tmp="$SEED.new.$$"
"$MYSQLDUMP" --host=localhost --port="$DB_PORT" --default-character-set=utf8 \
  --user="$DB_USER" --password="$DB_PASS" --protocol=tcp --routines --column-statistics=0 \
  --skip-triggers "$DB_NAME" > "$tmp"

# Validate before clobbering the (git-ignored, unrecoverable) seed.
sz=$(wc -c < "$tmp" | tr -d ' ')
[ "$sz" -gt 52428800 ] || { rm -f "$tmp"; die "new dump suspiciously small (${sz} bytes) -- aborting, seed left intact."; }
grep -q "flyway_schema_history" "$tmp" || { rm -f "$tmp"; die "new dump missing flyway_schema_history -- aborting, seed left intact."; }
tail -c 200 "$tmp" | grep -q "Dump completed" || { rm -f "$tmp"; die "new dump has no completion marker (truncated?) -- aborting, seed left intact."; }

# Back up the previous seed OUTSIDE the docker build context (so it doesn't bloat the build).
bakdir="${TMPDIR:-/tmp}/srt-repo-seed-backups"; mkdir -p "$bakdir"
if [ -f "$SEED" ]; then
  bak="$bakdir/oagis.sql.$(date +%Y%m%d%H%M%S)"
  mv "$SEED" "$bak"; log "previous seed backed up to $bak"
fi
mv "$tmp" "$SEED"
log "new seed in place: $SEED ($(du -h "$SEED" | cut -f1))"

log "[4/4] building $IMAGE_REPO:$TO_VERSION with the refreshed seed"
# The fleet is linux/amd64 and --push uploads this local image, so pin the platform (a bare build
# would use the host's, e.g. arm64 on Apple Silicon).
IMAGE_PLATFORMS="${IMAGE_PLATFORMS:-linux/amd64}" sh "$REPO_ROOT/score-repo/docker/build.sh" "$TO_VERSION"

if [ "$DO_PUSH" = 1 ]; then
  log "pushing $IMAGE_REPO:$TO_VERSION"
  docker push "$IMAGE_REPO:$TO_VERSION"
fi

log "Done. srt-repo:$TO_VERSION now bakes the migrated $TO_VERSION schema."
[ "$DO_PUSH" = 1 ] || log "Not pushed. To publish: docker push $IMAGE_REPO:$TO_VERSION  (then redeploy with WITH_DB=1)."
