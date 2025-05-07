package org.oagi.score.gateway.http.api.oas_management.controller.payload;

import org.oagi.score.gateway.http.api.oas_management.model.OasDoc;
import org.oagi.score.gateway.http.common.model.base.PaginationResponse;

import java.util.List;

public class GetOasDocListResponse extends PaginationResponse<OasDoc> {
    public GetOasDocListResponse(List<OasDoc> results, int page, int size, int length) {
        super(results, page, size, length);
    }
}
