package org.oagi.score.repo.api.bie.model;

import org.oagi.score.repo.api.base.Response;

import java.util.List;

public class GetUpliftedBieListResponse extends Response {

    private final List<TopLevelAsbiep> topLevelAsbiepList;

    public GetUpliftedBieListResponse(List<TopLevelAsbiep> topLevelAsbiepList) {
        this.topLevelAsbiepList = topLevelAsbiepList;
    }

    public List<TopLevelAsbiep> getTopLevelAsbiepList() {
        return topLevelAsbiepList;
    }

}
