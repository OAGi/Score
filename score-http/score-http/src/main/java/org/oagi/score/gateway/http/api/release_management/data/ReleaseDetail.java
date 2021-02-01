package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ReleaseDetail {
    private BigInteger releaseId;
    private String guid;
    private String releaseNum;
    private String releaseNote;
    private String releaseLicense;
    private BigInteger namespaceId;
    private String state;
}
