package org.oagi.score.gateway.http.api.tenant_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.common.model.PageRequest;

@Data
public class TenantListRequest {

    private String name;

    private PageRequest pageRequest;

}
