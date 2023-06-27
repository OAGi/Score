package org.oagi.score.repo.api.openapidoc.model;

import org.oagi.score.repo.api.base.PaginationResponse;
import org.oagi.score.repo.api.base.Response;

import java.math.BigInteger;
import java.util.List;

public class AddBieForOasDocResponse extends PaginationResponse<BieForOasDoc> {

    public AddBieForOasDocResponse(List<BieForOasDoc> results, int page, int size, int length) {
        super(results, page, size, length);
    }
}
