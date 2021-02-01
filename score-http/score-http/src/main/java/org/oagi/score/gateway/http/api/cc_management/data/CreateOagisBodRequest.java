package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CreateOagisBodRequest {

    private BigInteger verbManifestId;

    private BigInteger nounManifestId;

}
