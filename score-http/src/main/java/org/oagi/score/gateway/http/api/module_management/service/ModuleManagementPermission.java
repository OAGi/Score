package org.oagi.score.gateway.http.api.module_management.service;

import org.oagi.score.gateway.http.common.model.ScoreUser;

import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;

final class ModuleManagementPermission {

    private ModuleManagementPermission() {
    }

    static void requireDeveloper(ScoreUser requester) {
        if (!requester.hasRole(DEVELOPER)) {
            throw new IllegalArgumentException("Only users with the '" + DEVELOPER + "' role can manage module sets.");
        }
    }
}
