import {RELEASE_ASSIGN_TYPE_OPTIONS, ReleaseAssignComponent} from './release-assign.component';

describe('ReleaseAssignComponent', () => {
  it('should be defined', () => {
    expect(ReleaseAssignComponent).toBeTruthy();
  });

  describe('hrefLink', () => {
    const hrefLink = (type: string, manifestId: string): string[] =>
      ReleaseAssignComponent.prototype.hrefLink.call({}, {type, manifestId} as any);

    it('links a Data Type to the Data Type page', () => {
      expect(hrefLink('DT', '7')).toEqual(['/data_type', '7']);
    });

    it('keeps the routes of the other component types', () => {
      expect(hrefLink('ACC', '7')).toEqual(['/core_component', 'acc', '7']);
      expect(hrefLink('CODE_LIST', '7')).toEqual(['', 'code_list', '7']);
    });
  });

  describe('type filter', () => {
    it('offers Code List and Agency ID List', () => {
      const labels = Object.fromEntries(RELEASE_ASSIGN_TYPE_OPTIONS.map(t => [t.value, t.label]));
      expect(Object.keys(labels)).toEqual(['ACC', 'ASCCP', 'BCCP', 'CODE_LIST', 'AGENCY_ID_LIST', 'DT']);
      expect(labels['CODE_LIST']).toBe('Code List');
      expect(labels['AGENCY_ID_LIST']).toBe('Agency ID List');
    });

    it('shows only nodes of the selected Code List type', () => {
      const isVisible = (type: string, selected: string[]) =>
        ReleaseAssignComponent.prototype.isVisible.call(
          {filter: {den: '', type: selected, owner: []}}, {type, den: 'x', ownerUsername: 'u'} as any);
      expect(isVisible('CODE_LIST', ['CODE_LIST'])).toBe(true);
      expect(isVisible('AGENCY_ID_LIST', ['CODE_LIST'])).toBe(false);
      expect(isVisible('AGENCY_ID_LIST', ['AGENCY_ID_LIST'])).toBe(true);
    });
  });
});
