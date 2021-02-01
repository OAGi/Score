package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcAsccpCreateRequest {

    private BigInteger releaseId;
    private BigInteger roleOfAccManifestId;
    private String asccpType;
    private String initialPropertyTerm;

}
