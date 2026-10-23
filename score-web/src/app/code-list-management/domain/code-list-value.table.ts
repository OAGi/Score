import {defineTable} from '../../common/score-table';

/** The values of a code list, which its edit screen lists. */
export const CODE_LIST_VALUE_TABLE = defineTable({
  storageKey: 'TableColumns-CodeListValuePage',
  columns: [
    {id: 'value', label: 'Value', header: 'Code', width: 0, sortable: true},
    {id: 'meaning', label: 'Meaning', width: '10%', sortable: true},
    {id: 'deprecated', label: 'Deprecated', width: 100, sortable: true},
    {id: 'definition', label: 'Definition', width: '50%', sortable: true},
    {id: 'definitionSource', label: 'Definition Source', width: '15%', sortable: true},
  ],
  search: {key: 'search', placeholder: 'Search'},
});

/** The values that contain the text of the search, in their value, meaning or definition. */
export function codeListValueMatches(row: { value?: string; meaning?: string; definition?: { content?: string; source?: string } },
                                     text: string | null): boolean {
  const filter = (text ?? '').trim().toLowerCase();
  return !filter
    || (!!row.value && row.value.toLowerCase().includes(filter))
    || (!!row.meaning && row.meaning.toLowerCase().includes(filter))
    || (!!row.definition?.content && row.definition.content.toLowerCase().includes(filter))
    || (!!row.definition?.source && row.definition.source.toLowerCase().includes(filter));
}
