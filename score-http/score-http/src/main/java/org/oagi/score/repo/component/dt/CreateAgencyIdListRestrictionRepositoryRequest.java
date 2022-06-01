package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;

public class CreateAgencyIdListRestrictionRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtManifestId;
    private final BigInteger releaseId;
    
    private BigInteger agencyIdListManifestId;



    public CreateAgencyIdListRestrictionRepositoryRequest(AuthenticatedPrincipal user,
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

    public BigInteger getAgencyIdListManifestId() {
        return agencyIdListManifestId;
    }

    public void setAgencyIdListManifestId(BigInteger agencyIdListManifestId) {
        this.agencyIdListManifestId = agencyIdListManifestId;
    }
}
