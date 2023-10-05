package org.oagi.score.repo.component.bdt_pri_restri;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AvailableBdtPriRestri {

    private BigInteger bdtPriRestriId;
    private boolean isDefault;
    private BigInteger xbtId;
    private String xbtName;
    private BigInteger codeListManifestId;
    private BigInteger agencyIdListManifestId;

}
