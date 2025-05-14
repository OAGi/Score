package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepSummaryRecord;

import java.util.Collection;
import java.util.List;

public interface BbiepQueryRepository {

    BbiepSummaryRecord getBbiepSummary(BbiepId bbiepId);

    List<BbiepSummaryRecord> getBbiepSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList);

    BbiepDetailsRecord getBbiepDetails(BbiepId asbiepId);

    BbiepDetailsRecord getBbiepDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath);

}
