package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieEditBbieSc {

    private BigInteger bbieScManifestId = BigInteger.ZERO;
    private BigInteger bbieId = BigInteger.ZERO;
    private BigInteger dtScManifestId = BigInteger.ZERO;
    private boolean used;

}
