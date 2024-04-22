package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcVerifyAppendRequest {
    private BigInteger releaseId;
    private BigInteger accManifestId;
    private BigInteger basedAccManifestId;
    private BigInteger asccpManifestId;
    private BigInteger bccpManifestId;
    private String propertyTerm;
}
