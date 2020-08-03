package org.oagi.score.gateway.http.api.bie_management.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.common.data.TrackableImpl;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class AsccpForBie extends TrackableImpl {

    private long asccpId;
    private Long currentAsccpId;
    private String guid;
    private String propertyTerm;
    private Long moduleId;
    private String module;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;

    @Override
    public long getId() {
        return asccpId;
    }

    @Override
    public Long getCurrentId() {
        return currentAsccpId;
    }

}
