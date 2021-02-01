package org.oagi.score.repo.component.acc;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.service.common.data.CcState;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateAccStateRepositoryRequest extends RepositoryRequest {

    private final BigInteger accManifestId;
    private final CcState fromState;
    private final CcState toState;

    public UpdateAccStateRepositoryRequest(AuthenticatedPrincipal user,
                                           BigInteger accManifestId,
                                           CcState fromState,
                                           CcState toState) {
        super(user);
        this.accManifestId = accManifestId;
        this.fromState = fromState;
        this.toState = toState;
    }

    public UpdateAccStateRepositoryRequest(AuthenticatedPrincipal user,
                                           LocalDateTime localDateTime,
                                           BigInteger accManifestId,
                                           CcState fromState,
                                           CcState toState) {
        super(user, localDateTime);
        this.accManifestId = accManifestId;
        this.fromState = fromState;
        this.toState = toState;
    }

    public BigInteger getAccManifestId() {
        return accManifestId;
    }

    public CcState getFromState() {
        return fromState;
    }

    public CcState getToState() {
        return toState;
    }
}
