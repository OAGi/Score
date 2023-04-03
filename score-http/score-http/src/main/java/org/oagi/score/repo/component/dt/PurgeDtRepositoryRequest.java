package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class PurgeDtRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtManifestId;

    private boolean ignoreOnError;

    public PurgeDtRepositoryRequest(AuthenticatedPrincipal user,
                                    BigInteger dtManifestId) {
        super(user);
        this.dtManifestId = dtManifestId;
    }

    public PurgeDtRepositoryRequest(AuthenticatedPrincipal user,
                                    LocalDateTime localDateTime,
                                    BigInteger dtManifestId) {
        super(user, localDateTime);
        this.dtManifestId = dtManifestId;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }

    public boolean isIgnoreOnError() {
        return ignoreOnError;
    }

    public void setIgnoreOnError(boolean ignoreOnError) {
        this.ignoreOnError = ignoreOnError;
    }
}
