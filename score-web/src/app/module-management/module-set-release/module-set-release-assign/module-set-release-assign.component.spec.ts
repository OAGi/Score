import {SelectionModel} from '@angular/cdk/collections';
import {of} from 'rxjs';
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

  describe('selection of the assigned rows', () => {
    const rowA = {type: 'ACC', manifestId: '1'} as any;
    const rowB = {type: 'ASCCP', manifestId: '2'} as any;

    it('is cleared when another module is selected', () => {
      const tile = {elements: [], current: undefined} as any;
      const component: any = {
        tiles: [tile],
        rightSelection: new SelectionModel<any>(true, [rowA]),
        rightDataSource: {data: [rowA]},
        moduleSetRelease: {moduleSetReleaseId: 1},
        moduleService: {getAssigned: () => of({})},
        assignedMapToList: () => [rowB],
        isUpdating: false,
      };

      ModuleSetReleaseAssignComponent.prototype.onClickElement.call(component, tile, {moduleId: 9, directory: false} as any);

      expect(component.rightSelection.selected).toEqual([]);
      expect(component.rightDataSource.data).toEqual([rowB]);
    });

    it('is not unassigned when the row is not in the shown module', () => {
      const discardAssign = vi.fn();
      const component: any = {
        rightSelection: new SelectionModel<any>(true, [rowA]),
        rightDataSource: {data: [rowB]},
        moduleService: {discardAssign},
      };

      ModuleSetReleaseAssignComponent.prototype.unassign.call(component);

      expect(discardAssign).not.toHaveBeenCalled();
      expect(component.rightDataSource.data).toEqual([rowB]);
    });
  });
});
