package org.oagi.srt.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcAsccNode extends CcNode {

    private int seqKey;
    private long asccId;
    private long currentAsccId;
    private long fromAccId;
    private long toAsccpId;

    @Override
    public long getId() {
        return asccId;
    }

    @Override
    public Long getCurrentId() {
        return currentAsccId;
    }

}