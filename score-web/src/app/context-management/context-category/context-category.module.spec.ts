import {ContextCategoryModule} from './context-category.module';

describe('ContextCategoryModule', () => {
  let contextCategoryModule: ContextCategoryModule;

  beforeEach(() => {
    contextCategoryModule = new ContextCategoryModule();
  });

  it('should create an instance', () => {
    expect(contextCategoryModule).toBeTruthy();
  });
});
