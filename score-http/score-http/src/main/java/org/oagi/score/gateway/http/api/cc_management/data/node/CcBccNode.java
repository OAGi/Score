package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.service.common.data.BCCEntityType;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcBccNode extends CcNode {

    private int seqKey;
    private BigInteger bccId = BigInteger.ZERO;
    private BigInteger manifestId = BigInteger.ZERO;
    private BigInteger fromAccManifestId = BigInteger.ZERO;
    private BigInteger toBccpManifestId = BigInteger.ZERO;
    private BCCEntityType entityType;
    private BigInteger cardinalityMin = BigInteger.ZERO;
    private BigInteger cardinalityMax = BigInteger.ZERO;
    private String defaultValue;
    private String fixedValue;
    private boolean deprecated;

    @Override
    public BigInteger getId() {
        return bccId;
    }
}
