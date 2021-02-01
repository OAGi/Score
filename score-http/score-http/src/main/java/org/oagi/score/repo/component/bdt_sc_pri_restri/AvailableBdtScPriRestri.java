package org.oagi.score.repo.component.bdt_sc_pri_restri;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AvailableBdtScPriRestri {

    private BigInteger bdtScPriRestriId;
    private boolean isDefault;
    private BigInteger xbtId;
    private String xbtName;

}
