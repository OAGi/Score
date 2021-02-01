package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BusinessContextValue {

    private BigInteger bizCtxValueId = BigInteger.ZERO;
    private BigInteger bizCtxId = BigInteger.ZERO;
    private BigInteger ctxSchemeValueId = BigInteger.ZERO;
}
