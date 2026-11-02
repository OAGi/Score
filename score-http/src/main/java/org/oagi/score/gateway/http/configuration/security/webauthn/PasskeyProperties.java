package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oagi.score.gateway.http.api.application_management.model.PasskeySettingsInfo;
import org.oagi.score.gateway.http.api.application_management.service.PasskeySettingsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds the {@code score.passkey.*} configuration (application.yml, or the {@code SCORE_PASSKEY_*} environment
 * variables of a Docker deployment).
 * <p>
 * There is no separate on/off flag: passkey sign-in is enabled exactly when an RP ID and at least one allowed
 * origin are set. The RP ID is the domain passkeys are bound to, so it is a property of the deployment; changing it
 * makes every passkey registered for the old one stop working.
 * <p>
 * The values are validated when the application starts, so a mistake fails the deployment instead of leaving
 * users with a sign-in option that can never work.
 */
@Component
public class PasskeyProperties {

    private final Log logger = LogFactory.getLog(getClass());

    private final PasskeySettingsInfo settings;

    @Autowired
    public PasskeyProperties(@Value("${score.passkey.rp-id:}") String rpId,
                             @Value("${score.passkey.rp-name:}") String rpName,
                             @Value("${score.passkey.allowed-origins:}") String allowedOrigins) {
        PasskeySettingsInfo raw = new PasskeySettingsInfo();
        raw.setRpId(rpId);
        raw.setRpName(rpName);
        raw.setAllowedOrigins(splitOrigins(allowedOrigins));

        try {
            this.settings = PasskeySettingsValidator.validateAndNormalize(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid passkey configuration (score.passkey.*): " + e.getMessage(), e);
        }

        if (this.settings.isEnabled()) {
            logger.info("Passkey sign-in is enabled for the RP ID '" + this.settings.getRpId()
                    + "' and the origins " + this.settings.getAllowedOrigins() + ".");
        } else {
            logger.info("Passkey sign-in is disabled (score.passkey.rp-id and score.passkey.allowed-origins are not set).");
        }
    }

    /**
     * @return the validated settings; a copy, so callers cannot change the configuration
     */
    public PasskeySettingsInfo current() {
        PasskeySettingsInfo copy = new PasskeySettingsInfo();
        copy.setRpId(settings.getRpId());
        copy.setRpName(settings.getRpName());
        copy.setAllowedOrigins(new ArrayList<>(settings.getAllowedOrigins()));
        return copy;
    }

    public List<String> allowedOrigins() {
        return List.copyOf(settings.getAllowedOrigins());
    }

    public boolean isEnabled() {
        return settings.isEnabled();
    }

    /**
     * Origins are separated by commas or whitespace, so both {@code a,b} and a multi-line value work.
     */
    static List<String> splitOrigins(String value) {
        List<String> origins = new ArrayList<>();
        if (value != null) {
            for (String origin : value.split("[,\\s]+")) {
                if (!origin.isEmpty()) {
                    origins.add(origin);
                }
            }
        }
        return origins;
    }

}
