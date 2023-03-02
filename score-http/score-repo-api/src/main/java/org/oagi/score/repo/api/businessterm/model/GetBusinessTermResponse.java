package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Response;

public class GetBusinessTermResponse extends Response {

    private final BusinessTerm businessTerm;

    public GetBusinessTermResponse(BusinessTerm businessTerm) {
        this.businessTerm = businessTerm;
    }

    public final BusinessTerm getBusinessTerm() {
        return businessTerm;
    }
}
