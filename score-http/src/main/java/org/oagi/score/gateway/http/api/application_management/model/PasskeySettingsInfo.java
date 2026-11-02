package org.oagi.score.gateway.http.api.application_management.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Relying-party settings for passkey (WebAuthn) sign-in ({@code score.passkey.*}). Passkey sign-in is enabled exactly
 * when an RP ID and at least one allowed origin are set; there is no separate on/off flag.
 */
@Data
public class PasskeySettingsInfo {

    private String rpId;
    private String rpName;
    private List<String> allowedOrigins = new ArrayList<>();

    public boolean isEnabled() {
        return rpId != null && !rpId.isBlank()
                && allowedOrigins != null && allowedOrigins.stream().anyMatch(e -> e != null && !e.isBlank());
    }

    /**
     * The RP name shown by authenticators; defaults to the RP ID, as Spring Security does.
     */
    public String effectiveRpName() {
        return (rpName != null && !rpName.isBlank()) ? rpName : rpId;
    }

}
