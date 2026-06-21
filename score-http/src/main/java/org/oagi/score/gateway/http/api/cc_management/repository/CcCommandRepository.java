package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.List;
import java.util.Map;

public interface CcCommandRepository {

    /**
     * Creates the initial XBT manifest rows for a newly created {@code Working} release.
     *
     * <p>This is used during library creation when the first {@code Working} release is created.
     * Draft release creation normally copies XBT manifests from the library's existing
     * {@code Working} release, but a brand-new library has no prior release to copy from.</p>
     *
     * <p>Current limitation: XBT manifests are initialized directly from the global {@code xbt}
     * table rather than from a release-specific source graph. This is sufficient for the initial
     * {@code Working} release because the first library release needs concrete
     * {@code xbt_manifest} rows for the built-in XML Schema XBTs used by downstream draft and DT
     * primitive mappings.</p>
     *
     * @param releaseId the newly created {@code Working} release that should receive XBT manifest rows.
     */
    void createXbtManifestRecords(ReleaseId releaseId);

    void clearReplacement(ReleaseId releaseId);

    void delete(ReleaseId releaseId);

    void copyWorkingManifests(ReleaseId releaseId, ReleaseId workingReleaseId,
                              List<AccManifestId> accManifestIds,
                              List<AsccpManifestId> asccpManifestIds,
                              List<BccpManifestId> bccpManifestIds,
                              List<DtManifestId> dtManifestIds,
                              List<CodeListManifestId> codeListManifestIds,
                              List<AgencyIdListManifestId> agencyIdListManifestIds);

    Map<String, Integer> getCrossReleaseReferenceCounts(ReleaseId workingReleaseId, ReleaseId dependencyReleaseId);

    Map<String, List<String>> getCrossReleaseReferenceDetails(ReleaseId workingReleaseId, ReleaseId dependencyReleaseId);

    void remapCrossReleaseReferences(ReleaseId workingReleaseId, ReleaseId sourceReleaseId, ReleaseId targetReleaseId);

    void cleanUp(ReleaseId releaseId);

}
