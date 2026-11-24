import {CC_KINDS, ccCountOfState, ccCountsByKinds, ccCountsByUsersAndStates, ccDestinations} from './cc-summary-counts';

describe('ccCountsByUsersAndStates', () => {
  const summary = {
    ACC: {oagis: {WIP: 15}, other: {WIP: 1, Draft: 2}},
    ASCCP: {oagis: {WIP: 2}},
    BCCP: {oagis: {WIP: 4, Candidate: 1}},
    BDT: {oagis: {WIP: 4, Draft: 1}},
    CODE_LIST: {oagis: {WIP: 3}, dtonly: {Draft: 1}},
    AGENCY_ID_LIST: {other: {Candidate: 2}},
  };

  it('sums the Core Components, the Data Types, the Code Lists and the Agency ID Lists', () => {
    expect(ccCountsByUsersAndStates(summary)).toEqual({
      oagis: {WIP: 28, Candidate: 1, Draft: 1},
      other: {WIP: 1, Draft: 2, Candidate: 2},
      dtonly: {Draft: 1},
    });
  });

  it('counts every Data Type label as a Data Type', () => {
    for (const type of ['DT', 'CDT', 'BDT']) {
      expect(ccCountsByKinds({[type]: {oagis: {WIP: 4}}}).dt).toEqual({oagis: {WIP: 4}});
    }
  });

  it('is empty without a summary', () => {
    expect(ccCountsByUsersAndStates(undefined)).toEqual({});
    expect(ccCountsByKinds(undefined).cc).toEqual({});
  });

  it('keeps the kinds apart', () => {
    const byKinds = ccCountsByKinds(summary);
    expect(byKinds.cc).toEqual({oagis: {WIP: 21, Candidate: 1}, other: {WIP: 1, Draft: 2}});
    expect(byKinds.codeList).toEqual({oagis: {WIP: 3}, dtonly: {Draft: 1}});
    expect(byKinds.agencyIdList).toEqual({other: {Candidate: 2}});
  });
});

describe('ccCountOfState', () => {
  const byUsers = {oagis: {WIP: 21, Candidate: 1}, other: {WIP: 1}};

  it('totals a state over all users', () => {
    expect(ccCountOfState(byUsers, 'WIP')).toBe(22);
  });

  it('counts a state of one user', () => {
    expect(ccCountOfState(byUsers, 'WIP', 'other')).toBe(1);
    expect(ccCountOfState(byUsers, 'Draft', 'oagis')).toBe(0);
  });

  it('counts every state when none is given', () => {
    expect(ccCountOfState(byUsers, undefined)).toBe(23);
    expect(ccCountOfState(byUsers, undefined, 'oagis')).toBe(22);
  });
});

describe('ccDestinations', () => {
  const byKinds = ccCountsByKinds({
    ACC: {oagis: {WIP: 2, Draft: 1}},
    BDT: {oagis: {WIP: 1}},
    CODE_LIST: {oagis: {WIP: 3}, other: {WIP: 1}},
  });
  const q = (d: {queryParams: {q: string}}) => atob(d.queryParams.q);

  it('names each kind that has some, with how many, and opens its list with the filter of the number', () => {
    const destinations = ccDestinations(byKinds, [{key: 'releaseId', value: 7}], 'WIP', 'oagis');
    expect(destinations.map(d => d.label)).toEqual(['Core Components (2)', 'Data Types (1)', 'Code Lists (3)']);
    expect(destinations.map(d => d.route)).toEqual(['/core_component', '/data_type', '/code_list']);
    expect(q(destinations[0])).toBe('states=WIP&ownerLoginIdList=oagis&releaseId=7');
    expect(q(ccDestinations(byKinds, [], undefined, 'oagis')[0])).toBe('states=WIP,Draft,Candidate&ownerLoginIdList=oagis');
  });

  it('has the one kind that a number is of, when it is of one', () => {
    expect(ccDestinations(byKinds, [], 'Draft', 'oagis').map(d => d.route)).toEqual(['/core_component']);
    expect(ccDestinations(byKinds, [], 'WIP', 'other').map(d => d.route)).toEqual(['/code_list']);
  });

  it('has none for a number of nothing', () => {
    expect(ccDestinations(byKinds, [], 'Candidate', 'oagis')).toEqual([]);
  });

  it('knows the page of each kind', () => {
    expect(CC_KINDS.map(k => k.route)).toEqual(['/core_component', '/data_type', '/code_list', '/agency_id_list']);
  });
});
