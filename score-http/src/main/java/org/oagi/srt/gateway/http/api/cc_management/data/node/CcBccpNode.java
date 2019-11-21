package org.oagi.srt.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.srt.data.SeqKeySupportable;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcBccpNode extends CcNode implements SeqKeySupportable {

    private String type = "bccp";

    private long bccId;
    private boolean attribute;
    private int seqKey;

    private long bccpId;
    private Long currentBccpId;
    private long bdtId;

    @Override
    public long getId() {
        return bccpId;
    }

    @Override
    public Long getCurrentId() {
        return currentBccpId;
    }
}
