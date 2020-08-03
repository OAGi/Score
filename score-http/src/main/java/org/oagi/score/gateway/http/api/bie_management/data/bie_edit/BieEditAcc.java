package org.oagi.score.gateway.http.api.bie_management.data.bie_edit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public class BieEditAcc extends TrackableImpl {

    private long accId;
    private String guid;
    private int oagisComponentType;
    private Long basedAccId;
    private long currentAccId;

    @Override
    public long getId() {
        return accId;
    }

    @Override
    public Long getCurrentId() {
        return currentAccId;
    }
}
