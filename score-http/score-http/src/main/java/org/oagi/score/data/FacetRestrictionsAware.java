package org.oagi.score.data;


import java.math.BigInteger;

public interface FacetRestrictionsAware {

    BigInteger getMinLength();
    BigInteger getMaxLength();
    String getPattern();

}
