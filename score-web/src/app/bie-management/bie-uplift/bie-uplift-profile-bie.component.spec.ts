import {SelectionModel} from '@angular/cdk/collections';
import {BieUpliftProfileBieComponent} from './bie-uplift-profile-bie.component';

describe('BieUpliftProfileBieComponent', () => {
  it('should be defined', () => {
    expect(BieUpliftProfileBieComponent).toBeTruthy();
  });

  it('clears the branches of the previous library when the new library has no release', () => {
    const setFilter = vi.fn();
    const component: any = Object.create(BieUpliftProfileBieComponent.prototype);
    component.releases = [];
    component.selection = new SelectionModel<any>(false, []);
    component.sourceRelease = {releaseId: 1, releaseNum: '10.13.1'};
    component.targetRelease = {releaseId: 3, releaseNum: '10.13.3'};
    component.view = () => ({setFilter});
    component.sourceReleaseListFilterCtrl = {valueChanges: {subscribe: () => undefined}, setValue: () => undefined};
    component.targetReleaseListFilterCtrl = {valueChanges: {subscribe: () => undefined}, setValue: () => undefined};
    component.sourceReleaseFilteredList = {next: () => undefined};
    component.targetReleaseFilteredList = {next: () => undefined};

    component.initReleases([]);

    expect(component.sourceRelease).toBeUndefined();
    expect(component.targetRelease).toBeUndefined();
    expect(setFilter).toHaveBeenCalledWith('releaseIds', []);
  });

  it('clears the selected BIE when the source branch changes', () => {
    const component: any = Object.create(BieUpliftProfileBieComponent.prototype);
    component.auth = {getUserToken: () => undefined};
    component.selection = new SelectionModel<any>(false, []);
    component.selection.select({topLevelAsbiepId: 1});
    component.applySourceRelease = () => undefined;
    component.view = () => ({onSearch: () => undefined});
    component.targetReleaseListFilterCtrl = {valueChanges: {subscribe: () => undefined}, setValue: () => undefined};
    component.targetReleaseFilteredList = {next: () => undefined};
    Object.defineProperty(component, 'targetReleaseList', {get: () => []});

    component.onSourceReleaseChange({releaseId: 2, releaseNum: '10.13.2'});

    expect(component.selection.selected.length).toBe(0);
  });

  it('clears the selected BIE when the releases of the library are initialized', () => {
    const component: any = Object.create(BieUpliftProfileBieComponent.prototype);
    component.selection = new SelectionModel<any>(false, []);
    component.selection.select({topLevelAsbiepId: 1});
    component.view = () => ({setFilter: () => undefined});
    component.sourceReleaseListFilterCtrl = {valueChanges: {subscribe: () => undefined}, setValue: () => undefined};
    component.targetReleaseListFilterCtrl = {valueChanges: {subscribe: () => undefined}, setValue: () => undefined};
    component.sourceReleaseFilteredList = {next: () => undefined};
    component.targetReleaseFilteredList = {next: () => undefined};

    component.initReleases([]);

    expect(component.selection.selected.length).toBe(0);
  });
});
