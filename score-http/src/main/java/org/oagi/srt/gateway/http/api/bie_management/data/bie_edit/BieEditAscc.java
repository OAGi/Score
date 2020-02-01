package org.oagi.srt.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.srt.data.Cardinality;
import org.oagi.srt.data.SeqKeySupportable;
import org.oagi.srt.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAscc extends TrackableImpl implements SeqKeySupportable, Cardinality {

    private long asccId;
    private String guid;
    private long fromAccId;
    private long toAsccpId;
    private int seqKey;
    private long currentAsccId;

    private int cardinalityMin;
    private int cardinalityMax;

    @Override
    public long getId() {
        return asccId;
    }

    @Override
    public Long getCurrentId() {
        return currentAsccId;
    }

}
