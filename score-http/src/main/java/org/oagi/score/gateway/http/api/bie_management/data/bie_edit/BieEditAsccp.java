package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAsccp extends TrackableImpl {

    private long asccpId;
    private String guid;
    private String propertyTerm;
    private long roleOfAccId;
    private long currentAsccpId;

    @Override
    public long getId() {
        return asccpId;
    }

    @Override
    public Long getCurrentId() {
        return currentAsccpId;
    }

}
