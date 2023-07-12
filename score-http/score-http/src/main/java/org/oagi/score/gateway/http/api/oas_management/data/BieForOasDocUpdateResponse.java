package org.oagi.score.gateway.http.api.oas_management.data;

import org.oagi.score.repo.api.base.PaginationResponse;
import org.oagi.score.repo.api.openapidoc.model.BieForOasDoc;

import java.util.List;

public class BieForOasDocUpdateResponse extends PaginationResponse<BieForOasDoc> {
    public BieForOasDocUpdateResponse(List<BieForOasDoc> results, int page, int size, int length) {
        super(results, page, size, length);
    }
}
