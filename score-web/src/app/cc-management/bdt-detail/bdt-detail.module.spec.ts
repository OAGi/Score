import {BdtDetailModule} from './bdt-detail.module';

describe('BccpDetailModule', () => {
  let bccpDetailModule: BdtDetailModule;

  beforeEach(() => {
    bccpDetailModule = new BdtDetailModule();
  });

  it('should create an instance', () => {
    expect(bccpDetailModule).toBeTruthy();
  });
});
