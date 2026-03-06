export class ApplicationSettingsInfo {
  smtpSettingsInfo: SMTPSettingsInfo;
  bieSchemaFilenameExpression: string;
  biePackageSchemaFilenameExpression: string;
  bieSchemaFilenameDuplicateHandlerExpression: string;
  biePackageSchemaFilenameDuplicateHandlerExpression: string;
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
