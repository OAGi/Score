package org.oagi.score.repo.api.module.model;

import org.oagi.score.repo.api.base.PaginationResponse;

import java.util.List;

public class GetModuleSetReleaseListResponse extends PaginationResponse<ModuleSetRelease> {

    public GetModuleSetReleaseListResponse(List<ModuleSetRelease> results, int page, int size, int length) {
        super(results, page, size, length);
    }

}
