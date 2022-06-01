package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class CcUpdateStateListRequest {
    private String action;
    private String toState;
    private List<BigInteger> accManifestIds;
    private List<BigInteger> bccpManifestIds;
    private List<BigInteger> asccpManifestIds;
    private List<BigInteger> dtManifestIds;
}
