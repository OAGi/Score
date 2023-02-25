import {BusinessTermManagementModule} from './business-term-management.module';

describe('ContextManagementModule', () => {
  let contextManagementModule: BusinessTermManagementModule;

  beforeEach(() => {
    contextManagementModule = new BusinessTermManagementModule();
  });

  it('should create an instance', () => {
    expect(contextManagementModule).toBeTruthy();
  });
});
