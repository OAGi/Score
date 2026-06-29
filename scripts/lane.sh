#!/usr/bin/env bash
# lane.sh — spin up / drive / merge fully isolated development lanes.
#
# Each lane N is an independent docker compose project (score-lane-N) holding its own
# MariaDB, Redis, and a code-workspace container that clones develop -> feature/lane-N.
# Ports are offset by N*100 so multiple lanes coexist on one machine:
#
#            lane 0      lane 1      lane 2
#   web      4200        4300        4400
#   api      9000        9100        9200
#   db       3307        3407        3507
#   redis    6380        6480        6580
#
# Usage:
#   scripts/lane.sh up    N      # build + start db/redis/dev; clone+branch feature/lane-N
#   scripts/lane.sh serve N      # build & run backend + frontend inside the lane container
#   scripts/lane.sh logs  N [api|web]
#   scripts/lane.sh shell N      # bash into the workspace container
#   scripts/lane.sh stop  N      # stop the app processes (keep containers/data)
#   scripts/lane.sh merge N      # bundle feature/lane-N back to host & merge into develop
#   scripts/lane.sh down  N [--purge]   # tear down (REMOVES workspace + data volumes)
#   scripts/lane.sh list         # show running lanes
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT/docker-compose.dev.yml"
cd "$ROOT"

die() { echo "✗ $*" >&2; exit 1; }
require_n() { [[ "${1:-}" =~ ^[0-9]$ ]] || die "lane number (0-9) required, got '${1:-}'"; }

export_ports() {
  local n="$1" off=$(( $1 * 100 ))
  export LANE_N="$n"
  export FE_PORT_HOST=$(( 4200 + off ))
  export BE_PORT_HOST=$(( 9000 + off ))
  export DB_PORT_HOST=$(( 3307 + off ))
  export REDIS_PORT_HOST=$(( 6380 + off ))
  export GIT_USER_NAME="$(git config user.name  2>/dev/null || echo 'Hakju Oh')"
  export GIT_USER_EMAIL="$(git config user.email 2>/dev/null || echo 'dev-lane@local')"
}

dc() { docker compose -f "$COMPOSE_FILE" "$@"; }

port_table() {
  cat <<EOF

  lane $LANE_N  ·  branch feature/lane-$LANE_N
  ──────────────────────────────────────────────
  frontend   http://localhost:$FE_PORT_HOST
  backend    http://localhost:$BE_PORT_HOST
  mariadb    localhost:$DB_PORT_HOST   (db oagi · user oagi · pw oagi)
  redis      localhost:$REDIS_PORT_HOST
EOF
}

cmd_up() {
  require_n "$1"; export_ports "$1"
  mkdir -p "$ROOT/.lanes/lane-$LANE_N"
  echo "▶ lane $LANE_N: building dev image + starting db/redis/dev"
  echo "  (the MariaDB init script is heavy — the dev container waits for DB health first)…"
  dc up -d --build
  # `up -d` returns once the dev container has *started* its entrypoint — but on a fresh
  # lane the entrypoint is still cloning develop into the workspace, so probing git too
  # early races the clone. Wait until the workspace HEAD resolves before reporting.
  echo "✔ stack healthy; waiting for the workspace clone to finish…"
  local i=0
  until dc exec -T dev bash -lc 'git -C /workspace rev-parse --verify -q HEAD >/dev/null 2>&1'; do
    i=$(( i + 1 ))
    [ "$i" -gt 90 ] && die "workspace clone did not complete in time (docker logs score-lane-${LANE_N}-dev-1)"
    sleep 2
  done
  dc exec -T dev bash -lc 'cd /workspace && echo "  branch $(git branch --show-current) @ $(git rev-parse --short HEAD)"'
  port_table
  echo
  echo "  next:  scripts/lane.sh serve $LANE_N      # build & run the app"
  echo "         scripts/lane.sh shell $LANE_N      # work inside the lane"
}

cmd_serve() {
  require_n "$1"; export_ports "$1"
  echo "▶ lane $LANE_N: starting backend + frontend inside the container (first run installs deps)…"
  dc exec -T dev bash -lc '
    set -e
    cd /workspace
    # 0 if something is already listening on a container port. Used instead of pgrep, which
    # self-matches: this very script is the argv of its bash -lc parent, and that argv contains
    # the literal strings "spring-boot:run"/"ng serve" — so pgrep -f would always "find" them.
    listening() { (exec 3<>/dev/tcp/127.0.0.1/"$1") 2>/dev/null; }
    # browser STOMP/WebSocket must hit this lanes published backend port
    sed -i "s#ws://127.0.0.1:[0-9]\+/ws#ws://127.0.0.1:${BE_PORT_HOST}/ws#" \
      score-web/src/environments/environment.ts || true
    mkdir -p .lane
    if listening 9000; then echo "  backend  already running (:9000)"; else
      ( cd score-http && setsid nohup ./mvnw -Pdev spring-boot:run \
          >/workspace/.lane/api.log 2>&1 </dev/null & )
      echo "  backend  started (compiling + Flyway migrate — ~1-2 min; logs: .lane/api.log)"
    fi
    if [ ! -d score-web/node_modules ]; then
      # this repo commits no package-lock.json (.npmrc sets legacy-peer-deps) -> npm install,
      # not npm ci (which hard-requires a lockfile).
      if [ -f score-web/package-lock.json ]; then
        echo "  installing web deps (npm ci)…";      ( cd score-web && npm ci )
      else
        echo "  installing web deps (npm install)…"; ( cd score-web && npm install )
      fi
    fi
    if listening 4200; then echo "  frontend already running (:4200)"; else
      setsid nohup npm --prefix score-web start -- --host 0.0.0.0 --port 4200 \
        >/workspace/.lane/web.log 2>&1 </dev/null &
      echo "  frontend started (logs: .lane/web.log)"
    fi
  '
  port_table
  echo
  echo "  tail:  scripts/lane.sh logs $LANE_N api    |    scripts/lane.sh logs $LANE_N web"
  echo "  note:  GitHub integration is OFF in lanes; use a separate browser profile per lane"
  echo "         (localhost cookies ignore port, so lanes share the SESSION cookie)."
}

cmd_logs()  { require_n "$1"; export_ports "$1"; dc exec dev tail -n 100 -f "/workspace/.lane/${2:-api}.log"; }
cmd_shell() { require_n "$1"; export_ports "$1"; dc exec dev bash; }
cmd_stop()  {
  require_n "$1"; export_ports "$1"
  dc exec -T dev bash -lc 'pkill -f spring-boot:run 2>/dev/null || true; pkill -f "ng serve" 2>/dev/null || true; echo "✔ app processes stopped"'
}

cmd_merge() {
  require_n "$1"; export_ports "$1"
  local bundle="$ROOT/.lanes/lane-$LANE_N/lane-$LANE_N.bundle"
  echo "▶ lane $LANE_N: bundling feature/lane-$LANE_N from the container…"
  if ! dc exec -T dev bash -lc '
        cd /workspace
        # The bundle ships COMMITTED history only, so a dirty working tree never leaks into it.
        # We still refuse on uncommitted tracked work so it is not silently dropped — except
        # environments/environment.ts, which `serve` always rewrites (stomp port) as a lane-local hack.
        dirty="$(git status --porcelain --untracked-files=no | grep -v "score-web/src/environments/environment.ts" || true)"
        if [ -n "$dirty" ]; then
          echo "  uncommitted tracked changes in the lane — commit them first:" >&2
          printf "%s\n" "$dirty" | sed "s/^/    /" >&2
          echo "  (scripts/lane.sh shell ${LANE_N}  ->  git add <files> && git commit)" >&2
          exit 3
        fi
        git bundle create "/out/lane-${LANE_N}.bundle" "feature/lane-${LANE_N}" --not develop
      '; then
    die "nothing bundled (commit the lane work, then retry)"
  fi
  echo "▶ fetching feature/lane-$LANE_N into the host repo…"
  git fetch "$bundle" "feature/lane-$LANE_N:feature/lane-$LANE_N"
  echo "✔ host now has branch feature/lane-$LANE_N."
  if [ -z "$(git status --porcelain)" ] && [ "$(git branch --show-current)" = "develop" ]; then
    git merge --no-ff "feature/lane-$LANE_N" -m "Merge feature/lane-$LANE_N into develop"
    echo "✔ merged feature/lane-$LANE_N into develop."
  else
    echo "⚠ host working tree is dirty or not on develop. To finish:"
    echo "    git switch develop && git merge --no-ff feature/lane-$LANE_N"
  fi
  echo "⚠ Flyway: if this lane added a Vx_y_z migration that another lane also added at the"
  echo "  same version, renumber to a unique ascending version BEFORE running a shared DB."
}

cmd_down() {
  require_n "$1"; export_ports "$1"
  echo "▶ lane $LANE_N: tearing down containers + lane volumes…"
  dc down -v
  rm -rf "$ROOT/.lanes/lane-$LANE_N"
  if [ "${2:-}" = "--purge" ]; then
    git branch -D "feature/lane-$LANE_N" 2>/dev/null && echo "✔ deleted local branch feature/lane-$LANE_N" || true
  fi
  echo "✔ lane $LANE_N down. (the workspace volume is gone — unmerged code is lost)"
}

cmd_list() {
  docker ps -a --filter "label=com.docker.compose.project" --filter "name=score-lane-" \
    --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' 2>/dev/null || true
}

case "${1:-}" in
  up)    cmd_up "${2:-}";;
  serve) cmd_serve "${2:-}";;
  logs)  cmd_logs "${2:-}" "${3:-api}";;
  shell) cmd_shell "${2:-}";;
  stop)  cmd_stop "${2:-}";;
  merge) cmd_merge "${2:-}";;
  down)  cmd_down "${2:-}" "${3:-}";;
  list|status) cmd_list;;
  *) sed -n '2,30p' "$0"; exit 1;;
esac
