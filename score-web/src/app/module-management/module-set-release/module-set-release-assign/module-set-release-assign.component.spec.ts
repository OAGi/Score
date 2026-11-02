import {ModuleSetReleaseAssignComponent} from './module-set-release-assign.component';

describe('ModuleSetReleaseAssignComponent', () => {
  it('should be defined', () => {
    expect(ModuleSetReleaseAssignComponent).toBeTruthy();
  });

  describe('hrefLink', () => {
    const hrefLink = (type: string, manifestId: string): string[] =>
      ModuleSetReleaseAssignComponent.prototype.hrefLink.call({}, {type, manifestId} as any);

    it('links a Data Type to the Data Type page', () => {
      expect(hrefLink('DT', '7')).toEqual(['/data_type', '7']);
    });

    it('keeps the routes of the core component types', () => {
      expect(hrefLink('ACC', '7')).toEqual(['/core_component', 'acc', '7']);
      expect(hrefLink('ASCCP', '7')).toEqual(['/core_component', 'asccp', '7']);
    });
  });
});
