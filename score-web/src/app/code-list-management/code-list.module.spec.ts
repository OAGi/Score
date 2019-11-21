import {CodeListModule} from './code-list.module';

describe('CodeListModule', () => {
  let codeListModule: CodeListModule;

  beforeEach(() => {
    codeListModule = new CodeListModule();
  });

  it('should create an instance', () => {
    expect(codeListModule).toBeTruthy();
  });
});
