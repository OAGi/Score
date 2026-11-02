/** type -> username -> state -> count, as the summary API returns it. */
export type CcCountsByTypesUsersAndStates = {[type: string]: {[username: string]: {[state: string]: number}}};

/** The Core Component list does not show Data Types, so the Home page leaves them out of its counts. */
export const CC_SUMMARY_EXCLUDED_TYPES = ['DT', 'CDT', 'BDT'];

/** username -> state -> count, summed over the types the Core Component list shows. */
export function ccCountsByUsersAndStates(
  byTypes: CcCountsByTypesUsersAndStates | undefined
): {[username: string]: {[state: string]: number}} {
  const result: {[username: string]: {[state: string]: number}} = {};
  for (const [type, byUsers] of Object.entries(byTypes ?? {})) {
    if (CC_SUMMARY_EXCLUDED_TYPES.includes(type)) {
      continue;
    }
    for (const [username, byStates] of Object.entries(byUsers)) {
      const counts = result[username] ||= {};
      for (const [state, count] of Object.entries(byStates)) {
        counts[state] = (counts[state] || 0) + count;
      }
    }
  }
  return result;
}

/** The count of a state, over all users or, if given, one user. */
export function ccCountOfState(
  byUsersAndStates: {[username: string]: {[state: string]: number}},
  state: string,
  username?: string
): number {
  return Object.entries(byUsersAndStates)
    .filter(([u]) => username === undefined || u === username)
    .reduce((sum, [, byStates]) => sum + (byStates[state] || 0), 0);
}
