package org.oagi.score.gateway.http.api.tenant_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.PageRequest;

@Data
public class TenantListRequest {

    private String name;

    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

}
