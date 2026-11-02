import {defineTable} from '../../common/score-table';

/** The BIEs that reuse BIEs, which the reuse report lists. */
export const BIE_REUSE_REPORT_TABLE = defineTable({
  storageKey: 'TableColumns-BieReuseReportPage',
  columns: [
    {id: 'releaseNum', label: 'Release', width: 100, sortable: true},
    {id: 'reusingState', label: 'Reusing State', header: 'State', width: 100, sortable: true},
    {id: 'reusingDen', label: 'Reusing DEN', header: 'DEN', width: 0, sortable: true},
    {id: 'reusingOwner', label: 'Reusing Owner', header: 'Owner', width: 140, sortable: true},
    {id: 'reusingVersion', label: 'Reusing Version', header: 'Version', width: '5%', sortable: true},
    {id: 'reusingStatus', label: 'Reusing Status', header: 'Status', width: '5%', sortable: true},
    {id: 'reusingRemark', label: 'Reusing Remark', header: 'Remark', selected: false, width: '5%', sortable: true},
    {id: 'reusedState', label: 'Reused State', header: 'State', width: 100, sortable: true},
    {id: 'reusedDen', label: 'Reused DEN', header: 'Reused', width: 0, sortable: true},
    {id: 'reusedOwner', label: 'Reused Owner', header: 'Owner', width: 140, sortable: true},
    {id: 'reusedVersion', label: 'Reused Version', header: 'Version', width: '5%', sortable: true},
    {id: 'reusedStatus', label: 'Reused Status', header: 'Status', width: '5%', sortable: true},
    {id: 'reusedRemark', label: 'Reused Remark', header: 'Remark', selected: false, width: '5%', sortable: true},
  ],
});
