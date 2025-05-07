package org.oagi.score.gateway.http.api.xbt_management.repository;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;

import java.util.Collection;
import java.util.List;

public interface XbtQueryRepository {

    XbtSummaryRecord getXbtSummary(XbtManifestId xbtManifestId);

    List<XbtSummaryRecord> getXbtSummaryList(Collection<ReleaseId> releaseIdList);

    List<XbtSummaryRecord> getXbtSummaryList(ReleaseId releaseId);

}
