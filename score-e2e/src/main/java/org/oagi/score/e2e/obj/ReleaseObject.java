package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class ReleaseObject {

    private BigInteger releaseId;

    private String guid;

    private String releaseNumber;

    private String releaseNote;

    private String releaseLicence;

    private BigInteger namespaceId;

    private BigInteger createdby;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

    private String state;
}
