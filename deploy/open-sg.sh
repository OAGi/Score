#!/usr/bin/env bash
# Open the SSH security-group rules to the caller's current public IP.
#
# The test instance's port 22 is locked to a single IP by two SG rules; when your
# IP changes, SSH times out until the rules are re-pointed. This re-points them.
#
# Requires: aws CLI + credentials with ec2:DescribeSecurityGroupRules and
#           ec2:ModifySecurityGroupRules on the SG (region us-east-2).
#   Install:   brew install awscli
#   Configure: aws configure   (or export AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY)
#
# Usage: deploy/open-sg.sh [ip]     (ip defaults to the auto-detected public IP)
set -euo pipefail
# shellcheck source=deploy/config.sh
source "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/config.sh"

command -v aws >/dev/null 2>&1 || die "aws CLI not found. Install with: brew install awscli, then 'aws configure'."
require_sg

myip="${1:-$(curl -fsS https://checkip.amazonaws.com | tr -d '[:space:]')}"
[ -n "$myip" ] || die "could not determine public IP (pass one explicitly: deploy/open-sg.sh <ip>)."
cidr="${myip}/32"

log "Opening SG $SG_ID to $cidr (region $AWS_REGION)"
for rid in $SG_RULE_IDS; do
  # Preserve each rule's protocol/port/description; swap only the CIDR.
  IFS=$'\t' read -r proto from to desc < <(aws ec2 describe-security-group-rules --region "$AWS_REGION" \
      --security-group-rule-ids "$rid" \
      --query 'SecurityGroupRules[0].[IpProtocol,FromPort,ToPort,Description]' --output text)
  [ -n "${proto:-}" ] && [ "$proto" != "None" ] || die "could not describe rule $rid (check creds/region/rule id)."
  [ "$desc" = "None" ] && desc="devbox auto-managed"

  if [ "$proto" = "-1" ]; then
    inner="{\"IpProtocol\":\"-1\",\"CidrIpv4\":\"$cidr\",\"Description\":\"$desc\"}"
  else
    inner="{\"IpProtocol\":\"$proto\",\"FromPort\":$from,\"ToPort\":$to,\"CidrIpv4\":\"$cidr\",\"Description\":\"$desc\"}"
  fi
  printf '  rule %s: proto=%s ports=%s-%s desc="%s" -> %s\n' "$rid" "$proto" "$from" "$to" "$desc" "$cidr"
  aws ec2 modify-security-group-rules --region "$AWS_REGION" \
    --group-id "$SG_ID" \
    --security-group-rules "[{\"SecurityGroupRuleId\":\"$rid\",\"SecurityGroupRule\":$inner}]" >/dev/null
done
log "Ingress now open from $myip (rules: $SG_RULE_IDS)"
