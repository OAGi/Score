package org.oagi.score.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DTSC implements Serializable {

    private BigInteger dtScManifestId = BigInteger.ZERO;
    private BigInteger dtScId = BigInteger.ZERO;
    private String guid;
    private String propertyTerm;
    private String representationTerm;
    private String definition;
    private String definitionSource;
    private String defaultValue;
    private String fixedValue;
    private BigInteger ownerDtManifestId = BigInteger.ZERO;
    private BigInteger ownerDtId = BigInteger.ZERO;
    private int cardinalityMin;
    private int cardinalityMax;
    private BigInteger basedDtScManifestId = BigInteger.ZERO;
    private BigInteger basedDtScId = BigInteger.ZERO;
    private int revisionNum;

    public String getDen() {
        return getPropertyTerm() + ". " + getRepresentationTerm();
    }

}
