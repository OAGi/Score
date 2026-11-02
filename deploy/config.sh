# shellcheck shell=bash
# Shared configuration for the connectCenter deploy automation.
# Sourced by the other deploy/*.sh scripts -- do not run directly.
#
# Environment-specific identifiers (the AWS security-group id + rule ids, and any
# credential/path overrides) are NOT hard-coded here. Put them in deploy/deploy.env
# (git-ignored; copy deploy/deploy.env.example). Anything below can also be overridden
# by exporting the same variable before running.

# --- Paths ---
DEPLOY_DIR="$(CDPATH= cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(CDPATH= cd -- "$DEPLOY_DIR/.." && pwd)"

# Local, git-ignored overrides (SG_ID, SG_RULE_IDS, AWS_REGION, SSH_KEY, DB_*, ...).
# shellcheck source=/dev/null
[ -f "$DEPLOY_DIR/deploy.env" ] && . "$DEPLOY_DIR/deploy.env"

# --- Docker images (Docker Hub, oagi1docker org) ---
IMAGE_WEB="${IMAGE_WEB:-oagi1docker/srt-web}"
IMAGE_HTTP="${IMAGE_HTTP:-oagi1docker/srt-http-gateway}"
IMAGE_REPO="${IMAGE_REPO:-oagi1docker/srt-repo}"

# --- SSH / instances ---
# SSH_KEY (the .pem path) lives in deploy.env, not in committed source.
SSH_KEY="${SSH_KEY:-}"
SSH_USER="${SSH_USER:-ec2-user}"
SSH_OPTS="-o ConnectTimeout=20 -o StrictHostKeyChecking=accept-new -o IdentitiesOnly=yes"
[ -n "$SSH_KEY" ] && SSH_OPTS="$SSH_OPTS -i $SSH_KEY"

# instance name -> public host (the whole fleet, so the toolkit works for any of them)
instance_host() {
  case "$1" in
    test)     echo "test.connectcenter.oagi.org" ;;
    main)     echo "connectcenter.oagi.org" ;;
    training) echo "training.connectcenter.oagi.org" ;;
    modeldev) echo "modeldev.connectcenter.oagi.org" ;;
    cloud)    echo "cloud.connectcenter.oagi.org" ;;
    *) echo "unknown instance: $1" >&2; return 1 ;;
  esac
}

# --- AWS security group that gates ingress (SSH + DB) to one trusted IP ---
# SG_ID / SG_RULE_IDS are AWS resource ids -- keep them out of git via deploy/deploy.env.
# The rules cover tcp/22 (SSH) and tcp/3306 (DB); open-sg.sh re-points them to your IP.
AWS_REGION="${AWS_REGION:-us-east-2}"
SG_ID="${SG_ID:-}"
SG_RULE_IDS="${SG_RULE_IDS:-}"

# --- DB / dump settings ---
# Real values (dump tool path, output dir, db name, credentials) live in deploy/deploy.env,
# NOT in committed source. Only DB_PORT keeps a default (the standard MySQL/MariaDB port).
DUMPS_DIR="${DUMPS_DIR:-}"
MYSQLDUMP="${MYSQLDUMP:-}"
DB_USER="${DB_USER:-}"
DB_PASS="${DB_PASS:-}"
DB_NAME="${DB_NAME:-}"
DB_PORT="${DB_PORT:-3306}"

# Default version = the single source of truth (score-http/VERSION).
default_version() {
  tr -d '[:space:]' < "$REPO_ROOT/score-http/VERSION"
}

log()  { printf '\033[1;34m==>\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m!!\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31mERROR:\033[0m %s\n' "$*" >&2; exit 1; }

# Fail early (with guidance) when a step needs values that only live in deploy.env.
require_vars() {
  local v missing=""
  for v in "$@"; do
    [ -n "${!v:-}" ] || missing="$missing $v"
  done
  [ -z "$missing" ] || die "missing config:$missing — set in deploy/deploy.env (copy deploy.env.example)."
}
require_sg() { require_vars SG_ID SG_RULE_IDS; }

# Refuse an obviously non-release version (…-dev / …SNAPSHOT…) for fleet-mutating steps,
# so the score-http/VERSION default on a dev branch (e.g. 3.6.0-dev) can't silently
# poison a remote compose file. Override with ALLOW_DEV_VERSION=1.
require_release_version() {
  # A version is interpolated into a sed replacement and an ssh command string; reject any
  # char outside [A-Za-z0-9._-] so it can't corrupt the compose file or the remote command.
  case "$1" in
    *[!A-Za-z0-9._-]*)
      die "version '$1' has characters outside [A-Za-z0-9._-] — refusing (would corrupt the compose sed / ssh command)." ;;
  esac
  case "$1" in
    *-dev|*SNAPSHOT*)
      [ "${ALLOW_DEV_VERSION:-0}" = 1 ] || \
        die "refusing non-release version '$1' (dev/snapshot). Pass an explicit release tag (e.g. 3.5.2), or set ALLOW_DEV_VERSION=1 to override." ;;
  esac
}

# --- SSH reachability preflight ---
# Non-interactive probe: is SSH to $1 reachable right now?
ssh_ok() {
  local key_opt=""
  [ -n "${SSH_KEY:-}" ] && key_opt="-i $SSH_KEY"
  # shellcheck disable=SC2086
  ssh -o BatchMode=yes -o ConnectTimeout=8 -o StrictHostKeyChecking=accept-new -o IdentitiesOnly=yes \
    $key_opt "$SSH_USER@$1" true >/dev/null 2>&1
}
# Ensure SSH to $1 works; if not, re-point the SG to your current IP and retry.
# (open-sg.sh opens both the SSH (22) and DB (3306) rules.)
ensure_ssh() {
  local host="$1" i
  require_vars SSH_KEY
  if ssh_ok "$host"; then return 0; fi
  warn "SSH to $host not reachable — opening the security group to your current IP..."
  "$DEPLOY_DIR/open-sg.sh"
  for i in 1 2 3 4 5 6; do
    sleep 5
    if ssh_ok "$host"; then log "SSH to $host reachable."; return 0; fi
  done
  die "SSH to $host still unreachable after opening the SG. Check VPN / AWS creds / instance state."
}
