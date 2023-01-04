package org.oagi.score.gateway.http.api.cc_management.data.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.oagi.score.service.common.data.AccessPrivilege;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.TrackableImpl;

import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class CcNode extends TrackableImpl {

    private String guid;
    private String name;
    private CcState state;
    private boolean hasChild;
    private AccessPrivilege access;
    private BigInteger ownerUserId = BigInteger.ZERO;
}
