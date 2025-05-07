package org.oagi.score.gateway.http.api.cc_management.model.asccp;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

public record AsccpSummaryRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        AsccpManifestId asccpManifestId,
        AsccpId asccpId,
        Guid guid,
        AsccpType type,
        AccManifestId roleOfAccManifestId,

        String den,
        String propertyTerm,
        boolean reusable,
        boolean deprecated,
        boolean nillable,
        CcState state,
        NamespaceId namespaceId,
        Definition definition,

        int revisionNum,

        UserSummaryRecord owner,

        AsccpManifestId prevAsccpManifestId,
        AsccpManifestId nextAsccpManifestId) implements CoreComponent<AsccpId> {

    @Override
    public AsccpId getId() {
        return asccpId;
    }

}
