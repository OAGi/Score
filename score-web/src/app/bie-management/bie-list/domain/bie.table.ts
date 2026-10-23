import {HttpParams} from '@angular/common/http';
import {base64Decode, base64Encode} from '../../../common/utility';
import {defineTable, queryToUrl, TableDef, TableQuery} from '../../../common/score-table';

const TRUE_FALSE = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];

/** The filters that every screen listing BIEs has, in display order. */
export const BIE_FILTERS = {
  states: {type: 'select', key: 'states', label: 'State', options: ['WIP', 'QA', 'Production'].map(e => ({value: e, label: e}))},
  businessContext: {type: 'text', key: 'businessContext', label: 'Business Context'},
  version: {type: 'text', key: 'version', label: 'Version'},
  remark: {type: 'text', key: 'remark', label: 'Remark'},
  deprecated: {type: 'select', key: 'deprecated', label: 'Deprecated', options: TRUE_FALSE, onlyWhenOne: true},
  owner: {type: 'updater', key: 'ownerLoginIdList', label: 'Owner'},
  updater: {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
  updatedDate: {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpStart: 'updateStart', httpEnd: 'updateEnd'},
  // The branches are chosen beside the search box; they are part of the query.
  releaseIds: {type: 'select', key: 'releaseIds', label: 'Branch', hidden: true},
} as const;

/**
 * What the BIE list asks for and what other screens ask the list for (a link to the BIEs that a BIE is based on, ...): in the URL,
 * with no field to edit them.
 */
export const BIE_URL_FILTERS = [
  {type: 'text', key: 'topLevelAsbiepIds', label: 'Top-level ASBIEPs', hidden: true},
  {type: 'text', key: 'basedTopLevelAsbiepIds', label: 'Based top-level ASBIEPs', hidden: true},
  {type: 'text', key: 'excludeTopLevelAsbiepIds', label: 'Excluded top-level ASBIEPs', hidden: true},
  {type: 'text', key: 'asccpManifestId', label: 'ASCCP', hidden: true},
  {type: 'text', key: 'propertyTerm', label: 'Property term', hidden: true},
  {type: 'text', key: 'excludePropertyTerms', label: 'Excluded property terms', hidden: true},
  {type: 'text', key: 'access', label: 'Access', hidden: true},
  {type: 'text', key: 'types', label: 'Types', hidden: true},
] as const;

/** The BIE list, with the column settings that every screen listing BIEs shares. */
export const BIE_TABLE = defineTable({
  storageKey: 'TableColumns-BiePage',
  columns: [
    {id: 'state', label: 'State', width: 104, sortable: true},
    {id: 'branch', label: 'Branch', width: 100, sortable: true},
    {id: 'den', label: 'DEN', width: 0, sortable: true},
    {id: 'owner', label: 'Owner', width: 140, sortable: true},
    {id: 'businessContexts', label: 'Business Contexts', width: 180},
    {id: 'version', label: 'Version', width: 100, sortable: true},
    {id: 'status', label: 'Status', width: 100, sortable: true},
    {id: 'bizTerm', label: 'Business Term', width: 120, sortable: true},
    {id: 'remark', label: 'Remark', width: 200, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'den', placeholder: 'Search by DEN'},
  filters: [],
});

/** The search part of a link to the BIE list (a `q=` of the query of a list, with what the link says). */
export function bieLinkQuery(def: TableDef, query: TableQuery, extras: { [key: string]: any }): string {
  const q = queryToUrl(def, query);
  let params = new HttpParams({fromString: base64Decode(q.substring('q='.length))});
  Object.keys(extras).forEach(key => {
    params = params.set(key, extras[key]);
  });
  return 'q=' + base64Encode(params.toString());
}
