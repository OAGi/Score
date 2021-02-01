package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class AssignComponentsRequest {

    private BigInteger releaseId;

    private List<BigInteger> accManifestIds = Collections.emptyList();
    private List<BigInteger> asccpManifestIds = Collections.emptyList();
    private List<BigInteger> bccpManifestIds = Collections.emptyList();
}
