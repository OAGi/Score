import {BieUpliftProfileBieComponent} from './bie-uplift-profile-bie.component';

describe('BieUpliftProfileBieComponent', () => {
  it('should be defined', () => {
    expect(BieUpliftProfileBieComponent).toBeTruthy();
  });

  it('clears the branches of the previous library when the new library has no release', () => {
    const setFilter = vi.fn();
    const component: any = Object.create(BieUpliftProfileBieComponent.prototype);
    component.releases = [];
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
});
