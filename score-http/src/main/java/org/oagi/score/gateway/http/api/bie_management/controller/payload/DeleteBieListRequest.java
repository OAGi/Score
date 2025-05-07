package org.oagi.score.gateway.http.api.bie_management.controller.payload;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;

import java.util.List;

public record DeleteBieListRequest(List<TopLevelAsbiepId> topLevelAsbiepIdList) {
}
