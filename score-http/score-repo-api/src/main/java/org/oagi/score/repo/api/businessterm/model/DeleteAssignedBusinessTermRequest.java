package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class  DeleteAssignedBusinessTermRequest extends Request {

    private List<BieToAssign> assignedBtList = Collections.emptyList();

    public DeleteAssignedBusinessTermRequest(ScoreUser requester) {
        super(requester);
    }

    public List<BieToAssign> getAssignedBtList() {
        return assignedBtList;
    }

    public void setBusinessTermId(BieToAssign assignedBizTermId) {
        if (assignedBizTermId != null) {
            this.assignedBtList = Arrays.asList(assignedBizTermId);
        }
    }

    public DeleteAssignedBusinessTermRequest withassignedBizTermId(BieToAssign assignedBizTermId) {
        this.setBusinessTermId(assignedBizTermId);
        return this;
    }

    public void setAssignedBtList(List<BieToAssign> assignedBtList) {
        if (assignedBtList != null) {
            this.assignedBtList = assignedBtList;
        }
    }

    public DeleteAssignedBusinessTermRequest withAssignedBtList(List<BieToAssign> assignedBizTermId) {
        this.setAssignedBtList(assignedBizTermId);
        return this;
    }

}
