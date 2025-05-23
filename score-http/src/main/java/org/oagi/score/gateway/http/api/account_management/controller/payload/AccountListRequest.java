package org.oagi.score.gateway.http.api.account_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.common.model.PageRequest;

import java.math.BigInteger;
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
    private BigInteger tenantId;
    private boolean notConnectedToTenant;
    private List<Long> businessCtxIds;

    private PageRequest pageRequest;

}
