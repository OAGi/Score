package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.PaginationRequest;

import java.math.BigInteger;

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
