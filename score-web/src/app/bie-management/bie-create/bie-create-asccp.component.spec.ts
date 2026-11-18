import {SelectionModel} from '@angular/cdk/collections';
import {of} from 'rxjs';
import {BieCreateAsccpComponent} from './bie-create-asccp.component';

describe('BieCreateAsccpComponent', () => {
  it('should be defined', () => {
    expect(BieCreateAsccpComponent).toBeTruthy();
  });

  function newComponent(): any {
    const component: any = Object.create(BieCreateAsccpComponent.prototype);
    component.selection = new SelectionModel(false, []);
    component.selection.select({manifestId: 1});
    const calls: string[] = [];
    component.calls = calls;
    component.releaseChoice = {choose: () => {}, init: () => {}, release: {releaseId: 2}};
    component.libraryChoice = {choose: () => {}, library: {libraryId: 3}};
    component.extraParams = {set: () => calls.push('applyLibrary')};
    component.view = () => ({
      setFilter: () => {},
      onSearch: () => calls.push('onSearch:' + component.selection.selected.length)
    });
    component.releaseService = {getReleaseSummaryList: () => of([])};
    return component;
  }

  it('clears the selected top-level concept when the Branch is changed', () => {
    const component = newComponent();
    component.onBranchChange({releaseId: 2});
    expect(component.selection.selected.length).toBe(0);
    expect(component.calls).toEqual(['onSearch:0']);
  });

  it('clears the selected top-level concept when the Library is changed, before searching with the new Library', () => {
    const component = newComponent();
    component.onLibraryChange({libraryId: 3});
    expect(component.selection.selected.length).toBe(0);
    expect(component.calls).toEqual(['applyLibrary', 'onSearch:0']);
  });
});
