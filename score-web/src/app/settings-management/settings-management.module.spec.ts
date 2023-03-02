import {SettingsManagementModule} from './settings-management.module';

describe('settingsManagementModule', () => {
  let settingsManagementModule: SettingsManagementModule;

  beforeEach(() => {
    settingsManagementModule = new SettingsManagementModule();
  });

  it('should create an instance', () => {
    expect(settingsManagementModule).toBeTruthy();
  });
});
