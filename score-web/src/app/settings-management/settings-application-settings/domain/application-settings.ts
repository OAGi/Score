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
  /** Sent only to change the password: omitted keeps it, an empty string clears it. Never returned. */
  authPassword?: string;
  /** Returned only: whether a password is stored. */
  authPasswordSet?: boolean;
}
