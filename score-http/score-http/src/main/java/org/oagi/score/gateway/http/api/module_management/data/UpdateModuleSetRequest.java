package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class UpdateModuleSetRequest {

    private BigInteger moduleSetId;
    private String name;
    private String description;

}
