import {formatBytes, LOAD_STEPS, loadProgress} from './release-package-load';

describe('release package load', () => {
  it('goes through the steps in the order a load does, which ends at swapping', () => {
    expect(LOAD_STEPS.map(step => step.state)).toEqual(['PREFLIGHT', 'LOADING', 'MIGRATING', 'COPYING', 'VERIFYING', 'SWAPPING']);
  });

  it('is as far as the share of the steps it has gone through, and all of it when it has succeeded', () => {
    expect(loadProgress('PENDING')).toBe(0);
    expect(loadProgress('PREFLIGHT')).toBe(0);
    expect(loadProgress('COPYING')).toBe(50);
    expect(loadProgress('SWAPPING')).toBe(83);
    expect(loadProgress('SUCCEEDED')).toBe(100);
    expect(loadProgress('FAILED')).toBe(0);
  });

  it('tells sizes in the unit that fits and nothing for a size that is not known', () => {
    expect(formatBytes(-1)).toBe('');
    expect(formatBytes(512 * 1024)).toBe('512 KB');
    expect(formatBytes(68648776)).toBe('65.5 MB');
    expect(formatBytes(4 * 1024 * 1024 * 1024)).toBe('4.00 GB');
  });
});
