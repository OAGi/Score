package org.oagi.srt.gateway.http.api.cc_management.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.srt.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class BccpForAppendBccp extends TrackableImpl {

    private long bccpId;
    private Long currentBccpId;
    private String guid;
    private String propertyTerm;
    private String module;
    private String definition;
    private boolean deprecated;
    private int state;

    @Override
    public long getId() {
        return bccpId;
    }

    @Override
    public Long getCurrentId() {
        return currentBccpId;
    }

}
