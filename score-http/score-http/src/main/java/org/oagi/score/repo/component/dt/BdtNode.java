package org.oagi.score.repo.component.dt;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BdtNode {

    private String dataTypeTerm;
    private String qualifier;
    private String definition;
    private String den;
    private BigInteger facetMinLength;
    private BigInteger facetMaxLength;
    private String facetPattern;
    private String facetMinInclusive;
    private String facetMinExclusive;
    private String facetMaxInclusive;
    private String facetMaxExclusive;
    private BigInteger bdtManifestId;

}
