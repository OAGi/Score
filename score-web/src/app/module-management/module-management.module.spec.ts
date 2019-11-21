import {ModuleManagementModule} from './module-management.module';

describe('ModuleManagementModule', () => {
  let moduleManagementModule: ModuleManagementModule;

  beforeEach(() => {
    moduleManagementModule = new ModuleManagementModule();
  });

  it('should create an instance', () => {
    expect(moduleManagementModule).toBeTruthy();
  });
});
