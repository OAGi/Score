package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class ReviseBccpRepositoryRequest extends RepositoryRequest {

    private final BigInteger bccpManifestId;

    public ReviseBccpRepositoryRequest(AuthenticatedPrincipal user,
                                       BigInteger bccpManifestId) {
        super(user);
        this.bccpManifestId = bccpManifestId;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }
}
