import {ContextSchemeModule} from './context-scheme.module';

describe('ContextSchemeModule', () => {
  let contextSchemeModule: ContextSchemeModule;

  beforeEach(() => {
    contextSchemeModule = new ContextSchemeModule();
  });

  it('should create an instance', () => {
    expect(contextSchemeModule).toBeTruthy();
  });
});
