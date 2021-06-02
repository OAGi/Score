package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Response;

import java.util.List;

public class GetReuseBieListResponse extends Response {

    private final List<TopLevelAsbiep> topLevelAsbiepList;

    public GetReuseBieListResponse(List<TopLevelAsbiep> topLevelAsbiepList) {
        this.topLevelAsbiepList = topLevelAsbiepList;
    }

    public List<TopLevelAsbiep> getTopLevelAsbiepList() {
        return topLevelAsbiepList;
    }

}

