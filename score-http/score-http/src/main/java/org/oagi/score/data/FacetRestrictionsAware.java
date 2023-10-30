package org.oagi.score.data;


import java.math.BigInteger;

public interface FacetRestrictionsAware {

    BigInteger getFacetMinLength();
    BigInteger getFacetMaxLength();
    String getFacetPattern();

}
