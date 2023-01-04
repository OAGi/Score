export class ScoreUser {
  userId: number;
  username: string;
  roles: string[];
}

export class UserToken {
  username: string;
  roles: string[];
  authentication: string;
  enabled: boolean;
  isTenantInstance: boolean;
  tenantRoles: string[];

  constructor() {
    this.roles = ['',];
    this.authentication = '';
    this.username = 'unknown';
    this.enabled = false;
    this.isTenantInstance = false;
    this.tenantRoles = [];
  }
}

export class OAuth2AppInfo {
  loginUrl: string;
  providerName: string;
  displayProviderName: string;
  backgroundColor: string;
  fontColor: string;
}
