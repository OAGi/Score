package org.oagi.score.repo.api.agency.model;

import org.oagi.score.repo.api.base.PaginationResponse;
import org.oagi.score.repo.api.module.model.ModuleSet;

import java.util.List;

public class GetAgencyIdListListResponse extends PaginationResponse<AgencyIdList> {

    public GetAgencyIdListListResponse(List<AgencyIdList> results, int page, int size, int length) {
        super(results, page, size, length);
    }

}
