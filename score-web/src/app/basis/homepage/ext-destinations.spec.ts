import {extDestinations} from './ext-destinations';

describe('extDestinations', () => {
  const counts = [
    {releaseId: 7, releaseNum: '11.5', ownerUsername: 'bob', state: 'WIP', count: 2},
    {releaseId: 5, releaseNum: '10.13.3', ownerUsername: 'bob', state: 'WIP', count: 1},
    {releaseId: 5, releaseNum: '10.13.3', ownerUsername: 'bob', state: 'Production', count: 3},
    {releaseId: 3, releaseNum: '10.9', ownerUsername: 'bob', state: 'WIP', count: 4},
    {releaseId: 5, releaseNum: '10.13.3', ownerUsername: 'carol', state: 'WIP', count: 5},
  ];
  const q = (d: {queryParams: {q: string}}) => atob(d.queryParams.q);

  it('names each release that has some, with how many, the newest first', () => {
    const destinations = extDestinations(counts, 'UEG', 'WIP', 'bob');
    expect(destinations.map(d => d.label)).toEqual(['11.5 (2)', '10.13.3 (1)', '10.9 (4)']);
    expect(destinations.every(d => d.route === '/core_component')).toBe(true);
  });

  it('opens the list of the release with the filter of the number', () => {
    const [newest, release] = extDestinations(counts, 'UEG', 'WIP', 'bob');
    expect(q(newest)).toBe('states=WIP&ownerLoginIdList=bob&releaseId=7&types=ACC&componentTypes=UEG');
    expect(q(release)).toContain('releaseId=5');
  });

  it('sums the states and the users when the number has none', () => {
    expect(extDestinations(counts, 'UEG').map(d => d.label)).toEqual(['11.5 (2)', '10.13.3 (9)', '10.9 (4)']);
    expect(q(extDestinations(counts, 'UEG', undefined, 'carol')[0])).toBe('ownerLoginIdList=carol&releaseId=5&types=ACC&componentTypes=UEG');
  });

  it('has none for a number of nothing, or without a summary', () => {
    expect(extDestinations(counts, 'UEG', 'QA', 'bob')).toEqual([]);
    expect(extDestinations(undefined, 'UEG')).toEqual([]);
  });
});
