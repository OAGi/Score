package org.oagi.score.gateway.http.api.agency_id_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

public interface AgencyIdListCommandRepository {

    AgencyIdListManifestId create(
            ReleaseId releaseId, AgencyIdListManifestId agencyIdListManifestId);

    boolean update(AgencyIdListManifestId agencyIdListManifestId,
                   String name, String versionId, String listId,
                   AgencyIdListValueManifestId agencyIdListValueManifestId,
                   Definition definition, String remark,
                   NamespaceId namespaceId,
                   Boolean deprecated);

    boolean updateState(AgencyIdListManifestId agencyIdListManifestId, CcState state);

    boolean updateOwnership(ScoreUser targetUser, AgencyIdListManifestId agencyIdListManifestId);

    boolean delete(AgencyIdListManifestId agencyIdListManifestId);

    void revise(AgencyIdListManifestId agencyIdListManifestId);

    void cancel(AgencyIdListManifestId agencyIdListManifestId);

    AgencyIdListValueManifestId createValue(
            AgencyIdListManifestId agencyIdListManifestId,
            AgencyIdListId agencyIdListId,
            ReleaseId releaseId,
            String value, String name,
            Definition definition);

    boolean updateValue(
            AgencyIdListValueManifestId valueId,
            String value, String name,
            Definition definition,
            Boolean deprecated,
            Boolean isDeveloperDefault, Boolean isUserDefault);

    boolean deleteValue(AgencyIdListValueManifestId agencyIdListValueManifestId);

    boolean updateLogId(AgencyIdListManifestId agencyIdListManifestId, LogId logId);

}
