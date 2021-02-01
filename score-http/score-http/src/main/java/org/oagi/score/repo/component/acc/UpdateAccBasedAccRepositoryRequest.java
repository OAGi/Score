package org.oagi.score.repo.component.acc;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateAccBasedAccRepositoryRequest extends RepositoryRequest {

    private final BigInteger accManifestId;
    private final BigInteger basedAccManifestId;

    public UpdateAccBasedAccRepositoryRequest(AuthenticatedPrincipal user,
                                              BigInteger accManifestId,
                                              BigInteger basedAccManifestId) {
        super(user);
        this.accManifestId = accManifestId;
        this.basedAccManifestId = basedAccManifestId;
    }

    public UpdateAccBasedAccRepositoryRequest(AuthenticatedPrincipal user,
                                              LocalDateTime localDateTime,
                                              BigInteger accManifestId,
                                              BigInteger basedAccManifestId) {
        super(user, localDateTime);
        this.accManifestId = accManifestId;
        this.basedAccManifestId = basedAccManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }

    public BigInteger getBasedAccManifestId() {
        return basedAccManifestId;
    }
}
