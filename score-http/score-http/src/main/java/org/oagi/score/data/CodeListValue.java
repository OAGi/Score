package org.oagi.score.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CodeListValue {

    private BigInteger codeListValueManifestId = BigInteger.ZERO;
    private BigInteger codeListValueId = BigInteger.ZERO;
    private BigInteger codeListManifestId = BigInteger.ZERO;
    private BigInteger codeListId = BigInteger.ZERO;
    private String value;
    private String meaning;
    private String definition;
    private String definitionSource;

}
