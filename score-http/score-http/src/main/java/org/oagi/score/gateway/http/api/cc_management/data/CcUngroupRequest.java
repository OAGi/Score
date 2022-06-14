package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CcUngroupRequest {

    private BigInteger accManifestId;

    private BigInteger asccManifestId;

    private int pos = -1;
}
