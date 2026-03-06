package org.oagi.score.gateway.http.api.application_management.model;

import lombok.Data;

@Data
public class ApplicationSettingsInfo {

    private SMTPSettingsInfo smtpSettingsInfo;
    private String bieSchemaFilenameExpression;
    private String biePackageSchemaFilenameExpression;
    private String bieSchemaFilenameDuplicateHandlerExpression;
    private String biePackageSchemaFilenameDuplicateHandlerExpression;

}
