import {SelectionModel} from '@angular/cdk/collections';
import {BieCopyProfileBieComponent} from './bie-copy-profile-bie.component';

describe('BieCopyProfileBieComponent', () => {
  it('should be defined', () => {
    expect(BieCopyProfileBieComponent).toBeTruthy();
  });

  function newComponent(): any {
    const component: any = Object.create(BieCopyProfileBieComponent.prototype);
    component.selection = new SelectionModel<any>(false, []);
    component.selection.select({topLevelAsbiepId: 1});
    component.selectedReleases = [];
    component.view = () => ({setFilter: () => undefined, filterValue: () => undefined});
    return component;
  }

  it('clears the selected BIE when the branches or the library change', () => {
    const component = newComponent();

    component.applyReleases();

    expect(component.selection.selected.length).toBe(0);
  });

  it('clears the selected BIE when the releases of the library are initialized', () => {
    const component = newComponent();

    component.initReleases([]);

    expect(component.selection.selected.length).toBe(0);
  });
});
