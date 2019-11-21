package org.oagi.srt.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CcBdtScNode extends CcNode {

    private String type = "bdt_sc";

    private long bdtScId;

    @Override
    public long getId() {
        return bdtScId;
    }

    @Override
    public Long getCurrentId() {
        return null;
    }
}
