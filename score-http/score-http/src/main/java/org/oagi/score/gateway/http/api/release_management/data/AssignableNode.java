package org.oagi.score.gateway.http.api.release_management.data;

import lombok.Data;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class AssignableNode {
    private BigInteger manifestId;
    private CcType type;
    private CcState state;
    private String den;
    private String ownerUserId;
    private LocalDateTime timestamp;
    private BigInteger revision;
}
