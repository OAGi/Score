import {BieManagementModule} from './bie-management.module';

describe('BieManagementModule', () => {
  let bieManagementModule: BieManagementModule;

  beforeEach(() => {
    bieManagementModule = new BieManagementModule();
  });

  it('should create an instance', () => {
    expect(bieManagementModule).toBeTruthy();
  });
});
