import {defineTable} from '../../../common/score-table';

/** The operations (BIEs) of an OpenAPI document, which its edit screen lists. */
export const OAS_DOC_BIE_TABLE = defineTable({
  storageKey: 'TableColumns-BieForOasDocPage',
  columns: [
    {id: 'branch', label: 'Branch', width: 80, sortable: true},
    {id: 'den', label: 'DEN', width: 0, sortable: true},
    {id: 'remark', label: 'Remark', width: '12%', sortable: true},
    {id: 'verb', label: 'Verb', width: 80, sortable: true},
    {id: 'arrayIndicator', label: 'Array Indicator', width: 120, sortable: true},
    {id: 'suppressRootIndicator', label: 'Suppress Root Indicator', header: 'Suppress Root', width: 120, sortable: true},
    {id: 'messageBody', label: 'Message Body', width: 120, sortable: true},
    {id: 'security', label: 'Security', width: 120},
    {id: 'errorResponse', label: 'Error Response', width: 180},
    {id: 'resourceName', label: 'Resource Name', width: '15%', sortable: true},
    {id: 'operationId', label: 'Operation ID', width: '15%', sortable: true},
    {id: 'tagName', label: 'Tag Name', width: '10%', sortable: true},
  ],
  sortParam: 'sortActives',
  defaultSort: [{active: 'tagName', direction: 'asc'}, {active: 'operationId', direction: 'asc'}],
});
