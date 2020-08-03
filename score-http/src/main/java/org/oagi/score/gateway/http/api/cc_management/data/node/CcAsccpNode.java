package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.data.SeqKeySupportable;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcAsccpNode extends CcNode implements SeqKeySupportable {

    private String type = "asccp";

    private long asccId;
    private int seqKey;
    private long asccpId;
    private Long currentAsccpId;
    private int roleOfAccId;

    @Override
    public long getId() {
        return asccpId;
    }

    @Override
    public Long getCurrentId() {
        return currentAsccpId;
    }
}
