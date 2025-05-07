package org.oagi.score.gateway.http.api.cc_management.model.dt_sc;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.List;

public record DtScDetailsRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        DtScManifestId dtScManifestId,
        DtScId dtScId,
        Guid guid,

        DtSummaryRecord ownerDt,
        DtScSummaryRecord based,
        DtScSummaryRecord replacement,
        DtScSummaryRecord since,
        DtScSummaryRecord lastChanged,

        String den,
        String objectClassTerm,
        String propertyTerm,
        String representationTerm,
        Cardinality cardinality,
        Cardinality prevCardinality, // @TODO: consider eliminating for model simplification.
        boolean deprecated,
        CcState state,
        ValueConstraint valueConstraint,
        Definition definition,
        List<DtScAwdPriDetailsRecord> dtScAwdPriList,

        LogSummaryRecord log,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        DtScManifestId prevDtScManifestId,
        DtScManifestId nextDtScManifestId) implements CoreComponent<DtScId> {

    @Override
    public DtScId getId() {
        return dtScId;
    }

}
