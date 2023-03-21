package org.oagi.score.gateway.http.api.tenant_management.data;

import lombok.Data;

@Data
public class TenantInfo extends Tenant {

    private Integer usersCount;

    private Integer businessCtxCount;

}
