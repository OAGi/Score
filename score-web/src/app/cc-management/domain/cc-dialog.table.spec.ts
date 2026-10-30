import {ccStateList, usableCcStates} from './cc-dialog.table';

describe('ccStateList', () => {
  it('has no QA or Production on the Working branch', () => {
    expect(ccStateList(true)).toEqual(['WIP', 'Draft', 'Candidate', 'Published', 'Deleted']);
  });

  it('has QA and Production on a published release, and no developer state', () => {
    expect(ccStateList(false)).toEqual(['WIP', 'QA', 'Production', 'Published', 'Deleted']);
  });

  it('has no ReleaseDraft anywhere', () => {
    expect([...ccStateList(true), ...ccStateList(false)]).not.toContain('ReleaseDraft');
  });

  it('keeps Deleted out of the usable states', () => {
    expect(usableCcStates(true)).not.toContain('Deleted');
    expect(usableCcStates(false)).not.toContain('Deleted');
  });
});
