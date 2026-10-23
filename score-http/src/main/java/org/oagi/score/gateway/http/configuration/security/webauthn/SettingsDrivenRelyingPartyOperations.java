package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.oagi.score.gateway.http.api.application_management.model.PasskeySettingsInfo;
import org.springframework.security.web.webauthn.api.AuthenticatorSelectionCriteria;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialParameters;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.ResidentKeyRequirement;
import org.springframework.security.web.webauthn.api.UserVerificationRequirement;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialCreationOptionsRequest;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialRequestOptionsRequest;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.RelyingPartyAuthenticationRequest;
import org.springframework.security.web.webauthn.management.RelyingPartyRegistrationRequest;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A {@link WebAuthnRelyingPartyOperations} whose RP ID, RP name and allowed origins come from the supplied settings
 * ({@code score.passkey.*}).
 * <p>
 * Spring Security's {@code WebAuthnConfigurer} builds its own operations once, and insists on an RP ID, unless a bean
 * of this type exists. This bean builds (and caches) the delegate lazily, so an instance without passkey settings
 * still starts and simply reports that passkeys are not configured. The settings are read on each call, so a
 * supplier that can change is honoured.
 * <p>
 * Registration asks for a discoverable credential (so sign-in needs no username) and requires user verification
 * (PIN or biometric), because a passkey replaces the password as the only factor.
 */
public class SettingsDrivenRelyingPartyOperations implements WebAuthnRelyingPartyOperations {

    private record Key(String rpId, String rpName, Set<String> allowedOrigins) {
    }

    private record Cached(Key key, Webauthn4JRelyingPartyOperations operations) {
    }

    private final PublicKeyCredentialUserEntityRepository userEntities;
    private final UserCredentialRepository userCredentials;
    private final Supplier<PasskeySettingsInfo> settings;

    private volatile Cached cached;

    public SettingsDrivenRelyingPartyOperations(PublicKeyCredentialUserEntityRepository userEntities,
                                                UserCredentialRepository userCredentials,
                                                Supplier<PasskeySettingsInfo> settings) {
        this.userEntities = userEntities;
        this.userCredentials = userCredentials;
        this.settings = settings;
    }

    @Override
    public PublicKeyCredentialCreationOptions createPublicKeyCredentialCreationOptions(
            PublicKeyCredentialCreationOptionsRequest request) {
        return delegate().createPublicKeyCredentialCreationOptions(request);
    }

    @Override
    public CredentialRecord registerCredential(RelyingPartyRegistrationRequest request) {
        return delegate().registerCredential(request);
    }

    @Override
    public PublicKeyCredentialRequestOptions createCredentialRequestOptions(
            PublicKeyCredentialRequestOptionsRequest request) {
        return delegate().createCredentialRequestOptions(request);
    }

    @Override
    public PublicKeyCredentialUserEntity authenticate(RelyingPartyAuthenticationRequest request) {
        return delegate().authenticate(request);
    }

    private Webauthn4JRelyingPartyOperations delegate() {
        PasskeySettingsInfo current = settings.get();
        if (current == null || !current.isEnabled()) {
            throw new IllegalStateException("Passkey sign-in is not configured.");
        }

        Key key = new Key(current.getRpId(), current.effectiveRpName(),
                new LinkedHashSet<>(current.getAllowedOrigins()));
        Cached snapshot = this.cached;
        if (snapshot != null && Objects.equals(snapshot.key(), key)) {
            return snapshot.operations();
        }

        synchronized (this) {
            snapshot = this.cached;
            if (snapshot == null || !Objects.equals(snapshot.key(), key)) {
                snapshot = new Cached(key, build(key));
                this.cached = snapshot;
            }
            return snapshot.operations();
        }
    }

    private Webauthn4JRelyingPartyOperations build(Key key) {
        Webauthn4JRelyingPartyOperations operations = new Webauthn4JRelyingPartyOperations(
                userEntities, userCredentials,
                PublicKeyCredentialRpEntity.builder().id(key.rpId()).name(key.rpName()).build(),
                key.allowedOrigins());
        // ES256 first and always listed: password-manager extensions such as Bitwarden support only ES256 and refuse a
        // request without it. RS256 stays for authenticators that cannot do ES256 (some Windows Hello setups).
        operations.setCustomizeCreationOptions(builder -> builder
                .pubKeyCredParams(PublicKeyCredentialParameters.ES256, PublicKeyCredentialParameters.RS256)
                .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                        .residentKey(ResidentKeyRequirement.REQUIRED)
                        .userVerification(UserVerificationRequirement.REQUIRED)
                        .build()));
        operations.setCustomizeRequestOptions(builder -> builder.userVerification(UserVerificationRequirement.REQUIRED));
        return operations;
    }

}
