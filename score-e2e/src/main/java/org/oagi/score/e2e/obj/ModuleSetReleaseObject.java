package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class ModuleSetReleaseObject {

    private BigInteger moduleSetReleaseId;

    private BigInteger moduleSetId;

    private BigInteger releaseId;

    private String name;

    private String description;

    private boolean isDefault;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;

}
