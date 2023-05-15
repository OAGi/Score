package org.oagi.score.gateway.http.api.info.data;

import lombok.Data;
import org.oagi.score.service.common.data.CcState;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class SummaryCc {

    private BigInteger manifestId;
    private String type;
    private LocalDateTime lastUpdateTimestamp;
    private CcState state;

    private BigInteger ownerUserId;
    private String ownerUsername;

    private String den;

}
