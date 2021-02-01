package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CreateOagisVerbRequest {

    private BigInteger basedVerbAccManifestId;

    private String initialObjectClassTerm;

    private String initialPrpertyTerm;

}
