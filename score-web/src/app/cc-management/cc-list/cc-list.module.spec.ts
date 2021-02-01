import {CcListModule} from './cc-list.module';

describe('CcListModule', () => {
  let ccListModule: CcListModule;

  beforeEach(() => {
    ccListModule = new CcListModule();
  });

  it('should create an instance', () => {
    expect(ccListModule).toBeTruthy();
  });
});
