package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.BieCodeListStateDependencyRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditUsed;

import java.util.Collection;
import java.util.List;

public interface BbieQueryRepository {

    BbieSummaryRecord getBbieSummary(BbieId bbieId);

    List<BbieSummaryRecord> getBbieSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList);

    List<BbieSummaryRecord> getBbieSummaryList(AbieId fromAbieId, TopLevelAsbiepId topLevelAsbiepId);

    BbieDetailsRecord getBbieDetails(BbieId bbieId);

    BbieDetailsRecord getBbieDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath);

    List<BieEditUsed> getUsedBbieList(TopLevelAsbiepId topLevelAsbiepId);

    /**
     * Returns distinct code lists currently assigned by BBIE nodes that belong
     * to the given top-level BIE.
     *
     * @param topLevelAsbiepId owning top-level BIE
     * @return lightweight code-list summaries used by state dependency checks
     */
    List<BieCodeListStateDependencyRecord> getAssignedCodeListSummaryList(TopLevelAsbiepId topLevelAsbiepId);

}
