package org.oagi.score.gateway.http.api.cc_management.model.acc;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

public record AccSummaryRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        AccManifestId accManifestId,
        AccId accId,
        Guid guid,
        String type,
        AccManifestId basedAccManifestId,

        String den,
        String objectClassTerm,
        String objectClassQualifier,
        OagisComponentType componentType,
        boolean isAbstract,
        boolean deprecated,
        CcState state,
        NamespaceId namespaceId,
        Definition definition,

        int revisionNum,

        UserSummaryRecord owner,

        AccManifestId prevAccManifestId,
        AccManifestId nextAccManifestId) implements CoreComponent<AccId> {

    @Override
    public AccId getId() {
        return accId;
    }

    public boolean isGroup() {
        return this.componentType.isGroup();
    }
    
}
