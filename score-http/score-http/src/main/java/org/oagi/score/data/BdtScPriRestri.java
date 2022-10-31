package org.oagi.score.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BdtScPriRestri implements Serializable {

    private BigInteger bdtScPriRestriId = BigInteger.ZERO;
    private BigInteger bdtScManifestId = BigInteger.ZERO;
    private BigInteger cdtScAwdPriXpsTypeMapId = BigInteger.ZERO;
    private BigInteger codeListManifestId = BigInteger.ZERO;
    private BigInteger agencyIdListManifestId = BigInteger.ZERO;
    private boolean defaulted;

    public boolean isDefault() {
        return isDefaulted();
    }

}
