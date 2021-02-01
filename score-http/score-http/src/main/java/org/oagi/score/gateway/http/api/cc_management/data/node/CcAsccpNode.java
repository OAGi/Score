package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.data.SeqKeySupportable;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcAsccpNode extends CcNode implements SeqKeySupportable {

    private CcType type = CcType.ASCCP;

    private BigInteger asccpId = BigInteger.ZERO;
    private BigInteger asccId = BigInteger.ZERO;
    private BigInteger roleOfAccId = BigInteger.ZERO;
    private int seqKey;
    private BigInteger manifestId = BigInteger.ZERO;
    private BigInteger asccManifestId = BigInteger.ZERO;
    private String asccpType;

    @Override
    public BigInteger getId() {
        return asccpId;
    }
}
