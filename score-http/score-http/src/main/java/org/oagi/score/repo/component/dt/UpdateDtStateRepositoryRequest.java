package org.oagi.score.repo.component.dt;

import org.oagi.score.data.RepositoryRequest;
import org.oagi.score.service.common.data.CcState;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpdateDtStateRepositoryRequest extends RepositoryRequest {

    private final BigInteger dtManifestId;
    private final CcState fromState;
    private final CcState toState;

    public UpdateDtStateRepositoryRequest(AuthenticatedPrincipal user,
                                          BigInteger dtManifestId,
                                          CcState fromState,
                                          CcState toState) {
        super(user);
        this.dtManifestId = dtManifestId;
        this.fromState = fromState;
        this.toState = toState;
    }

    public UpdateDtStateRepositoryRequest(AuthenticatedPrincipal user,
                                          LocalDateTime localDateTime,
                                          BigInteger dtManifestId,
                                          CcState fromState,
                                          CcState toState) {
        super(user, localDateTime);
        this.dtManifestId = dtManifestId;
        this.fromState = fromState;
        this.toState = toState;
    }

    public BigInteger getDtManifestId() {
        return dtManifestId;
    }


    public CcState getFromState() {
        return fromState;
    }

    public CcState getToState() {
        return toState;
    }
}
