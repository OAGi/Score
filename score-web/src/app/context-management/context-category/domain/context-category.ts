import {PageRequest} from '../../../basis/basis';

export class ContextCategory {
  ctxCategoryId: number;
  guid: string;
  name: string;
  description?: string;
  used: boolean;
}

export class ContextCategoryListRequest {
  filters: {
    name: string;
    description: string;
  };
  page: PageRequest;

  constructor() {
    this.filters = {
      name: '',
      description: ''
    };
  }

}
