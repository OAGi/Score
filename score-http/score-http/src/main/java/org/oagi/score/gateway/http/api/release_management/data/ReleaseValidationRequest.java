package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class ReleaseValidationRequest {

    private List<BigInteger> assignedAccComponentManifestIds = Collections.emptyList();
    private List<BigInteger> assignedAsccpComponentManifestIds = Collections.emptyList();
    private List<BigInteger> assignedBccpComponentManifestIds = Collections.emptyList();
    private List<BigInteger> assignedCodeListComponentManifestIds = Collections.emptyList();
}
