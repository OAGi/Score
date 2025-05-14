package org.oagi.score.gateway.http.api.bie_management.model;

import java.util.Date;

public record SourceBiePackageRecord(
        BiePackageId biePackageId,
        String versionName,
        String versionId,
        String sourceAction,
        Date when) {
}
