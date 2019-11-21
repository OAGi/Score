import {BccpDetailModule} from './bccp-detail.module';

describe('BccpDetailModule', () => {
  let bccpDetailModule: BccpDetailModule;

  beforeEach(() => {
    bccpDetailModule = new BccpDetailModule();
  });

  it('should create an instance', () => {
    expect(bccpDetailModule).toBeTruthy();
  });
});
