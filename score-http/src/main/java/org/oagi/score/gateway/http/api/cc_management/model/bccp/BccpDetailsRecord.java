package org.oagi.score.gateway.http.api.cc_management.model.bccp;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record BccpDetailsRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        BccpManifestId bccpManifestId,
        BccpId bccpId,
        Guid guid,
        DtSummaryRecord dt,
        BccpSummaryRecord replacement,
        BccpSummaryRecord since,
        BccpSummaryRecord lastChanged,

        String den,
        String propertyTerm,
        String representationTerm,
        boolean hasChild,
        boolean deprecated,
        boolean nillable,
        CcState state,
        NamespaceSummaryRecord namespace,
        ValueConstraint valueConstraint,
        Definition definition,
        AccessPrivilege access,

        LogSummaryRecord log,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        BccpManifestId prevBccpManifestId,
        BccpManifestId nextBccpManifestId,
        BccpId prevBccpId,
        BccpId nextBccpId) implements CoreComponent<BccpId> {

    @Override
    public BccpId getId() {
        return bccpId;
    }

}
