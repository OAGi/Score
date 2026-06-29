#!/usr/bin/env bash
# Dev-lane workspace bootstrap. Runs once per container start.
# On a fresh /workspace volume it clones the host's committed `develop` and branches to
# feature/lane-N. The clone's `origin` is the read-only host repo, so `git fetch origin`
# later picks up host develop, and merge-back is done via `git bundle` (see scripts/lane.sh).
set -euo pipefail

LANE_N="${LANE_N:-0}"
BRANCH="${REPO_BRANCH:-feature/lane-${LANE_N}}"

git config --global --add safe.directory /host-repo
git config --global --add safe.directory /workspace
git config --global user.name  "${GIT_USER_NAME:-Hakju Oh}"
git config --global user.email "${GIT_USER_EMAIL:-dev-lane@local}"
git config --global init.defaultBranch develop

if [ ! -d /workspace/.git ]; then
  if [ -n "$(ls -A /workspace 2>/dev/null || true)" ]; then
    echo "[lane ${LANE_N}] ERROR: /workspace is non-empty but has no .git — refusing to clobber." >&2
    exit 1
  fi
  echo "[lane ${LANE_N}] cloning host repo (committed develop) into the lane workspace…"
  git clone --no-hardlinks /host-repo /workspace
  cd /workspace
  git fetch origin develop --quiet
  git checkout -B "${BRANCH}" origin/develop
  echo "[lane ${LANE_N}] branched ${BRANCH} from develop @ $(git rev-parse --short HEAD)"
else
  cd /workspace
  echo "[lane ${LANE_N}] workspace ready on branch $(git branch --show-current) @ $(git rev-parse --short HEAD)"
fi

exec "$@"
