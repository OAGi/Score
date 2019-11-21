import {ExtensionDetailModule} from './extension-detail.module';

describe('ExtensionDetailModule', () => {
  let extensionDetailModule: ExtensionDetailModule;

  beforeEach(() => {
    extensionDetailModule = new ExtensionDetailModule();
  });

  it('should create an instance', () => {
    expect(extensionDetailModule).toBeTruthy();
  });
});
