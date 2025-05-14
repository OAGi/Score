package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;

import java.util.Collection;
import java.util.List;

public interface AsbiepQueryRepository {

    AsbiepSummaryRecord getAsbiepSummary(AsbiepId asbiepId);

    List<AsbiepSummaryRecord> getAsbiepSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList);

    AsbiepDetailsRecord getAsbiepDetails(AsbiepId asbiepId);

    AsbiepDetailsRecord getAsbiepDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath);

}
