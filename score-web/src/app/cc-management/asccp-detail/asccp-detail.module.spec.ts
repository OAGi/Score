import {AsccpDetailModule} from './asccp-detail.module';

describe('AsccpDetailModule', () => {
  let asccpDetailModule: AsccpDetailModule;

  beforeEach(() => {
    asccpDetailModule = new AsccpDetailModule();
  });

  it('should create an instance', () => {
    expect(asccpDetailModule).toBeTruthy();
  });
});
