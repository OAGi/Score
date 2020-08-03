package org.oagi.score.gateway.http.api.cc_management.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class AsccpForAppendAsccp extends TrackableImpl {

    private long asccpId;
    private Long currentAsccpId;
    private String guid;
    private String propertyTerm;
    private String module;
    private String definition;
    private boolean deprecated;
    private int state;

    @Override
    public long getId() {
        return asccpId;
    }

    @Override
    public Long getCurrentId() {
        return currentAsccpId;
    }
}
