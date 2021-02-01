package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.PaginationRequest;
import org.oagi.score.repo.api.user.model.ScoreUser;

public class GetContextSchemeValueListRequest extends PaginationRequest<ContextScheme> {

    private String value;

    public GetContextSchemeValueListRequest(ScoreUser requester) {
        super(requester, ContextScheme.class);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
