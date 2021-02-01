package org.oagi.score.repo.component.bbie_sc;

import org.oagi.score.data.RepositoryRequest;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.math.BigInteger;
import java.time.LocalDateTime;

public class UpsertBbieScRequest extends RepositoryRequest {

    private final BigInteger topLevelAsbiepId;
    private final BbieScNode.BbieSc bbieSc;

    public UpsertBbieScRequest(AuthenticatedPrincipal user, LocalDateTime localDateTime,
                               BigInteger topLevelAsbiepId, BbieScNode.BbieSc bbieSc) {
        super(user, localDateTime);
        this.topLevelAsbiepId = topLevelAsbiepId;
        this.bbieSc = bbieSc;
    }

    public BigInteger getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public BbieScNode.BbieSc getBbieSc() {
        return bbieSc;
    }
}
