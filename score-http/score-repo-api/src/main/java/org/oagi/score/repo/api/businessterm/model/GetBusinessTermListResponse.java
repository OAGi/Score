package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetBusinessTermListResponse extends PaginationResponse<BusinessTerm> {

    public GetBusinessTermListResponse(List<BusinessTerm> results, int page, int size, int length) {
        super(results, page, size, length);
    }

}
