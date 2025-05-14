package org.oagi.score.gateway.http.api.bie_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;

import java.util.Collection;
import java.util.List;

public interface TopLevelAsbiepQueryRepository {

    TopLevelAsbiepSummaryRecord getTopLevelAsbiepSummary(TopLevelAsbiepId topLevelAsbiepId);

    List<TopLevelAsbiepSummaryRecord> getReusedTopLevelAsbiepSummaryList(TopLevelAsbiepId topLevelAsbiepId);

    List<TopLevelAsbiepSummaryRecord> getReusingTopLevelAsbiepSummaryList(TopLevelAsbiepId topLevelAsbiepId);

    List<TopLevelAsbiepSummaryRecord> getRefTopLevelAsbiepSummaryList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList);

    List<TopLevelAsbiepSummaryRecord> getDerivedTopLevelAsbiepSummaryList(TopLevelAsbiepId basedTopLevelAsbiepId);

    List<BusinessContextId> getAssignedBusinessContextList(TopLevelAsbiepId topLevelAsbiepId);

    int countReferences(AsccManifestId asccManifestId);

    int countReferences(BccManifestId bccManifestId);

    int countReferences(DtScManifestId dtScManifestId);

}
