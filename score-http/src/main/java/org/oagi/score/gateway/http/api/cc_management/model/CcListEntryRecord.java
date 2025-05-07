package org.oagi.score.gateway.http.api.cc_management.model;

import org.oagi.score.gateway.http.api.account_management.model.UserSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.library_management.model.LibrarySummaryRecord;
import org.oagi.score.gateway.http.api.log_management.model.LogSummaryRecord;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.Id;
import org.oagi.score.gateway.http.common.model.WhoAndWhen;

import java.util.Collection;

public record CcListEntryRecord(
        CcType type,

        LibrarySummaryRecord library,
        ReleaseSummaryRecord release,

        Id manifestId,
        Guid guid,
        Id basedManifestId,
        String den,
        String name,
        Definition definition,
        String module,
        OagisComponentType oagisComponentType,
        CcState state,
        boolean deprecated,
        boolean newComponent,

        String dtType,
        String sixDigitId,
        String defaultValueDomain,

        Collection<TagSummaryRecord> tagList,

        LogSummaryRecord log,
        UserSummaryRecord owner,
        WhoAndWhen created,
        WhoAndWhen lastUpdated) {

    public boolean isOwnedByDeveloper() {
        return owner.isDeveloper();
    }

}


