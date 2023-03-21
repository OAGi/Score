package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetAssignedBusinessTermListResponse extends PaginationResponse<AssignedBusinessTerm> {

    public GetAssignedBusinessTermListResponse(List<AssignedBusinessTerm> results, int page, int size, int length) {
        super(results, page, size, length);
    }

}
