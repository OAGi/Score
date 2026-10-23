#!/usr/bin/env bash
# One-command deploy of connectCenter to a fleet instance.
#
#   build local images -> push to Docker Hub -> roll the remote instance -> verify
#
# Usage:
#   deploy/deploy.sh [instance] [version] [flags]
#     instance   fleet name (default: test)
#     version    image tag   (default: score-http/VERSION)
#   flags:
#     --open-sg      re-point the SSH security group to your current IP first (needs aws CLI)
#     --no-build     skip local image build (reuse already-built images)
#     --no-push      skip docker push
#     --no-db        do not bump/recreate the db container (frontend + backend only)
#     --remote-only  == --no-build --no-push (just roll + verify using existing pushed images)
#     -y, --yes      do not prompt for confirmation
#
# Examples:
#   deploy/deploy.sh test                 # full 3.5.2 deploy to test (build+push+roll+verify)
#   deploy/deploy.sh test --open-sg       # ...opening SSH to your current IP first
#   deploy/deploy.sh test --remote-only   # roll test to the already-pushed images
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

INSTANCE="test"
VERSION=""
DO_SG=0; DO_BUILD=1; DO_PUSH=1; WITH_DB=1; ASSUME_YES=0

# positional (instance, version) then flags, in any order
for arg in "$@"; do
  case "$arg" in
    --open-sg)     DO_SG=1 ;;
    --no-build)    DO_BUILD=0 ;;
    --no-push)     DO_PUSH=0 ;;
    --no-db)       WITH_DB=0 ;;
    --remote-only) DO_BUILD=0; DO_PUSH=0 ;;
    -y|--yes)      ASSUME_YES=1 ;;
    -h|--help)     grep -E '^#( |$)' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    --*)           die "unknown flag: $arg" ;;
    *)             if [ -z "${VERSION_SET:-}" ] && instance_host "$arg" >/dev/null 2>&1; then
                     INSTANCE="$arg"
                   else
                     VERSION="$arg"; VERSION_SET=1
                   fi ;;
  esac
done
VERSION="${VERSION:-$(default_version)}"
require_release_version "$VERSION"   # abort before build/push if VERSION looks like a dev/snapshot build
HOST="$(instance_host "$INSTANCE")" || exit 1

echo "-------------------------------------------------------------"
echo " instance : $INSTANCE ($HOST)"
echo " version  : $VERSION"
echo " steps    : $([ $DO_SG = 1 ] && echo -n 'open-sg ')$([ $DO_BUILD = 1 ] && echo -n 'build ')$([ $DO_PUSH = 1 ] && echo -n 'push ')roll verify"
echo " with_db  : $WITH_DB"
echo "-------------------------------------------------------------"
if [ "$ASSUME_YES" != 1 ]; then
  printf 'Proceed? [y/N] '; read -r ans; case "$ans" in y|Y|yes) ;; *) die "aborted." ;; esac
fi

[ "$DO_SG"    = 1 ] && "$DEPLOY_DIR/open-sg.sh"
[ "$DO_BUILD" = 1 ] && "$DEPLOY_DIR/build-images.sh" "$VERSION"
[ "$DO_PUSH"  = 1 ] && "$DEPLOY_DIR/push-images.sh" "$VERSION"
WITH_DB="$WITH_DB" "$DEPLOY_DIR/remote-deploy.sh" "$INSTANCE" "$VERSION"

log "Deploy of $VERSION to '$INSTANCE' complete."
