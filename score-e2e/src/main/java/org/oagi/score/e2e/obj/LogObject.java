package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;

@Data
public class LogObject {

    private String reference;

    private String type;

    private BigInteger manifestId;

}
