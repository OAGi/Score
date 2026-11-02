import {defineTable} from '../../common/score-table';

/** The values of an agency ID list, which its edit screen lists. */
export const AGENCY_ID_LIST_VALUE_TABLE = defineTable({
  storageKey: 'TableColumns-AgencyIdValueListPage',
  columns: [
    {id: 'value', label: 'Value', width: 0, sortable: true},
    {id: 'name', label: 'Meaning', width: '10%', sortable: true},
    {id: 'deprecated', label: 'Deprecated', width: 100, sortable: true},
    {id: 'definition', label: 'Definition', width: '50%', sortable: true},
    {id: 'definitionSource', label: 'Definition Source', width: '15%', sortable: true},
  ],
  search: {key: 'search', placeholder: 'Search'},
});

/** The values that contain the text of the search, in their value, meaning or definition. */
export function agencyIdListValueMatches(row: { value?: string; name?: string; definition?: { content?: string } },
                                         text: string | null): boolean {
  const filter = (text ?? '').trim().toLowerCase();
  return !filter
    || (!!row.value && row.value.toLowerCase().includes(filter))
    || (!!row.name && row.name.toLowerCase().includes(filter))
    || (!!row.definition?.content && row.definition.content.toLowerCase().includes(filter));
}
