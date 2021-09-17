import {AgencyIdListModule} from './agency-id-list.module';

describe('AgencyIdListModule', () => {
  let codeListModule: AgencyIdListModule;

  beforeEach(() => {
    codeListModule = new AgencyIdListModule();
  });

  it('should create an instance', () => {
    expect(codeListModule).toBeTruthy();
  });
});
