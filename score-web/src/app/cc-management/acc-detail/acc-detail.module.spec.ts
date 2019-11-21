import {AccDetailModule} from './acc-detail.module';

describe('AccDetailModule', () => {
  let accDetailModule: AccDetailModule;

  beforeEach(() => {
    accDetailModule = new AccDetailModule();
  });

  it('should create an instance', () => {
    expect(accDetailModule).toBeTruthy();
  });
});
