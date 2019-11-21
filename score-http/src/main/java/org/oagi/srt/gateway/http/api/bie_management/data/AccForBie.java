package org.oagi.srt.gateway.http.api.bie_management.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.srt.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccForBie extends TrackableImpl {

    private long accId;
    private Long currentAccId;
    private String guid;

    @Override
    public long getId() {
        return accId;
    }

    @Override
    public Long getCurrentId() {
        return currentAccId;
    }

}
