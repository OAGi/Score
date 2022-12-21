package org.oagi.score.gateway.http.api.account_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.PageRequest;

import java.util.Collections;
import java.util.List;

@Data
public class AccountListRequest {

    private String loginId;
    private String name;
    private String organization;
    private Boolean enabled;
    private List<String> roles = Collections.emptyList();
    private boolean excludeSSO;
    private Boolean excludeRequester;
    private Long tenantId;
    private boolean notConnectedToTenant;
    private List<Long> businessCtxIds;

    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

}
