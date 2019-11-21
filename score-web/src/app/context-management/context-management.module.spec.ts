import {ContextManagementModule} from './context-management.module';

describe('ContextManagementModule', () => {
  let contextManagementModule: ContextManagementModule;

  beforeEach(() => {
    contextManagementModule = new ContextManagementModule();
  });

  it('should create an instance', () => {
    expect(contextManagementModule).toBeTruthy();
  });
});
