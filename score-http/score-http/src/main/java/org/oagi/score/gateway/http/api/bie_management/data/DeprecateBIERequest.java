package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class DeprecateBIERequest {

    private BigInteger topLevelAsbiepId;
    private String reason;
    private String remark;

}
