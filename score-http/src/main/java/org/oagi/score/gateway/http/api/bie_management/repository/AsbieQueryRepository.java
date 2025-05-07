package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieId;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.BieEditUsed;
import org.oagi.score.gateway.http.api.bie_management.model.bie_edit.tree.BieEditRef;

import java.util.Collection;
import java.util.List;

public interface AsbieQueryRepository {

    AsbieSummaryRecord getAsbieSummary(AsbieId asbieId);

    List<AsbieSummaryRecord> getAsbieSummaryList(Collection<TopLevelAsbiepId> ownerTopLevelAsbiepIdList);

    List<AsbieSummaryRecord> getAsbieSummaryList(AbieId fromAbieId, TopLevelAsbiepId topLevelAsbiepId);

    AsbieDetailsRecord getAsbieDetails(AsbieId asbieId);

    AsbieDetailsRecord getAsbieDetails(TopLevelAsbiepId topLevelAsbiepId, String hashPath);

    List<BieEditUsed> getUsedAsbieList(TopLevelAsbiepId topLevelAsbiepId);

    List<BieEditRef> getBieRefList(TopLevelAsbiepId topLevelAsbiepId);

}
