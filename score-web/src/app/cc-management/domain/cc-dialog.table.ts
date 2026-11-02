import {HttpParams} from '@angular/common/http';
import {defineTable} from '../../common/score-table';
import {OagisComponentTypes} from './core-component-node';

/** The Working branch is where developers work: WIP, Draft, Candidate, then Published, with ReleaseDraft just before a release. */
const WORKING_STATE_LIST = ['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted'];
/** A published release is where end users work: WIP, QA, Production, and Published. */
const RELEASE_STATE_LIST = ['WIP', 'QA', 'Production', 'Published', 'Deleted'];
const separate = (state: string) => state.replace(/[A-Z][a-z]/g, (str) => ' ' + str).trim();

/** Every state that a core component or a data type may have, for the lists that browse them; only the branch tells. */
export function ccStateList(workingRelease: boolean): string[] {
  return workingRelease ? WORKING_STATE_LIST : RELEASE_STATE_LIST;
}

/**
 * The states that a component may be picked in, by a dialog that builds a new component on it.
 * A deleted one is gone, and a release draft is about to be released (it becomes a Candidate or Published),
 * so nothing is to depend on it.
 */
export function pickableCcStates(workingRelease: boolean): string[] {
  return ccStateList(workingRelease).filter(e => e !== 'Deleted' && e !== 'ReleaseDraft');
}

/** With no state chosen, a dialog asks for the states that a component may be picked in, as if all of them were chosen. */
export function withPickableStates(params: HttpParams, pickable: string[]): HttpParams {
  return params.has('states') ? params : params.set('states', pickable.join(','));
}

/** The State filter's options for the states a screen gives. */
export function ccStateOptions(states: string[]): { value: string, label: string }[] {
  return states.map(e => ({value: e, label: separate(e)}));
}

export const TRUE_FALSE = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];

/** The filters that the dialogs choosing a core component share; a dialog takes the ones that it had, in its order. */
export const CC_DIALOG_FILTERS = {
  states: {type: 'select', key: 'states', label: 'State'},
  definition: {type: 'text', key: 'definition', label: 'Definition'},
  module: {type: 'text', key: 'module', label: 'CORE_COMPONENT_LIST.MODULE'},
  deprecated: {type: 'select', key: 'deprecated', label: 'Deprecated', options: TRUE_FALSE, onlyWhenOne: true},
  commonlyUsed: {type: 'select', key: 'commonlyUsed', label: 'Commonly Used', options: TRUE_FALSE, onlyWhenOne: true},
  newComponent: {type: 'select', key: 'newComponent', label: 'New', options: TRUE_FALSE, onlyWhenOne: true},
  componentTypes: {type: 'select', key: 'componentTypes', label: 'Component Type',
    options: OagisComponentTypes.map(e => ({value: e.value, label: e.name}))},
  tags: {type: 'select', key: 'tags', label: 'Tag'},
  namespaces: {type: 'select', key: 'namespaces', label: 'Namespace', searchable: true},
  owner: {type: 'updater', key: 'ownerLoginIdList', label: 'Owner'},
  updater: {type: 'updater', key: 'updaterLoginIdList', label: 'Updater'},
  updatedDate: {type: 'dateRange', key: 'updatedDate', label: 'Updated', httpStart: 'updateStart', httpEnd: 'updateEnd'},
} as const;

/** The core components without a type column: State, DEN, Revision, Owner, Module, Updated On. */
export const CC_WITHOUT_TYPE_TABLE = defineTable({
  storageKey: 'TableColumns-CoreComponentWithoutTypeColumnPage',
  columns: [
    {id: 'state', label: 'State', width: 104, sortable: true},
    {id: 'den', label: 'DEN', width: 0, sortable: true},
    {id: 'revision', label: 'Revision', width: 80, sortable: true},
    {id: 'owner', label: 'Owner', width: 140, sortable: true},
    {id: 'module', label: 'Module', width: 400, sortable: true},
    {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
  ],
  defaultSort: {active: 'lastUpdateTimestamp', direction: 'desc'},
  search: {key: 'den', placeholder: 'Search by DEN'},
  filters: [],
});

const DT_COLUMNS = [
  {id: 'state', label: 'State', width: 104, sortable: true},
  {id: 'den', label: 'DEN', width: 0, sortable: true},
  {id: 'valueDomain', label: 'Value Domain', width: 126, sortable: true},
  {id: 'sixDigitId', label: 'Six Hexadecimal ID', width: 140, sortable: true},
  {id: 'revision', label: 'Revision', width: 80, sortable: true},
  {id: 'owner', label: 'Owner', width: 140, sortable: true},
  {id: 'module', label: 'Module', width: 400, sortable: true},
  {id: 'lastUpdateTimestamp', label: 'Updated On', header: 'Updated on', width: 160, sortable: true},
] as const;

/** The data types without a type column. */
export const DT_WITHOUT_TYPE_TABLE = defineTable({
  ...CC_WITHOUT_TYPE_TABLE,
  storageKey: 'TableColumns-DataTypeWithoutTypeColumnPage',
  columns: DT_COLUMNS,
});

/** The data types, with the column settings of the data type list. */
export const DT_DIALOG_TABLE = defineTable({
  ...CC_WITHOUT_TYPE_TABLE,
  storageKey: 'TableColumns-DataTypePage',
  columns: DT_COLUMNS,
});

/** The core components with a type column, with the column settings of the core component list. */
export const CC_WITH_TYPE_TABLE = defineTable({
  ...CC_WITHOUT_TYPE_TABLE,
  storageKey: 'TableColumns-CoreComponentPage',
  columns: [
    {id: 'type', label: 'Type', width: 80, sortable: true},
    ...CC_WITHOUT_TYPE_TABLE.columns,
  ],
});
