package org.oagi.score.repo.component.bcc;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class RefactorBccRepositoryRequest extends RepositoryRequest {

    private final BigInteger bccManifestId;
    private final BigInteger accManifestId;

    public RefactorBccRepositoryRequest(AuthenticatedPrincipal user, BigInteger bccManifestId, BigInteger accManifestId) {
        super(user);
        this.bccManifestId = bccManifestId;
        this.accManifestId = accManifestId;
    }

    public RefactorBccRepositoryRequest(AuthenticatedPrincipal user, LocalDateTime localDateTime, BigInteger bccManifestId, BigInteger accManifestId) {
        super(user, localDateTime);
        this.bccManifestId = bccManifestId;
        this.accManifestId = accManifestId;
    }

    public BigInteger getBccManifestId() {
        return bccManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
