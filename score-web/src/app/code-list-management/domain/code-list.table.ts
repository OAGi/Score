import {defineTable} from '../../common/score-table';

/**
 * The code list list. Its column settings are shared by every screen that lists code lists, so they keep this storage key;
 * a screen with other filters takes it over: `defineTable({...CODE_LIST_TABLE, filters: [...]})`.
 */
export const CODE_LIST_TABLE = defineTable({
  storageKey: 'TableColumns-CodeListPage',
  columns: [
    {id: 'state', label: 'State', width: 104, sortable: true},
    {id: 'codeListName', label: 'Name', width: 0, sortable: true},
    {id: 'basedCodeListName', label: 'Based Code List', width: 200, sortable: true},
    {id: 'agencyId', label: 'Agency ID', width: 100, sortable: true},
    {id: 'versionId', label: 'Version', width: 100, sortable: true},
    {id: 'extensible', label: 'Extensible', width: 100, sortable: true},
    {id: 'revision', label: 'Revision', width: 100, sortable: true},
    {id: 'owner', label: 'Owner', width: 140, sortable: true},
    {id: 'module', label: 'Module', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'updater', key: 'ownerLoginIdList', label: 'Owner'},
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
  ],
});
