package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ResetDetailBIERequest {

    private BigInteger topLevelAsbiepId;
    private String bieType;
    private String path;
}
