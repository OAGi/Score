package org.oagi.score.gateway.http.api.release_management.controller.payload;

import lombok.Data;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class UnassignComponentsRequest {

    private ReleaseId releaseId;

    private List<BigInteger> accManifestIds = Collections.emptyList();
    private List<BigInteger> asccpManifestIds = Collections.emptyList();
    private List<BigInteger> bccpManifestIds = Collections.emptyList();
}
