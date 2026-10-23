#!/usr/bin/env bash
# Pre-deploy fleet DB backup: mysqldump each instance's 'oagi' DB over TCP:3306, then
# bundle the day's dumps into a single ~/dumps/YYYYMMDD.zip.
#
# Reaching :3306 requires the fleet SG's DB (3306) rule to point at your current IP --
# all fleet instances share that SG, so this script opens it first via deploy/open-sg.sh
# (skip with --no-sg). SG id/rule ids come from deploy/deploy.env (git-ignored).
#
# Dump flags mirror the established manual command exactly (MySQL 8 mysqldump is required
# for --column-statistics=0 against MariaDB). Note: no --single-transaction, so the dump
# briefly LOCK TABLES READ -- same as the manual command; pass MYSQLDUMP_EXTRA=--single-transaction
# for a non-blocking InnoDB snapshot if you prefer.
#
# Usage:
#   deploy/backup-dbs.sh [host ...] [flags]
#     host...      override the default host list (defaults to main/training/modeldev/cloud)
#   flags:
#     --no-sg      don't re-point the SG (assume 3306 is already open to your IP)
#     --keep-sql   keep the raw .sql files (default: remove them once the zip verifies)
#     --out DIR    output directory (default: ~/dumps)
#     -y, --yes    no confirmation prompt
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

# DUMPS_DIR / MYSQLDUMP / DB_USER / DB_PASS / DB_NAME / DB_PORT come from config.sh (deploy.env).
MYSQLDUMP_EXTRA="${MYSQLDUMP_EXTRA:-}"

# Instances backed up before a deploy (test is excluded on purpose -- it's the pre-release box).
DEFAULT_HOSTS="connectcenter.oagi.org training.connectcenter.oagi.org modeldev.connectcenter.oagi.org cloud.connectcenter.oagi.org"

DO_SG=1; KEEP_SQL=0; ASSUME_YES=0; HOSTS=""
while [ $# -gt 0 ]; do
  case "$1" in
    --no-sg)    DO_SG=0 ;;
    --keep-sql) KEEP_SQL=1 ;;
    --out)      DUMPS_DIR="$2"; shift ;;
    -y|--yes)   ASSUME_YES=1 ;;
    -h|--help)  grep -E '^#( |$)' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    --*)        die "unknown flag: $1" ;;
    *)          HOSTS="$HOSTS $1" ;;
  esac
  shift
done
HOSTS="${HOSTS:-$DEFAULT_HOSTS}"

require_vars DUMPS_DIR MYSQLDUMP DB_USER DB_PASS DB_NAME DB_PORT
[ -x "$MYSQLDUMP" ] || die "mysqldump not found/executable at $MYSQLDUMP (set MYSQLDUMP in deploy.env)."
command -v zip   >/dev/null 2>&1 || die "zip not found."
command -v unzip >/dev/null 2>&1 || die "unzip not found."
mkdir -p "$DUMPS_DIR"
DATE="$(date +%Y%m%d)"

echo "-------------------------------------------------------------"
echo " pre-deploy DB backup"
echo "   hosts : $HOSTS"
echo "   out   : $DUMPS_DIR   archive: $DATE.zip   keep-sql: $KEEP_SQL"
echo "   open-sg first: $DO_SG"
echo "-------------------------------------------------------------"
if [ "$ASSUME_YES" != 1 ]; then
  printf 'Proceed? [y/N] '; read -r ans; case "$ans" in y|Y|yes) ;; *) die "aborted." ;; esac
fi

# Ensure :3306 (and :22) are open to the current IP across the fleet SG.
[ "$DO_SG" = 1 ] && "$DEPLOY_DIR/open-sg.sh"

ok_files=(); ok_hosts=(); fail_hosts=()
for host in $HOSTS; do
  out="$DUMPS_DIR/${host}_${DATE}.sql"
  tmp="$out.part"; err="$out.err"
  log "dumping $host -> $(basename "$out")"
  if "$MYSQLDUMP" --host="$host" --port="$DB_PORT" --default-character-set=utf8 \
       --user="$DB_USER" --password="$DB_PASS" --protocol=tcp --routines \
       --column-statistics=0 --skip-triggers $MYSQLDUMP_EXTRA "$DB_NAME" > "$tmp" 2> "$err"; then
    sz=$(wc -c < "$tmp" | tr -d ' ')
    if [ "$sz" -gt 102400 ] && tail -c 200 "$tmp" | grep -q "Dump completed"; then
      mv "$tmp" "$out"; rm -f "$err"
      ok_files+=("$out"); ok_hosts+=("$host")
      log "  ok ($(du -h "$out" | cut -f1))"
    else
      warn "  $host: dump looks incomplete (size=$sz, no completion marker) -- discarding"
      rm -f "$tmp"; fail_hosts+=("$host")
    fi
  else
    warn "  $host: mysqldump FAILED -- $(grep -vi 'using a password' "$err" | tail -2 | tr '\n' ' ')"
    rm -f "$tmp" "$err"; fail_hosts+=("$host")
  fi
done

zipf="$DUMPS_DIR/${DATE}.zip"
if [ "${#ok_files[@]}" -gt 0 ]; then
  log "archiving ${#ok_files[@]} dump(s) -> $(basename "$zipf")"
  ( cd "$DUMPS_DIR" && zip -q -j "$zipf" "${ok_files[@]##*/}" )
  unzip -tq "$zipf" >/dev/null || die "zip verification failed for $zipf (raw .sql kept)."
  if [ "$KEEP_SQL" != 1 ]; then rm -f "${ok_files[@]}"; log "removed raw .sql (kept the verified zip)"; fi
  log "archive ready: $zipf ($(du -h "$zipf" | cut -f1))"
else
  warn "no successful dumps -- no archive created."
fi

echo "-------------------------------------------------------------"
echo " backed up : ${ok_hosts[*]:-none}"
[ "${#fail_hosts[@]}" -gt 0 ] && echo " FAILED    : ${fail_hosts[*]}"
echo "-------------------------------------------------------------"
[ "${#fail_hosts[@]}" -eq 0 ] || exit 1
