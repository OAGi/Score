import {MessageManagementModule} from './message-management.module';

describe('MessageManagementModule', () => {
  let messageManagementModule: MessageManagementModule;

  beforeEach(() => {
    messageManagementModule = new MessageManagementModule();
  });

  it('should create an instance', () => {
    expect(MessageManagementModule).toBeTruthy();
  });
});
