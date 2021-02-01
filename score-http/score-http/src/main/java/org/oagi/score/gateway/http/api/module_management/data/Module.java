package org.oagi.score.gateway.http.api.module_management.data;

import lombok.Data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class Module {

    public static String MODULE_SEPARATOR = "\\";

    private BigInteger moduleId;
    private String name;
    private BigInteger namespaceId;

    private List<ModuleDependency> moduleDependencies = Collections.emptyList();

}
