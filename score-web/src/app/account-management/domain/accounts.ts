import {PageRequest} from '../../basis/basis';

export class AccountListRequest {
  filters: {
    loginId: string;
    name: string;
    organization: string;
    role: string;
  };
  page: PageRequest;

  constructor() {
    this.filters = {
      loginId: '',
      name: '',
      organization: '',
      role: '',
    };
  }
}

export class AccountList {
  appUserId: number;
  loginId: string;
  password: string;
  name: string;
  organization: string;
  developer: boolean;
}

