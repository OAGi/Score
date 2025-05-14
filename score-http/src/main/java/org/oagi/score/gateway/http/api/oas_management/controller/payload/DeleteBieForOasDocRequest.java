package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeleteBieForOasDocRequest extends Request {
    private BigInteger oasDocId;
    private List<BieForOasDoc> bieForOasDocList = Collections.emptyList();

    public DeleteBieForOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public void setBieForOasDoc(BieForOasDoc bieForOasDoc) {
        if (bieForOasDoc != null) {
            this.bieForOasDocList = Arrays.asList(bieForOasDoc);
        }
    }

    public DeleteBieForOasDocRequest withBieForOasDoc(BieForOasDoc bieForOasDoc) {
        this.setBieForOasDoc(bieForOasDoc);
        return this;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public List<BieForOasDoc> getBieForOasDocList() {
        return bieForOasDocList;
    }

    public void setBieForOasDocList(List<BieForOasDoc> bieForOasDocList) {
        this.bieForOasDocList = bieForOasDocList;
    }

    public DeleteBieForOasDocRequest withBieForOasDocList(List<BieForOasDoc> bieForOasDocList) {
        this.setBieForOasDocList(bieForOasDocList);
        return this;
    }
}
