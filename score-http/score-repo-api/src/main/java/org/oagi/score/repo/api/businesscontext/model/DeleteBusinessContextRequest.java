package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeleteBusinessContextRequest extends Request {

    private List<BigInteger> businessContextIdList = Collections.emptyList();

    public DeleteBusinessContextRequest(ScoreUser requester) {
        super(requester);
    }

    public List<BigInteger> getBusinessContextIdList() {
        return businessContextIdList;
    }

    public void setBusinessContextId(BigInteger businessContextId) {
        if (businessContextId != null) {
            this.businessContextIdList = Arrays.asList(businessContextId);
        }
    }

    public DeleteBusinessContextRequest withBusinessContextId(BigInteger businessContextId) {
        this.setBusinessContextId(businessContextId);
        return this;
    }

    public void setBusinessContextIdList(List<BigInteger> businessContextIdList) {
        if (businessContextIdList != null) {
            this.businessContextIdList = businessContextIdList;
        }
    }

    public DeleteBusinessContextRequest withBusinessContextIdList(List<BigInteger> businessContextIdList) {
        this.setBusinessContextIdList(businessContextIdList);
        return this;
    }

}
