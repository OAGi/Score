package org.oagi.score.gateway.http.api.cc_management.model.bccp;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

public record BccpSummaryRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        BccpManifestId bccpManifestId,
        BccpId bccpId,
        Guid guid,
        DtManifestId dtManifestId,

        String den,
        String propertyTerm,
        String representationTerm,
        boolean deprecated,
        boolean nillable,
        CcState state,
        NamespaceId namespaceId,
        ValueConstraint valueConstraint,
        Definition definition,

        int revisionNum,

        UserSummaryRecord owner,

        BccpManifestId prevBccpManifestId,
        BccpManifestId nextBccpManifestId) implements CoreComponent<BccpId> {

    @Override
    public BccpId getId() {
        return bccpId;
    }

}
