import {defineTable} from '../../../common/score-table';

/** The values of a business context, which the create and the edit screen of it list. */
export const BUSINESS_CONTEXT_VALUE_TABLE = defineTable({
  storageKey: 'TableColumns-BusinessContextValuePage',
  columns: [
    {id: 'contextCategoryName', label: 'Context Category', width: 0, sortable: true},
    {id: 'contextSchemeName', label: 'Context Scheme', width: '33%', sortable: true},
    {id: 'contextSchemeValue', label: 'Context Scheme Value', width: '33%', sortable: true},
  ],
});
