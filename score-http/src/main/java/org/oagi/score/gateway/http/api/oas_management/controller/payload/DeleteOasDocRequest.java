package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeleteOasDocRequest extends Request {

    private List<OasDocId> oasDocIdList = Collections.emptyList();

    public DeleteOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public List<OasDocId> getOasDocIdList() {
        return oasDocIdList;
    }

    public void setOasDocIdList(List<OasDocId> oasDocIdList) {
        if (oasDocIdList != null) {
            this.oasDocIdList = oasDocIdList;
        }
    }

    public void setOasDocId(OasDocId oasDocId) {
        if (oasDocId != null) {
            this.oasDocIdList = Arrays.asList(oasDocId);
        }
    }

    public DeleteOasDocRequest withOasDocId(OasDocId oasDocId) {
        this.setOasDocId(oasDocId);
        return this;
    }

    public DeleteOasDocRequest withOasDocIdList(List<OasDocId> oasDocIdList) {
        this.setOasDocIdList(oasDocIdList);
        return this;
    }

}
