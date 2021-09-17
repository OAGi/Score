package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class CreateBdtRepositoryRequest extends RepositoryRequest {

    private final BigInteger basedDdtManifestId;
    private final BigInteger releaseId;

    private String initialPropertyTerm = "Property Term";

    public CreateBdtRepositoryRequest(AuthenticatedPrincipal user,
                                      BigInteger basedDdtManifestId, BigInteger releaseId) {
        super(user);
        this.basedDdtManifestId = basedDdtManifestId;
        this.releaseId = releaseId;
    }

    public CreateBdtRepositoryRequest(AuthenticatedPrincipal user,
                                      LocalDateTime localDateTime,
                                      BigInteger basedDdtManifestId, BigInteger releaseId) {
        super(user, localDateTime);
        this.basedDdtManifestId = basedDdtManifestId;
        this.releaseId = releaseId;
    }

    public BigInteger getBasedDdtManifestId() {
        return basedDdtManifestId;
    }

    public BigInteger getReleaseId() {
        return releaseId;
    }

    public String getInitialPropertyTerm() {
        return initialPropertyTerm;
    }

    public void setInitialPropertyTerm(String initialPropertyTerm) {
        this.initialPropertyTerm = initialPropertyTerm;
    }
}
