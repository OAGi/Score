package org.oagi.srt.gateway.http.api.account_management.data;

import lombok.Data;
import org.oagi.srt.gateway.http.api.common.data.PageRequest;

@Data
public class AccountListRequest {

    private String loginId;
    private String name;
    private String organization;
    private String role;
    private Boolean excludeRequester;

    private PageRequest pageRequest;

}
