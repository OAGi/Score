import {defineTable} from '../../common/score-table';

/** The business term list. Also the table of the dialog that assigns business terms to a BIE (they share the column settings). */
export const BUSINESS_TERM_TABLE = defineTable({
  storageKey: 'TableColumns-BusinessTermPage',
  columns: [
    {id: 'businessTerm', label: 'Business Term', width: 0, sortable: true},
    {id: 'externalReferenceUri', label: 'External Reference URI', width: '15%', sortable: true},
    {id: 'externalReferenceId', label: 'External Reference ID', width: '15%', sortable: true},
    {id: 'definition', label: 'Definition', width: '40%', sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'businessTerm', placeholder: 'Search by Business Term'},
  filters: [
    {type: 'updater', key: 'updaterUsernameList', httpParam: 'updaterLoginIdList', label: 'Updater'},
    {
      type: 'dateRange', key: 'updatedDate', label: 'Updated',
      httpStart: 'updateStart', httpEnd: 'updateEnd', urlStart: 'updateStart', urlEnd: 'updateEnd'
    },
    {type: 'text', key: 'externalReferenceUri', label: 'External Reference URI'},
    {type: 'text', key: 'externalReferenceId', label: 'External Reference ID'},
    {type: 'text', key: 'definition', label: 'Definition'},
  ],
});
