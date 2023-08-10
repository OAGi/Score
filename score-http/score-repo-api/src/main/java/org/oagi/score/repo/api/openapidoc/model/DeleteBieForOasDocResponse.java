package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
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
