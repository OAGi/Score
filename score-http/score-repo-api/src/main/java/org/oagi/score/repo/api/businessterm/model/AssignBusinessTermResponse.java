package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class AssignBusinessTermResponse extends Response {

    private final List<BigInteger> assignedBusinessTermIdList;

    public AssignBusinessTermResponse(List<BigInteger> assignedBusinessTermIdList) {
        this.assignedBusinessTermIdList = assignedBusinessTermIdList;
    }

    public List<BigInteger> getAssignedBusinessTermIdList() {
        return assignedBusinessTermIdList;
    }
}
