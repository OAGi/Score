package org.oagi.score.gateway.http.api.cc_management.model.dt_sc;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

public record DtScSummaryRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        DtScManifestId dtScManifestId,
        DtScId dtScId,
        Guid guid,

        DtManifestId ownerDtManifestId,
        DtScManifestId basedDtScManifestId,

        String den,
        String objectClassTerm,
        String propertyTerm,
        String representationTerm,
        Cardinality cardinality,
        boolean deprecated,
        CcState state,
        ValueConstraint valueConstraint,
        Definition definition,

        int revisionNum,

        UserSummaryRecord owner,

        DtScManifestId prevDtScManifestId,
        DtScManifestId nextDtScManifestId) implements CoreComponent<DtScId> {

    @Override
    public DtScId getId() {
        return dtScId;
    }

}
