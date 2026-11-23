package org.oagi.score.gateway.http.api.application_management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    /**
     * The new password of the SMTP authentication. It is accepted on an update and never returned.
     * <ul>
     *   <li>absent or {@code null}: the stored password is kept;</li>
     *   <li>empty: the stored password is cleared;</li>
     *   <li>otherwise: the stored password is replaced.</li>
     * </ul>
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String authPassword;

    /** Whether a password is stored. Returned only, so the stored password itself does not leave the server. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean authPasswordSet;

}
