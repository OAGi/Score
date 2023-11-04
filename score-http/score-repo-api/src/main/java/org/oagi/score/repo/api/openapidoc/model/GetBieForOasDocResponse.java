package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetBieForOasDocResponse extends PaginationResponse<BieForOasDoc> {

    public GetBieForOasDocResponse(List<BieForOasDoc> results, int page, int size, int length) {
        super(results, page, size, length);
    }
}
