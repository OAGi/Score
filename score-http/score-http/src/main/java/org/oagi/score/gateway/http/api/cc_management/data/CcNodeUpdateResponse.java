package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcNodeUpdateResponse {

    private CcType type;
    private BigInteger manifestId;
    private String state;
    private String access;
    private String den;
}
