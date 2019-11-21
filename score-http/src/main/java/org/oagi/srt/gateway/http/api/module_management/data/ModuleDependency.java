package org.oagi.srt.gateway.http.api.module_management.data;

import lombok.Data;

@Data
public class ModuleDependency {

    private long moduleDepId;
    private String dependencyType;
    private long relatedModuleId;

}
