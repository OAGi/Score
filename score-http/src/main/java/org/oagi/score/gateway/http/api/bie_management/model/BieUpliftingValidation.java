package org.oagi.score.gateway.http.api.bie_management.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BieUpliftingValidation {

    private String bieType;
    private BigInteger bieId;
    private String sourcePath;
    private boolean isValid;
    private String message;
    private String status;

}
