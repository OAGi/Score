package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Response;

public class GetAssignedBusinessTermResponse extends Response {

    private final AssignedBusinessTerm assignedBt;

    public GetAssignedBusinessTermResponse(AssignedBusinessTerm assignedBt) {
        this.assignedBt = assignedBt;
    }

    public final AssignedBusinessTerm getAssignedBt() {
        return assignedBt;
    }
}
