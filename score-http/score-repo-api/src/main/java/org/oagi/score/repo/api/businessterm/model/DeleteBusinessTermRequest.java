package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeleteBusinessTermRequest extends Request {

    private List<BigInteger> businessTermIdList = Collections.emptyList();

    public DeleteBusinessTermRequest(ScoreUser requester) {
        super(requester);
    }

    public List<BigInteger> getBusinessTermIdList() {
        return businessTermIdList;
    }

    public void setBusinessTermId(BigInteger businessTermId) {
        if (businessTermId != null) {
            this.businessTermIdList = Arrays.asList(businessTermId);
        }
    }

    public DeleteBusinessTermRequest withBusinessTermId(BigInteger businessTermId) {
        this.setBusinessTermId(businessTermId);
        return this;
    }

    public void setBusinessTermIdList(List<BigInteger> businessTermIdList) {
        if (businessTermIdList != null) {
            this.businessTermIdList = businessTermIdList;
        }
    }

    public DeleteBusinessTermRequest withBusinessTermIdList(List<BigInteger> businessTermIdList) {
        this.setBusinessTermIdList(businessTermIdList);
        return this;
    }

}
