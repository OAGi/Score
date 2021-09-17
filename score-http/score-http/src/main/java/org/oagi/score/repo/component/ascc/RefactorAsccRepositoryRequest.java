package org.oagi.score.repo.component.ascc;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.service.common.data.CcState;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class RefactorAsccRepositoryRequest extends RepositoryRequest {

    private final BigInteger asccManifestId;
    private final BigInteger accManifestId;

    public RefactorAsccRepositoryRequest(AuthenticatedPrincipal user, BigInteger asccManifestId, BigInteger accManifestId) {
        super(user);
        this.asccManifestId = asccManifestId;
        this.accManifestId = accManifestId;
    }

    public RefactorAsccRepositoryRequest(AuthenticatedPrincipal user, LocalDateTime localDateTime, BigInteger asccManifestId, BigInteger accManifestId) {
        super(user, localDateTime);
        this.asccManifestId = asccManifestId;
        this.accManifestId = accManifestId;
    }

    public BigInteger getAsccManifestId() {
        return asccManifestId;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }
}
