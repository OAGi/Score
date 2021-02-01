package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieEditXbt {

    private BigInteger priRestriId;
    private boolean isDefault;
    private BigInteger xbtId;
    private String xbtName;

}
