package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieEditAbie {

    private BigInteger abieId;
    private BigInteger basedAccManifestId;

}
