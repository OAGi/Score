package org.oagi.score.gateway.http.configuration.security.webauthn;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.passkey_management.model.PasskeyUser;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores the WebAuthn user entity (user handle) of an application user in {@code app_webauthn_user}.
 * The entity name is the account's login ID.
 */
@Component
@Transactional
public class ScorePublicKeyCredentialUserEntityRepository implements PublicKeyCredentialUserEntityRepository {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private SessionService sessionService;

    private ScoreUser system() {
        return sessionService.getScoreSystemUser();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicKeyCredentialUserEntity findById(Bytes id) {
        return repositoryFactory.passkeyQueryRepository(system())
                .findPasskeyUserByHandle(id.getBytes())
                .map(ScorePublicKeyCredentialUserEntityRepository::toEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicKeyCredentialUserEntity findByUsername(String username) {
        return repositoryFactory.passkeyQueryRepository(system())
                .findPasskeyUserByLoginId(username)
                .map(ScorePublicKeyCredentialUserEntityRepository::toEntity)
                .orElse(null);
    }

    @Override
    public void save(PublicKeyCredentialUserEntity userEntity) {
        ScoreUser system = system();
        var query = repositoryFactory.passkeyQueryRepository(system);
        if (query.findPasskeyUserByLoginId(userEntity.getName()).isPresent()) {
            return;
        }
        UserId userId = query.findUserIdByLoginId(userEntity.getName())
                .orElseThrow(() -> new IllegalArgumentException("The account does not exist: " + userEntity.getName()));
        repositoryFactory.passkeyCommandRepository(system)
                .insertPasskeyUser(userId, userEntity.getId().getBytes());
    }

    @Override
    public void delete(Bytes id) {
        repositoryFactory.passkeyCommandRepository(system()).deletePasskeyUserByHandle(id.getBytes());
    }

    private static PublicKeyCredentialUserEntity toEntity(PasskeyUser user) {
        return ImmutablePublicKeyCredentialUserEntity.builder()
                .name(user.loginId())
                .id(new Bytes(user.userHandle()))
                .displayName(user.displayName())
                .build();
    }

}
