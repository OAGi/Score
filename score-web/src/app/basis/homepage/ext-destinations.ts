import {Destination} from '../../common/destinations/destinations';
import {encodeListQuery} from '../../common/state-progress-bar/state-progress-bar';

/** How many User Extension Groups a user has in a state in a release, as the summary API returns it. */
export interface ExtReleaseCount {
  releaseId: number;
  releaseNum: string;
  ownerUsername: string;
  state: string;
  count: number;
}

/** The newest release first. */
function byRelease(a: {releaseNum: string}, b: {releaseNum: string}): number {
  return b.releaseNum.localeCompare(a.releaseNum, undefined, {numeric: true});
}

/**
 * The pages that a number of the User Extensions opens: the list of each release that has some of them (of the state, of the user, or of
 * both), with how many it lists. The User Extensions are in the releases they were made for, so a number asks which one to open.
 */
export function extDestinations(
  counts: ExtReleaseCount[] | undefined,
  componentType: string,
  state?: string,
  username?: string
): Destination[] {
  const byReleaseId = new Map<number, {releaseNum: string, count: number}>();
  for (const e of counts ?? []) {
    if ((state !== undefined && e.state !== state) || (username !== undefined && e.ownerUsername !== username)) {
      continue;
    }
    const entry = byReleaseId.get(e.releaseId) ?? {releaseNum: e.releaseNum, count: 0};
    entry.count += e.count;
    byReleaseId.set(e.releaseId, entry);
  }
  return Array.from(byReleaseId.entries())
    .filter(([, e]) => e.count > 0)
    .sort(([, a], [, b]) => byRelease(a, b))
    .map(([releaseId, e]) => ({
      label: e.releaseNum + ' (' + e.count + ')',
      route: '/core_component',
      queryParams: {
        q: encodeListQuery([
          ...(state ? [{key: 'states', value: state}] : []),
          ...(username ? [{key: 'ownerLoginIdList', value: username}] : []),
          {key: 'releaseId', value: releaseId},
          {key: 'types', value: ['ACC']},
          {key: 'componentTypes', value: [componentType]}
        ])
      }
    }));
}
