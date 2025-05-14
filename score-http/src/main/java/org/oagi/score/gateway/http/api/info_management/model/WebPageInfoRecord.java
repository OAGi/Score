package org.oagi.score.gateway.http.api.info_management.model;

import java.util.Map;

public record WebPageInfoRecord(
        String brand,
        String favicon,
        String signInStatement,
        Map<String, BoxColorSet> componentStateColorSetMap,
        Map<String, BoxColorSet> releaseStateColorSetMap,
        Map<String, BoxColorSet> userRoleColorSetMap) {
}
