# connectCenter deploy automation

End-to-end deploy of the three connectCenter images
(`oagi1docker/srt-web`, `srt-http-gateway`, `srt-repo`) to a fleet instance:

```
open SSH (SG) → build images → push to Docker Hub → roll remote compose → verify
```

The version comes from `score-http/VERSION` (single source of truth); pass an explicit
tag to override. A `-dev`/`SNAPSHOT` version (e.g. `3.6.0-dev` on a dev branch) is
**refused** for fleet-mutating steps — pass an explicit release tag, or set
`ALLOW_DEV_VERSION=1` to override.

## Fleet

| name | host |
|---|---|
| `test` | test.connectcenter.oagi.org |
| `main` (PROD) | connectcenter.oagi.org |
| `training` | training.connectcenter.oagi.org |
| `modeldev` | modeldev.connectcenter.oagi.org |
| `cloud` | cloud.connectcenter.oagi.org |

Each instance runs `~/docker-compose.yml` (compose v2, project `ec2-user`) with services
`frontend backend db redis` (+ `connect-center-api` on `test`). Only `test` in this
toolkit is fully exercised; the others share the same shape.

## One command

```bash
deploy/deploy.sh test              # build + push + roll test to score-http/VERSION, then verify
deploy/deploy.sh test --open-sg    # ...re-open SSH to your current IP first (needs aws CLI)
deploy/deploy.sh test --remote-only  # roll test to the already-pushed images (no rebuild)
deploy/deploy.sh test --no-db      # frontend + backend only; leave the db container alone
deploy/deploy.sh test 3.5.2 -y     # explicit version, no confirmation prompt
deploy/deploy.sh test --images=http --remote-only   # roll only the backend image (any of web,http,repo)
# per-image tags: TAG_WEB / TAG_HTTP / TAG_REPO env (srt-repo has its own score-repo/VERSION)
```

## Individual steps

| script | what it does |
|---|---|
| `open-sg.sh [ip]` | re-point the two SSH SG rules to your current public IP (aws CLI) |
| `build-images.sh [ver]` | build all three images (wraps each module's `docker/build.sh`); local build = `linux/amd64`. `PUSH_IMAGE=1` builds **amd64 + arm64** and pushes the multi-arch manifest to Docker Hub directly |
| `push-images.sh [ver]` | `docker push` the three tags to Docker Hub (tags already published by a `PUSH_IMAGE=1` build are skipped) |
| `remote-deploy.sh <inst> [ver]` | prune disk + **pull images (validates the tag before editing anything)** + backup compose + bump tags + `up -d` + wait for clean backend startup |
| `verify.sh <inst> [ver]` | **hard-asserts** public 200, frontend/backend image==ver, backend 401, Flyway head==ver & success=1 (exit≠0 on any failure); STOMP wss 101 as a warning |
| `rollback.sh <inst> [ver\|--list]` | restore newest `~/docker-compose.yml.bak-*` (or re-point to a version); `--list` shows each backup's tags |
| `regen-repo-image.sh <from> [to]` | refresh the baked DB seed: seed clean `<from>` data → migrate with `<to>` backend → dump → rebuild `srt-repo:<to>` |
| `backup-dbs.sh <domain>` | `mysqldump` one instance's DB through SSH (`score-db dump` on the host; 3306 is not exposed) → `~/dumps/<domain>_YYYYMMDD.sql` |

### DB backup (`backup-dbs.sh`)

Dumps one instance's `oagi` DB to `<out>/<domain>_YYYYMMDD.sql` (default `<out>` = `DUMPS_DIR`
from `deploy.env`, else `~/dumps`). The DB port (3306) is bound to `127.0.0.1` and not open in the
security group on the instances, so the dump runs **on the instance over SSH** via the
`score-db dump` helper (no DB password needed, only SSH access). It uses `--single-transaction`
(no table locks) and rejects a dump that is tiny or lacks the `Dump completed` marker.

```bash
deploy/backup-dbs.sh connectcenter.oagi.org
deploy/backup-dbs.sh cloud.connectcenter.oagi.org --out /tmp/dumps
```

### SSH preflight (automatic SG open)

Every step that SSHes to an instance (`remote-deploy.sh`, `verify.sh`, `rollback.sh`) first probes
SSH; **if it's unreachable, it runs `open-sg.sh` to re-point the SG to your current IP and retries**
— so you don't have to remember to open it when your IP changes. If SSH already works, nothing is
changed. `backup-dbs.sh` also uses SSH only; run `open-sg.sh` first if your IP changed.

## Prerequisites

**Local build/push**
- Docker running. On Apple Silicon the images are emulated `linux/amd64` (slower, correct).
- `docker login` to Docker Hub with push rights to `oagi1docker/*`.
- score-web build needs Node 22 (via nvm) and prebuilt docs at `docs/user_guide/_build/html`.
- score-http build needs the Maven wrapper and the MariaDB JDBC driver in `~/.m2`.

**SSH SG automation (`--open-sg` / `open-sg.sh`)**
- `brew install awscli` + AWS credentials with `ec2:DescribeSecurityGroupRules` +
  `ec2:ModifySecurityGroupRules` on the fleet SG: `aws configure`, or export
  `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` (+ `AWS_SESSION_TOKEN` if temporary).
- Copy `deploy/deploy.env.example` → `deploy/deploy.env` (git-ignored) and fill in
  `SG_ID` / `SG_RULE_IDS` / `AWS_REGION`. Without this, open the ports to your IP by hand
  in the EC2 console before running a deploy.

## Safety notes

- **Validate before mutate:** `remote-deploy.sh` pulls the target `srt-*:<ver>` images
  *before* it edits the compose file, so a bad/missing tag aborts (exit 2) with the compose
  file **unchanged** — no "bumped-but-not-recreated" state.
- **Version guard:** a `-dev`/`SNAPSHOT` version is refused (set `ALLOW_DEV_VERSION=1` to force),
  so the dev-branch `score-http/VERSION` default can't poison a fleet compose file by accident.
- **Verify is a gate:** `verify.sh` exits non-zero if the public door isn't 200, the running
  frontend/backend image ≠ `<ver>`, the backend isn't 401, or Flyway head ≠ `<ver>`/success≠1.
  So a failed `remote-deploy.sh` (which runs verify last) actually reports failure.
- **Backups:** every roll writes `~/docker-compose.yml.bak-<version>-<timestamp>` before editing.
- **Disk:** the test box has a 30 GB root; the roll prunes *unreferenced* images/build cache
  before pulling (never touches running or tagged images). Old version tags stay for rollback.
- **DB:** bumping `srt-repo` recreates the `db` container on the persistent volume; the schema
  is migrated at runtime by the backend's Flyway, not by the image. Skip with `--no-db`.
- **Pre-release Flyway conflict:** if an instance already ran a *pre-release* build of the target
  version (e.g. an `-rc`), its `flyway_schema_history` holds that version's row. The final build
  validates cleanly **only if** the final migration SQL is byte-identical to the pre-release one
  (it recomputes the same checksum). If it differs, the backend dies with
  `FlywayValidateException` — delete the stale row from `flyway_schema_history` first.
  (`remote-deploy.sh` detects this failure and aborts before leaving a broken backend.)
- **Rollback:** `deploy/rollback.sh <instance>` restores the newest compose backup and recreates.
