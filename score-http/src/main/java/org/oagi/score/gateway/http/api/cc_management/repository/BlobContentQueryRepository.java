package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.Collection;
import java.util.List;

public interface BlobContentQueryRepository {

    List<BlobContentSummaryRecord> getBlobContentSummaryList(Collection<ReleaseId> releaseIdList);

}
