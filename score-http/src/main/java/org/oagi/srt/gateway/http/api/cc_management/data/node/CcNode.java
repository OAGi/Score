package org.oagi.srt.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.srt.gateway.http.api.cc_management.data.CcState;
import org.oagi.srt.gateway.http.api.common.data.TrackableImpl;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class CcNode extends TrackableImpl {

    private String guid;
    private String name;
    private int rawState;
    private CcState state;
    private boolean hasChild;
    private String access;
}
