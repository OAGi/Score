package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcExtensionCreateRequest {

    private BigInteger accManifestId;

}
