package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcBdtScNode extends CcNode {

    private CcType type = CcType.DT_SC;

    private BigInteger bdtScId = BigInteger.ZERO;
    private BigInteger manifestId = BigInteger.ZERO;

    @Override
    public BigInteger getId() {
        return bdtScId;
    }
}
