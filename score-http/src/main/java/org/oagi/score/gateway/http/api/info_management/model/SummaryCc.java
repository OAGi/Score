package org.oagi.score.gateway.http.api.info_management.model;

import lombok.Data;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class SummaryCc {

    private BigInteger manifestId;
    private String type;
    private LocalDateTime lastUpdateTimestamp;
    private CcState state;

    private UserId ownerUserId;
    private String ownerUsername;

    private String den;

}
