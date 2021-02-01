package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;

public class UpdateBusinessContextRequest extends Request {

    private BigInteger businessContextId;

    private String name;

    private Collection<BusinessContextValue> businessContextValueList;

    public UpdateBusinessContextRequest(ScoreUser requester) {
        super(requester);
    }

    public BigInteger getBusinessContextId() {
        return businessContextId;
    }

    public void setBusinessContextId(BigInteger businessContextId) {
        this.businessContextId = businessContextId;
    }

    public UpdateBusinessContextRequest withBusinessContextId(BigInteger businessContextId) {
        this.setBusinessContextId(businessContextId);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UpdateBusinessContextRequest withName(String name) {
        this.setName(name);
        return this;
    }

    public Collection<BusinessContextValue> getBusinessContextValueList() {
        return (businessContextValueList == null) ? Collections.emptyList() : businessContextValueList;
    }

    public void setBusinessContextValueList(Collection<BusinessContextValue> businessContextValueList) {
        this.businessContextValueList = businessContextValueList;
    }

    public UpdateBusinessContextRequest withBusinessContextValueList(
            Collection<BusinessContextValue> businessContextValueList) {
        this.setBusinessContextValueList(businessContextValueList);
        return this;
    }

}
