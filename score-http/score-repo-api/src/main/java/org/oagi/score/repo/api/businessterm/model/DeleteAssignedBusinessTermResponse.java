package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Response;

import java.util.List;

public class DeleteAssignedBusinessTermResponse extends Response {

    private final List<BieToAssign> assignedBtList;

    public DeleteAssignedBusinessTermResponse(List<BieToAssign> assignedBizTermIdList) {
        this.assignedBtList = assignedBizTermIdList;
    }

    public List<BieToAssign> getAssignedBtList() {
        return assignedBtList;
    }

    public boolean contains(BieToAssign assignedBizTermId) {
        return this.assignedBtList.contains(assignedBizTermId);
    }

    public boolean containsAll(List<BieToAssign> assignedBizTermIdList) {
        for (BieToAssign assignedBizTermId : assignedBizTermIdList) {
            if (!this.assignedBtList.contains(assignedBizTermId)) {
                return false;
            }
        }
        return true;
    }
}
