import {CompactRecord} from '../../common/record-compact-row';

/** What of an account the compact row shows. The lists name it `name` or `username`. */
export interface AccountCompactSource {
  loginId: string;
  username?: string;
  name?: string;
  organization?: string;
  developer?: boolean;
  enabled?: boolean;
}

/**
 * What the compact row of an account shows: its role on the left; login id and name, and, in a line, its organization (and, for a list that
 * has the status of it, its status) beside it.
 */
export function accountCompactRecord(account: AccountCompactSource, options: { status?: boolean; link?: string } = {}): CompactRecord {
  return {
    badges: account.developer === undefined ? [] : [{text: account.developer ? 'Developer' : 'End User'}],
    title: account.loginId,
    link: options.link,
    subtitle: account.username ?? account.name,
    meta: [
      ...(account.organization ? [{label: 'Organization', value: account.organization}] : []),
      ...(options.status && account.enabled !== undefined ? [{label: 'Status', value: account.enabled ? 'Enable' : 'Disable', priority: 1 as const}] : [])
    ]
  };
}
