package org.oagi.score.gateway.http.api.bie_management.model;

import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;

import java.util.Date;

public record SourceTopLevelAsbiepRecord(
        ReleaseSummaryRecord release,
        TopLevelAsbiepId topLevelAsbiepId,
        String den,
        String displayName,
        String sourceAction,
        Date when) {
}
