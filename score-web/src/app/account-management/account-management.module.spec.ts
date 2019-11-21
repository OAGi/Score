import {AccountManagementModule} from './account-management.module';

describe('AccountManagementModule', () => {
  let accountManagementModule: AccountManagementModule;

  beforeEach(() => {
    accountManagementModule = new AccountManagementModule();
  });

  it('should create an instance', () => {
    expect(accountManagementModule).toBeTruthy();
  });
});
