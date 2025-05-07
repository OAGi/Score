package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpDetailsRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.Collection;
import java.util.List;

public interface BccpQueryRepository {

    BccpDetailsRecord getBccpDetails(BccpManifestId bccpManifestId);

    BccpDetailsRecord getPrevBccpDetails(BccpManifestId bccpManifestId);

    BccpSummaryRecord getBccpSummary(BccpManifestId bccpManifestId);

    List<BccpSummaryRecord> getBccpSummaryList(Collection<ReleaseId> releaseIdList);

    List<BccpSummaryRecord> getBccpSummaryList(DtManifestId dtManifestId);

    List<BccpSummaryRecord> getBccpSummaryList(LibraryId libraryId, String releaseNum, CcState state);

    boolean hasRecordsByNamespaceId(NamespaceId namespaceId);

}
