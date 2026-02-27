package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

public class GetOasDocRequest extends Request {
    private OasDocId oasDocId;

    public GetOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public OasDocId getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(OasDocId oasDocId) {
        this.oasDocId = oasDocId;
    }

    public GetOasDocRequest withOasDocId(OasDocId oasDocId) {
        this.setOasDocId(oasDocId);
        return this;
    }
}
