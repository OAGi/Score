package org.oagi.score.repo.component.bccp;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class CreateBccpRepositoryRequest extends RepositoryRequest {

    private final BigInteger bdtManifestId;
    private final BigInteger releaseId;

    private String initialPropertyTerm = "Property Term";

    public CreateBccpRepositoryRequest(AuthenticatedPrincipal user,
                                       BigInteger bdtManifestId, BigInteger releaseId) {
        super(user);
        this.bdtManifestId = bdtManifestId;
        this.releaseId = releaseId;
    }

    public CreateBccpRepositoryRequest(AuthenticatedPrincipal user,
                                       LocalDateTime localDateTime,
                                       BigInteger bdtManifestId, BigInteger releaseId) {
        super(user, localDateTime);
        this.bdtManifestId = bdtManifestId;
        this.releaseId = releaseId;
    }

    public BigInteger getBdtManifestId() {
        return bdtManifestId;
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
