package org.oagi.score.gateway.http.api.account_management.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AccountUpdateRequest {

    private String email;

    private Map<String, Object> parameters = new HashMap<>();

}
