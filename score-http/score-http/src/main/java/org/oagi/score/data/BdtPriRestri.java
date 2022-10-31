package org.oagi.score.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BdtPriRestri implements Serializable {

    private BigInteger bdtPriRestriId = BigInteger.ZERO;
    private BigInteger bdtManifestId = BigInteger.ZERO;
    private BigInteger cdtAwdPriXpsTypeMapId = BigInteger.ZERO;
    private BigInteger codeListManifestId = BigInteger.ZERO;
    private BigInteger agencyIdListManifestId = BigInteger.ZERO;
    private boolean defaulted;

    public boolean isDefault() {
        return isDefaulted();
    }

}
