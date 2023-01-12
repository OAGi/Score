package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;

@Data
public class DTObject {

    private BigInteger dtManifestId;

    private BigInteger releaseId;

    private BigInteger dtId;

    private BigInteger basedDtManifestId;

    private BigInteger basedDtId;

    private String guid;

    private String dataTypeTerm;

    private String representationTerm;

    private String den;

    private String definition;

    private String definitionSource;

}
