import {NamespaceManagementModule} from './namespace-management.module';

describe('NamespaceManagementModule', () => {
  let namespaceManagementModule: NamespaceManagementModule;

  beforeEach(() => {
    namespaceManagementModule = new NamespaceManagementModule();
  });

  it('should create an instance', () => {
    expect(namespaceManagementModule).toBeTruthy();
  });
});
