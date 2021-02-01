package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ModuleDependency {

    private BigInteger moduleDepId;
    private String dependencyType;
    private BigInteger relatedModuleId;

}
