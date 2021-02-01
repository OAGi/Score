package org.oagi.score.repo.component.dt;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BdtNode {

    private String dataTypeTerm;
    private String qualifier;
    private String definition;
    private String den;
    private BigInteger bdtManifestId;

}
