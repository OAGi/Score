package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeleteBieForOasDocRequest extends Request {
    private List<BieForOasDoc> assignedBieList = Collections.emptyList();

    public DeleteBieForOasDocRequest(ScoreUser requester, List<BieForOasDoc> assignedBieList) {
        super(requester);
        this.assignedBieList = assignedBieList;
    }

    public void setBieForOasDocId(BieForOasDoc assignedBieId) {
        if (assignedBieId != null) {
            this.assignedBieList = Arrays.asList(assignedBieId);
        }
    }

    public DeleteBieForOasDocRequest withAssignedBieId(BieForOasDoc assignedBieId) {
        this.setBieForOasDocId(assignedBieId);
        return this;
    }

    public List<BieForOasDoc> getAssignedBieList() {
        return assignedBieList;
    }

    public void setAssignedBieList(List<BieForOasDoc> assignedBieList) {
        this.assignedBieList = assignedBieList;
    }

    public DeleteBieForOasDocRequest withAssignedBieId(List<BieForOasDoc> assignedBieId) {
        this.setAssignedBieList(assignedBieId);
        return this;
    }


}
