package org.oagi.score.gateway.http.api.tenant_management.model;

public record TenantInfo(
        TenantId tenantId,
        String name,
        Integer usersCount,
        Integer businessCtxCount) {
}
