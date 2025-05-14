package org.oagi.score.gateway.http.api.log_management.model;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class Log {

    private BigInteger logId;
    private String hash;
    private int revisionNum;
    private int revisionTrackingNum;
    private LogAction logAction;
    private String loginId;
    private LocalDateTime timestamp;
    private BigInteger prevLogId;
    private boolean isDeveloper;

}
