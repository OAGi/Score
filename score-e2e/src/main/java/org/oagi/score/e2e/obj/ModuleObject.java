package org.oagi.score.e2e.obj;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class ModuleObject {
    private BigInteger moduleId;

    private BigInteger parentModuleId;

    private BigInteger moduleSetId;

    private String name;

    private String type;

    private String path;

    private BigInteger namespaceId;

    private String versionNumber;

    private BigInteger createdBy;

    private BigInteger lastUpdatedBy;

    private BigInteger ownerUserId;

    private LocalDateTime creationTimestamp;

    private LocalDateTime lastUpdateTimestamp;
}
