package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class ModuleList {

    private BigInteger moduleId = BigInteger.ZERO;
    private String module;
    private String namespace;
    private BigInteger ownerUserId = BigInteger.ZERO;
    private String owner;
    private String lastUpdatedBy;
    private Date lastUpdateTimestamp;
    private boolean canEdit;

}
