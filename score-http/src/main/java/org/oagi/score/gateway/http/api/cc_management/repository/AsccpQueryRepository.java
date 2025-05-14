package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.Collection;
import java.util.List;

public interface AsccpQueryRepository {

    AsccpDetailsRecord getAsccpDetails(AsccpManifestId asccpManifestId);

    AsccpDetailsRecord getPrevAsccpDetails(AsccpManifestId asccpManifestId);

    AsccpSummaryRecord getAsccpSummary(AsccpManifestId asccpManifestId);

    AsccpSummaryRecord getAsccpSummary(AsccpId asccpId, ReleaseId releaseId);

    List<AsccpSummaryRecord> getAsccpSummaryList(Collection<ReleaseId> releaseIdList);

    List<AsccpSummaryRecord> getAsccpSummaryList(AccManifestId roleOfAccManifestId);

    List<AsccpSummaryRecord> getAsccpSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    boolean hasRecordsByNamespaceId(NamespaceId namespaceId);

    AsccpSummaryRecord findNextAsccpManifest(
            TopLevelAsbiepId topLevelAsbiepId, ReleaseId nextReleaseId);

    AsccpSummaryRecord findNextAsccpManifest(
            AsccpManifestId targetAsccpManifestId, ReleaseId nextReleaseId);

}
