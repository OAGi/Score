export class UserToken {
  username: string;
  role: string;
  authentication: string;

  constructor() {
    this.role = '';
    this.authentication = '';
    this.username = 'unknown';
  }
}

export class OAuth2AppInfo {
  loginUrl: string;
  providerName: string;
  displayProviderName: string;
  backgroundColor: string;
  fontColor: string;
}
