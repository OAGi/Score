import {ModuleSetCreateComponent} from './module-set-create.component';
import {ModuleSet} from '../../domain/module';

describe('ModuleSetCreateComponent', () => {
  it('should be defined', () => {
    expect(ModuleSetCreateComponent).toBeTruthy();
  });

  describe('initReleases', () => {
    it('clears the Release and the Module Set Release chosen for the previous library', () => {
      const moduleSet = new ModuleSet();
      moduleSet.createModuleSetRelease = true;
      moduleSet.targetReleaseId = 1n as any;
      moduleSet.targetModuleSetReleaseId = 2n as any;
      const component: any = {
        moduleSet,
        releaseList: [],
        releaseListFilterCtrl: {valueChanges: {subscribe: () => ({})}, setValue: () => {}},
        filteredReleaseList: {next: () => {}},
      };

      ModuleSetCreateComponent.prototype.initReleases.call(component, []);

      expect(moduleSet.targetReleaseId).toBeUndefined();
      expect(moduleSet.targetModuleSetReleaseId).toBeUndefined();
      moduleSet.name = 'Name';
      expect(Object.getOwnPropertyDescriptor(ModuleSetCreateComponent.prototype, 'canCreate')!.get!.call(component)).toBe(false);
    });
  });
});
