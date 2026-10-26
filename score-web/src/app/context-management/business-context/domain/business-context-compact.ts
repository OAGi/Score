import {CompactRecord} from '../../../common/record-compact-row';
import {BusinessContextListEntry} from './business-context';

/** What the compact row of a business context shows of it. `tenant`: the list has the tenants of it. */
export function businessContextCompactRecord(context: BusinessContextListEntry, options: { tenant?: boolean } = {}): CompactRecord {
  const tenants = (context as { connectedTenantNames?: string }).connectedTenantNames;
  return {
    title: context.name,
    link: '/context_management/business_context/' + context.businessContextId,
    subtitle: context.guid,
    meta: options.tenant && tenants ? [{label: 'Tenant', value: tenants, priority: 2}] : [],
    updated: context.lastUpdated ? {when: context.lastUpdated.when, by: context.lastUpdated.who?.loginId} : undefined
  };
}
