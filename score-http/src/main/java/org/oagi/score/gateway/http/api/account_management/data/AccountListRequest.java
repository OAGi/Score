package org.oagi.score.gateway.http.api.account_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.common.data.PageRequest;

@Data
public class AccountListRequest {

    private String loginId;
    private String name;
    private String organization;
    private String role;
    private boolean excludeSSO;
    private Boolean excludeRequester;

    private PageRequest pageRequest;

}
