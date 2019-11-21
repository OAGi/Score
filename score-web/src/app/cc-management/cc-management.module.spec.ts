import {CcManagementModule} from './cc-management.module';

describe('CcManagementModule', () => {
  let ccManagementModule: CcManagementModule;

  beforeEach(() => {
    ccManagementModule = new CcManagementModule();
  });

  it('should create an instance', () => {
    expect(ccManagementModule).toBeTruthy();
  });
});
