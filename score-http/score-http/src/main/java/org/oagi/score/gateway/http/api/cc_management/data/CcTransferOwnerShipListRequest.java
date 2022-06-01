package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class CcTransferOwnerShipListRequest {
    private String targetLoginId;
    private List<BigInteger> accManifestIds;
    private List<BigInteger> bccpManifestIds;
    private List<BigInteger> asccpManifestIds;
    private List<BigInteger> dtManifestIds;
}
