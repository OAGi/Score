package org.oagi.score.gateway.http.api.cc_management.model.dt;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.List;

public record DtDetailsRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        DtManifestId dtManifestId,
        DtId dtId,
        Guid guid,
        DtSummaryRecord based,
        DtSummaryRecord replacement,
        DtSummaryRecord since,
        DtSummaryRecord lastChanged,

        String den,
        String dataTypeTerm,
        String qualifier,
        String representationTerm,
        String sixDigitId,
        boolean hasChild,
        boolean commonlyUsed,
        boolean deprecated,
        CcState state,
        NamespaceSummaryRecord namespace,
        String contentComponentDefinition,
        Definition definition,
        List<DtAwdPriDetailsRecord> dtAwdPriList,

        AccessPrivilege access,

        LogSummaryRecord log,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        DtManifestId prevDtManifestId,
        DtManifestId nextDtManifestId,
        DtId prevDtId,
        DtId nextDtId) implements CoreComponent<DtId> {

    @Override
    public DtId getId() {
        return dtId;
    }
}
