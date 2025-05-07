package org.oagi.score.gateway.http.api.cc_management.controller.payload;

import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.tag_management.model.TagSummaryRecord;

import java.util.Collection;
import java.util.List;

public record CcChangesResponse(Collection<CcChange> ccChangeList) {

    public enum CcChangeType {
        NEW_COMPONENT,
        REVISED;
    }

    public record CcChange(CcType type,
                           ManifestId manifestId,
                           String den,
                           CcChangeType changeType,
                           List<TagSummaryRecord> tagList) {
    }

}
