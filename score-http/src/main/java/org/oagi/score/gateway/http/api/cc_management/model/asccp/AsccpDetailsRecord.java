package org.oagi.score.gateway.http.api.cc_management.model.asccp;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

public record AsccpDetailsRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        AsccpManifestId asccpManifestId,
        AsccpId asccpId,
        Guid guid,
        AsccpType type,
        AccSummaryRecord roleOfAcc,
        AsccpSummaryRecord replacement,
        AsccpSummaryRecord since,
        AsccpSummaryRecord lastChanged,

        String den,
        String propertyTerm,
        boolean reusable,
        boolean deprecated,
        boolean nillable,
        CcState state,
        NamespaceSummaryRecord namespace,
        Definition definition,
        AccessPrivilege access,

        LogSummaryRecord log,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        AsccpManifestId prevAsccpManifestId,
        AsccpManifestId nextAsccpManifestId,
        AsccpId prevAsccpId,
        AsccpId nextAsccpId) implements CoreComponent<AsccpId> {

    @Override
    public AsccpId getId() {
        return asccpId;
    }

}
