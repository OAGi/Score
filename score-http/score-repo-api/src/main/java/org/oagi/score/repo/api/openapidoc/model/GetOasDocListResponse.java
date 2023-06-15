package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetOasDocListResponse extends PaginationResponse<OasDoc> {
    public GetOasDocListResponse(List<OasDoc> results, int page, int size, int length) {
        super(results, page, size, length);
    }
}
