package org.oagi.score.gateway.http.api.log_management.model;

import java.util.Date;

public record Log(
        LogId logId,
        String hash,
        int revisionNum,
        int revisionTrackingNum,
        LogAction logAction,
        String loginId,
        Date timestamp,
        LogId prevLogId,
        boolean isDeveloper) {
}
