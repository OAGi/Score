import {defineTable} from '../../common/score-table';

/**
 * The account list. The screens that list accounts share its column settings, so they keep this storage key;
 * a screen with other columns or filters takes it over: `defineTable({...ACCOUNT_TABLE, filters: [...]})`.
 */
export const ACCOUNT_TABLE = defineTable({
  storageKey: 'TableColumns-AccountPage',
  columns: [
    {id: 'loginId', label: 'Login ID', width: 0, sortable: true},
    {id: 'role', label: 'Role', width: 200, sortable: true},
    {id: 'name', label: 'Name', width: 400, sortable: true},
    {id: 'organization', label: 'Organization', width: 400, sortable: true},
    {id: 'status', label: 'Status', width: 100, sortable: true},
  ],
  sortParam: 'sortActive',
  defaultSort: {active: 'name', direction: 'asc'},
  search: {key: 'loginId', placeholder: 'Search by Login ID'},
  filters: [
    {type: 'text', key: 'name', label: 'Name'},
    {type: 'text', key: 'organization', label: 'Organization'},
    {
      type: 'select', key: 'roles', label: 'Role',
      options: [{value: 'developer', label: 'Developer'}, {value: 'end-user', label: 'End-User'}]
    },
    {
      type: 'select', key: 'status', label: 'Status',
      options: [{value: 'enable', label: 'Enable'}, {value: 'disable', label: 'Disable'}]
    },
  ],
});
