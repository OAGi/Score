import {BieExpressModule} from './bie-express.module';

describe('BieExpressModule', () => {
  let bieExpressModule: BieExpressModule;

  beforeEach(() => {
    bieExpressModule = new BieExpressModule();
  });

  it('should create an instance', () => {
    expect(bieExpressModule).toBeTruthy();
  });
});
