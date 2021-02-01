package org.oagi.score.repo.api.businesscontext.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetBusinessContextListResponse extends PaginationResponse<BusinessContext> {

    public GetBusinessContextListResponse(List<BusinessContext> results, int page, int size, int length) {
        super(results, page, size, length);
    }

}
