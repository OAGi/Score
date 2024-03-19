package org.oagi.score.gateway.http.api.account_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class EmailValidationInfo {

    private BigInteger appUserId;

    private String email;

    private Date timestamp;

}
