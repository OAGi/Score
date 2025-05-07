export class ScoreUser {
  userId: number;
  loginId: string;
  username: string;
  roles: string[];
}

export class BusinessTermProperties {
  enabled: boolean;
}

export class BIEProperties {
  inverseMode: boolean;
}

export class FunctionsRequiringEmailTransmissionProperties {
  enabled: boolean;
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
  functionsRequiringEmailTransmission: FunctionsRequiringEmailTransmissionProperties;

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

    this.functionsRequiringEmailTransmission = new FunctionsRequiringEmailTransmissionProperties();
    this.functionsRequiringEmailTransmission.enabled = false;
  }
}

export class OAuth2AppInfo {
  loginUrl: string;
  providerName: string;
  displayProviderName: string;
  backgroundColor: string;
  fontColor: string;
}
