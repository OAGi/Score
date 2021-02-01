package org.oagi.score.repo.component.bccp;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.service.common.data.CcState;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateBccpStateRepositoryRequest extends RepositoryRequest {

    private final BigInteger bccpManifestId;
    private final CcState fromState;
    private final CcState toState;

    public UpdateBccpStateRepositoryRequest(AuthenticatedPrincipal user,
                                            BigInteger bccpManifestId,
                                            CcState fromState,
                                            CcState toState) {
        super(user);
        this.bccpManifestId = bccpManifestId;
        this.fromState = fromState;
        this.toState = toState;
    }

    public UpdateBccpStateRepositoryRequest(AuthenticatedPrincipal user,
                                            LocalDateTime localDateTime,
                                            BigInteger bccpManifestId,
                                            CcState fromState,
                                            CcState toState) {
        super(user, localDateTime);
        this.bccpManifestId = bccpManifestId;
        this.fromState = fromState;
        this.toState = toState;
    }

    public BigInteger getBccpManifestId() {
        return bccpManifestId;
    }


    public CcState getFromState() {
        return fromState;
    }

    public CcState getToState() {
        return toState;
    }
}
