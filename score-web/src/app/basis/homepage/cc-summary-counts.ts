import {Destination} from '../../common/destinations/destinations';
import {encodeListQuery} from '../../common/state-progress-bar/state-progress-bar';

/** type -> username -> state -> count, as the summary API returns it. */
export type CcCountsByTypesUsersAndStates = {[type: string]: {[username: string]: {[state: string]: number}}};

/** username -> state -> count. */
export type CountsByUsersAndStates = {[username: string]: {[state: string]: number}};

/** The states that the summary counts: a list that is opened for a number without a state is limited to them, so that it lists what was counted. */
export const CC_COUNTED_STATES = ['WIP', 'Draft', 'Candidate'];

/** What the summary counts, by the page that lists it: a number opens the list of one of these. */
export const CC_KINDS: {kind: string, label: string, route: string, types: string[]}[] = [
  {kind: 'cc', label: 'Core Components', route: '/core_component', types: ['ACC', 'ASCCP', 'BCCP']},
  {kind: 'dt', label: 'Data Types', route: '/data_type', types: ['DT', 'CDT', 'BDT']},
  {kind: 'codeList', label: 'Code Lists', route: '/code_list', types: ['CODE_LIST']},
  {kind: 'agencyIdList', label: 'Agency ID Lists', route: '/agency_id_list', types: ['AGENCY_ID_LIST']},
];

function add(result: CountsByUsersAndStates, byUsers: CountsByUsersAndStates) {
  for (const [username, byStates] of Object.entries(byUsers)) {
    const counts = result[username] ||= {};
    for (const [state, count] of Object.entries(byStates)) {
      counts[state] = (counts[state] || 0) + count;
    }
  }
}

/** kind -> username -> state -> count, summed over the types of each kind. */
export function ccCountsByKinds(byTypes: CcCountsByTypesUsersAndStates | undefined): {[kind: string]: CountsByUsersAndStates} {
  const result: {[kind: string]: CountsByUsersAndStates} = {};
  for (const {kind, types} of CC_KINDS) {
    result[kind] = {};
    for (const type of types) {
      add(result[kind], byTypes?.[type] ?? {});
    }
  }
  return result;
}

/** username -> state -> count, summed over all the kinds. */
export function ccCountsByUsersAndStates(byTypes: CcCountsByTypesUsersAndStates | undefined): CountsByUsersAndStates {
  const result: CountsByUsersAndStates = {};
  for (const byUsers of Object.values(ccCountsByKinds(byTypes))) {
    add(result, byUsers);
  }
  return result;
}

/** The count of a state, or of every state when none is given, over all users or, if given, one user. */
export function ccCountOfState(
  byUsersAndStates: CountsByUsersAndStates,
  state: string | undefined,
  username?: string
): number {
  return Object.entries(byUsersAndStates)
    .filter(([u]) => username === undefined || u === username)
    .reduce((sum, [, byStates]) => sum + (state === undefined
      ? Object.values(byStates).reduce((a, b) => a + b, 0)
      : (byStates[state] || 0)), 0);
}

/**
 * The pages that a number opens: the list of each kind that has some of the items of the number (of the state, of the user, or of both).
 * `filter` is what every list carries, such as the release.
 */
export function ccDestinations(
  byKinds: {[kind: string]: CountsByUsersAndStates},
  filter: {key: string, value: any}[],
  state?: string,
  username?: string
): Destination[] {
  const params: {key: string, value: any}[] = [
    {key: 'states', value: state ?? CC_COUNTED_STATES.join(',')},
    ...(username ? [{key: 'ownerLoginIdList', value: username}] : []),
    ...filter
  ];
  return CC_KINDS
    .map(kind => ({kind, count: ccCountOfState(byKinds[kind.kind] ?? {}, state, username)}))
    .filter(e => e.count > 0)
    .map(({kind, count}) => ({label: kind.label + ' (' + count + ')', route: kind.route, queryParams: {q: encodeListQuery(params)}}));
}
