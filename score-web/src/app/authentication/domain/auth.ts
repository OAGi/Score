export class ScoreUser {
  userId: number;
  username: string;
  roles: string[];
}

export class BusinessTermProperties {
  enabled: boolean;
}

export class BIEProperties {
  inverseMode: boolean;
}

export class TenantProperties {
  enabled: boolean;
  roles: string[];
}

export class UserToken {
  username: string;
  roles: string[];
  authentication: string;
  enabled: boolean;

  tenant: TenantProperties;
  businessTerm: BusinessTermProperties;
  bie: BIEProperties;

  constructor() {
    this.roles = ['', ];
    this.authentication = '';
    this.username = 'unknown';
    this.enabled = false;

    this.tenant = new TenantProperties();
    this.tenant.enabled = false;
    this.tenant.roles = [];

    this.businessTerm = new BusinessTermProperties();
    this.businessTerm.enabled = false;

    this.bie = new BIEProperties();
    this.bie.inverseMode = false;
  }
}

export class OAuth2AppInfo {
  loginUrl: string;
  providerName: string;
  displayProviderName: string;
  backgroundColor: string;
  fontColor: string;
}
