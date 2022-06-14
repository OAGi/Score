package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcBdtCreateRequest {

    private BigInteger releaseId;
    private BigInteger bdtManifestId;
    private BigInteger specId;

}
