package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.common.model.base.Response;

import java.util.List;

public class DeleteBieForOasDocResponse extends Response {
    private final List<BieForOasDoc> bieForOasDocList;

    public DeleteBieForOasDocResponse(List<BieForOasDoc> bieForOasDocList) {
        this.bieForOasDocList = bieForOasDocList;
    }

    public List<BieForOasDoc> getBieForOasDocList() {
        return bieForOasDocList;
    }

    public boolean contains(BieForOasDoc bieForOasDoc) {
        return this.bieForOasDocList.contains(bieForOasDoc);
    }

    public boolean containsAll(List<BieForOasDoc> bieForOasDocList) {
        for (BieForOasDoc bieForOasDoc: bieForOasDocList) {
            if (!this.bieForOasDocList.contains(bieForOasDoc)) {
                return false;
            }
        }
        return true;
    }
}
