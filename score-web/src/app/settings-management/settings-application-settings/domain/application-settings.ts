export class ApplicationSettingsInfo {
  smtpSettingsInfo: SMTPSettingsInfo;
}

export class SMTPSettingsInfo {
  host: string;
  port: number;
  auth: boolean;
  sslEnable: boolean;
  startTlsEnable: boolean;
  authMethod: string;
  authUsername: string;
  authPassword: string;
}
