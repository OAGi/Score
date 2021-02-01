package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class ModuleSetModule {

    private BigInteger moduleId = BigInteger.ZERO;
    private String path;
    private BigInteger namespaceId;
    private String namespaceUri;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;
    private boolean assigned;

}
