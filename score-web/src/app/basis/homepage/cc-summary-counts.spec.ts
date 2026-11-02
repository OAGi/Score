import {ccCountOfState, ccCountsByUsersAndStates} from './cc-summary-counts';

describe('ccCountsByUsersAndStates', () => {
  const summary = {
    ACC: {oagis: {WIP: 15}, other: {WIP: 1, Draft: 2}},
    ASCCP: {oagis: {WIP: 2}},
    BCCP: {oagis: {WIP: 4, Candidate: 1}},
    BDT: {oagis: {WIP: 4, Draft: 1}},
  };

  it('sums the types that the core component list shows', () => {
    expect(ccCountsByUsersAndStates(summary)).toEqual({
      oagis: {WIP: 21, Candidate: 1},
      other: {WIP: 1, Draft: 2},
    });
  });

  it('leaves out every Data Type label', () => {
    for (const type of ['DT', 'CDT', 'BDT']) {
      expect(ccCountsByUsersAndStates({[type]: {oagis: {WIP: 4}}})).toEqual({});
    }
  });

  it('drops a user whose components are all Data Types', () => {
    expect(ccCountsByUsersAndStates({BDT: {dtonly: {Draft: 1}}, ACC: {oagis: {WIP: 1}}}))
      .toEqual({oagis: {WIP: 1}});
  });

  it('is empty without a summary', () => {
    expect(ccCountsByUsersAndStates(undefined)).toEqual({});
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
});
