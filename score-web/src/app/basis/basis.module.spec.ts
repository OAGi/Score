import {BasisModule} from './basis.module';

describe('BasisModule', () => {
  let basisModule: BasisModule;

  beforeEach(() => {
    basisModule = new BasisModule();
  });

  it('should create an instance', () => {
    expect(basisModule).toBeTruthy();
  });
});
