package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.base.Request;

import java.math.BigInteger;
import java.util.List;

public class UpdateBieForOasDocRequest extends Request {
    private List<BieForOasDoc> bieForOasDocList;
    private BigInteger oasDocId;

    public UpdateBieForOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public List<BieForOasDoc> getBieForOasDocList() {
        return bieForOasDocList;
    }

    public void setBieForOasDocList(List<BieForOasDoc> bieForOasDocList) {
        this.bieForOasDocList = bieForOasDocList;
    }

    public UpdateBieForOasDocRequest withBieForOasDocList(List<BieForOasDoc> bieForOasDocList) {
        setBieForOasDocList(bieForOasDocList);
        return this;
    }

    public BigInteger getOasDocId() {
        return oasDocId;
    }

    public void setOasDocId(BigInteger oasDocId) {
        this.oasDocId = oasDocId;
    }

    public UpdateBieForOasDocRequest withOasDocId(BigInteger oasDocId) {
        setOasDocId(oasDocId);
        return this;
    }

    @Override
    public String toString() {
        return "UpdateBieForOasDocRequest{" +
                "bieForOasDocList=" + bieForOasDocList +
                ", oasDocId=" + oasDocId +
                '}';
    }
}
