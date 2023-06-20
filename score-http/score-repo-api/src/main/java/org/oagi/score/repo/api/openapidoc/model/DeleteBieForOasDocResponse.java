package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Response;

import java.util.List;

public class DeleteBieForOasDocResponse extends Response {
    private final List<BieForOasDoc> assignedBieList;

    public DeleteBieForOasDocResponse(List<BieForOasDoc> assignedBieList) {
        this.assignedBieList = assignedBieList;
    }

    public List<BieForOasDoc> getAssignedBieList() {
        return assignedBieList;
    }

    public boolean contains(BieForOasDoc assignedBieId) {
        return this.assignedBieList.contains(assignedBieId);
    }

    public boolean containsAll(List<BieForOasDoc> assignedBieList) {
        for (BieForOasDoc assignedBieId : assignedBieList) {
            if (!this.assignedBieList.contains(assignedBieId)) {
                return false;
            }
        }
        return true;
    }
}
