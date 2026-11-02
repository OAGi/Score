import {CompactRecord} from '../../../common/record-compact-row';
import {ReuseReport} from './bie-report';

/**
 * What the compact row shows of one of the two BIEs of a reuse: the one that reuses (`reusing`, with the release of the reuse) or the one
 * that is reused (`reused`). `link` is where the BIE of the report is, for a BIE that the user may open.
 */
export function reuseCompactRecord(side: 'reusing' | 'reused', report: ReuseReport,
                                   link?: { path: string; queryParams?: Record<string, string> }): CompactRecord {
  const r = report as unknown as Record<string, any>;
  const state: string = r[side + 'State'];
  return {
    badges: [
      {text: state, kind: 'state', cssClass: 'bie-state', state},
      ...(r[side + 'Deprecated'] ? [{
        text: 'Deprecated', kind: 'state' as const, cssClass: 'bie-state', state: 'Deprecated',
        tooltip: (r[side + 'DeprecatedReason'] ? '• ' + r[side + 'DeprecatedReason'] + '\n' : '')
          + (r[side + 'DeprecatedRemark'] ? '• ' + r[side + 'DeprecatedRemark'] : '')
      }] : []),
      ...(side === 'reusing' && report.releaseNum ? [{text: report.releaseNum}] : [])
    ],
    title: r[side + 'Den'],
    titleTag: r[side + 'DisplayName'] || undefined,
    link: link?.path,
    linkQueryParams: link?.queryParams,
    subtitle: r[side + 'Guid'],
    meta: [
      {label: 'Owner', value: r[side + 'Owner'], strong: true},
      ...(r[side + 'Version'] ? [{label: 'Version', value: r[side + 'Version'], priority: 1 as const}] : []),
      ...(r[side + 'Status'] ? [{label: 'Status', value: r[side + 'Status'], priority: 2 as const}] : []),
      ...(r[side + 'Remark'] ? [{label: 'Remark', value: r[side + 'Remark'], priority: 3 as const}] : [])
    ]
  };
}
