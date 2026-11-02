import {defineTable} from '../../../common/score-table';

/** The business context list. Also the table of the screens that choose a business context for a BIE (they share the column settings). */
export const BUSINESS_CONTEXT_TABLE = defineTable({
  storageKey: 'TableColumns-BusinessContextPage',
  columns: [
    {id: 'name', label: 'Name', width: 0, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'name', placeholder: 'Search by Name'},
  filters: [
    {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
    {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpParam: 'lastUpdatedOn'},
  ],
});
