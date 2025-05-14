package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;

import java.util.Collection;
import java.util.List;

public interface AbieQueryRepository {

    AbieSummaryRecord getAbieSummary(AbieId abieId);

    List<AbieSummaryRecord> getAbieSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList);

    AbieDetailsRecord getAbieDetails(AbieId abieId);

    AbieDetailsRecord getAbieDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath);

}
