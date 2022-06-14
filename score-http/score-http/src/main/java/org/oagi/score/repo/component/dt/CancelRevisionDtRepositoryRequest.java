package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class CancelRevisionDtRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtManifestId;

    public CancelRevisionDtRepositoryRequest(AuthenticatedPrincipal user,
                                             BigInteger dtManifestId) {
        super(user);
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }
}
