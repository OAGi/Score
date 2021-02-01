package org.oagi.score.gateway.http.api.account_management.data;

import lombok.Data;
import org.oagi.score.service.common.data.PageRequest;

@Data
public class AccountListRequest {

    private String loginId;
    private String name;
    private String organization;
    private Boolean enabled;
    private String role;
    private boolean excludeSSO;
    private Boolean excludeRequester;

    private PageRequest pageRequest = PageRequest.EMPTY_INSTANCE;

}
