package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class ModuleSet {

    private BigInteger moduleSetId = BigInteger.ZERO;
    private String guid;
    private String name;
    private String description;
    private Date lastUpdateTimestamp;
    private String lastUpdateUser;

}
