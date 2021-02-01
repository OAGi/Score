package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcAsccNode extends CcNode {

    private int seqKey;
    private BigInteger asccId = BigInteger.ZERO;
    private BigInteger manifestId = BigInteger.ZERO;
    private BigInteger fromAccManifestId = BigInteger.ZERO;
    private BigInteger toAsccpManifestId = BigInteger.ZERO;
    private BigInteger cardinalityMin = BigInteger.ZERO;
    private BigInteger cardinalityMax = BigInteger.ZERO;
    private boolean deprecated;

    @Override
    public BigInteger getId() {
        return asccId;
    }

}