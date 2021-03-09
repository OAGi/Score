package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetModuleListResponse extends PaginationResponse<Module> {

    public GetModuleListResponse(List<Module> results, int page, int size, int length) {
        super(results, page, size, length);
    }

}
