package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

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
