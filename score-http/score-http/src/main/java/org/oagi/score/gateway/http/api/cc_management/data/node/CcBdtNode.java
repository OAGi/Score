package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.data.SeqKeySupportable;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcBdtNode extends CcNode {

    private CcType type = CcType.BDT;

    private BigInteger bdtId = BigInteger.ZERO;

    private BigInteger manifestId = BigInteger.ZERO;
    private BigInteger bccManifestId = BigInteger.ZERO;
    private String den;
    private BigInteger prevBccpId;
    private BigInteger nextBccpId;

    @Override
    public BigInteger getId() {
        return bdtId;
    }
}
