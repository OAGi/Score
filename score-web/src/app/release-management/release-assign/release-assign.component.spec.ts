import {ReleaseAssignComponent} from './release-assign.component';

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
});
