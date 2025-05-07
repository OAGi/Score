import {DtListModule} from './dt-list.module';

describe('CcListModule', () => {
  let ccListModule: DtListModule;

  beforeEach(() => {
    ccListModule = new DtListModule();
  });

  it('should create an instance', () => {
    expect(ccListModule).toBeTruthy();
  });
});
