package org.oagi.score.gateway.http.api.log_management.model;

public record LogSummaryRecord(LogId logId,
                               int revisionNum,
                               int revisionTrackingNum) {
}
