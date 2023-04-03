package org.oagi.score.repo.component.asccp;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class PurgeAsccpRepositoryRequest extends RepositoryRequest {

    private final BigInteger asccpManifestId;

    private boolean ignoreState;

    private boolean ignoreOnError;

    public PurgeAsccpRepositoryRequest(AuthenticatedPrincipal user,
                                       BigInteger asccpManifestId) {
        super(user);
        this.asccpManifestId = asccpManifestId;
    }

    public PurgeAsccpRepositoryRequest(AuthenticatedPrincipal user,
                                       LocalDateTime localDateTime,
                                       BigInteger asccpManifestId) {
        super(user, localDateTime);
        this.asccpManifestId = asccpManifestId;
    }

    public BigInteger getAsccpManifestId() {
        return asccpManifestId;
    }

    public boolean isIgnoreState() {
        return ignoreState;
    }

    public void setIgnoreState(boolean ignoreState) {
        this.ignoreState = ignoreState;
    }

    public boolean isIgnoreOnError() {
        return ignoreOnError;
    }

    public void setIgnoreOnError(boolean ignoreOnError) {
        this.ignoreOnError = ignoreOnError;
    }
}
