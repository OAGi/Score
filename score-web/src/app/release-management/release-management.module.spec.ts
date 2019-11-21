import {ReleaseManagementModule} from './release-management.module';

describe('ReleaseManagementModule', () => {
  let releaseManagementModule: ReleaseManagementModule;

  beforeEach(() => {
    releaseManagementModule = new ReleaseManagementModule();
  });

  it('should create an instance', () => {
    expect(releaseManagementModule).toBeTruthy();
  });
});
