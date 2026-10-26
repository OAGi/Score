import {CompactRecord} from '../../common/record-compact-row';
import {BusinessTermListEntry} from './business-term';

/** What the compact row of a business term shows of it. */
export function businessTermCompactRecord(term: BusinessTermListEntry): CompactRecord {
  return {
    title: term.businessTerm,
    link: '/business_term_management/business_term/' + term.businessTermId,
    subtitle: term.guid,
    meta: [
      ...(term.externalReferenceId ? [{label: 'Reference ID', value: term.externalReferenceId, priority: 1 as const}] : []),
      ...(term.externalReferenceUri ? [{label: 'URI', value: term.externalReferenceUri, href: term.externalReferenceUri, priority: 2 as const}] : []),
      ...(term.definition ? [{label: 'Definition', value: term.definition, priority: 3 as const}] : [])
    ],
    updated: term.lastUpdated ? {when: term.lastUpdated.when, by: term.lastUpdated.who?.loginId} : undefined
  };
}
