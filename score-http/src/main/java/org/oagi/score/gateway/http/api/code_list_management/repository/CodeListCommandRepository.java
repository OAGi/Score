package org.oagi.score.gateway.http.api.code_list_management.repository;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueManifestId;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.data.util.Pair;

import java.util.List;

public interface CodeListCommandRepository {

    CodeListManifestId create(ReleaseId releaseId,
                              CodeListManifestId basedCodeListManifestId);

    boolean update(CodeListManifestId codeListManifestId,
                   String name, String versionId, String listId,
                   AgencyIdListValueManifestId agencyIdListValueManifestId,
                   Definition definition, String remark,
                   NamespaceId namespaceId,
                   Boolean deprecated, Boolean extensible);

    boolean updateState(CodeListManifestId codeListManifestId, CcState state);

    boolean updateOwnership(ScoreUser targetUser, CodeListManifestId codeListManifestId);

    boolean delete(CodeListManifestId codeListManifestId);

    void revise(CodeListManifestId codeListManifestId);

    void cancel(CodeListManifestId codeListManifestId);

    CodeListValueManifestId createValue(
            CodeListManifestId codeListManifestId,
            CodeListId codeListId,
            ReleaseId releaseId,
            String value, String meaning,
            Definition definition);

    boolean updateValue(
            CodeListValueManifestId valueId,
            String value, String meaning,
            Definition definition,
            Boolean deprecated);

    boolean deleteValue(CodeListValueManifestId codeListValueManifestId);

    boolean updateLogId(CodeListManifestId codeListManifestId, LogId logId);

    Pair<CodeListManifestId, List<String>> uplift(
            ScoreUser requester, CodeListManifestId codeListManifestId, ReleaseId targetReleaseId);

}
