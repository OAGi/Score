package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.data.Cardinality;
import org.oagi.score.data.SeqKeySupportable;
import org.oagi.score.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBcc extends TrackableImpl implements SeqKeySupportable, Cardinality {

    private long bccId;
    private String guid;
    private long fromAccId;
    private long toBccpId;
    private int seqKey;
    private int entityType;
    private long currentBccId;

    private int cardinalityMin;
    private int cardinalityMax;

    public boolean isAttribute() {
        return (entityType == 0);
    }

    @Override
    public long getId() {
        return bccId;
    }

    @Override
    public Long getCurrentId() {
        return currentBccId;
    }

}
