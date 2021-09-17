package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateBccpBdtRepositoryRequest extends RepositoryRequest {

    private final BigInteger bccpManifestId;
    private final BigInteger bdtManifestId;

    public UpdateBccpBdtRepositoryRequest(AuthenticatedPrincipal user,
                                          BigInteger bccpManifestId,
                                          BigInteger bdtManifestId) {
        super(user);
        this.bccpManifestId = bccpManifestId;
        this.bdtManifestId = bdtManifestId;
    }

    public UpdateBccpBdtRepositoryRequest(AuthenticatedPrincipal user,
                                          LocalDateTime localDateTime,
                                          BigInteger bccpManifestId,
                                          BigInteger bdtManifestId) {
        super(user, localDateTime);
        this.bccpManifestId = bccpManifestId;
        this.bdtManifestId = bdtManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }

    public BigInteger getBdtManifestId() {
        return bdtManifestId;
    }
}
