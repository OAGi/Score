package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.common.model.base.PaginationResponse;

import java.util.List;

public class GetBieForOasDocListResponse extends PaginationResponse<BieForOasDoc> {
    public GetBieForOasDocListResponse(List<BieForOasDoc> results, int page, int size, int length) {
        super(results, page, size, length);
    }
}
