package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDocId;
import org.oagi.score.gateway.http.common.model.base.Response;

import java.util.List;

public class DeleteOasDocResponse extends Response {
    private final List<OasDocId> oasDocIdList;

    public DeleteOasDocResponse(List<OasDocId> oasDocIdList) {
        this.oasDocIdList = oasDocIdList;
    }

    public List<OasDocId> getOasDocIdList() {
        return oasDocIdList;
    }

    public boolean contains(OasDocId oasDocId) {
        return this.oasDocIdList.contains(oasDocId);
    }

    public boolean containsAll(List<OasDocId> oasDocIdList) {
        for (OasDocId oasDocId : oasDocIdList) {
            if (!this.oasDocIdList.contains(oasDocId)) {
                return false;
            }
        }
        return true;
    }
}
