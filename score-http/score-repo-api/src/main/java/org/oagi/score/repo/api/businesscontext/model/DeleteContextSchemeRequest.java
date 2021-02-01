package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeleteContextSchemeRequest extends Request {

    private List<BigInteger> contextSchemeIdList = Collections.emptyList();

    public DeleteContextSchemeRequest(ScoreUser requester) {
        super(requester);
    }

    public List<BigInteger> getContextSchemeIdList() {
        return contextSchemeIdList;
    }

    public void setContextSchemeId(BigInteger contextSchemeId) {
        if (contextSchemeId != null) {
            this.contextSchemeIdList = Arrays.asList(contextSchemeId);
        }
    }

    public DeleteContextSchemeRequest withContextSchemeId(BigInteger contextSchemeId) {
        this.setContextSchemeId(contextSchemeId);
        return this;
    }

    public void setContextSchemeIdList(List<BigInteger> contextSchemeIdList) {
        if (contextSchemeIdList != null) {
            this.contextSchemeIdList = contextSchemeIdList;
        }
    }

    public DeleteContextSchemeRequest withContextSchemeIdList(List<BigInteger> contextSchemeIdList) {
        this.setContextSchemeIdList(contextSchemeIdList);
        return this;
    }

}
