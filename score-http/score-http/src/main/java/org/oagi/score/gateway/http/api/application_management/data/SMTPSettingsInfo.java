package org.oagi.score.gateway.http.api.application_management.data;

import lombok.Data;

@Data
public class SMTPSettingsInfo {

    private String host;
    private int port;
    private boolean auth;
    private boolean sslEnable;
    private boolean startTlsEnable;
    private String authMethod;
    private String authUsername;
    private String authPassword;

}
