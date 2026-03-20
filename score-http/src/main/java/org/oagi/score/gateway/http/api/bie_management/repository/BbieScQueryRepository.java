package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.BieCodeListStateDependencyRecord;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditUsed;

import java.util.Collection;
import java.util.List;

public interface BbieScQueryRepository {

    BbieScSummaryRecord getBbieScSummary(BbieScId bbieScId);

    List<BbieScSummaryRecord> getBbieScSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList);

    List<BbieScSummaryRecord> getBbieScSummaryList(BbieId bbieId, TopLevelAsbiepId topLevelAsbiepId);

    BbieScDetailsRecord getBbieScDetails(BbieScId bbieScId);

    BbieScDetailsRecord getBbieScDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath);

    List<BieEditUsed> getUsedBbieScList(TopLevelAsbiepId topLevelAsbiepId);

    /**
     * Returns distinct code lists currently assigned by BBIE_SC nodes that
     * belong to the given top-level BIE.
     *
     * @param topLevelAsbiepId owning top-level BIE
     * @return lightweight code-list summaries used by state dependency checks
     */
    List<BieCodeListStateDependencyRecord> getAssignedCodeListSummaryList(TopLevelAsbiepId topLevelAsbiepId);

}
