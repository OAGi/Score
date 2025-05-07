package org.oagi.score.gateway.http.api.cc_management.model.dt;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

public record DtSummaryRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        DtManifestId dtManifestId,
        DtId dtId,
        Guid guid,
        DtManifestId basedDtManifestId,

        String den,
        String dataTypeTerm,
        String qualifier,
        String representationTerm,
        String sixDigitId,
        boolean commonlyUsed,
        boolean deprecated,
        CcState state,
        NamespaceId namespaceId,
        String contentComponentDefinition,
        Definition definition,

        int revisionNum,

        UserSummaryRecord owner,

        DtManifestId prevDtManifestId,
        DtManifestId nextDtManifestId) implements CoreComponent<DtId> {

    @Override
    public DtId getId() {
        return dtId;
    }

}
