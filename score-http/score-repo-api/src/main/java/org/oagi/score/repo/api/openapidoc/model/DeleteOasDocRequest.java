package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeleteOasDocRequest extends Request {

    private List<BigInteger> oasDocIdList = Collections.emptyList();

    public DeleteOasDocRequest(ScoreUser requester) {
        super(requester);
    }

    public List<BigInteger> getOasDocIdList() {
        return oasDocIdList;
    }

    public void setOasDocIdList(List<BigInteger> oasDocIdList) {
        if (oasDocIdList != null) {
            this.oasDocIdList = oasDocIdList;
        }
    }

    public void setOasDocId(BigInteger oasDocId) {
        if (oasDocId != null) {
            this.oasDocIdList = Arrays.asList(oasDocId);
        }
    }

    public DeleteOasDocRequest withOasDocId(BigInteger oasDocId) {
        this.setOasDocId(oasDocId);
        return this;
    }

    public DeleteOasDocRequest withOasDocIdList(List<BigInteger> oasDocIdList) {
        this.setOasDocIdList(oasDocIdList);
        return this;
    }

}
