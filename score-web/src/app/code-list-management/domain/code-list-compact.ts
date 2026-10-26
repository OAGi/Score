import {CompactRecord} from '../../common/record-compact-row';
import {SeparatePipe} from '../../common/utility';
import {CodeListListEntry} from './code-list';

const separate = new SeparatePipe();

/** What the compact row of a code list shows of it. */
export function codeListCompactRecord(entry: CodeListListEntry): CompactRecord {
  return {
    badges: [
      {text: separate.transform(entry.state), kind: 'state', cssClass: 'cl-state', state: entry.state},
      ...(entry.deprecated ? [{text: 'Deprecated', kind: 'state' as const, cssClass: 'cl-state', state: 'Deprecated'}] : [])
    ],
    isNew: entry.newComponent,
    title: entry.name,
    link: '/code_list/' + entry.codeListManifestId,
    subtitle: entry.guid,
    meta: [
      {label: 'Rev.', value: '' + entry.log?.revisionNum},
      {label: 'Owner', value: entry.owner?.loginId, strong: true},
      ...(entry.versionId ? [{label: 'Version', value: entry.versionId, priority: 1 as const}] : []),
      ...(entry.agencyIdListValue?.value ? [{label: 'Agency ID', value: entry.agencyIdListValue.value, priority: 2 as const}] : []),
      ...(entry.extensible === undefined || entry.extensible === null ? []
        : [{label: 'Extensible', value: entry.extensible ? 'Yes' : 'No', priority: 3 as const}]),
      ...(entry.based ? [{label: 'Based on', value: entry.based.name, link: '/code_list/' + entry.based.codeListManifestId, priority: 4 as const}] : []),
      ...(entry.module ? [{label: 'Module', value: entry.module, priority: 4 as const}] : [])
    ],
    updated: entry.lastUpdated ? {when: entry.lastUpdated.when, by: entry.lastUpdated.who?.loginId} : undefined
  };
}
