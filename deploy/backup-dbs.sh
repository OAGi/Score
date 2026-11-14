#!/usr/bin/env bash
# DB backup of one fleet instance: mysqldump of its 'oagi' DB -> <domain>_YYYYMMDD.sql.
#
# The DB port (3306) is not exposed on the instances (it is bound to 127.0.0.1 and not open in the
# security group), so the dump runs ON the instance through SSH, using the "score-db dump" helper
# the provisioning tool installs (it runs mysqldump inside the db container; no password needed).
# Only SSH (tcp/22) has to be reachable from this machine.
#
# Usage:
#   deploy/backup-dbs.sh <domain> [flags]
#     domain       e.g. connectcenter.oagi.org, cloud.connectcenter.oagi.org
#   flags:
#     --out DIR    output directory (default: DUMPS_DIR from deploy.env, else ~/dumps)
#
# Output: <out>/<domain>_YYYYMMDD.sql  (InnoDB snapshot via --single-transaction, no table locks)
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

OUT_DIR="${DUMPS_DIR:-$HOME/dumps}"; DOMAIN=""
while [ $# -gt 0 ]; do
  case "$1" in
    --out)     [ $# -ge 2 ] || die "--out needs a directory."; OUT_DIR="$2"; shift ;;
    -h|--help) grep -E '^#( |$)' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    -*)        die "unknown flag: $1" ;;
    *)         [ -z "$DOMAIN" ] || die "only one domain is accepted (got '$DOMAIN' and '$1')."; DOMAIN="$1" ;;
  esac
  shift
done
[ -n "$DOMAIN" ] || die "usage: backup-dbs.sh <domain> [--out DIR]"
# The domain ends up in a file name: reject anything that is not a plain host name.
case "$DOMAIN" in *[!A-Za-z0-9.-]*) die "invalid domain: $DOMAIN" ;; esac

mkdir -p "$OUT_DIR"
out="$OUT_DIR/${DOMAIN}_$(date +%Y%m%d).sql"
tmp="$out.part"; err="$out.err"
trap 'rm -f "$tmp" "$err"' EXIT

log "dumping $DOMAIN -> $out"
# shellcheck disable=SC2086  # SSH_OPTS is a word list
if ! ssh $SSH_OPTS "$SSH_USER@$DOMAIN" \
     'sudo score-db dump -uoagi --default-character-set=utf8 --single-transaction --routines oagi' \
     > "$tmp" 2> "$err"; then
  die "dump FAILED -- $(tail -2 "$err" | tr '\n' ' ')"
fi
sz=$(wc -c < "$tmp" | tr -d ' ')
if [ "$sz" -le 102400 ] || ! tail -c 200 "$tmp" | grep -q "Dump completed"; then
  die "dump looks incomplete (size=$sz, no completion marker) -- discarded."
fi
mv "$tmp" "$out"
log "ok: $out ($(du -h "$out" | cut -f1))"
