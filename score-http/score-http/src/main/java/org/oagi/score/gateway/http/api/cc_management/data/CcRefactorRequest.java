package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcRefactorRequest {

    private String type;

    private BigInteger targetManifestId;

    private BigInteger destinationManifestId;

}
