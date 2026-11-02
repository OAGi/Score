import {ccStateList, ccStateOptions, pickableCcStates} from './cc-dialog.table';

describe('ccStateList', () => {
  it('has the developer states, and ReleaseDraft and Deleted, on the Working branch', () => {
    expect(ccStateList(true)).toEqual(['WIP', 'Draft', 'Candidate', 'ReleaseDraft', 'Published', 'Deleted']);
  });

  it('has the end-user states, and no developer state, on a published release', () => {
    expect(ccStateList(false)).toEqual(['WIP', 'QA', 'Production', 'Published', 'Deleted']);
  });
});

describe('pickableCcStates', () => {
  it('leaves out Deleted and ReleaseDraft on the Working branch', () => {
    expect(pickableCcStates(true)).toEqual(['WIP', 'Draft', 'Candidate', 'Published']);
  });

  it('leaves out Deleted on a published release', () => {
    expect(pickableCcStates(false)).toEqual(['WIP', 'QA', 'Production', 'Published']);
  });
});

describe('ccStateOptions', () => {
  it('labels the states it is given, and no others', () => {
    expect(ccStateOptions(['Draft', 'ReleaseDraft'])).toEqual([
      {value: 'Draft', label: 'Draft'}, {value: 'ReleaseDraft', label: 'Release Draft'}]);
  });
});
