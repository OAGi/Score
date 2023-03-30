package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class CreateCodeListRestrictionRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtManifestId;
    private final BigInteger releaseId;

    private BigInteger codeListManifestId;



    public CreateCodeListRestrictionRepositoryRequest(AuthenticatedPrincipal user,
                                                      BigInteger dtManifestId, BigInteger releaseId) {
        super(user);
        this.dtManifestId = dtManifestId;
        this.releaseId = releaseId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }

    public void setCodeListManifestId(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }
}
