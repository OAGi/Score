import {defineTable} from '../../../common/score-table';

/** The values of a context scheme, which the create and the edit screen of it list. */
export const CONTEXT_SCHEME_VALUE_TABLE = defineTable({
  storageKey: 'TableColumns-ContextSchemeValuePage',
  columns: [
    {id: 'value', label: 'Value', width: 0, sortable: true},
    {id: 'meaning', label: 'Meaning', width: '50%', sortable: true},
  ],
  search: {key: 'search', placeholder: 'Search'},
});

/** The values that contain the text of the search, in their value or meaning. */
export function contextSchemeValueMatches(row: { value?: string; meaning?: string }, text: string | null): boolean {
  const filter = (text ?? '').trim().toLowerCase();
  return !filter
    || (!!row.value && row.value.toLowerCase().includes(filter))
    || (!!row.meaning && row.meaning.toLowerCase().includes(filter));
}
