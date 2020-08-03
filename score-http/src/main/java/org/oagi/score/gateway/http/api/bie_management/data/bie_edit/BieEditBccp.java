package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditBccp extends TrackableImpl {

    private long bccpId;
    private String guid;
    private String propertyTerm;
    private long bdtId;
    private long currentBccpId;

    @Override
    public long getId() {
        return bccpId;
    }

    @Override
    public Long getCurrentId() {
        return currentBccpId;
    }

}
