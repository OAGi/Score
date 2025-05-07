package org.oagi.score.gateway.http.api.cc_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.util.List;

public interface CcCommandRepository {

    void clearReplacement(ReleaseId releaseId);

    void delete(ReleaseId releaseId);

    void copyWorkingManifests(ReleaseId releaseId, ReleaseId workingReleaseId,
                              List<AccManifestId> accManifestIds,
                              List<AsccpManifestId> asccpManifestIds,
                              List<BccpManifestId> bccpManifestIds,
                              List<DtManifestId> dtManifestIds,
                              List<CodeListManifestId> codeListManifestIds,
                              List<AgencyIdListManifestId> agencyIdListManifestIds);

    void cleanUp(ReleaseId releaseId);

}
