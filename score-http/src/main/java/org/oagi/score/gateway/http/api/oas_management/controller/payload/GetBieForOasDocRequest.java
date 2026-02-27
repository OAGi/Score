package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.PaginationRequest;

import java.math.BigInteger;

public class GetBieForOasDocRequest extends PaginationRequest<BieForOasDoc> {
    private OasDocId oasDocId;
    private String businessContext;
    private TopLevelAsbiepId topLevelAsbiepId;

    public GetBieForOasDocRequest(ScoreUser requester) {
        super(requester, BieForOasDoc.class);
        this.oasDocId = oasDocId;
    }

    public OasDocId getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(OasDocId oasDocId) {
        this.oasDocId = oasDocId;
    }

    public String getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(String businessContext) {
        this.businessContext = businessContext;
    }

    public GetBieForOasDocRequest withOasDocId(OasDocId oasDocId) {
        this.setOasDocId(oasDocId);
        return this;
    }

    public TopLevelAsbiepId getTopLevelAsbiepId() {
        return topLevelAsbiepId;
    }

    public void setTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        this.topLevelAsbiepId = topLevelAsbiepId;
    }

    public GetBieForOasDocRequest withTopLevelAsbiepId(TopLevelAsbiepId topLevelAsbiepId) {
        setTopLevelAsbiepId(topLevelAsbiepId);
        return this;
    }
}
