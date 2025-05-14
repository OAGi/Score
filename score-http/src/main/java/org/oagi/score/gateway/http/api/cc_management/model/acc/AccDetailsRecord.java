package org.oagi.score.gateway.http.api.cc_management.model.acc;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcAssociation;
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

public record AccDetailsRecord(
        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        AccManifestId accManifestId,
        AccId accId,
        Guid guid,
        String type,
        AccSummaryRecord based,
        AccSummaryRecord replacement,
        AccSummaryRecord since,
        AccSummaryRecord lastChanged,

        String den,
        String objectClassTerm,
        String objectClassQualifier,
        OagisComponentType componentType,
        boolean isAbstract,
        boolean isGroup,
        boolean hasExtension,
        boolean hasChild,
        boolean deprecated,
        CcState state,
        NamespaceSummaryRecord namespace,
        Definition definition,
        AccessPrivilege access,
        List<CcAssociation> associations,

        LogSummaryRecord log,

        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated,

        AccManifestId prevAccManifestId,
        AccManifestId nextAccManifestId,

        AccId prevAccId,
        AccId nextAccId) implements CoreComponent<AccId> {

    @Override
    public AccId getId() {
        return accId;
    }

    public boolean isGroup() {
        return this.componentType.isGroup();
    }

}
