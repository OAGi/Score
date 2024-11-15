package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Response;

public class GetBaseBieResponse extends Response {

    private final TopLevelAsbiep baseTopLevelAsbiep;

    public GetBaseBieResponse(TopLevelAsbiep baseTopLevelAsbiep) {
        this.baseTopLevelAsbiep = baseTopLevelAsbiep;
    }

    public TopLevelAsbiep getBaseTopLevelAsbiep() {
        return baseTopLevelAsbiep;
    }
}

