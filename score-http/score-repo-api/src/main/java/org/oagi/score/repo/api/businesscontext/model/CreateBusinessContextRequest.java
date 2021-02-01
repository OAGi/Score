package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.util.Collection;

public class CreateBusinessContextRequest extends Request {

    private String name;

    private Collection<BusinessContextValue> businessContextValueList;

    public CreateBusinessContextRequest(ScoreUser requester) {
        super(requester);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateBusinessContextRequest withName(String name) {
        this.setName(name);
        return this;
    }

    public Collection<BusinessContextValue> getBusinessContextValueList() {
        return businessContextValueList;
    }

    public void setBusinessContextValueList(Collection<BusinessContextValue> businessContextValueList) {
        this.businessContextValueList = businessContextValueList;
    }

    public CreateBusinessContextRequest withBusinessContextValueList(
            Collection<BusinessContextValue> businessContextValueList) {
        this.setBusinessContextValueList(businessContextValueList);
        return this;
    }

}
