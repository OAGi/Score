import {defineTable} from '../../../common/score-table';

/** The operations (BIEs) of an OpenAPI document, which its edit screen lists. */
export const OAS_DOC_BIE_TABLE = defineTable({
  storageKey: 'TableColumns-BieForOasDocPage',
  columns: [
    {id: 'branch', label: 'Branch', width: 100, sortable: true},
    {id: 'den', label: 'DEN', width: 300, sortable: true},
    {id: 'remark', label: 'Remark', width: 240, sortable: true},
    {id: 'verb', label: 'Verb', width: 100, sortable: true},
    {id: 'arrayIndicator', label: 'Array Indicator', width: 120, sortable: true},
    {id: 'suppressRootIndicator', label: 'Suppress Root Indicator', header: 'Suppress Root', width: 120, sortable: true},
    {id: 'messageBody', label: 'Message Body', width: 150, sortable: true},
    {id: 'security', label: 'Security', width: 130},
    {id: 'errorResponse', label: 'Error Response', width: 180},
    {id: 'requestParameters', label: 'Request Parameters', width: 140},
    {id: 'responseHeaders', label: 'Response Headers', width: 140},
    {id: 'resourceName', label: 'Resource Name', width: 300, sortable: true},
    {id: 'operationId', label: 'Operation ID', width: 280, sortable: true},
    {id: 'tagName', label: 'Tag Name', width: 180, sortable: true},
  ],
  sortParam: 'sortActives',
  defaultSort: [{active: 'tagName', direction: 'asc'}, {active: 'operationId', direction: 'asc'}],
});
