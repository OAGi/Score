package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetModuleSetListResponse extends PaginationResponse<ModuleSet> {

    public GetModuleSetListResponse(List<ModuleSet> results, int page, int size, int length) {
        super(results, page, size, length);
    }

}
