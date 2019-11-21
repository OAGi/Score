import {BusinessContextModule} from './business-context.module';

describe('BusinessContextModule', () => {
  let businessContextModule: BusinessContextModule;

  beforeEach(() => {
    businessContextModule = new BusinessContextModule();
  });

  it('should create an instance', () => {
    expect(businessContextModule).toBeTruthy();
  });
});
