package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;

public class GetBieForOasDocListRequest extends PaginationRequest<BieForOasDoc> {
    private BigInteger oasDocId;
    public GetBieForOasDocListRequest(ScoreUser requester) {
        super(requester, BieForOasDoc.class);
    }

    public GetBieForOasDocListRequest(ScoreUser requester, Class<BieForOasDoc> type) {
        super(requester, type);
    }
    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }
}
